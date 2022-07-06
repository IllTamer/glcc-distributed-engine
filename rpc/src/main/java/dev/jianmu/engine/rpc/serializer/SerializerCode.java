package dev.jianmu.engine.rpc.serializer;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 序列化类型编码枚举
 * */
@Getter
@AllArgsConstructor
public enum SerializerCode {

    JSON(1),

    HESSIAN(2),

    PROTOBUF(3);

    private final int code;

}
