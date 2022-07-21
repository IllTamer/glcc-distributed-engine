package dev.jianmu.engine.api.config.application;

import dev.jianmu.engine.api.dto.TaskDTO;
import dev.jianmu.engine.api.service.RegisterService;
import dev.jianmu.engine.provider.Task;
import dev.jianmu.engine.provider.TaskStatus;
import dev.jianmu.engine.register.ExecutionNode;
import dev.jianmu.engine.register.NodeInstancePool;
import dev.jianmu.engine.rpc.translate.RpcClientProxy;
import dev.jianmu.engine.rpc.util.Assert;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Getter
public class RegisterApplication {

    private final NodeInstancePool nodeInstancePool;

    public RegisterApplication(NodeInstancePool nodeInstancePool) {
        this.nodeInstancePool = nodeInstancePool;
    }

    public void refreshNodes() {
        nodeInstancePool.refreshLocalNode();
        final List<ExecutionNode> broadcast = nodeInstancePool.broadcast();
        if (broadcast == null || broadcast.size() == 0)
            log.warn("No node available");
    }

    /**
     * 获取下一个全局事务Id
     * */
    public Long getNextTransactionId() {
        final RpcClientProxy rpcClientProxy = nodeInstancePool.getRpcClientProxy();
        final AtomicLong globalTransactionId = nodeInstancePool.getGlobalTransactionId();
        List<Long> idList = nodeInstancePool.getTempExecutionNodes().stream()
                .map(node -> getNodeTransactionId(node, rpcClientProxy))
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
        Assert.notEmpty(idList, "No available node!");
        globalTransactionId.set(idList.get(0));
        return globalTransactionId.incrementAndGet();
    }

    public ExecutionNode publish(Task task) {
        // TODO
        // 本机任务发布 + 分布式任务调度
        return null;
    }

    public Task createTask(TaskDTO dto) {
        return Task.builder()
                .uuid(UUID.randomUUID().toString())
                .transactionId(dto.getTransactionId())
                .priority(dto.getPriority())
                .script(dto.getScript())
                .status(TaskStatus.WAITING)
                .build();
    }

    private static Long getNodeTransactionId(ExecutionNode node, RpcClientProxy rpcClientProxy) {
        RpcClientProxy copyProxy = rpcClientProxy.copy(name -> node.getAddress());
        final RegisterService proxy = copyProxy.getProxy(RegisterService.class);
        return proxy.getTransactionId();
    }

}
