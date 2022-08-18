package dev.jianmu.engine.api.controller;

import dev.jianmu.engine.api.config.application.RegisterApplication;
import dev.jianmu.engine.api.dto.TaskDTO;
import dev.jianmu.engine.api.pojo.AjaxResult;
import dev.jianmu.engine.provider.Task;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/task/register")
public class RegisterController {

    private final RegisterApplication registerApplication;

    public RegisterController(RegisterApplication registerApplication) {
        this.registerApplication = registerApplication;
    }

    /**
     * 创建任务
     * @return 注册节点的信息
     * */
    @NotNull
    @PostMapping
    public AjaxResult createTask(@RequestBody TaskDTO taskDTO) {
        Task task = taskDTO.format();
        return AjaxResult.success(registerApplication.submitTask(task));
    }

    /**
     * 获取最新任务Id
     * <p>
     * 在 #submitTask() 前调用，用于获取传入任务的全局序列号
     * */
    @GetMapping("/obtain")
    public AjaxResult obtainTaskId() {
        registerApplication.refreshNodes();
        return AjaxResult.success(registerApplication.getNextTransactionId());
    }

}
