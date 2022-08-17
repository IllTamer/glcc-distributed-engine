package dev.jianmu.engine.api.config.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import dev.jianmu.engine.api.config.EngineProperties;
import dev.jianmu.engine.api.dto.TaskDTO;
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
import dev.jianmu.engine.provider.TaskStatus;
import dev.jianmu.engine.provider.Worker;
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
import java.time.LocalDateTime;
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
     * 提交任务
     * */
    @NotNull
    public TaskPublishVO submitTask(Task task) {
        // 更新Node状态(CPU使用率，内存使用率等)
        refreshNodes();
        Map<String, String> workerIdMap = null;
        EngineLock lock = null;
        try {
            // 分布式锁，入锁(拒绝新增)
            lock = pessimisticLockService.tryLock(SUBMIT_BUSINESS_CODE);
            workerIdMap = publish(task);
            final boolean unlock = pessimisticLockService.unlock(lock);
            Assert.isTrue(unlock, "Unlock failed: Lock(%s)", lock);
            lock = null;
        } catch (InterruptedException e) {
            log.warn("Failed to request lock: {}", SUBMIT_BUSINESS_CODE);
        } finally {
            // 分布式锁，出锁(允许新增)
            if (lock != null)
                pessimisticLockService.unlock(lock);
        }
        return TaskPublishVO.builder()
                .uuid(task.getUuid())
                .workerIdMap(workerIdMap)
                .build();
    }

    /**
     * 恢复未执行的任务
     * */
    public void recoverFutureAndFailureTasks() {
        int maxPage = 64;
        final int count = futureTaskService.count();
        LambdaQueryWrapper<FutureTask> wrapper = new LambdaQueryWrapper<>();
        List<FutureTask> failedFutureTasks = new ArrayList<>();
        for (int i = 1; i*maxPage < count; ++ i) {
            Page<FutureTask> page = new Page<>(i, maxPage);
            for (FutureTask futureTask : futureTaskService.page(page, wrapper).getRecords()) {
                final Task task = Task.parse(futureTask);
                try {
                    final String cron = task.getCron();
                    // 移出记录表
                    futureTaskService.removeByTaskUUID(task);
                    if (cron == null) { // overload
                        final Map<String, String> workerIdMap = publish(task);
                        log.debug("Overload task#{} recovery, workerIdMap={}", task.getUuid(), workerIdMap);
                    } else { // future
                        // 删除 tryRecordInsert: 过期的移除 -> 发布普通任务; 未过期的移除 -> 重新发布定时任务
                        taskService.removeByUUID(task);
                        if (CronParser.timeout(cron, futureTask.getStartTime().toInstant(ZoneOffset.ofHours(8)).toEpochMilli())) {
                            task.setCron(null);
                            log.debug("Future task#{} timeout", task.getUuid());
                        }
                        final Map<String, String> workerIdMap = publish(task);
                        log.info("Future task#{} recovery, workerIdMap={}", task.getUuid(), workerIdMap);
                    }
                } catch (Exception e) {
                    failedFutureTasks.add(futureTask);
                    log.warn("Some error occurred when publish future task#{}, try reinsert to future_table later", task.getUuid());
                }
            }
        }
        // table_task 中余下的 WAITING 数据皆为未执行任务
        recoverFailureTasks();
        // reinsert failure future tasks into future_table
        if (failedFutureTasks.size() == 0) return;
        try {
            futureTaskService.saveBatch(failedFutureTasks);
        } catch (Exception e) {
            log.warn("Unknown exception in reinserting failure future tasks", e);
        }
    }

    public Task createTask(TaskDTO dto) {
        return Task.builder()
                .uuid(UUID.randomUUID().toString())
                .transactionId(dto.getTransactionId())
                .cron(dto.getCron())
                .type(dto.getType())
                .priority(dto.getPriority())
                // default to shell_worker
                .script(Worker.WORKER_MAP.get(Worker.Type.SHELL).parseScript(dto.getScript()))
                .status(TaskStatus.WAITING)
                .startTime(LocalDateTime.now())
                .build();
    }

    /**
     * refresh 所有节点状态
     * */
    public void refreshNodes() {
        final List<ExecutionNode> broadcast = nodeInstancePool.broadcast();
        if (broadcast.size() == 1)
            log.debug("No available remote node");
    }

    /**
     * 获取下一个全局事务Id
     * */
    public Long getNextTransactionId() {
        final AtomicLong globalTransactionId = nodeInstancePool.getGlobalTransactionId();
        globalTransactionId.set(taskService.getNextTransactionId());
        return globalTransactionId.incrementAndGet();
    }

    /**
     * 发布任务
     * @return
     *  1. Key: host, Value: workerId
     *  2. Key: 'overload', Value: id (FutureTask)
     *  3. Key: 'future', Value: id (FutureTask)
     * */
    @NotNull
    protected Map<String, String> publish(Task task) throws PublishException {
        if (nodeInstancePool.isAllOverload()) {
            return doPublishOverload(task);
        }
        // 预先插入一次，校验有无重复 transactionId
        final boolean noRepeatId = taskService.tryRecordInsert(task);
        if (task.getCron() == null && !noRepeatId)
            throw new PublishException("重复的 transactionId");
        // 定时分发检测
        if (task.getCron() != null) {
            if (noRepeatId) {
                return doPublishFuture(task);
            }
            log.debug("分发定时任务 Task({})", task.getUuid());
            // remove schedule task in future_task table
            futureTaskService.removeByTaskUUID(task);
        }
        if ("iterate".equalsIgnoreCase(task.getType())) {
            return doPublishIterate(task);
        } else { // TYPE.DISPATCH
            final RpcClientProxy rpcClientProxy = nodeInstancePool.getRpcClientProxy();
            return doPublishDispatch(task, rpcClientProxy);
        }
    }

    /**
     * 处理超负荷任务
     * */
    protected Map<String, String> doPublishOverload(Task task) {
        final FutureTask futureTask = FutureTask.parse(task);
        // 储存因所有节点不可用而阻塞的任务
        futureTaskService.save(futureTask);
        return Collections.singletonMap("overload", futureTask.getId().toString());
    }

    /**
     * 处理定时任务
     * */
    protected Map<String, String> doPublishFuture(Task task) {
        log.debug("注册定时分发任务 Task({})-corn:{}", task.getUuid(), task.getCron());
        scheduledThreadPool.schedule(() -> publish(task), CronParser.parse(task.getCron()), CronParser.TIME_UNIT);
        final FutureTask futureTask = FutureTask.parse(task);
        futureTaskService.save(futureTask);
        return Collections.singletonMap("future", futureTask.getId().toString());
    }

    /**
     * 处理迭代任务
     * */
    protected Map<String, String> doPublishIterate(Task task) {
        Map<String, String> workerIdMap = new HashMap<>();
        // 创建轮询代理
        final RpcClientProxy rpcClientProxy = nodeInstancePool.getRpcClientProxy()
                .copy(new OnlineNodeServiceDiscovery(nodeInstancePool, new RoundRobinLoadBalancer()));
        final int size = nodeInstancePool.getTempExecutionNodes().size();
        for (int i = 0; i < size; ++i) {
            workerIdMap.putAll(doPublishDispatch(task, rpcClientProxy));
        }
        return workerIdMap;
    }

    /**
     * 处理调度任务
     * */
    protected Map<String, String> doPublishDispatch(Task task, RpcClientProxy rpcClientProxy) {
        final ConsumerService proxy = rpcClientProxy.getProxy(ConsumerService.class);
        final String workerId = proxy.dispatchTask(task);
        final InetSocketAddress lastAddress = rpcClientProxy.getClient().getLastAddress();
        return Collections.singletonMap(lastAddress.getHostString() + ':' + lastAddress.getPort(), workerId);
    }

    @Scheduled(fixedRate = LONGEST_EXECUTION_SECONDS, initialDelay = 3 * 60, timeUnit = TimeUnit.SECONDS)
    protected void recoverFailureTasks() {
        List<Task> tasks = taskService.queryAllTimeoutWaiting(LONGEST_EXECUTION_SECONDS);
        for (Task task : tasks) { // failure
            try {
                log.info("Failure invoke task({}) restart", task);
                final Map<String, String> workerIdMap = publish(task);
                log.info("Failure task#{} recovery, workerIdMap={}", task.getUuid(), workerIdMap);
            } catch (Exception e) {
                log.warn("Some error occurred when publish task#{}", task.getUuid());
            }
        }
    }

}
