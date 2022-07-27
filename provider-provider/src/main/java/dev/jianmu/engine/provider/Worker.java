package dev.jianmu.engine.provider;

import lombok.Getter;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Task-Worker 实体类
 * @apiNote Worker 与 ExecutionNode 绑定(many->one), 其在线状态由 NodeInstancePool 维护
 * */
@Getter
public abstract class Worker {

    public static final HashMap<Type, Worker> WORKER_MAP = new HashMap<>();

    protected static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase(Locale.ROOT).startsWith("windows");

    protected static final Charset DEFAULT_CHARSET = IS_WINDOWS ? Charset.forName("GB2312") : StandardCharsets.UTF_8;

    @Getter
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

    abstract public String parseScript(List<String> scripts);

    public enum Type {

        SHELL

    }

    @Override
    public String toString() {
        return "Worker(" +
                "id=" + id +
                ", name=" + name +
                ", type=" + type +
                ", createdTime=" + createdTime +
                ')';
    }

}
