package dev.jianmu.engine.provider;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 任务实体类
 * */
@Builder
@Getter
@Setter
@ToString
@TableName("jianmu_engine_task")
public class Task implements Comparable<Task>, Serializable {

    private static final long serialVersionUID = 765743073L;

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
     * - local: 本地执行
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
     * worker 的 Id
     * <p>
     * jianmu 进程唯一
     * */
    @TableField("worker_id")
    private String workerId;

    /**
     * 执行的指令列表
     * */
    private String script;

    /**
     * 任务运行状态
     * */
    @Setter
    private TaskStatus status;

    /**
     * 开始时间
     * */
    @TableField("start_time")
    private LocalDateTime startTime;

    /**
     * 结束时间
     * */
    @Setter
    @TableField("end_time")
    private LocalDateTime endTime;

    @Override
    public int compareTo(@NotNull Task o) {
        return o.priority.compareTo(this.priority);
    }

}
