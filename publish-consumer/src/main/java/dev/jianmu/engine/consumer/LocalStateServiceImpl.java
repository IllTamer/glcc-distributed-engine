package dev.jianmu.engine.consumer;

import com.google.gson.JsonObject;
import com.sun.management.OperatingSystemMXBean;
import dev.jianmu.engine.rpc.annotation.RpcService;

import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 本机状态获取 Service
 * */
@RpcService
public class LocalStateServiceImpl implements LocalStateService {

    private static final long _1GB = 1024 * 1024 * 1024;

    /**
     * @return CPU使用率，内存使用率
     * */
    public JsonObject info() {
        OperatingSystemMXBean bean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        JsonObject object = new JsonObject();
        object.addProperty(PROCESS_CPU_LOAD, twoDecimal(bean.getProcessCpuLoad() * 100));
        object.addProperty(SYSTEM_CPU_LOAD, twoDecimal(bean.getSystemCpuLoad() * 100));

        long totalPhysicalMemorySize = bean.getTotalPhysicalMemorySize();
        long freePhysicalMemorySize = bean.getFreePhysicalMemorySize();
        double totalMemory = 1D * totalPhysicalMemorySize / _1GB;
        double freeMemory = 1D * freePhysicalMemorySize / _1GB;
        double memoryUseRatio = 1D * (totalPhysicalMemorySize - freePhysicalMemorySize) / totalPhysicalMemorySize * 100;

        object.addProperty(MEMORY_USE_RATIO, twoDecimal(memoryUseRatio));
        object.addProperty(TOTAL_MEMORY, twoDecimal(totalMemory));
        object.addProperty(FREE_MEMORY, twoDecimal(freeMemory));
        return object;
    }

    public static double twoDecimal(double doubleValue) {
        BigDecimal bigDecimal = new BigDecimal(doubleValue).setScale(2, RoundingMode.HALF_UP);
        return bigDecimal.doubleValue();
    }

}
