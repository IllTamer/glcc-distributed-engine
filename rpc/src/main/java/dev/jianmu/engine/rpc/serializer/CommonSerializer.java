package dev.jianmu.engine.rpc.serializer;

/**
 * 通用的序列化反序列化接口
 * */
public interface CommonSerializer {

    int JSON_SERIALIZER = 1;

    int HESSIAN_SERIALIZER = 2;

    int PROTOBUF_SERIALIZER = 3;

    int DEFAULT_SERIALIZER = JSON_SERIALIZER;

    byte[] serialize(Object object);

    Object deserialize(byte[] bytes, Class<?> clazz);

    int getCode();

    static CommonSerializer getByCode(int code) {
        switch (code) {
            case JSON_SERIALIZER:
                return new JsonSerializer();
            case HESSIAN_SERIALIZER:
                return new HessianSerializer();
            case PROTOBUF_SERIALIZER:
                return new ProtostuffSerializer();
            default:
                return null;
        }
    }



}
