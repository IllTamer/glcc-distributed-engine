package dev.jianmu.engine.monitor.event;

import lombok.Builder;
import lombok.Data;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.Map;

/**
 * 任务执行节点
 * @apiNote 本节点接口不提供权限管理功能
 * */
@Builder
@Data
public class ExecutionNode {

    /**
     * 链接地址
     * */
    private final InetSocketAddress address;

    /**
     * 事务Id
     * <p>
     * 分布式全局唯一，确定操作的先后顺序
     * @apiNote 仅用于临时节点
     * */
    private Long transactionId;

    /**
     * 节点数据版本号
     * <p>
     * 每次数据变化而自增
     * */
    private Long dataVersion;

    /**
     * 节点创建时间
     * */
    private Date createTime;

    /**
     * 最后修改节点数据时间
     * */
    private Date modifyTime;

    /**
     * 节点数据
     * <p>
     * see dev.jianmu.engine.consumer.LocalStateService
     * */
    private Map<String, Object> nodeInfo;

    /**
     * 节点类型
     * */
    private Type type;

    /**
     * 节点状态
     * */
    private Status status;

    public enum Type {

        /**
         * 临时节点
         * <p>
         * 会话断开后删除，适用于服务发现场景
         * */
        EPHEMERAL,

        /**
         * 持久化节点
         * <p>
         * 持久化保持属性。适用于选举，分布式锁
         * */
        PERSISTENT;

    }

    public enum Status {

        /**
         * 可用
         * */
        AVAILABLE,

        /**
         * 任务执行能力过低
         * */
        OVERLOAD,

        /**
         * 失联
         * */
        DISCONNECTED

    }

}
