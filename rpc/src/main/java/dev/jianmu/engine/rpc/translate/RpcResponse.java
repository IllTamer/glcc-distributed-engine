package dev.jianmu.engine.rpc.translate;

import dev.jianmu.engine.rpc.ResponseCode;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 服务端处理完后，向客户端返回的对象
 * */
@Data
@NoArgsConstructor
public class RpcResponse<T> implements Serializable {

    /**
     * 响应对应的请求号
     * */
    private String requestId;
    /**
     * 响应状态码
     * */
    private Integer statusCode;

    /**
     * 状态码对应的信息
     * */
    private String message;

    /**
     * 成功时的响应数据
     * */
    private T data;

    /**
     * 构造调用成功时返回服务端的协议对象
     * @param data 方法的返回值
     * */
    public static <T> RpcResponse <T> success(T data, String requestId) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setRequestId(requestId);
        response.setStatusCode(ResponseCode.SUCCESS.getCode());
        response.setData(data);
        return response;
    }

    /**
     * 构造调用失败时返回服务端的协议对象
     * */
    public static <T> RpcResponse <T> fail(ResponseCode code, String requestId) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setRequestId(requestId);
        response.setStatusCode(code.getCode());
        response.setMessage(code.getMessage());
        return response;
    }

}
