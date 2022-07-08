package dev.jianmu.engine.rpc.provider;

import dev.jianmu.engine.rpc.RpcError;
import dev.jianmu.engine.rpc.exception.RpcException;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认服务注册表，保存服务端本地服务
 * */
@Slf4j
public class DefaultServiceProvider implements ServiceProvider {

    /**
     * key - 接口名称
     * value - 实现类实例
     * */
    private final Map<String, Object> serviceMap = new ConcurrentHashMap<>();

    @Override
    public <T> void addServiceProvider(T service, String serviceName) {
        if (serviceMap.containsKey(serviceName)) return;
        serviceMap.put(serviceName, service);
        log.debug("已将服务 {} 注册至接口 {}", serviceName, service.getClass().getInterfaces());
    }

    @Override
    public Object getServiceProvider(String serviceName) {
        Object service = serviceMap.get(serviceName);
        if (service == null)
            throw new RpcException(RpcError.SERVICE_NOT_FOUND);
        return service;
    }

}
