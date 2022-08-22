package dev.jianmu.engine.api.controller;

import dev.jianmu.engine.api.config.application.MonitorApplication;
import dev.jianmu.engine.api.pojo.ResponseResult;
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
    public ResponseResult pauseTask(@PathVariable String uuid) {
        return ResponseResult.success(monitorApplication.pauseTask(uuid));
    }

    /**
     * 恢复暂停任务
     * <p>
     * 恢复并重新发布暂停的任务
     * @param uuid 任务的uuid
     * */
    @NotNull
    @PutMapping("/continue/{uuid}")
    public ResponseResult continueTask(@PathVariable String uuid) {
        return ResponseResult.success(monitorApplication.continueTask(uuid));
    }

    /**
     * 查询任务调度过程
     * @param uuid 任务的uuid
     * */
    @NotNull
    @GetMapping("/{uuid}")
    public ResponseResult checkProgress(@PathVariable String uuid) {
        return ResponseResult.success(monitorApplication.checkProgress(uuid));
    }

}
