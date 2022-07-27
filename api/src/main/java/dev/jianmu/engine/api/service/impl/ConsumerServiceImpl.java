package dev.jianmu.engine.api.service.impl;

import dev.jianmu.engine.api.config.application.ConsumerApplication;
import dev.jianmu.engine.api.service.ConsumerService;
import dev.jianmu.engine.provider.ProviderInfo;
import dev.jianmu.engine.provider.Task;
import dev.jianmu.engine.rpc.annotation.RpcService;
import dev.jianmu.engine.rpc.factory.SingletonFactory;
import org.springframework.context.ApplicationContext;

@RpcService
public class ConsumerServiceImpl implements ConsumerService {

    private final ConsumerApplication consumerApplication =
            SingletonFactory.getInstance(ApplicationContext.class).getBean(ConsumerApplication.class);

    @Override
    public String dispatchTask(Task task) {
        final ProviderInfo info = consumerApplication.push(task);
        // TODO thread pool usage
        return info.getWorkerId();
    }

}
