package dev.jianmu.engine.rpc.translate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 传输协议
 *  - 客户端向服务端传输的对象
 * */
@Data
//@Builder // 使用创建者模式，一次性给所有变量初始赋值
@NoArgsConstructor
@AllArgsConstructor
public class RpcRequest implements Serializable {

    /**
     * 请求序列号
     * */
    private String requestId;

    /**
     * 待调用接口的名称
     * */
    private String interfaceName;

    /**
     * 待调用方法的名称
     * */
    private String methodName;

    /**
     * 待调用方法的参数
     * */
    private Object[] parameters;

    /**
     * 待调用方法的参数类型
     * */
    private Class<?>[] paramTypes;

    /**
     * 是否是心跳包
     * */
    public boolean heartBeat;

}
