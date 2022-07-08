package dev.jianmu.engine.api.service.impl;

import dev.jianmu.engine.api.service.HelloService;

//@RpcService 测试注解+配置
public class HelloServiceImpl implements HelloService {

    @Override
    public String hello(String name) {
        System.out.println("Hello " + name);
        return "Hi " + name;
    }

}
