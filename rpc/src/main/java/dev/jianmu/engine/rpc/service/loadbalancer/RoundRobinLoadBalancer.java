package dev.jianmu.engine.rpc.service.loadbalancer;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 负载均衡 - 轮转算法
 * */
public class RoundRobinLoadBalancer implements LoadBalancer {

    private int index = 0;

    @Override
    public InetSocketAddress select(List<InetSocketAddress> instances) {
        if (index >= instances.size()) index %= instances.size();
        return instances.get(index);
    }

}
