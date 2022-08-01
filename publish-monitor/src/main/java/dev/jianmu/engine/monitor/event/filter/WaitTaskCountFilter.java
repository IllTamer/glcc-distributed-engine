package dev.jianmu.engine.monitor.event.filter;

import dev.jianmu.engine.monitor.event.ExecutionNode;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 等待任务数标准过滤器
 * */
public class WaitTaskCountFilter extends AbstractAvailabilityFilter {

    public WaitTaskCountFilter() {
        super(new NodeStateFilter());
    }

    @Override
    @NotNull
    List<ExecutionNode> doFilter(@NotNull List<ExecutionNode> tempExecutionNodes) {
        return null;
    }

}
