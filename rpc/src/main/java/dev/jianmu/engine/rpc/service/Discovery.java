package dev.jianmu.engine.rpc.service;

import lombok.Data;
import lombok.EqualsAndHashCode;

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

    /**
     * 权重
     * */
    @EqualsAndHashCode.Exclude
    private Integer weight;

}
