package dev.jianmu.engine.api.config;

import dev.jianmu.engine.register.WeightedMinTaskLoadBalancer;
import dev.jianmu.engine.rpc.annotation.RpcService;
import dev.jianmu.engine.rpc.provider.DefaultServiceProvider;
import dev.jianmu.engine.rpc.provider.ServiceProvider;
import dev.jianmu.engine.rpc.serializer.CommonSerializer;
import dev.jianmu.engine.rpc.service.ConfigureServiceRegistry;
import dev.jianmu.engine.rpc.service.Discovery;
import dev.jianmu.engine.rpc.service.ServiceRegistry;
import dev.jianmu.engine.rpc.service.loadbalancer.LoadBalancer;
import dev.jianmu.engine.rpc.service.loadbalancer.RoundRobinLoadBalancer;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "jianmu.engine")
public class EngineProperties {

    @NotNull
    private Boolean debug = false;

    /**
     * RPC 序列化方式
     * */
    @NotNull
    private CommonSerializer serializer = CommonSerializer.getByCode(CommonSerializer.DEFAULT_SERIALIZER);

    @NotNull
    private ServiceProvider serviceProvider = new DefaultServiceProvider();

    private Service service;

    @Setter
    @Getter
    public static class Service {

        /**
         * 本地服务开放地址
         * */
        @NotNull
        private String host = "localhost";

        /**
         * 本地服务开放端口
         * */
        @NotNull
        private Integer registerPort = 12321;

        /**
         * 服务发现地址配置
         * key: host
         * value: port
         * */
        @NotNull
        private Set<Discovery> discoveries;

        /**
         * RPC 代理类注册
         * @apiNote 也可使用 {@link RpcService} 注册
         * */
        @NotNull
        private Map<String, Class<?>> map;

        /**
         * 负载均衡策略
         * */
        private LoadBalancer loadBalancer;

        /**
         * 服务注册策略
         * */
        @NotNull
        private ServiceRegistry serviceRegistry = new ConfigureServiceRegistry();

    }

}
