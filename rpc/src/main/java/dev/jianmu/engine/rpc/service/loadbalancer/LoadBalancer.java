package dev.jianmu.engine.rpc.service.loadbalancer;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 负载均衡接口
 * */
public interface LoadBalancer {

    /**
     * 从一系列 InetAddress 中选择一个
     * @param instances 可用链接地址列表
     * */
    InetSocketAddress select(List<InetSocketAddress> instances);

}
