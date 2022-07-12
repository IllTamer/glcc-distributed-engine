package dev.jianmu.engine.api.config;

import dev.jianmu.engine.register.NodeInstancePool;
import dev.jianmu.engine.rpc.translate.RpcClientProxy;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * 延迟初始化处理类
 * */
@Component
public class InitializeBeanPostProcessor implements BeanPostProcessor {

    private final RpcClientProxy rpcClientProxy;

    public InitializeBeanPostProcessor(RpcClientProxy rpcClientProxy) {
        this.rpcClientProxy = rpcClientProxy;
    }

    /**
     * 循环依赖处理
     * */
    @Override
    public Object postProcessAfterInitialization(@NotNull Object bean, @NotNull String beanName) throws BeansException {
        if (bean instanceof NodeInstancePool)
            ((NodeInstancePool) bean).setRpcClientProxy(rpcClientProxy);
        return bean;
    }

}
