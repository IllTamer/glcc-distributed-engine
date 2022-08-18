package dev.jianmu.engine.api.dto;

import dev.jianmu.engine.monitor.event.ExecutionNode;
import dev.jianmu.engine.provider.Task;
import dev.jianmu.engine.provider.TaskStatus;
import dev.jianmu.engine.provider.Worker;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 任务 DTO
 * */
@Setter
@Getter
public class TaskDTO {

    /**
     * 分布式全局任务 Id {@link ExecutionNode#getTransactionId()}
     * <p>
     * 用于确保接口幂等，防止重复发布。
     * */
    private Long transactionId;

    /**
     * 任务类型
     * <p>
     * - local: 本地执行
     * - dispatch: 分布式调度
     * - iterate: 所有节点均执行
     * */
    private String type;

    /**
     * 优先级
     * <p>
     * 数值越小优先级越高
     * */
    private Integer priority = 100;

    // 计划任务表达式
    private String cron;

    // 命令列表
    private List<String> script;

    public Task format() {
        return Task.builder()
                .uuid(UUID.randomUUID().toString())
                .transactionId(transactionId)
                .cron(cron)
                .type(type)
                .priority(priority)
                // default to shell_worker
                .script(Worker.WORKER_MAP.get(Worker.Type.SHELL).parseScript(script))
                .status(TaskStatus.WAITING)
                .startTime(LocalDateTime.now())
                .build();
    }

}
