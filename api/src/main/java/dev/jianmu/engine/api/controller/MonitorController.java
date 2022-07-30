package dev.jianmu.engine.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import dev.jianmu.engine.api.service.TaskService;
import dev.jianmu.engine.api.vo.TaskProcessVO;
import dev.jianmu.engine.provider.Task;
import dev.jianmu.engine.register.ExecutionNode;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/task/monitor")
public class MonitorController {

    private final TaskService taskService;

    public MonitorController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * 暂停任务
     * <p>
     * 已经提交的任务单中未执行的任务可以通过调度引擎暂停
     * TODO
     * */
    @RequestMapping("/pause")
    public ExecutionNode pauseTask(String taskUUID) {
        return null;
    }

    /**
     * 查询任务调度过程
     * */
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
