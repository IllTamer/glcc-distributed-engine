package dev.jianmu.engine.register.util;

import dev.jianmu.engine.rpc.serializer.CommonSerializer;
import dev.jianmu.engine.rpc.service.ConfigureServiceDiscovery;
import dev.jianmu.engine.rpc.service.loadbalancer.RoundRobinLoadBalancer;
import dev.jianmu.engine.rpc.translate.RpcRequest;
import dev.jianmu.engine.rpc.translate.RpcResponse;
import dev.jianmu.engine.rpc.translate.client.NettyClient;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * 节点工具类
 * */
@Slf4j
@UtilityClass
public class NodeUtil {

    public static Map<String, Integer> pingAllNodes(Map<String, Integer> discoveries, CommonSerializer serializer) {
        int round = discoveries.size();
        NettyClient client = new NettyClient(new ConfigureServiceDiscovery(new RoundRobinLoadBalancer(), discoveries), serializer);

        Map<String, Integer> recallAddress = new LinkedHashMap<>();
        for (int i = 0; i < round; ++ i) {
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
                if ("pong".equals(response.getData())) {
                    recallAddress.put(lastAddress.getHostString(), lastAddress.getPort());
                }
            } catch (InterruptedException | ExecutionException e) {
                log.debug("Ping request exception", e);
                log.warn("Failed ping [{}:{}]", lastAddress.getHostString(), lastAddress.getPort());
            }
        }
        return recallAddress;
    }

}
