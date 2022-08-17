package dev.jianmu.engine.api.controller;

import dev.jianmu.engine.api.config.application.MonitorApplication;
import dev.jianmu.engine.api.vo.TaskProcessVO;
import dev.jianmu.engine.api.vo.TaskPublishVO;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/task/monitor")
public class MonitorController {

    private final MonitorApplication monitorApplication;

    public MonitorController(MonitorApplication monitorApplication) {
        this.monitorApplication = monitorApplication;
    }

    /**
     * 暂停任务
     * <p>
     * 已经提交的任务单中未执行的任务可以通过调度引擎暂停
     * @param uuid 任务的uuid
     * */
    @NotNull
    @PutMapping("/pause/{uuid}")
    public Boolean pauseTask(@PathVariable String uuid) {
        return monitorApplication.pauseTask(uuid);
    }

    /**
     * 恢复暂停任务
     * <p>
     * 恢复并重新发布暂停的任务
     * */
    @NotNull
    @PutMapping("/continue/{uuid}")
    public TaskPublishVO continueTask(@PathVariable String uuid) {
        return monitorApplication.continueTask(uuid);
    }

    /**
     * 查询任务调度过程
     * */
    @NotNull
    @GetMapping("/{uuid}")
    public TaskProcessVO checkProgress(@PathVariable String uuid) {
        return monitorApplication.checkProgress(uuid);
    }

}
