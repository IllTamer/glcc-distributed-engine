package dev.jianmu.engine.consumer;

import dev.jianmu.engine.provider.Task;
import dev.jianmu.engine.provider.TaskStatus;
import dev.jianmu.engine.provider.Worker;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.TreeSet;
import java.util.function.Consumer;

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

    private final Consumer<Task> publishConsumer;
    private final Consumer<Task> databaseConsumer;
    @Getter
    private final Worker worker;

    public TaskRunner(Consumer<Task> publishConsumer, Consumer<Task> databaseConsumer) {
        this.publishConsumer = publishConsumer;
        this.databaseConsumer = databaseConsumer;
        this.worker = Worker.WORKER_MAP.get(Worker.Type.SHELL);
    }

    @Override
    public void run() {
        Task task;
        while ((task = dispatchTask()) != null) {
            task.setStatus(TaskStatus.RUNNING);
            try {
                worker.runTask(task);
                task.setStatus(TaskStatus.EXECUTION_SUCCEEDED);
            } catch (Exception e) {
                task.setStatus(TaskStatus.EXECUTION_FAILED);
                log.error("Error occurred when running task({})", task, e);
            }
            task.setEndTime(LocalDateTime.now());
            try {
                databaseConsumer.accept(task);
            } catch (Exception e) {
                log.error("Error occurred when refresh task data in database", e);
            }
            try {
                publishConsumer.accept(task);
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
