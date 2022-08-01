package dev.jianmu.engine.monitor.event.filter;

import dev.jianmu.engine.monitor.event.ExecutionNode;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 可用性过滤器
 * */
public interface AvailabilityFilter {

    /**
     * 执行判定操作
     * */
    List<ExecutionNode> filter(@NotNull List<ExecutionNode> tempExecutionNodes);

}
