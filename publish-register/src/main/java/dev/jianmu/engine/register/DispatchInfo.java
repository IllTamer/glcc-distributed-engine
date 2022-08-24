package dev.jianmu.engine.register;

import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

/**
 * 调度信息实体类
 * */
@Data
@Builder
public class DispatchInfo {

    public static final String DISPATCHED = "dispatched";
    public static final String OVERLOAD = "overload";
    public static final String FUTURE = "future";

    /**
     * 已分配节点的域名
     * @apiNote 任务未能及时发布时，返回为 null
     * */
    @Nullable
    private String host;

    /**
     * 已分配节点的端口
     * @apiNote 任务未能及时发布时，返回为 null
     * */
    @Nullable
    private Integer port;

    /**
     * 已分配 Worker 的 Id
     * @apiNote 任务未能及时发布时，返回为 null
     * */
    @Nullable
    private String workerId;

    /**
     * 发布任务状态
     * @apiNote 任务未能及时发布时，返回为 null
     * */
    private String status;

}
