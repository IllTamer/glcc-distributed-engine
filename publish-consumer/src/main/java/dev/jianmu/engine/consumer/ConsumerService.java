package dev.jianmu.engine.consumer;

import dev.jianmu.engine.provider.Task;

public interface ConsumerService {

    /**
     * 分派分布式任务
     * <p>
     * 异步执行任务，不关心结果，通过 MonitorService 查询任务状态
     * @return workerId
     * */
    String dispatchTask(Task task);

    /**
     * 获取节点 consumer 任务线程池使用率
     * */
    int getTaskThreadPoolUsage();

}
