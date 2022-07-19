package dev.jianmu.engine.api.controller;

import dev.jianmu.engine.api.vo.DispatchProgressVO;
import dev.jianmu.engine.register.ExecutionNode;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/task/monitor")
public class MonitorController {

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
    public DispatchProgressVO checkProgress(String taskUUID) {
        return null;
    }

}
