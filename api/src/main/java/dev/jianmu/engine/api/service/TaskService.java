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

import java.util.List;

@Service
public class TaskService extends ServiceImpl<TaskMapper, Task> {

    @Transactional
    public Task queryByUUID(String uuid) {
        final LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getUuid, uuid);
        return getOne(wrapper);
    }

    /**
     * 刷新任务数据
     * */
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

    @Transactional
    public boolean removeByUUID(Task task) {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        return getBaseMapper().delete(wrapper
                .eq(Task::getUuid, task.getUuid())
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

    @Transactional
    public List<Task> queryAllTimeoutWaiting(long limitSeconds) {
        long timeLimit = (System.currentTimeMillis() - limitSeconds * 1000L) * 1000L;
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper // <= 最迟开始时间即为超时
                .le(Task::getStartTime, timeLimit)
                .last("LIMIT 256");
        return getBaseMapper().selectList(wrapper);
    }
}
