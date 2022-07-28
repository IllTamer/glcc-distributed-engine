package dev.jianmu.engine.api;

import dev.jianmu.engine.api.config.application.RegisterApplication;
import dev.jianmu.engine.consumer.LocalStateServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ApiApplicationTests {

    @Autowired
    private RegisterApplication registerApplication;

    @Test
    void rpcInvoke() throws InterruptedException {
        System.out.println(registerApplication.getNodeInstancePool().getTempExecutionNodes());
//        final Map<String, Class<?>> serviceMap = properties.getService().getMap();
//        final Set<Discovery> discoveries = properties.getService().getDiscoveries();
//        final LoadBalancer loadBalancer = properties.getService().getLoadBalancer();
//        NettyClient client = new NettyClient(new ConfigureServiceDiscovery(loadBalancer, discoveries), CommonSerializer.getByCode(CommonSerializer.DEFAULT_SERIALIZER));
//        RpcClientProxy proxy = new RpcClientProxy(client, serviceMap);
//        HelloService service = proxy.getProxy(HelloService.class);
//        System.out.println(service.hello("IllTamer"));
        Thread.sleep(9999999L);
    }

    public static void main(String[] args) {
        Integer a = null;
        synchronized (a) {
            System.out.println(1);
        }
        System.out.println(2);
    }

}
