package dev.jianmu.engine.rpc.translate;

import dev.jianmu.engine.rpc.provider.ServiceProvider;
import dev.jianmu.engine.rpc.translate.server.RequestHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * Netty 中处理从客户端传来的 RpcRequest
 * */
@Slf4j
public class NettyServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private final RequestHandler requestHandler;;

    public NettyServerHandler(ServiceProvider serviceProvider) {
        this.requestHandler = new RequestHandler(serviceProvider);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) {
        try {
            if (msg.isHeartBeat()) {
                log.debug("接收到客户端心跳包");
                return;
            }
            log.info("服务端接收到请求：{}", msg);
            Object response = requestHandler.handle(msg);
            if (ctx.channel().isActive() && ctx.channel().isWritable()) {
                // 此处通道未 workerGroup 中的，NettyServer 中创建的是 bossGroup 的
                ctx.writeAndFlush(response);
            } else {
                log.error("通道不可读写");
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                log.info("长时间未收到心跳包，断开连接");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("处理过程调用时有错误发生：");
        cause.printStackTrace();
        ctx.close();
    }

}
