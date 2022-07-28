package dev.jianmu.engine.rpc.serializer;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import dev.jianmu.engine.rpc.exception.SerializeException;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 基于 Hessian 协议的序列化器
 *      <br>线程安全<br/>
 * */
@Slf4j
public class HessianSerializer implements CommonSerializer {

    @Override
    public byte[] serialize(Object object) {
        HessianOutput output = null;
        try (ByteArrayOutputStream byteOutput = new ByteArrayOutputStream()) {
            output = new HessianOutput(byteOutput);
            output.writeObject(object);
            return byteOutput.toByteArray();
        } catch (Exception | Error e) {
            log.error("序列化时有错误发生，当前对象: {}", object, e);
            throw new SerializeException("序列化时有错误发生");
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    log.error("关闭output流时有错误发生" + e);
                }
            }
        }
    }

    @Override
    public Object deserialize(byte[] bytes, Class<?> clazz) {
        HessianInput input = null;
        try (ByteArrayInputStream byteInput = new ByteArrayInputStream(bytes)) {
            input = new HessianInput(byteInput);
            return input.readObject();
        } catch (Exception e) {
            log.error("序列化时有错误发生" + e);
            throw new SerializeException("序列化时有错误发生");
        } finally {
            if (input != null) {
                input.close();
            }
        }
    }

    @Override
    public int getCode() {
        return SerializerCode.valueOf("HESSIAN").getCode();
    }

}
