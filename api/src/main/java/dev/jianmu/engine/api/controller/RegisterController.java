package dev.jianmu.engine.api.controller;

import dev.jianmu.engine.api.dto.TaskDTO;
import dev.jianmu.engine.provider.Task;
import dev.jianmu.engine.register.ExecutionNode;
import dev.jianmu.engine.api.config.application.RegisterApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/task/register")
public class RegisterController {

    private final RegisterApplication registerApplication;

    public RegisterController(RegisterApplication registerApplication) {
        this.registerApplication = registerApplication;
    }

    /**
     * 提交任务
     * TODO
     * @return 注册节点的信息
     * */
    @RequestMapping("/submit")
    public ExecutionNode submitTask(TaskDTO taskDTO) {
        // 创建Task
        Task task = registerApplication.createTask(taskDTO);
        // 更新Node状态(CPU使用率，内存使用率等)
        registerApplication.refreshNodes();
        // 分布式锁，入锁(拒绝新增) // 分布式建立时需要进行状态转变（申请->等待->确认，防止出现其它进程同时抢占的情况）
            // 查询

        // 分布式锁，出锁(允许新增)
        return null;
    }

}
