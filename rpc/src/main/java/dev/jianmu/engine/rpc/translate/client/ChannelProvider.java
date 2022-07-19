package dev.jianmu.engine.rpc.translate.client;

import dev.jianmu.engine.rpc.RpcError;
import dev.jianmu.engine.rpc.codec.CommonDecoder;
import dev.jianmu.engine.rpc.codec.CommonEncoder;
import dev.jianmu.engine.rpc.exception.RpcException;
import dev.jianmu.engine.rpc.serializer.CommonSerializer;
import dev.jianmu.engine.rpc.translate.NettyClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 客户端获取 Channel 对象
 * */
@Slf4j
public class ChannelProvider {

    private static final Bootstrap bootstrap = initializeBootstrap();
    /**
     * 保存所有客户端 Channel
     * */
    private static final Map<String, Channel> channels = new ConcurrentHashMap<>();
    private static EventLoopGroup worker;

    @NotNull
    public static Channel get(InetSocketAddress address, CommonSerializer serializer) {
        String key = address.toString() + serializer.getCode();
        if (channels.containsKey(key)) {
            Channel channel = channels.get(key);
            if (channel != null && channel.isActive())
                return channel;
            else channels.remove(key);
        }
        bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel ch) {
                ch.pipeline()
                        .addLast(new CommonEncoder(serializer))
                        // 心跳检测 如果 5 秒内 write() 方法未被调用则触发一次 userEventTrigger() 方法
                        .addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS))
                        .addLast(new CommonDecoder())
                        .addLast(new NettyClientHandler(serializer));
            }
        });
        try {
            Channel channel = connect(address);
            channels.put(key, channel);
            return channel;
        } catch (InterruptedException | ExecutionException | IllegalStateException e) {
            throw new RpcException(RpcError.CLIENT_CONNECT_SERVER_FAILURE);
        }
    }

    public static void releaseAll() {
        worker.shutdownGracefully();
        channels.forEach((s, channel) -> channel.close());
    }

    /**
     * Netty 客户端创建通道连接，实现连接失败重试机制
     * */
    private static Channel connect(InetSocketAddress address) throws ExecutionException, InterruptedException, IllegalStateException {
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        ChannelProvider.bootstrap.connect(address).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.debug("客户端连接成功！");
                completableFuture.complete(future.channel());
            } else {
                throw new IllegalStateException(address.toString());
            }
        });
        return completableFuture.get();
    }

    private static Bootstrap initializeBootstrap() {
        worker = new NioEventLoopGroup();
        return new Bootstrap()
                .group(worker)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                // 启用心跳机制 (TCP 层)，默认间隔 2h
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true);
    }

}
