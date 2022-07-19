package dev.jianmu.engine.provider;

/**
 * 任务状态枚举
 * */
public enum TaskStatus {

    /**
     * 阻塞
     * */
    WAITING,

    /**
     * 执行
     * */
    RUNNING,

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
     * */
    DISPATCH_FAILED

}
