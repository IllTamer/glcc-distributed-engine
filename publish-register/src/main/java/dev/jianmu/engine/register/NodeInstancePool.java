package dev.jianmu.engine.register;

import dev.jianmu.engine.consumer.ConsumerService;
import dev.jianmu.engine.consumer.LocalStateService;
import dev.jianmu.engine.monitor.event.ExecutionNode;
import dev.jianmu.engine.monitor.event.filter.AvailabilityFilter;
import dev.jianmu.engine.rpc.service.Discovery;
import dev.jianmu.engine.rpc.translate.RpcClientProxy;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * 节点维护池
 * */
@Slf4j
@Getter
@SuppressWarnings("unchecked")
public class NodeInstancePool {

    private final ReentrantLock lock = new ReentrantLock();
    private final AvailabilityFilter filter;
    /**
     * 全局事务Id
     * */
    private final AtomicLong globalTransactionId = new AtomicLong(0);

    @Setter
    private RpcClientProxy rpcClientProxy;

    /**
     * 可用活跃的临时节点列表
     * */
    private final List<ExecutionNode> tempExecutionNodes;

    /**
     * 过期节点列表
     * <p>
     * 储存连接失败/任务执行能力过低的节点
     * @apiNote 过期节点在每次更新节点状态时尝试刷新
     * */
    private final List<ExecutionNode> deprecatedNodes = new ArrayList<>();

    /**
     * 本机持久化节点
     * */
    private final ExecutionNode localPersistentNode;

    public NodeInstancePool(Set<Discovery> discoveries, Integer port, AvailabilityFilter filter) {
        this.filter = filter;
        this.tempExecutionNodes = discoveries.stream()
                .map(discovery -> {
                    final Date createTime = new Date();
                    return ExecutionNode.builder()
                            .address(new InetSocketAddress(discovery.getHost(), discovery.getPort()))
                            .transactionId(globalTransactionId.getAndIncrement())
                            .createTime(createTime)
                            .modifyTime(createTime)
                            .type(ExecutionNode.Type.EPHEMERAL)
                            .status(ExecutionNode.Status.DISCONNECTED)
                            .dataVersion(0L)
                            .nodeInfo(Collections.EMPTY_MAP)
                            .build();
                })
                .collect(Collectors.toCollection(CopyOnWriteArrayList::new));
        final Date createTime = new Date();
        this.localPersistentNode = ExecutionNode.builder()
                .address(new InetSocketAddress("localhost", port))
                .transactionId(globalTransactionId.getAndIncrement())
                .createTime(createTime)
                .modifyTime(createTime)
                .type(ExecutionNode.Type.PERSISTENT)
                .status(ExecutionNode.Status.AVAILABLE)
                .dataVersion(0L)
                .nodeInfo(Collections.EMPTY_MAP)
                .build();
        this.tempExecutionNodes.add(localPersistentNode);
    }

    /**
     * refresh 所有节点状态
     * */
    public List<ExecutionNode> broadcast() {
        lock.lock();
        List<ExecutionNode> allNodes = new ArrayList<>(tempExecutionNodes.size() + deprecatedNodes.size());
        allNodes.addAll(tempExecutionNodes);
        allNodes.addAll(deprecatedNodes);
        final List<ExecutionNode> available = this.filter.filter(allNodes.stream()
                .map(node -> refreshNode(node, rpcClientProxy))
                .filter(node -> node.getStatus() == ExecutionNode.Status.AVAILABLE)
                .collect(Collectors.toList())
        );
        final List<ExecutionNode> deprecated = allNodes.stream()
                .filter(node -> !available.contains(node))
                .collect(Collectors.toList());
        deprecated.forEach(node -> {
            if (node.getStatus() == ExecutionNode.Status.AVAILABLE)
                node.setStatus(ExecutionNode.Status.OVERLOAD);
        });
        tempExecutionNodes.clear();
        deprecatedNodes.clear();
        tempExecutionNodes.addAll(available);
        deprecatedNodes.addAll(deprecated);
        lock.unlock();
        return tempExecutionNodes;
    }

    /**
     * 查询是否所有节点达到负荷上限
     * */
    public boolean isAllOverload() {
        return tempExecutionNodes.size() == 0;
    }

    @NotNull
    private static ExecutionNode refreshNode(ExecutionNode node, RpcClientProxy rpcClientProxy) {
        try {
            RpcClientProxy copyProxy = rpcClientProxy.copy(name -> node.getAddress());
            final LocalStateService localStateService = copyProxy.getProxy(LocalStateService.class);
            Map<String, Object> nodeInfo = localStateService.info();
            node.setNodeInfo(nodeInfo);
            final ConsumerService consumerService = copyProxy.getProxy(ConsumerService.class);
            node.getNodeInfo().put("taskThreadPoolUsage", consumerService.getTaskThreadPoolUsage());
            node.setStatus(ExecutionNode.Status.AVAILABLE);
        } catch (Exception e) {
            node.setNodeInfo(Collections.EMPTY_MAP);
            node.setStatus(ExecutionNode.Status.DISCONNECTED);
            log.warn("远程节点({})状态刷新失败 Message: {}", node.getAddress(), e.getMessage());
        }
        node.setDataVersion(node.getDataVersion()+1);
        node.setModifyTime(new Date());
        return node;
    }

}
