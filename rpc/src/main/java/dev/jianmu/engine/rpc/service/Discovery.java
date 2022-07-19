package dev.jianmu.engine.rpc.service;

import lombok.Data;

@Data
public class Discovery {

    /**
     * 域名
     * */
    private String host;

    /**
     * 端口
     * */
    private Integer port;

}
