package dev.jianmu.engine.api.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.jianmu.engine.api.mapper.FutureTaskMapper;
import dev.jianmu.engine.provider.FutureTask;
import dev.jianmu.engine.provider.Task;
import org.springframework.stereotype.Service;

@Service
public class FutureTaskService extends ServiceImpl<FutureTaskMapper, FutureTask> {

    public boolean removeByTaskUUID(Task task) {
        LambdaQueryWrapper<FutureTask> wrapper = new LambdaQueryWrapper<>();
        return getBaseMapper().delete(wrapper
                .eq(FutureTask::getUuid, task.getUuid())
        ) > 0;
    }

}
