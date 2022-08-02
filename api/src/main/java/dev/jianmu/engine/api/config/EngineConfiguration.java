package dev.jianmu.engine.api.config;

import dev.jianmu.engine.api.config.application.RegisterApplication;
import dev.jianmu.engine.api.service.ConsumerService;
import dev.jianmu.engine.api.service.impl.ConsumerServiceImpl;
import dev.jianmu.engine.register.NodeInstancePool;
import dev.jianmu.engine.register.OnlineNodeServiceDiscovery;
import dev.jianmu.engine.register.WeightedMinLoadLoadBalancer;
import dev.jianmu.engine.rpc.codec.CommonDecoder;
import dev.jianmu.engine.rpc.codec.CommonEncoder;
import dev.jianmu.engine.rpc.factory.SingletonFactory;
import dev.jianmu.engine.rpc.serializer.CommonSerializer;
import dev.jianmu.engine.rpc.service.loadbalancer.LoadBalancer;
import dev.jianmu.engine.rpc.translate.NettyServerHandler;
import dev.jianmu.engine.rpc.translate.RpcClientProxy;
import dev.jianmu.engine.rpc.translate.client.ChannelProvider;
import dev.jianmu.engine.rpc.translate.client.NettyClient;
import dev.jianmu.engine.rpc.translate.server.AbstractServerBootstrap;
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
import org.springframework.beans.BeansException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.scheduling.annotation.Async;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * RPC 配置类
 * */
@Slf4j
@Configuration
public class EngineConfiguration extends AbstractServerBootstrap implements ApplicationRunner, ApplicationListener<ContextClosedEvent> {

    private final EngineProperties properties;
    private final ApplicationContext context;

    private Channel serverChannel;

    public EngineConfiguration(EngineProperties properties, ApplicationContext context) {
        super(
                properties.getService().getHost(),
                properties.getService().getRegisterPort(),
                properties.getService().getServiceRegistry(),
                properties.getServiceProvider(),
                "dev.jianmu.engine.api.service",
                properties.getService().getMap(),
                aClass -> {
                    try {
                        return context.getBean(aClass);
                    } catch (BeansException e) {
                        return null;
                    }
                }
        );
        this.properties = properties;
        this.context = context;
    }

    /**
     * {@link ApplicationRunner}
     * */
    @Async
    @Override
    public void run(ApplicationArguments args) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap server = new ServerBootstrap();
            if (properties.getDebug())
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
                                    .addLast(new CommonEncoder(properties.getSerializer())) // out
                                    .addLast(new CommonDecoder()) // in
                                    .addLast(new NettyServerHandler(serviceProvider)); // in
                        }
                    })
                    .bind(port).sync();
            this.serverChannel = future.channel();
            future.addListener((ChannelFutureListener) listener -> log.info("RPC-Server启动，在(port): {}", port));
            // After server init
            initEngineComponents(properties, context);
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

    @Bean
    public static RpcClientProxy getRpcClientProxy(EngineProperties properties, RegisterApplication registerApplication) {
        final Map<String, Class<?>> serviceMap = properties.getService().getMap();
        final LoadBalancer loadBalancer = properties.getService().getLoadBalancer();
        final CommonSerializer serializer = properties.getSerializer();
        NettyClient client = new NettyClient(new OnlineNodeServiceDiscovery(registerApplication.getNodeInstancePool(), loadBalancer), serializer);
        return new RpcClientProxy(client, serviceMap);
    }

    /**
     * 初始化组件
     * */
    private void initEngineComponents(EngineProperties properties, ApplicationContext context) {
        final RegisterApplication registerApplication = context.getBean(RegisterApplication.class);
        final RpcClientProxy rpcClientProxy = context.getBean(RpcClientProxy.class);
        final NodeInstancePool nodeInstancePool = registerApplication.getNodeInstancePool();
        nodeInstancePool.setRpcClientProxy(rpcClientProxy);

        EngineProperties.Service serviceProperties = properties.getService();
        final LoadBalancer loadBalancer = serviceProperties.getLoadBalancer();
        if (loadBalancer instanceof WeightedMinLoadLoadBalancer)
            ((WeightedMinLoadLoadBalancer) loadBalancer).setNodeInstancePool(nodeInstancePool);

//        SingletonFactory.setInstance(ConsumerService.class, new ConsumerServiceImpl());
        registerApplication.refreshNodes();
    }

}