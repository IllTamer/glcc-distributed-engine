package dev.jianmu.engine.rpc.request;

import dev.jianmu.engine.rpc.provider.ServiceProvider;
import dev.jianmu.engine.rpc.response.ResponseCode;
import dev.jianmu.engine.rpc.response.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 实际执行方法调用的处理器
 * */
@Slf4j
public class RequestHandler {

    private final ServiceProvider serviceProvider;

    public RequestHandler(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    /**
     * @return {@link RpcResponse<Object>}
     * */
    public Object handle(RpcRequest rpcRequest) {
        // 从服务端本地注册表中获取服务实体
        Object service = serviceProvider.getServiceProvider(rpcRequest.getInterfaceName());
        return invokeTargetMethod(rpcRequest, service);
    }

    private Object invokeTargetMethod(RpcRequest rpcRequest, Object service) {
        Object result;
        try {
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
            result = method.invoke(service, rpcRequest.getParameters());
            log.info("服务 {} 成功调用方法 {}", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            return RpcResponse.fail(ResponseCode.METHOD_NOT_FOUND, rpcRequest.getRequestId());
        }
        return RpcResponse.success(result, rpcRequest.getRequestId());
    }

}
