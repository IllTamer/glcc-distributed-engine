package dev.jianmu.engine.api.config;

import dev.jianmu.engine.api.ApiApplication;
import dev.jianmu.engine.register.util.NodeUtil;
import dev.jianmu.engine.rpc.codec.CommonDecoder;
import dev.jianmu.engine.rpc.codec.CommonEncoder;
import dev.jianmu.engine.rpc.serializer.CommonSerializer;
import dev.jianmu.engine.rpc.service.ConfigureServiceDiscovery;
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

    private final Boolean loggingInfo;
    private final CommonSerializer serializer;
    private final Map<String, Integer> discoveries;

    private Channel serverChannel;

    public EngineConfiguration(EngineProperties properties, ApplicationContext context) {
        super(
                properties.getService().getHost(),
                properties.getService().getRegisterPort(),
                properties.getService().getServiceRegistry(),
                properties.getServiceProvider(),
                ApiApplication.class,
                properties.getService().getMap(),
                aClass -> {
                    try {
                        return context.getBean(aClass);
                    } catch (BeansException e) {
                        return null;
                    }
                }
        );
        this.loggingInfo = properties.getDebug();
        this.serializer = properties.getSerializer();
        this.discoveries = properties.getService().getDiscoveries();
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
                    .bind(port).sync();

            this.serverChannel = future.channel();
            future.addListener((ChannelFutureListener) listener -> log.info("RPC服务启动"));
            initEngineComponents();
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
    public RpcClientProxy getRpcClientProxy(EngineProperties properties) {
        final Map<String, Class<?>> serviceMap = properties.getService().getMap();
        final LoadBalancer loadBalancer = properties.getService().getLoadBalancer();
        // TODO 前置节点离线检测
        NettyClient client = new NettyClient(new ConfigureServiceDiscovery(loadBalancer, discoveries), serializer);
        return new RpcClientProxy(client, serviceMap);
    }

    /**
     * 初始化组件
     * TODO
     * */
    private void initEngineComponents() {
        // register
        // 默认 “发布任务即leader” 思想
        // - 配置 jianmu.service.discoveries（可选，empty 即分布式任务->普通任务）
        // - 配置 jianmu.service.register-port（可选，默认数值即不开启分布式支持）
        // 测试配置的所有服务发现地址（Server）是否可用
        Map<String, Integer> recall = NodeUtil.pingAllNodes(discoveries, serializer);

        // 创建Node，维护Node
        // - 考虑 ServiceDiscovery 结合 Node

        // 集群任务发布：分布式锁
        // 调用方 -> 创建临时节点（服务发现），生成持久化节点（选举？，分布式锁）
        // 提供方 ->

        // consumer
        // LoadBalance 加权最小请求数算法
        // “批次处理” + priority

        // provider

        // monitor
    }

}
