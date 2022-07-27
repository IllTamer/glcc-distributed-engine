package dev.jianmu.engine.api.service;

import dev.jianmu.engine.provider.Task;

public interface ConsumerService {

    /**
     * 分派分布式任务
     * <p>
     * 异步执行任务，不关心结果，通过 MonitorService 查询任务状态
     * @return workerId
     * */
    String dispatchTask(Task task);

}
