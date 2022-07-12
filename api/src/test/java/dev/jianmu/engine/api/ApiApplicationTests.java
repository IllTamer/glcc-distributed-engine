package dev.jianmu.engine.api;

import dev.jianmu.engine.api.config.EngineProperties;
import dev.jianmu.engine.api.service.HelloService;
import dev.jianmu.engine.rpc.serializer.CommonSerializer;
import dev.jianmu.engine.rpc.service.ConfigureServiceDiscovery;
import dev.jianmu.engine.rpc.service.Discovery;
import dev.jianmu.engine.rpc.service.loadbalancer.LoadBalancer;
import dev.jianmu.engine.rpc.translate.RpcClientProxy;
import dev.jianmu.engine.rpc.translate.client.NettyClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;
import java.util.Set;

@SpringBootTest
class ApiApplicationTests {

    @Autowired
    private EngineProperties properties;

    @Test
    void rpcInvoke() {
        final Map<String, Class<?>> serviceMap = properties.getService().getMap();
        final Set<Discovery> discoveries = properties.getService().getDiscoveries();
        final LoadBalancer loadBalancer = properties.getService().getLoadBalancer();
        NettyClient client = new NettyClient(new ConfigureServiceDiscovery(loadBalancer, discoveries), CommonSerializer.getByCode(CommonSerializer.DEFAULT_SERIALIZER));
        RpcClientProxy proxy = new RpcClientProxy(client, serviceMap);
        HelloService service = proxy.getProxy(HelloService.class);
        System.out.println(service.hello("IllTamer"));
    }

}
