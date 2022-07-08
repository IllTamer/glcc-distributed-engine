package dev.jianmu.engine.rpc.service;

import dev.jianmu.engine.rpc.RpcError;
import dev.jianmu.engine.rpc.annotation.RpcService;
import dev.jianmu.engine.rpc.exception.RpcException;
import dev.jianmu.engine.rpc.service.loadbalancer.LoadBalancer;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 配置文件服务发现实现
 * @apiNote 也可使用 {@link RpcService} 注册
 * */
@Slf4j
public class ConfigureServiceDiscovery implements ServiceDiscovery{

    private final LoadBalancer loadBalancer;
    private final List<InetSocketAddress> inetAddresses;

    public ConfigureServiceDiscovery(LoadBalancer loadBalancer, Map<String, Integer> discoveries) {
        this.loadBalancer = loadBalancer;
        this.inetAddresses = discoveries.entrySet().stream()
                .map(entry -> new InetSocketAddress(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public InetSocketAddress lookupService(String name) {
        if (inetAddresses.size() == 0) {
            log.error("找不到对应服务 {}", name);
            throw new RpcException(RpcError.SERVICE_NOT_FOUND);
        }
        return loadBalancer.select(inetAddresses);
    }

}
