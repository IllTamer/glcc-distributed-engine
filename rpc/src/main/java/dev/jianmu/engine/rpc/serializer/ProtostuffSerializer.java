package dev.jianmu.engine.rpc.serializer;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Protostuff 序列化器
 * */
@SuppressWarnings("unchecked")
public class ProtostuffSerializer implements CommonSerializer {

    private final LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);

    private final Map<Class<?>, Schema<?>> schemaCache = new ConcurrentHashMap<>();

    @Override
    public byte[] serialize(Object object) {
        Class<Object> clazz = (Class<Object>) object.getClass();
        Schema<Object> schema = getSchema(clazz);
        byte[] data;
        try { // 序列化
            data = ProtostuffIOUtil.toByteArray(object, schema, buffer);
        } finally {
            buffer.clear();
        }
        return data;
    }

    @Override
    public Object deserialize(byte[] bytes, Class<?> clazz) {
        Schema<Object> schema = getSchema((Class<Object>) clazz);
        Object object = schema.newMessage();
        ProtostuffIOUtil.mergeFrom(bytes, object, schema); // 反序列化
        return object;
    }

    @Override
    public int getCode() {
        return SerializerCode.valueOf("PROTOBUF").getCode();
    }

    /**
     * 获取 schema 并添加缓存
     * */
    private <T> Schema<T> getSchema(Class<T> clazz) {
        Schema<T> schema = (Schema<T>) schemaCache.get(clazz);
        if (schema == null) {
            schema = RuntimeSchema.getSchema(clazz);
            if (schema != null) {
                schemaCache.put(clazz, schema);
            }
        }
        return schema;
    }

}
