package dev.jianmu.engine.rpc.codec;

import dev.jianmu.engine.rpc.PackageType;
import dev.jianmu.engine.rpc.RpcError;
import dev.jianmu.engine.rpc.exception.RpcException;
import dev.jianmu.engine.rpc.request.RpcRequest;
import dev.jianmu.engine.rpc.response.RpcResponse;
import dev.jianmu.engine.rpc.serializer.CommonSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 通用解码器
 * <pre>
 * +---------------+---------------+-----------------+-------------+
 * |  Magic Number |  Package Type | Serializer Type | Data Length |
 * |    4 bytes    |    4 bytes    |     4 bytes     |   4 bytes   |
 * +---------------+---------------+-----------------+-------------+
 * |                          Data Bytes                           |
 * |                   Length: ${Data Length}                      |
 * +---------------------------------------------------------------+
 * </pre>
 * */
@Slf4j
public class CommonDecoder extends ReplayingDecoder<Object> {

    private static final int MAGIC_NUMBER = 0x07060507;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) {
        int magic = buf.readInt();
        if (magic != MAGIC_NUMBER) {
            log.error("不识别的协议包：{}", magic);
            throw new RpcException(RpcError.UNKNOWN_PROTOCOL);
        }
        int packageCode = buf.readInt();
        Class<?> packageClass;
        if (packageCode == PackageType.REQUEST_PACK.getCode()) {
            packageClass = RpcRequest.class;
        } else if (packageCode == PackageType.RESPONSE_PACK.getCode()) {
            packageClass = RpcResponse.class;
        } else {
            log.error("不识别的数据包：{}", packageCode);
            throw new RpcException(RpcError.UNKNOWN_PACKAGE_TYPE);
        }
        int serializerCode = buf.readInt();
        CommonSerializer serializer = CommonSerializer.getByCode(serializerCode);
        if (serializer == null) {
            log.error("不识别的反序列化器：{}", serializerCode);
            throw new RpcException(RpcError.UNKNOWN_SERIALIZER);
        }
        int length = buf.readInt();
        System.out.println(length + " " + buf);
        byte[] bytes = new byte[length];
        buf.readBytes(bytes);
        Object object = serializer.deserialize(bytes, packageClass);
        out.add(object);
    }

}
