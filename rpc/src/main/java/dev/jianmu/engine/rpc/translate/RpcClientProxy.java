package dev.jianmu.engine.rpc.translate;

import dev.jianmu.engine.rpc.exception.AssertException;
import dev.jianmu.engine.rpc.request.RpcRequest;
import dev.jianmu.engine.rpc.response.RpcResponse;
import dev.jianmu.engine.rpc.translate.client.NettyClient;
import dev.jianmu.engine.rpc.util.Assert;
import dev.jianmu.engine.rpc.util.RpcMessageChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * RPC 客户端动态代理
 * */
public class RpcClientProxy implements InvocationHandler {

    private static final Logger logger = LoggerFactory.getLogger(RpcClientProxy.class);
    private final NettyClient client;

    public RpcClientProxy(NettyClient client) {
        this.client = client;
    }

    /**
     * 获取 JDK 动态代理对象
     * @param clazz 需要生成代理类的接口
     * */
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
    }

    /**
     * 远程调用方法实现
     *  客户端调用时向服务端传入方法参数并接受执行结果
     *  {@link RpcRequest} 调用方法的信息传输协议
     * @return {@link RpcResponse}
     * */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        logger.debug("调用方法：{}#{}", method.getDeclaringClass().getName(), method.getName());
        RpcRequest rpcRequest = new RpcRequest(UUID.randomUUID().toString(),
                method.getDeclaringClass().getName(),
                method.getName(),
                args,
                method.getParameterTypes(),
                false);
        RpcResponse<?> rpcResponse;
        try { // 异步获取调用结果
            CompletableFuture<RpcResponse<?>> completableFuture = client.sendRequest(rpcRequest);
            rpcResponse = completableFuture.get();
            Assert.notNull(rpcResponse.getData(), "Unexpect null pointer");
        } catch (InterruptedException | ExecutionException | IllegalStateException e) {
            logger.error("方法调用请求失败", e);
            return null;
        }
        RpcMessageChecker.check(rpcRequest, rpcResponse);
        return rpcResponse.getData();
    }

}
