package dev.jianmu.engine.monitor.event.filter;

import dev.jianmu.engine.monitor.event.ExecutionNode;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 节点状态标准过滤器
 * */
public class NodeStateFilter extends AbstractAvailabilityFilter {

    public NodeStateFilter() {
        super(null);
    }

    @Override
    @NotNull
    List<ExecutionNode> doFilter(@NotNull List<ExecutionNode> tempExecutionNodes) {
        return tempExecutionNodes.stream()
                .filter(node -> {
                    final Map<String, Object> info = node.getNodeInfo();
                    final double systemCpuLoad = (double) info.get("systemCpuLoad");
                    final double memoryUseRatio = (double) info.get("memoryUseRatio");
                    return systemCpuLoad <= 85D && memoryUseRatio <= 90D;
                }).collect(Collectors.toList());
    }

}
