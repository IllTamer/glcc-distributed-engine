package dev.jianmu.engine.rpc.translate.client;

import dev.jianmu.engine.rpc.translate.RpcResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 未处理的请求（对所有客户端请求进行统一管理）
 * */
public class UnprocessedRequests {

    private static final ConcurrentHashMap<String, CompletableFuture<RpcResponse<?>>> UNPROCESSED_REQUESTS = new ConcurrentHashMap<>();

    public void put(String requestId, CompletableFuture<RpcResponse<?>> future) {
        UNPROCESSED_REQUESTS.put(requestId, future);
    }

    public void remove(String requestId) {
        UNPROCESSED_REQUESTS.remove(requestId);
    }

    public void complete(RpcResponse<?> rpcResponse) {
        // 完成并移除请求
        CompletableFuture<RpcResponse<?>> future = UNPROCESSED_REQUESTS.remove(rpcResponse.getRequestId());
        if (future != null) {
            future.complete(rpcResponse);
        } else {
            throw new IllegalStateException();
        }
    }

}
