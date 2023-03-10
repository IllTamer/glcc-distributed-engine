package dev.jianmu.engine.rpc.serializer;

import dev.jianmu.engine.rpc.exception.SerializeException;
import org.jetbrains.annotations.NotNull;

/**
 * 通用的序列化反序列化接口
 * */
public interface CommonSerializer {

    int JSON_SERIALIZER = 1;

    // hessian 有 bug, invoke ConsumerService#dispatchTask 时序列化 Task 会栈溢出
    @Deprecated
    int HESSIAN_SERIALIZER = 2;

    int PROTOBUF_SERIALIZER = 3;

    int DEFAULT_SERIALIZER = PROTOBUF_SERIALIZER;

    byte[] serialize(Object object);

    Object deserialize(byte[] bytes, Class<?> clazz);

    int getCode();

    @NotNull
    static CommonSerializer getByCode(int code) {
        switch (code) {
            case JSON_SERIALIZER:
                return new JsonSerializer();
            case HESSIAN_SERIALIZER:
                return new HessianSerializer();
            case PROTOBUF_SERIALIZER:
                return new ProtostuffSerializer();
            default:
                throw new SerializeException("Unknown serialize code: " + code);
        }
    }

}
