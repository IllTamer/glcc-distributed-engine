package dev.jianmu.engine.api.config.application;

import dev.jianmu.engine.api.dto.TaskDTO;
import dev.jianmu.engine.provider.Task;
import dev.jianmu.engine.register.ExecutionNode;
import dev.jianmu.engine.register.NodeInstancePool;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Getter
public class RegisterApplication {

    private final NodeInstancePool nodeInstancePool;

    public RegisterApplication(NodeInstancePool nodeInstancePool) {
        this.nodeInstancePool = nodeInstancePool;
    }

    public Task createTask(TaskDTO dto) {
        return null;
    }

    public void refreshNodes() {
        final List<ExecutionNode> broadcast = nodeInstancePool.broadcast();
        if (broadcast == null || broadcast.size() == 0)
            log.warn("No node available");
    }

}
