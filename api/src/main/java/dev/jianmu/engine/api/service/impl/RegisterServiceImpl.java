package dev.jianmu.engine.api.service.impl;

import dev.jianmu.engine.api.config.application.RegisterApplication;
import dev.jianmu.engine.api.service.RegisterService;
import dev.jianmu.engine.rpc.annotation.RpcService;
import dev.jianmu.engine.rpc.factory.SingletonFactory;
import org.springframework.context.ApplicationContext;

@RpcService
public class RegisterServiceImpl implements RegisterService {

    private final RegisterApplication registerApplication =
            SingletonFactory.getInstance(ApplicationContext.class).getBean(RegisterApplication.class);

    @Override
    public Long getTransactionId() {
        return registerApplication.getNodeInstancePool().getGlobalTransactionId().get();
    }

}
