package dev.jianmu.engine.monitor.event.filter;

import dev.jianmu.engine.monitor.event.ExecutionNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class AbstractAvailabilityFilter implements AvailabilityFilter {

    @Nullable
    private final AvailabilityFilter preFilter;

    public AbstractAvailabilityFilter(@Nullable AvailabilityFilter preFilter) {
        this.preFilter = preFilter;
    }

    @NotNull
    abstract List<ExecutionNode> doFilter(@NotNull List<ExecutionNode> tempExecutionNodes);

    @Override
    public List<ExecutionNode> filter(@NotNull List<ExecutionNode> tempExecutionNodes) {
        List<ExecutionNode> nodes = preFilter != null ? preFilter.filter(tempExecutionNodes) : tempExecutionNodes;
        return doFilter(nodes);
    }

}
