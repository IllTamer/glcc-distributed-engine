package dev.jianmu.engine.monitor.event.filter;

import dev.jianmu.engine.monitor.event.ExecutionNode;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 任务线程池使用率过滤器
 * */
public class TaskThreadPoolUsageFilter extends AbstractAvailabilityFilter {

    public static final int MAX_USAGE = 75;

    public TaskThreadPoolUsageFilter() {
        super(new NodeStateFilter());
    }

    @Override
    @NotNull
    List<ExecutionNode> doFilter(@NotNull List<ExecutionNode> tempExecutionNodes) {
        return tempExecutionNodes.stream()
                .filter(node -> ((int) node.getNodeInfo().get("taskThreadPoolUsage")) <= MAX_USAGE)
                .collect(Collectors.toList());
    }

}
