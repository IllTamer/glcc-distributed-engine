package dev.jianmu.engine.rpc;

import dev.jianmu.engine.rpc.annotation.RpcService;

@RpcService("dev.jianmu.engine.rpc.Ping")
public class Ping {

    public String ping() {
        return "pong";
    }

}
