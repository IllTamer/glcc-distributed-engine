package dev.jianmu.engine.register;

import com.google.gson.Gson;
import dev.jianmu.engine.consumer.LocalStateService;
import dev.jianmu.engine.rpc.service.Discovery;
import dev.jianmu.engine.rpc.translate.RpcClientProxy;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 节点维护池
 * TODO 持久化+分布式锁
 * */
@Slf4j
public class NodeInstancePool {

    /**
     * 全局事务Id
     * TODO 使用时读取全局最大值
     * */
    private final AtomicLong globalTransactionId;

    @Setter
    private RpcClientProxy rpcClientProxy;

    /**
     * 临时节点列表
     * */
    @Getter
    private final List<ExecutionNode> tempExecutionNodes;

    public NodeInstancePool(Set<Discovery> discoveries) {
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
     * refresh 并返回所有可用节点
     * */
    public List<ExecutionNode> broadcast() {
        synchronized (tempExecutionNodes) {
            CopyOnWriteArrayList<ExecutionNode> callback = tempExecutionNodes.stream()
                    .map(node -> refreshNode(node, rpcClientProxy))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(CopyOnWriteArrayList::new));
            tempExecutionNodes.clear();
            tempExecutionNodes.addAll(callback);
        }
        return tempExecutionNodes;
    }

    @SuppressWarnings("unchecked")
    private static ExecutionNode refreshNode(ExecutionNode node, RpcClientProxy rpcClientProxy) {
        try {
            RpcClientProxy copyProxy = rpcClientProxy.copy(name -> node.getAddress());
            LocalStateService service = copyProxy.getProxy(LocalStateService.class);
            Map<String, Object> nodeInfo = new Gson().fromJson(service.info(), Map.class);
            node.setNodeInfo(nodeInfo);
            return node;
        } catch (Exception e) {
            log.error("", e);
            return null;
        }
    }

}
