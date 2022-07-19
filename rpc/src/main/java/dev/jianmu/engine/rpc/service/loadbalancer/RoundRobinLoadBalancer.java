package dev.jianmu.engine.rpc.service.loadbalancer;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 负载均衡 - 轮转算法
 * */
public class RoundRobinLoadBalancer implements LoadBalancer {

    private final ThreadLocal<Integer> index = new ThreadLocal<>();

    @Override
    public InetSocketAddress select(List<InetSocketAddress> instances) {
        Integer integer = index.get();
        if (integer >= instances.size()) integer %= instances.size();
        return instances.get(integer);
    }

}
