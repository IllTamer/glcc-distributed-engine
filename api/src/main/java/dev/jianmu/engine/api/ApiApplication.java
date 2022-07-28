package dev.jianmu.engine.api;

import dev.jianmu.engine.rpc.factory.SingletonFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@SpringBootApplication
public class ApiApplication {

    // TODO 调通任务发布流程
    public static void main(String[] args) {
        SingletonFactory.setInstance(ApplicationContext.class, SpringApplication.run(ApiApplication.class, args));
    }

}
