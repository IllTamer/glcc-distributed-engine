package dev.jianmu.engine.rpc.serializer;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import dev.jianmu.engine.rpc.exception.SerializeException;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * Json序列化器
 * */
@Slf4j
public class JsonSerializer implements CommonSerializer {
    // Gson 线程安全
    private final Gson gson = new Gson();

    @Override
    public byte[] serialize(Object object) {
        try {
            return gson.toJson(object).getBytes(StandardCharsets.UTF_8);
        } catch (JsonIOException e) {
            throw new SerializeException("序列化时有错误发生");
        }
    }

    @Override
    public Object deserialize(byte[] bytes, Class<?> clazz) {
        try {
            String json = new String(bytes, StandardCharsets.UTF_8);
            return gson.fromJson(json, clazz);
        } catch (JsonSyntaxException e) {
            throw new SerializeException("反序列化时有错误发生");
        }
    }

    @Override
    public int getCode() {
        return SerializerCode.valueOf("JSON").getCode();
    }
}
