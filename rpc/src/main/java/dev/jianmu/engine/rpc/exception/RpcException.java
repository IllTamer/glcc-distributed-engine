package dev.jianmu.engine.rpc.exception;

import dev.jianmu.engine.rpc.RpcError;

/**
 * RPC 调用异常
 * */
public class RpcException extends RuntimeException {

    public RpcException(RpcError error, String detail) {
        super(error.getMessage() + ": " + detail);
    }

    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcException(RpcError error) {
        super(error.getMessage());
    }

}
