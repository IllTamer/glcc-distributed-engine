package dev.jianmu.engine.register;

import dev.jianmu.engine.consumer.LocalStateServiceImpl;
import dev.jianmu.engine.rpc.service.loadbalancer.LoadBalancer;
import dev.jianmu.engine.rpc.util.Assert;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 加权最小占用负载均衡算法
 * <p>
 * 权重由 work节点的CPU使用率，内存使用率 计算得出
 * @see LocalStateServiceImpl
 * */
@Slf4j
public class WeightedMinLoadLoadBalancer implements LoadBalancer {

    public static Double MIN_CPU_LOAD = 8D;

    public static Double MIN_MEMORY_LOAD = 10D;

    private final NodeInstancePool nodeInstancePool;

    public WeightedMinLoadLoadBalancer(NodeInstancePool nodeInstancePool) {
        this.nodeInstancePool = nodeInstancePool;
    }

    @Override
    public InetSocketAddress select(List<InetSocketAddress> addresses) {
        Assert.notEmpty(addresses, "No available node!");
        LinkedHashMap<ExecutionNode, Integer> sort = new LinkedHashMap<>();
        for (ExecutionNode node : nodeInstancePool.getTempExecutionNodes()) {
            Map<String, Object> nodeInfo = node.getNodeInfo();
            Double memoryLoad = (Double) nodeInfo.get(LocalStateServiceImpl.MEMORY_USE_RATIO);
            Double cpuLoad = (Double) nodeInfo.get(LocalStateServiceImpl.SYSTEM_CPU_LOAD);

            Integer weight = getWeight(100 - memoryLoad, 100 - cpuLoad);
            sort.put(node, weight);
        }
        Optional<Map.Entry<ExecutionNode, Integer>> first = sort.entrySet().stream()
                .max(Map.Entry.comparingByValue());
        final Map.Entry<ExecutionNode, Integer> entry = first.get();
        if (entry.getValue() <= MIN_MEMORY_LOAD * MIN_CPU_LOAD)
            log.warn("Node load is too high in {}", entry.getKey());
        return entry.getKey().getAddress();
    }

    private static Integer getWeight(Double pm, Double pc) {
        if (pm <= MIN_MEMORY_LOAD || pc <= MIN_CPU_LOAD) return 1;
        return (int) (pm * pc);
    }

}
