package dev.jianmu.engine.rpc.translate.client;

import dev.jianmu.engine.rpc.RpcError;
import dev.jianmu.engine.rpc.exception.RpcException;
import dev.jianmu.engine.rpc.factory.SingletonFactory;
import dev.jianmu.engine.rpc.serializer.CommonSerializer;
import dev.jianmu.engine.rpc.service.ServiceDiscovery;
import dev.jianmu.engine.rpc.translate.RpcRequest;
import dev.jianmu.engine.rpc.translate.RpcResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class NettyClient {

    private final UnprocessedRequests unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);

    @Getter
    private final ServiceDiscovery serviceDiscovery;

    @Getter
    private final CommonSerializer serializer;

    @Getter
    private InetSocketAddress lastAddress;

    public NettyClient(ServiceDiscovery discovery, CommonSerializer serializer) {
        this.serviceDiscovery = discovery;
        this.serializer = serializer;
    }

    @NotNull
    public CompletableFuture<RpcResponse<?>> sendRequest(RpcRequest rpcRequest) throws IllegalStateException {
        if (serializer == null) {
            log.error("未设置序列化器");
            throw new RpcException(RpcError.SERVICE_NOT_FOUND);
        }
        CompletableFuture<RpcResponse<?>> resultFuture = new CompletableFuture<>();
        try {
            // 获取提供对应服务的服务端地址
            InetSocketAddress address = (this.lastAddress = serviceDiscovery.lookupService(rpcRequest.getInterfaceName()));
            Channel channel = ChannelProvider.get(address, serializer);
            if (!channel.isActive())
                throw new IllegalStateException();
            // 放入新的待处理请求
            unprocessedRequests.put(rpcRequest.getRequestId(), resultFuture);
            // 向服务器发送请求并设置监听
            channel.writeAndFlush(rpcRequest).addListener((ChannelFutureListener) future1 -> {
                if(future1.isSuccess()){
                    log.debug("客户端发送消息: {}", rpcRequest);
                } else {
                    future1.channel().close();
                    resultFuture.completeExceptionally(future1.cause());
                    log.error("发送消息时有错误发生:", future1.cause());
                }
            });
        } catch (Exception e) {
            unprocessedRequests.remove(rpcRequest.getRequestId());
            log.error("发送消息时有错误发生:", e);
            // 使当前线程退出阻塞状态并结束
            Thread.currentThread().interrupt();
        }
        return resultFuture;
    }

}
