package dev.jianmu.engine.rpc.service;

import java.net.InetSocketAddress;

/**
 * 服务注册接口
 * */
public interface ServiceRegistry {

    /**
     * 将一个服务注册到注册表
     * */
    void register(String serviceName, InetSocketAddress address);

}
