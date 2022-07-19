package dev.jianmu.engine.consumer;

import com.google.gson.JsonObject;

public interface LocalStateService {

    /**
     * 该进程 CPU使用率 (%)
     * */
    String PROCESS_CPU_LOAD = "processCpuLoad";

    /**
     * 系统CPU使用率 (%)
     * */
    String SYSTEM_CPU_LOAD = "systemCpuLoad";

    /**
     * 内存使用率 (%)
     * */
    String MEMORY_USE_RATIO = "memoryUseRatio";

    /**
     * 系统总内存 (GB)
     * */
    String TOTAL_MEMORY = "totalMemory";

    /**
     * 系统剩余内存 (GB)
     * */
    String FREE_MEMORY = "freeMemory";

    /**
     * @return CPU使用率，内存使用率
     * */
    JsonObject info();

}
