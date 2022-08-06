package dev.jianmu.engine.provider;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 计划任务
 * <p>
 * 包含 定时任务 与 过载任务
 * @apiNote 任务运行状态统一为 WAITING
 * */
@Builder
@Getter
@Setter
@ToString
@TableName("jianmu_engine_future")
public class FutureTask implements Serializable {

    private static final long serialVersionUID = 863522624L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String uuid;

    /**
     * 分布式全局任务 Id
     * <p>
     * 用于确保接口幂等，防止重复发布。
     * */
    @TableField("transaction_id")
    private Long transactionId;

    /**
     * 任务类型
     * <p>
     * - dispatch: 分布式调度(如果本机权重较高，也会分配给本机)
     * - iterate: 所有节点均执行
     * */
    private String type;

    /**
     * 优先级
     * <p>
     * 数值越小优先级越高
     * */
    private Integer priority;

    /**
     * 计划任务表达式
     * @see CronParser
     * */
    private String cron;

    /**
     * 执行的指令列表
     * */
    private String script;

    /**
     * 开始时间
     * */
    @TableField("start_time")
    private LocalDateTime startTime;

    public static FutureTask parse(Task task) {
        return builder()
                .uuid(task.getUuid())
                .transactionId(task.getTransactionId())
                .type(task.getType())
                .priority(task.getPriority())
                .cron(task.getCron())
                .script(task.getScript())
                .startTime(task.getStartTime())
                .build();
    }
}
