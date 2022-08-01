package dev.jianmu.engine.register.util;

import dev.jianmu.engine.monitor.event.ExecutionNode;
import dev.jianmu.engine.rpc.translate.RpcRequest;
import dev.jianmu.engine.rpc.translate.RpcResponse;
import dev.jianmu.engine.rpc.translate.client.NettyClient;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * 节点工具类
 * */
@Slf4j
@UtilityClass
public class NodeUtil {

    /**
     * ping 节点
     * @return 响应成功返回当前节点 {@link ExecutionNode}，否则返回 null
     * @deprecated @see RpcClientProxy
     * */
    @Nullable
    @Deprecated
    public static ExecutionNode pingNode(NettyClient client, ExecutionNode node) {
        RpcRequest pingRequest = new RpcRequest(
                UUID.randomUUID().toString(),
                "dev.jianmu.engine.rpc.Ping",
                "ping",
                new Object[0],
                new Class<?>[0],
                false
        );
        InetSocketAddress lastAddress = client.getLastAddress();
        CompletableFuture<RpcResponse<?>> future = client.sendRequest(pingRequest);
        try {
            RpcResponse<?> response = future.get();
            if ("pong".equals(response.getData()))
                return node;
        } catch (InterruptedException | ExecutionException e) {
            log.debug("Ping request exception", e);
            log.warn("Failed ping [{}:{}]", lastAddress.getHostString(), lastAddress.getPort());
        }
        return null;
    }

}
