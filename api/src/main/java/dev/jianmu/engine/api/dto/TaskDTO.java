package dev.jianmu.engine.api.dto;

import dev.jianmu.engine.register.ExecutionNode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

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

    // 优先级
    private Integer priority;

    // 计划任务表达式
    private String cron;

    // 命令列表
    private List<String> script;

}
