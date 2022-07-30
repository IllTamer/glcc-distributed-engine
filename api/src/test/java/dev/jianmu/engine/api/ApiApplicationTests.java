package dev.jianmu.engine.api;

import dev.jianmu.engine.api.config.application.RegisterApplication;
import dev.jianmu.engine.register.util.CronParser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ApiApplicationTests {

    @Autowired
    private RegisterApplication registerApplication;

    @Test
    void rpcInvoke() throws InterruptedException {
        System.out.println(registerApplication.getNodeInstancePool().getLocalPersistentNode().getNodeInfo());
//        final Map<String, Class<?>> serviceMap = properties.getService().getMap();
//        final Set<Discovery> discoveries = properties.getService().getDiscoveries();
//        final LoadBalancer loadBalancer = properties.getService().getLoadBalancer();
//        NettyClient client = new NettyClient(new ConfigureServiceDiscovery(loadBalancer, discoveries), CommonSerializer.getByCode(CommonSerializer.DEFAULT_SERIALIZER));
//        RpcClientProxy proxy = new RpcClientProxy(client, serviceMap);
//        HelloService service = proxy.getProxy(HelloService.class);
//        System.out.println(service.hello("IllTamer"));
    }

    public static void main(String[] args) {
        System.out.println(CronParser.parse("5s9s1m7s0s"));
    }

}
