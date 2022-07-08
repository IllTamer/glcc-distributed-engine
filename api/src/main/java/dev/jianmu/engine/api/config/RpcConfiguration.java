package dev.jianmu.engine.api.config;

import dev.jianmu.engine.rpc.codec.CommonDecoder;
import dev.jianmu.engine.rpc.codec.CommonEncoder;
import dev.jianmu.engine.rpc.provider.ServiceProvider;
import dev.jianmu.engine.rpc.serializer.CommonSerializer;
import dev.jianmu.engine.rpc.translate.NettyServerHandler;
import dev.jianmu.engine.rpc.translate.client.ChannelProvider;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;

import java.util.concurrent.TimeUnit;

/**
 * RPC 配置类
 * */
@Slf4j
@Configuration
public class RpcConfiguration implements ApplicationRunner, ApplicationListener<ContextClosedEvent> {

    private final Boolean loggingInfo;
    private final CommonSerializer serializer;
    private final ServiceProvider serviceProvider;

    private Channel serverChannel;

    public RpcConfiguration(EngineProperties properties) {
        this.loggingInfo = properties.getDeBug();
        this.serializer = properties.getSerializer();
        this.serviceProvider = properties.getServiceProvider();
    }

    /**
     * {@link ApplicationRunner}
     * */
    @Override
    public void run(ApplicationArguments args) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap server = new ServerBootstrap();
            if (loggingInfo)
                server.handler(new LoggingHandler(LogLevel.INFO));

            ChannelFuture future = server
                    .group(bossGroup, workerGroup)
                    // 服务端接受连接的最大队列长度
                    .option(ChannelOption.SO_BACKLOG, 128)
                    // TCP的心跳机制
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    // 禁用Nagle算法
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline()
                                    // 应用层心跳机制
                                    .addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS))
                                    .addLast(new CommonEncoder(serializer))
                                    .addLast(new CommonDecoder())
                                    .addLast(new NettyServerHandler(serviceProvider));
                        }
                    })
                    .bind(25).sync();

            this.serverChannel = future.channel();
            future.addListener((ChannelFutureListener) listener -> log.info("RPC服务启动"));
            serverChannel.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    /**
     * 监听服务关闭事件释放 Netty 资源
     * {@link ApplicationListener<ContextClosedEvent>}
     * */
    @Override
    public void onApplicationEvent(@NotNull ContextClosedEvent event) {
        if (this.serverChannel != null)
            serverChannel.close();
        ChannelProvider.releaseAll();
        log.info("RPC服务停止");
    }

}
