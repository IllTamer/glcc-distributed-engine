package dev.jianmu.engine.api.service.impl;

import dev.jianmu.engine.api.config.application.ConsumerApplication;
import dev.jianmu.engine.consumer.ConsumerService;
import dev.jianmu.engine.provider.Task;
import dev.jianmu.engine.rpc.annotation.RpcService;
import dev.jianmu.engine.rpc.factory.SingletonFactory;
import org.springframework.context.ApplicationContext;

@RpcService
public class ConsumerServiceImpl implements ConsumerService {

    @Override
    public String dispatchTask(Task task) {
        final ApplicationContext context = SingletonFactory.getInstance(ApplicationContext.class);
        ConsumerApplication consumerApplication = context.getBean(ConsumerApplication.class);
        return consumerApplication.push(task);
    }

    @Override
    public int getTaskThreadPoolUsage() {
        final ApplicationContext context = SingletonFactory.getInstance(ApplicationContext.class);
        ConsumerApplication consumerApplication = context.getBean(ConsumerApplication.class);
        return consumerApplication.getThreadPoolUsage();
    }

}
