package dev.jianmu.engine.consumer;

import dev.jianmu.engine.provider.Task;
import dev.jianmu.engine.provider.TaskStatus;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Slf4j
public class ConsumerApplication {

    private static final Integer MAX_PER_WAIT_LENGTH = 64;

    private final Deque<TreeSet<Task>> priorityVector = new ArrayDeque<>();

    /**
     * 分配任务
     * @return 将执行的任务，为 null 则代表没有任务需要被执行
     * @apiNote 返回的任务状态为 {@link TaskStatus#WAITING}
     * */
    @Nullable
    public Task dispatchTask() {
        if (priorityVector.size() == 0) return null;
        TreeSet<Task> prioritySet = priorityVector.getFirst();
        if (priorityVector.getFirst().size() == 0) {
            priorityVector.removeFirst();
            prioritySet = priorityVector.getFirst();
        }
        return prioritySet.pollFirst();
    }

    /**
     * 添加任务
     * <p>
     * priority + 批次处理思想
     * */
    public void push(Task task) {
        TreeSet<Task> prioritySet = null;
        if (priorityVector.size() == 0 || priorityVector.getLast().size() == MAX_PER_WAIT_LENGTH) {
            priorityVector.add(prioritySet = new TreeSet<>());
        }
        if (prioritySet == null) {
            prioritySet = priorityVector.getLast();
        }
        prioritySet.add(task);
    }

}
