package dev.jianmu.engine.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import dev.jianmu.engine.api.service.TaskService;
import dev.jianmu.engine.api.vo.TaskProcessVO;
import dev.jianmu.engine.api.vo.TaskPublishVO;
import dev.jianmu.engine.provider.Task;
import dev.jianmu.engine.provider.TaskStatus;
import dev.jianmu.engine.rpc.util.Assert;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/task/monitor")
public class MonitorController {

    private final RegisterController registerController;
    private final TaskService taskService;

    public MonitorController(
            RegisterController registerController,
            TaskService taskService
    ) {
        this.registerController = registerController;
        this.taskService = taskService;
    }

    /**
     * 暂停任务
     * <p>
     * 已经提交的任务单中未执行的任务可以通过调度引擎暂停
     * */
    @RequestMapping("/pause")
    public Boolean pauseTask(String taskUUID) {
        final Task task = taskService.queryByUUID(taskUUID);
        if (task.getStatus() != TaskStatus.WAITING) return false;
        task.setStatus(TaskStatus.PAUSE);
        return taskService.refreshTask(task);
    }

    /**
     * 恢复暂停任务
     * <p>
     * 恢复并重新发布暂停的任务
     * */
    @NotNull
    @RequestMapping("/continue")
    public TaskPublishVO continueTask(String taskUUID) {
        final Task task = taskService.queryByUUID(taskUUID);
        Assert.isTrue(task.getStatus() == TaskStatus.PAUSE, "Wrong task status: {}", task.getStatus());
        task.setStatus(TaskStatus.WAITING);
        return registerController.doSubmitTask(task);
    }

    /**
     * 查询任务调度过程
     * */
    @NotNull
    @RequestMapping("progress")
    public TaskProcessVO checkProgress(String taskUUID) {
        final LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        Task task = taskService.getOne(wrapper.eq(Task::getUuid, taskUUID));
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
