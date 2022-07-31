package dev.jianmu.engine.api.controller;

import dev.jianmu.engine.api.config.application.RegisterApplication;
import dev.jianmu.engine.api.dto.TaskDTO;
import dev.jianmu.engine.api.pojo.EngineLock;
import dev.jianmu.engine.api.service.PessimisticLockService;
import dev.jianmu.engine.api.vo.TaskPublishVO;
import dev.jianmu.engine.provider.Task;
import dev.jianmu.engine.rpc.util.Assert;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/task/register")
public class RegisterController {

    private static final String SUBMIT_BUSINESS_CODE = "submit";

    private final RegisterApplication registerApplication;
    private final PessimisticLockService pessimisticLockService;

    public RegisterController(
            RegisterApplication registerApplication,
            PessimisticLockService pessimisticLockService
    ) {
        this.registerApplication = registerApplication;
        this.pessimisticLockService = pessimisticLockService;
    }

    /**
     * 提交任务
     * @return 注册节点的信息
     *  Key: hostName,
     *  Value: workerId
     * */
    @NotNull
    @RequestMapping("/submit")
    public TaskPublishVO submitTask(@RequestBody TaskDTO taskDTO) {
        // 创建Task
        Task task = registerApplication.createTask(taskDTO);
        return doSubmitTask(task);
    }

    /**
     * 获取最新任务Id
     * <p>
     * 在 #submitTask() 前调用，用于获取传入任务的全局序列号
     * */
    @RequestMapping("/obtain")
    public Long obtainTaskId() {
        registerApplication.refreshNodes();
        return registerApplication.getNextTransactionId();
    }

    @NotNull
    protected TaskPublishVO doSubmitTask(Task task) {
        // 更新Node状态(CPU使用率，内存使用率等)
        registerApplication.refreshNodes();
        Map<String, String> workerIdMap = null;
        EngineLock lock = null;
        try {
            // 分布式锁，入锁(拒绝新增)
            lock = pessimisticLockService.tryLock(SUBMIT_BUSINESS_CODE);
            workerIdMap = registerApplication.publish(task);
            final boolean unlock = pessimisticLockService.unlock(lock);
            Assert.isTrue(unlock, "Unlock failed: Lock(%s)", lock);
            lock = null;
        } catch (InterruptedException e) {
            log.warn("Failed to request lock: {}", SUBMIT_BUSINESS_CODE);
        } catch (Exception e) {
            log.warn("Something happened when publish Task({})", task, e);
        } finally {
            // 分布式锁，出锁(允许新增)
            if (lock != null)
                pessimisticLockService.unlock(lock);
        }
        return TaskPublishVO.builder()
                .uuid(task.getUuid())
                .workerIdMap(workerIdMap)
                .build();
    }

}
