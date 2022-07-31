package dev.jianmu.engine.consumer;

import dev.jianmu.engine.provider.Task;
import dev.jianmu.engine.provider.TaskStatus;
import dev.jianmu.engine.provider.Worker;
import dev.jianmu.engine.provider.event.TaskFinishEvent;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 任务执行类
 * <p>
 * 最小调度对象，后续操作定期/延时任务均操作此类
 * */
@Slf4j
public class TaskRunner implements Runnable {

    public static final Integer MAX_PER_WAIT_LENGTH = 64;

    @Getter
    private final long createTime = System.currentTimeMillis();
    private final TreeSet<Task> prioritySet = new TreeSet<>();

    private final Consumer<TaskFinishEvent> publishConsumer;
    private final Consumer<Task> refreshConsumer;
    private final Function<String, Task> queryFunction;
    @Getter
    private final Worker worker;

    public TaskRunner(
            Consumer<TaskFinishEvent> publishConsumer,
            Consumer<Task> refreshConsumer,
            Function<String, Task> queryFunction
    ) {
        this.publishConsumer = publishConsumer;
        this.refreshConsumer = refreshConsumer;
        this.queryFunction = queryFunction;
        this.worker = Worker.WORKER_MAP.get(Worker.Type.SHELL);
    }

    @Override
    public void run() {
        Task task;
        while ((task = dispatchTask()) != null) {
            final Task store = queryFunction.apply(task.getUuid());
            if (store.getStatus() == TaskStatus.PAUSE) {
                log.debug("Task#{} is suspended, skip execution", task.getUuid());
                // 清除分配标志，恢复时重新发布
                store.setWorkerId(null);
                continue;
            }
            task.setStatus(TaskStatus.RUNNING);
            try {
                refreshConsumer.accept(task);
                worker.runTask(task);
                task.setStatus(TaskStatus.EXECUTION_SUCCEEDED);
            } catch (Exception e) {
                task.setStatus(TaskStatus.EXECUTION_FAILED);
                log.error("Error occurred when running task({})", task, e);
            }
            task.setEndTime(LocalDateTime.now());
            try {
                refreshConsumer.accept(task);
            } catch (Exception e) {
                log.error("Error occurred when refresh task data in database", e);
            }
            try {
                publishConsumer.accept(
                        TaskFinishEvent.builder()
                                .task(task)
                                .status(task.getStatus())
                                .build()
                );
            } catch (Exception e) {
                log.error("Unexpected exception occurred when publish task({})", task, e);
            }
        }
    }

    /**
     * 添加任务
     * */
    public boolean push(Task task) {
        if (prioritySet.size() == MAX_PER_WAIT_LENGTH) {
            return false;
        }
        task.setWorkerId(worker.getId());
        prioritySet.add(task);
        return true;
    }

    /**
     * 分配任务
     * @return 将执行的任务，为 null 则代表没有任务需要被执行
     * @apiNote 返回的任务状态为 {@link TaskStatus#WAITING}
     * */
    @Nullable
    protected Task dispatchTask() {
        if (prioritySet.size() == 0) return null;
        return prioritySet.pollFirst();
    }

}
