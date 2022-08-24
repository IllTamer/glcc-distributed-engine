package dev.jianmu.engine.consumer;

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
    public LocalState info() {
        OperatingSystemMXBean bean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        long totalPhysicalMemorySize = bean.getTotalPhysicalMemorySize();
        long freePhysicalMemorySize = bean.getFreePhysicalMemorySize();
        double totalMemory = 1D * totalPhysicalMemorySize / _1GB;
        double freeMemory = 1D * freePhysicalMemorySize / _1GB;
        double memoryUseRatio = 1D * (totalPhysicalMemorySize - freePhysicalMemorySize) / totalPhysicalMemorySize * 100;

        return LocalState.builder()
                .processCpuLoad(twoDecimal(bean.getProcessCpuLoad() * 100))
                .systemCpuLoad(twoDecimal(bean.getSystemCpuLoad() * 100))
                .memoryUseRatio(twoDecimal(memoryUseRatio))
                .totalMemory(twoDecimal(totalMemory))
                .freeMemory(twoDecimal(freeMemory))
                .build();
    }

    public static double twoDecimal(double doubleValue) {
        BigDecimal bigDecimal = new BigDecimal(doubleValue).setScale(2, RoundingMode.HALF_UP);
        return bigDecimal.doubleValue();
    }

}
