package dev.jianmu.engine.register;

import dev.jianmu.engine.register.util.NodeUtil;
import dev.jianmu.engine.rpc.service.Discovery;
import dev.jianmu.engine.rpc.translate.RpcClientProxy;
import dev.jianmu.engine.rpc.translate.client.NettyClient;
import lombok.Setter;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 节点维护池
 * TODO 持久化+分布式锁
 * */
public class NodeInstancePool {

    /**
     * 全局事务Id
     * */
    private final AtomicLong globalTransactionId;

    @Setter
    private RpcClientProxy rpcClientProxy;

    /**
     * 临时节点列表
     * */
    private final List<ExecutionNode> tempExecutionNodes;

    public NodeInstancePool(Set<Discovery> discoveries) {
        // TODO 使用时读取全局最大值
        this.globalTransactionId = new AtomicLong(0);
        this.tempExecutionNodes = discoveries.stream()
                .map(discovery -> {
                    final Date createTime = new Date();
                    // configure from database
                    return ExecutionNode.builder()
                            .address(new InetSocketAddress(discovery.getHost(), discovery.getPort()))
                            .transactionId(globalTransactionId.getAndIncrement())
                            .createTime(createTime)
                            .modifyTime(createTime)
                            .type(ExecutionNode.Type.EPHEMERAL)
                            // TODO database support
                            .dataVersion(0L)
                            .nodeInfo(new HashMap<>())
                            .build();
                })
                .collect(Collectors.toCollection(CopyOnWriteArrayList::new));
    }

    /**
     * ping 并返回所有可用节点
     * */
    public List<ExecutionNode> broadcast() {
        final NettyClient client = rpcClientProxy.getClient();
        synchronized (tempExecutionNodes) {
            CopyOnWriteArrayList<ExecutionNode> callback = tempExecutionNodes.stream()
                    .map(node -> NodeUtil.pingNode(client, node))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(CopyOnWriteArrayList::new));
            tempExecutionNodes.clear();
            tempExecutionNodes.addAll(callback);
        }
        return tempExecutionNodes;
    }

}
