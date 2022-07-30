package dev.jianmu.engine.api.vo;

import dev.jianmu.engine.provider.TaskStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 任务调度信息 VO
 * */
@Builder
@Getter
public class TaskProcessVO {

    private final String uuid;

    /**
     * 分布式全局任务 Id
     * */
    private final Long transactionId;

    /**
     * 任务类型
     * <p>
     * - local: 本地执行
     * - dispatch: 分布式调度(如果本机权重较高，也会分配给本机)
     * - iterate: 所有节点均执行
     * */
    private final String type;

    /**
     * worker 的 Id
     * <p>
     * jianmu 进程唯一
     * */
    private final String workerId;

    /**
     * 任务运行状态
     * */
    private final TaskStatus status;

    /**
     * 开始时间
     * */
    private final LocalDateTime startTime;

    /**
     * 结束时间
     * */
    private final LocalDateTime endTime;

}
