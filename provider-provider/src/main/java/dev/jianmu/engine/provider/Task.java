package dev.jianmu.engine.provider;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务实体类
 * */
@Builder
@Getter
public class Task {

    private final String uuid;

    // 优先级
    private final Integer priority;

    // workerId
    private final String workerId;

    // 命令列表
    private final List<String> script;

    // 执行顺序号
    private final Long serialNo;

    // 任务运行状态
    @Setter
    private TaskStatus status;

    // 结束时间
    @Setter
    private LocalDateTime endTime;

    // 开始时间
    private final LocalDateTime startTime = LocalDateTime.now();

}
