package dev.jianmu.engine.api.config.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import dev.jianmu.engine.api.service.TaskService;
import dev.jianmu.engine.api.vo.TaskProcessVO;
import dev.jianmu.engine.api.vo.TaskPublishVO;
import dev.jianmu.engine.provider.Task;
import dev.jianmu.engine.provider.TaskStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class MonitorApplication {

    private final RegisterApplication registerApplication;
    private final TaskService taskService;

    public MonitorApplication(
            RegisterApplication registerApplication,
            TaskService taskService
    ) {
        this.registerApplication = registerApplication;
        this.taskService = taskService;
    }

    public Boolean pauseTask(String uuid) {
        final Task task = taskService.queryByUUID(uuid);
        if (task.getStatus() != TaskStatus.WAITING) return false;
        task.setStatus(TaskStatus.PAUSE);
        return taskService.refreshTask(task);
    }

    public TaskPublishVO continueTask(String uuid) {
        final Task task = taskService.queryByUUID(uuid);
        Assert.notNull(task, "Can't find paused task#" + uuid);
        Assert.isTrue(task.getStatus() == TaskStatus.PAUSE, "Wrong task status: " + task.getStatus());
        task.setStatus(TaskStatus.WAITING);
        return registerApplication.submitTask(task);
    }

    public TaskProcessVO checkProgress(String uuid) {
        final LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        Task task = taskService.getOne(wrapper.eq(Task::getUuid, uuid));
        return TaskProcessVO.builder()
                .uuid(task.getUuid())
                .transactionId(task.getTransactionId())
                .type(task.getType())
                .workerId(task.getWorkerId())
                .status(task.getStatus())
                .startTime(task.getStartTime())
                .endTime(task.getEndTime())
                .build();
    }

}
