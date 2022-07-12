package dev.jianmu.engine.rpc.service;

import java.net.InetSocketAddress;

/**
 * 服务发现接口
 * */
public interface ServiceDiscovery {

    /**
     * 根据服务名称查找服务端地址
     * @param name 此参数为第三方组件如 Nacos 设计，在本实现中
     *  默认所有服务提供端均实现所有服务，故该方法进行全地址的服务发现
     * */
    InetSocketAddress lookupService(String name);

}
