package dev.jianmu.engine.provider.event;

import dev.jianmu.engine.provider.Task;
import dev.jianmu.engine.provider.TaskStatus;
import lombok.Builder;
import lombok.Getter;

/**
 * 任务结束事件
 * */
@Builder
@Getter
public class TaskFinishEvent {

    private final Task task;

    private final TaskStatus status;

}
