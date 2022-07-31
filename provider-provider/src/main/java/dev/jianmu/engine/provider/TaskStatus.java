package dev.jianmu.engine.provider;

/**
 * 任务状态枚举
 * */
public enum TaskStatus {

    /**
     * 等待执行中(阻塞)
     * */
    WAITING,

    /**
     * 执行
     * */
    RUNNING,

    /**
     * 暂停中
     * */
    PAUSE,

    /**
     * 执行成功
     * */
    EXECUTION_SUCCEEDED,

    /**
     * 执行失败
     * */
    EXECUTION_FAILED,

    /**
     * 分配失败
     * @deprecated 本业务预期任务分配异常的情况仅 transactionId 重复一种，此为 register 模块
     *  逻辑，不应也不存在与 consumer 模块中，故最终回调并记录的任务中不存在此种状态。
     * */
    @Deprecated
    DISPATCH_FAILED

}
