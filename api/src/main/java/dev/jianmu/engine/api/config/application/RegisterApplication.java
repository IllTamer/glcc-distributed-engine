package dev.jianmu.engine.api.config.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import dev.jianmu.engine.api.config.EngineProperties;
import dev.jianmu.engine.api.exception.PublishException;
import dev.jianmu.engine.api.pojo.EngineLock;
import dev.jianmu.engine.api.service.FutureTaskService;
import dev.jianmu.engine.api.service.PessimisticLockService;
import dev.jianmu.engine.api.service.TaskService;
import dev.jianmu.engine.api.vo.TaskPublishVO;
import dev.jianmu.engine.consumer.ConsumerService;
import dev.jianmu.engine.monitor.event.ExecutionNode;
import dev.jianmu.engine.provider.FutureTask;
import dev.jianmu.engine.provider.Task;
import dev.jianmu.engine.register.DispatchInfo;
import dev.jianmu.engine.register.NodeInstancePool;
import dev.jianmu.engine.register.OnlineNodeServiceDiscovery;
import dev.jianmu.engine.register.util.CronParser;
import dev.jianmu.engine.rpc.service.loadbalancer.RoundRobinLoadBalancer;
import dev.jianmu.engine.rpc.translate.RpcClientProxy;
import dev.jianmu.engine.rpc.util.Assert;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Getter
@Component
public class RegisterApplication {
    public static final int LONGEST_EXECUTION_SECONDS = 15 * 60;
    public static final String SUBMIT_BUSINESS_CODE = "submit";
    public static final int MAX_RECOVER_PAGE = 64;

    private final PessimisticLockService pessimisticLockService;
    private final TaskService taskService;
    private final FutureTaskService futureTaskService;
    private final ScheduledExecutorService scheduledThreadPool;
    private final NodeInstancePool nodeInstancePool;

    public RegisterApplication(
            PessimisticLockService pessimisticLockService,
            TaskService taskService,
            FutureTaskService futureTaskService,
            ScheduledExecutorService scheduledThreadPool,
            EngineProperties properties
    ) {
        this.pessimisticLockService = pessimisticLockService;
        this.taskService = taskService;
        this.futureTaskService = futureTaskService;
        this.scheduledThreadPool = scheduledThreadPool;
        this.nodeInstancePool = new NodeInstancePool(
                properties.getService().getDiscoveries(),
                properties.getService().getRegisterPort(),
                properties.getAvailabilityFilter()
        );
    }

    /**
     * ????????????
     * */
    @NotNull
    public TaskPublishVO submitTask(Task task) {
        // ??????Node??????(CPU??????????????????????????????)
        refreshNodes();
        List<DispatchInfo> dispatchInfos = null;
        EngineLock lock = null;
        try {
            // ?????????????????????
            lock = pessimisticLockService.tryLock(task.getTransactionId());
            dispatchInfos = publish(task);
            final boolean unlock = pessimisticLockService.unlock(lock);
            Assert.isTrue(unlock, "Unlock failed: Lock(%s)", lock);
            lock = null;
        } catch (InterruptedException e) {
            log.warn("Failed to request lock: {}", SUBMIT_BUSINESS_CODE);
        } finally {
            // ?????????????????????(????????????)
            if (lock != null)
                pessimisticLockService.unlock(lock);
        }
        return TaskPublishVO.builder()
                .uuid(task.getUuid())
                .dispatchInfos(dispatchInfos)
                .build();
    }

    /**
     * ????????????????????????
     * */
    public void recoverFutureAndTimeoutTasks() {
        final int count = futureTaskService.count();
        if (count == 0) return;
        int totalPage = (count / MAX_RECOVER_PAGE) + (count % MAX_RECOVER_PAGE) == 0 ? 0 : 1;
        final List<FutureTask> failedFutureTasks = new ArrayList<>();
        final LambdaQueryWrapper<FutureTask> wrapper = new LambdaQueryWrapper<>();
        for (int i = 0; i < totalPage; ++ i) {
            Page<FutureTask> page = new Page<>(i, MAX_RECOVER_PAGE);
            List<FutureTask> records = futureTaskService.page(page, wrapper).getRecords();
            failedFutureTasks.addAll(recoverFutureTasks(records));
        }
        // table_task ???????????? WAITING ???????????????????????????
        recoverTimeoutTasks();
        // reinsert failure future tasks into future_table
        if (failedFutureTasks.size() == 0) return;
        try {
            futureTaskService.saveBatch(failedFutureTasks);
        } catch (Exception e) {
            log.warn("Unknown exception in reinserting failure future tasks", e);
        }
    }

    /**
     * refresh ??????????????????
     * */
    public void refreshNodes() {
        final List<ExecutionNode> broadcast = nodeInstancePool.broadcast();
        if (broadcast.size() == 1)
            log.debug("No available remote node");
    }

    /**
     * ???????????????????????????Id
     * */
    @NotNull
    public Long getNextTransactionId() {
        final AtomicLong globalTransactionId = nodeInstancePool.getGlobalTransactionId();
        globalTransactionId.set(taskService.getNextTransactionId());
        return globalTransactionId.incrementAndGet();
    }

    /**
     * ??????????????????
     * */
    @NotNull
    protected List<FutureTask> recoverFutureTasks(List<FutureTask> futureTasks) {
        List<FutureTask> failures = new ArrayList<>();
        for (FutureTask futureTask : futureTasks) {
            final Task task = Task.parse(futureTask);
            try {
                // ???????????????
                futureTaskService.removeByTaskUUID(task);
                if (task.getCron() == null) {
                    doRecoverOverload(task);
                } else {
                    doRecoverFuture(task);
                }
            } catch (Exception e) {
                failures.add(futureTask);
                log.warn("Some error occurred when publish future task#{}, try reinsert to future_table later", task.getUuid());
            }
        }
        return failures;
    }

    /**
     * ??????????????????
     * */
    @Scheduled(fixedRate = LONGEST_EXECUTION_SECONDS, initialDelay = 3 * 60, timeUnit = TimeUnit.SECONDS)
    protected void recoverTimeoutTasks() {
        List<Task> tasks = taskService.queryAllTimeoutWaiting(LONGEST_EXECUTION_SECONDS);
        for (Task task : tasks) { // failure
            try {
                log.info("Failure invoke task({}) restart", task);
                final List<DispatchInfo> dispatchInfos = publish(task);
                log.info("Failure task#{} recovery, dispatchInfos={}", task.getUuid(), dispatchInfos);
            } catch (Exception e) {
                log.warn("Some error occurred when publish task#{}", task.getUuid());
            }
        }
    }

    /**
     * ???????????????????????????
     * */
    protected void doRecoverOverload(Task task) {
        final List<DispatchInfo> dispatchInfos = publish(task);
        log.debug("Overload task#{} recovery, dispatchInfos={}", task.getUuid(), dispatchInfos);
    }

    /**
     * ????????????????????????
     * */
    protected void doRecoverFuture(Task task) {
        // ?????? tryRecordInsert: ??????????????? -> ??????????????????; ?????????????????? -> ????????????????????????
        taskService.removeByUUID(task);
        if (CronParser.timeout(task.getCron(), task.getStartTime().toInstant(ZoneOffset.ofHours(8)).toEpochMilli())) {
            task.setCron(null);
            log.debug("Future task#{} timeout", task.getUuid());
        }
        final List<DispatchInfo> dispatchInfos = publish(task);
        log.info("Future task#{} recovery, dispatchInfos={}", task.getUuid(), dispatchInfos);
    }

    /**
     * ????????????
     * @return
     *  1. Key: host, Value: workerId
     *  2. Key: 'overload', Value: id (FutureTask)
     *  3. Key: 'future', Value: id (FutureTask)
     * */
    @NotNull
    protected List<DispatchInfo> publish(Task task) throws PublishException {
        if (nodeInstancePool.isAllOverload()) {
            return Collections.singletonList(doPublishOverload(task));
        }
        // ??????????????????????????????????????? transactionId
        final boolean noRepeatId = taskService.tryRecordInsert(task);
        if (task.getCron() == null && !noRepeatId)
            throw new PublishException("????????? transactionId");
        // ??????????????????
        if (task.getCron() != null) {
            if (noRepeatId) {
                return Collections.singletonList(doPublishFuture(task));
            }
            log.debug("?????????????????? Task({})", task.getUuid());
            // remove schedule task in future_task table
            futureTaskService.removeByTaskUUID(task);
        }
        if ("iterate".equalsIgnoreCase(task.getType())) {
            return doPublishIterate(task);
        } else { // TYPE.DISPATCH
            final RpcClientProxy rpcClientProxy = nodeInstancePool.getRpcClientProxy();
            return Collections.singletonList(doPublishDispatch(task, rpcClientProxy));
        }
    }

    /**
     * ?????????????????????
     * */
    protected DispatchInfo doPublishOverload(Task task) {
        final FutureTask futureTask = FutureTask.parse(task);
        // ????????????????????????????????????????????????
        futureTaskService.save(futureTask);
        return DispatchInfo.builder()
                .status(DispatchInfo.OVERLOAD)
                .build();
    }

    /**
     * ??????????????????
     * */
    protected DispatchInfo doPublishFuture(Task task) {
        log.debug("???????????????????????? Task({})-corn:{}", task.getUuid(), task.getCron());
        scheduledThreadPool.schedule(() -> publish(task), CronParser.parse(task.getCron()), CronParser.TIME_UNIT);
        final FutureTask futureTask = FutureTask.parse(task);
        futureTaskService.save(futureTask);
        return DispatchInfo.builder()
                .status(DispatchInfo.FUTURE)
                .build();
    }

    /**
     * ??????????????????
     * */
    protected List<DispatchInfo> doPublishIterate(Task task) {
        // ??????????????????
        final RpcClientProxy rpcClientProxy = nodeInstancePool.getRpcClientProxy()
                .copy(new OnlineNodeServiceDiscovery(nodeInstancePool, new RoundRobinLoadBalancer()));
        final int size = nodeInstancePool.getTempExecutionNodes().size();
        List<DispatchInfo> dispatchInfos = new ArrayList<>(size);
        for (int i = 0; i < size; ++i) {
            dispatchInfos.add(doPublishDispatch(task, rpcClientProxy));
        }
        return dispatchInfos;
    }

    /**
     * ??????????????????
     * */
    protected DispatchInfo doPublishDispatch(Task task, RpcClientProxy rpcClientProxy) {
        final ConsumerService proxy = rpcClientProxy.getProxy(ConsumerService.class);
        final String workerId = proxy.dispatchTask(task);
        final InetSocketAddress lastAddress = rpcClientProxy.getClient().getLastAddress();
        return DispatchInfo.builder()
                .host(lastAddress.getHostString())
                .port(lastAddress.getPort())
                .workerId(workerId)
                .status(DispatchInfo.DISPATCHED)
                .build();
    }

}
