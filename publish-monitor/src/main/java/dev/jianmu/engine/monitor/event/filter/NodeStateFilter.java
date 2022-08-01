package dev.jianmu.engine.monitor.event.filter;

import dev.jianmu.engine.monitor.event.ExecutionNode;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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
        return null;
    }

}
