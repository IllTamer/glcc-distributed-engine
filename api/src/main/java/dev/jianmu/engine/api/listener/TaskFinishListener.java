package dev.jianmu.engine.api.listener;

import dev.jianmu.engine.provider.event.TaskFinishEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TaskFinishListener {

    @EventListener(TaskFinishEvent.class)
    public void onFinish(TaskFinishEvent event) {
        log.info("Task({}) has been finished#{}, we can do sth.", event.getTask().getUuid(), event.getStatus());
    }

}
