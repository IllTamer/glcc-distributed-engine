package dev.jianmu.engine.provider;

import lombok.Data;

/**
 * provider 信息实体类
 * */
@Data
public class ProviderInfo {

    /**
     * 线程池使用率 [0, 100]
     * */
    private int threadPoolUsage;

    private String workerId;

}
