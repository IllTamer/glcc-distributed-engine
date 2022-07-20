package dev.jianmu.engine.register;

import com.google.gson.Gson;
import dev.jianmu.engine.consumer.LocalStateService;
import dev.jianmu.engine.consumer.LocalStateServiceImpl;
import dev.jianmu.engine.rpc.service.Discovery;
import dev.jianmu.engine.rpc.translate.RpcClientProxy;
import dev.jianmu.engine.rpc.util.Assert;
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
 * TODO 分布式锁
 * */
@Slf4j
@Getter
@SuppressWarnings("unchecked")
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

    /**
     * 本机持久化节点
     * */
    private final ExecutionNode localPersistentNode;

    public NodeInstancePool(Set<Discovery> discoveries, Integer port) {
        this.globalTransactionId = new AtomicLong(0);
        this.tempExecutionNodes = discoveries.stream()
                .map(discovery -> {
                    final Date createTime = new Date();
                    return ExecutionNode.builder()
                            .address(new InetSocketAddress(discovery.getHost(), discovery.getPort()))
                            .transactionId(globalTransactionId.getAndIncrement())
                            .createTime(createTime)
                            .modifyTime(createTime)
                            .type(ExecutionNode.Type.EPHEMERAL)
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
                .dataVersion(0L)
                .nodeInfo(Collections.EMPTY_MAP)
                .build();
        refreshLocalNode();
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

    /**
     * 更新本地节点数据
     * */
    public void refreshLocalNode() {
        this.localPersistentNode.setNodeInfo(new Gson().fromJson(new LocalStateServiceImpl().info(), Map.class));
    }

    private static ExecutionNode refreshNode(ExecutionNode node, RpcClientProxy rpcClientProxy) {
        try {
            RpcClientProxy copyProxy = rpcClientProxy.copy(name -> node.getAddress());
            LocalStateService service = copyProxy.getProxy(LocalStateService.class);
            Map<String, Object> nodeInfo = new Gson().fromJson(service.info(), Map.class);
            node.setNodeInfo(nodeInfo);
            node.setModifyTime(new Date());
            node.setDataVersion(node.getDataVersion()+1);
            return node;
        } catch (Exception e) {
            log.error("", e);
            return null;
        }
    }

}
