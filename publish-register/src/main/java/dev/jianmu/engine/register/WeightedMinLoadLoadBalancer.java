package dev.jianmu.engine.register;

import dev.jianmu.engine.consumer.LocalStateServiceImpl;
import dev.jianmu.engine.rpc.service.loadbalancer.LoadBalancer;
import dev.jianmu.engine.rpc.util.Assert;
import lombok.Getter;
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

    @Getter
    private Integer latestNodeLoad;

    public WeightedMinLoadLoadBalancer(NodeInstancePool nodeInstancePool) {
        this.nodeInstancePool = nodeInstancePool;
    }

    @Override
    public InetSocketAddress select(List<InetSocketAddress> addresses) {
        Assert.notEmpty(addresses, "No available node!");
        LinkedHashMap<ExecutionNode, Integer> sort = new LinkedHashMap<>();
        for (ExecutionNode node : nodeInstancePool.getTempExecutionNodes()) {
            sort.put(node, getWeight(node));
        }
        Optional<Map.Entry<ExecutionNode, Integer>> first = sort.entrySet().stream()
                .max(Map.Entry.comparingByValue());
        final Map.Entry<ExecutionNode, Integer> entry = first.get();
        if ((latestNodeLoad = entry.getValue()) <= MIN_MEMORY_LOAD * MIN_CPU_LOAD)
            log.warn("Node load is too high in {}", entry.getKey());
        return entry.getKey().getAddress();
    }

    public static Integer getWeight(ExecutionNode node) {
        Map<String, Object> nodeInfo = node.getNodeInfo();
        Double memoryLoad = (Double) nodeInfo.get(LocalStateServiceImpl.MEMORY_USE_RATIO);
        Double cpuLoad = (Double) nodeInfo.get(LocalStateServiceImpl.SYSTEM_CPU_LOAD);
        Assert.notNull(memoryLoad, "Uninitialized node");
        Assert.notNull(cpuLoad, "Uninitialized node");
        final double pm = 100 - memoryLoad;
        final double pc = 100 - cpuLoad;
        return (pm <= MIN_MEMORY_LOAD || pc <= MIN_CPU_LOAD) ? 1 : (int) (pm * pc);
    }

}
