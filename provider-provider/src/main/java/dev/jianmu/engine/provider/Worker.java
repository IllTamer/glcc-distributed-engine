package dev.jianmu.engine.provider;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Task-Worker 实体类
 * @apiNote Worker 与 ExecutionNode 绑定(many->one), 其在线状态由 NodeInstancePool 维护
 * */
@Getter
public abstract class Worker {

    private final String id;

    private final String name;

    private final Type type;

    private final LocalDateTime createdTime = LocalDateTime.now();

    public Worker(String id, String name, Type type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    abstract public void runTask(Task task);

    public enum Type {
        SHELL
    }

}
