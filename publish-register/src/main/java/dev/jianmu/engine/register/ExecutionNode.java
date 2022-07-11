package dev.jianmu.engine.register;

import lombok.Data;

import java.util.Date;

/**
 * 任务执行节点
 * @apiNote 本节点接口不提供权限管理功能
 * */
@Data
public class ExecutionNode {

    /**
     * 事务Id
     * <p>
     * 分布式全局唯一，确定操作的先后顺序
     * */
    private Long transactionId;

    /**
     * 数据版本号
     * <p>
     * 每次数据变化而自增
     * */
    private Long dataVersion;

    /**
     * 创建时间
     * */
    private Date createTime;

    /**
     * 最后修改时间
     * */
    private Date modifyTime;

    /**
     * 节点数据
     * @apiNote 未定
     * */
    private Object undecided;

    enum Type {

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

}
