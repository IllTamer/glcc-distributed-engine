package dev.jianmu.engine.api.config.application;

import dev.jianmu.engine.api.config.EngineProperties;
import dev.jianmu.engine.api.dto.TaskDTO;
import dev.jianmu.engine.api.mapper.TaskMapper;
import dev.jianmu.engine.api.service.ConsumerService;
import dev.jianmu.engine.api.service.TaskService;
import dev.jianmu.engine.provider.Task;
import dev.jianmu.engine.provider.TaskStatus;
import dev.jianmu.engine.provider.Worker;
import dev.jianmu.engine.register.ExecutionNode;
import dev.jianmu.engine.register.NodeInstancePool;
import dev.jianmu.engine.register.OnlineNodeServiceDiscovery;
import dev.jianmu.engine.register.WeightedMinLoadLoadBalancer;
import dev.jianmu.engine.rpc.factory.SingletonFactory;
import dev.jianmu.engine.rpc.service.loadbalancer.RoundRobinLoadBalancer;
import dev.jianmu.engine.rpc.translate.RpcClientProxy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Getter
@Component
public class RegisterApplication {

    private final TaskService taskService;
    private final NodeInstancePool nodeInstancePool;

    public RegisterApplication(
            TaskService taskService,
            EngineProperties properties
    ) {
        this.taskService = taskService;
        this.nodeInstancePool = new NodeInstancePool(properties.getService().getDiscoveries(), properties.getService().getRegisterPort());
    }

    public void refreshNodes() {
        nodeInstancePool.refreshLocalNode();
        final List<ExecutionNode> broadcast = nodeInstancePool.broadcast();
        if (broadcast.size() <= 1)
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
     * TODO 定时分发
     * @return Key: hostName, Value: workerId
     * */
    public Map<String, String> publish(Task task) {
        Map<String, String> workerIdMap = new HashMap<>();
        // 校验有无重复 transactionId, 预先插入一次
        if (!taskService.tryRecordInsert(task)) return workerIdMap;
        if ("iterate".equals(task.getType())) {
            // 创建轮询代理
            final int size = nodeInstancePool.getTempExecutionNodes().size();
            final RpcClientProxy rpcClientProxy = nodeInstancePool.getRpcClientProxy()
                    .copy(new OnlineNodeServiceDiscovery(nodeInstancePool, new RoundRobinLoadBalancer()));
            for (int i = 0; i < size; ++i) {
                final ConsumerService proxy = rpcClientProxy.getProxy(ConsumerService.class);
                final String workerId = proxy.dispatchTask(task);
                workerIdMap.put(rpcClientProxy.getClient().getLastAddress().getHostString(), workerId);
            }
        } else {
            final RpcClientProxy rpcClientProxy = nodeInstancePool.getRpcClientProxy();
            ConsumerService service;
            if ("local".equals(task.getType())) {
                service = SingletonFactory.getInstance(ConsumerService.class);
            } else { // TYPE.DISPATCH
                service = rpcClientProxy.getProxy(ConsumerService.class);
                // 判断本地节点权重 (自身也可执行任务/单机)
                final WeightedMinLoadLoadBalancer loadBalancer = (WeightedMinLoadLoadBalancer)
                        ((OnlineNodeServiceDiscovery) rpcClientProxy.getClient().getServiceDiscovery()).getLoadBalancer();
                // 自身权重更大，转为本地执行
                if (WeightedMinLoadLoadBalancer.getWeight(nodeInstancePool.getLocalPersistentNode()) >= loadBalancer.getLatestNodeLoad()) {
                    service = SingletonFactory.getInstance(ConsumerService.class);
                    task.setType("local");
                }
            }
            final String workerId = service.dispatchTask(task);
            workerIdMap.put(rpcClientProxy.getClient().getLastAddress().getHostString(), workerId);
        }
        return workerIdMap;
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

}
