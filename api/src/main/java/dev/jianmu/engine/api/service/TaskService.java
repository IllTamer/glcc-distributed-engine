package dev.jianmu.engine.api.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.jianmu.engine.api.mapper.TaskMapper;
import dev.jianmu.engine.consumer.TaskRunner;
import dev.jianmu.engine.provider.Task;
import dev.jianmu.engine.rpc.util.Assert;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService extends ServiceImpl<TaskMapper, Task> {

    @Transactional
    public boolean refreshTask(Task task) {
        LambdaUpdateWrapper<Task> wrapper = new LambdaUpdateWrapper<>();
        return getBaseMapper().update(task, wrapper
                .eq(Task::getTransactionId, task.getTransactionId())
                .set(Task::getType, task.getType())
                .set(Task::getWorkerId, task.getWorkerId())
                .set(Task::getStatus, task.getStatus())
                .set(Task::getEndTime, task.getEndTime())
        ) > 0;
    }

    /**
     * 尝试记录 Task
     * <p>
     * 校验有无重复 transactionId,
     * */
    @Transactional
    public boolean tryRecordInsert(Task task) {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        final Task exist = getBaseMapper().selectOne(wrapper
                .eq(Task::getTransactionId, task.getTransactionId())
        );
        Assert.isNull(exist, "Transaction-Id in Task(%s) has been used, please request another", exist);
        return this.save(task);
    }

    @Transactional
    public long getNextTransactionId() {
        final TaskMapper mapper = getBaseMapper();
        final Long transactionId = mapper.getMaxTransactionId();
        return (transactionId == null) ? 0 : transactionId;
    }

    @Async("consumerThreadPool")
    public void startRunner(TaskRunner runner) {
        // do some process control
        runner.run();
    }

}
