package dev.jianmu.engine.rpc.util;

import dev.jianmu.engine.rpc.ResponseCode;
import dev.jianmu.engine.rpc.RpcError;
import dev.jianmu.engine.rpc.exception.RpcException;
import dev.jianmu.engine.rpc.translate.RpcRequest;
import dev.jianmu.engine.rpc.translate.RpcResponse;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * 检查响应和请求
 * */
@Slf4j
@UtilityClass
public class RpcMessageChecker {

    private static final String INTERFACE_NAME = "interfaceName";

    public static void check(RpcRequest rpcRequest, RpcResponse<?> rpcResponse) {
        if(rpcResponse == null) {
            log.error("调用服务失败，serviceName: {}", rpcRequest.getInterfaceName());
            throw new RpcException(RpcError.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }
        //响应与请求的请求号不同
        if(!rpcRequest.getRequestId().equals(rpcResponse.getRequestId())) {
            throw new RpcException(RpcError.RESPONSE_NOT_MATCH, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }
        //调用失败
        if(rpcResponse.getStatusCode() == null || !rpcResponse.getStatusCode().equals(ResponseCode.SUCCESS.getCode())){
            log.error("调用服务失败，serviceName: {}，RpcResponse: {}", rpcRequest.getInterfaceName(), rpcResponse);
            throw new RpcException(RpcError.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }
    }

}
