package dev.jianmu.engine.consumer;

import lombok.Builder;
import lombok.Data;

/**
 * 本地节点状态实体类
 * */
@Data
@Builder
public class LocalState {

    /**
     * 该进程 CPU 使用率 (%)
     * */
    private Double processCpuLoad;

    /**
     * 系统 CPU 使用率 (%)
     * */
    private Double systemCpuLoad;

    /**
     * 内存使用率 (%)
     * */
    private Double memoryUseRatio;

    /**
     * 系统剩余内存 (GB)
     * */
    private Double freeMemory;

    /**
     * 系统总内存 (GB)
     * */
    private Double totalMemory;

}
