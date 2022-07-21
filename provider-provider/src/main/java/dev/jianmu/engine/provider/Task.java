package dev.jianmu.engine.provider;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务实体类
 * */
@Builder
@Getter
public class Task implements Comparable<Integer> {

    private final String uuid;

    /**
     * 分布式全局任务 Id
     * <p>
     * 用于确保接口幂等，防止重复发布。
     * */
    private final Long transactionId;

    /**
     * 优先级
     * <p>
     * 数值越小优先级越高
     * */
    private final Integer priority;

    // workerId
    // consumer 执行时赋值
    private final String workerId;

    // 命令列表
    private final List<String> script;

    // 任务运行状态
    @Setter
    private TaskStatus status;

    // 结束时间
    // consumer 执行完赋值
    @Setter
    private LocalDateTime endTime;

    // 开始时间
    private final LocalDateTime startTime = LocalDateTime.now();

    @Override
    public int compareTo(@NotNull Integer o) {
        return o.compareTo(priority);
    }

}
