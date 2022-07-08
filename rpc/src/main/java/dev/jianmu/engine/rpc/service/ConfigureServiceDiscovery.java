package dev.jianmu.engine.rpc.service;

import java.net.InetSocketAddress;

/**
 * 配置文件服务发现
 * */
public class ConfigureServiceDiscovery implements ServiceDiscovery{
    @Override
    public InetSocketAddress lookupService(String name) {
        return null;
    }
}
