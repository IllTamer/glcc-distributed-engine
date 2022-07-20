package dev.jianmu.engine.register;

import dev.jianmu.engine.rpc.RpcError;
import dev.jianmu.engine.rpc.exception.RpcException;
import dev.jianmu.engine.rpc.service.ServiceDiscovery;
import dev.jianmu.engine.rpc.service.loadbalancer.LoadBalancer;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 在线临时节点服务发现
 * */
public class OnlineNodeServiceDiscovery implements ServiceDiscovery {

    private final NodeInstancePool nodeInstancePool;
    private final LoadBalancer loadBalancer;

    public OnlineNodeServiceDiscovery(NodeInstancePool nodeInstancePool, LoadBalancer loadBalancer) {
        this.nodeInstancePool = nodeInstancePool;
        this.loadBalancer = loadBalancer;
    }

    @Override
    public InetSocketAddress lookupService(String name) {
        // 节点离线检测
        List<InetSocketAddress> addresses = nodeInstancePool.getTempExecutionNodes().stream()
                .map(ExecutionNode::getAddress)
                .collect(Collectors.toList());
        if (addresses.size() == 0)
            throw new RpcException(RpcError.SERVICE_NOT_FOUND);
        return loadBalancer.select(addresses);
    }

}
