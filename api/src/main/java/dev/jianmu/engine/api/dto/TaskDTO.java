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

}
