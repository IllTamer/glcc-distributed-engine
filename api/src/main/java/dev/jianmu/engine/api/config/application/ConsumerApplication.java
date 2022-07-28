package dev.jianmu.engine.api.config.application;

import dev.jianmu.engine.api.config.ScheduleConfiguration;
import dev.jianmu.engine.api.service.TaskService;
import dev.jianmu.engine.consumer.TaskRunner;
import dev.jianmu.engine.provider.ProviderInfo;
import dev.jianmu.engine.provider.ShellWorker;
import dev.jianmu.engine.provider.Task;
import dev.jianmu.engine.provider.Worker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Component
public class ConsumerApplication {

    public static final int MAX_CONSUMER_POOL_SIZE = 64;

    private final ReentrantLock lock = new ReentrantLock(true);
    private final ApplicationEventPublisher publisher;
    private final TaskService taskService;

    private ThreadPoolExecutor consumerThreadPool;
    private volatile TaskRunner runner;

    public ConsumerApplication(
            ApplicationEventPublisher publisher,
            TaskService taskService
    ) {
        this.publisher = publisher;
        this.taskService = taskService;
        // configurable
        Worker.WORKER_MAP.put(Worker.Type.SHELL, new ShellWorker(UUID.randomUUID().toString(), Worker.Type.SHELL.name()));
    }

    /**
     * 定时启动 TaskRunner
     * <p>
     * 每 300ms 检测一次，超时 500ms 即启动，根据业务量自行调整
     * */
    @Scheduled(fixedDelay = 300)
    public void scheduleStartRunner() {
        if (runner != null && timeout(runner.getCreateTime(), 500)) {
            log.info("runner timed out");
            lock.lock();
            // 无需双重校验
            taskService.startRunner(runner);
            runner = null; // 必须赋null，不然持续运行空 Runner
            lock.unlock();
        }
    }

    /**
     * 添加任务
     * priority + 批次处理思想
     * */
    public ProviderInfo push(Task task) {
        lock.lock();
        if (runner == null || !runner.push(task)) {
            if (runner != null) {
                taskService.startRunner(runner);
            }
            runner = new TaskRunner(publisher::publishEvent, taskService::refreshTask);
            runner.push(task);
        }
        lock.unlock();
        ProviderInfo info = new ProviderInfo();
        info.setThreadPoolUsage(consumerThreadPool.getActiveCount() * 100 / MAX_CONSUMER_POOL_SIZE);
        info.setWorkerId(runner.getWorker().getId());
        return info;
    }

    @Bean("consumerThreadPool")
    public ThreadPoolExecutor getConsumerThreadPool() {
        return consumerThreadPool = new ThreadPoolExecutor(
                0,
                MAX_CONSUMER_POOL_SIZE,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                new ScheduleConfiguration.CustomThreadFactory("consumer-pool-")
        );
    }

    private static boolean timeout(long taskCreateTime, long limitTIme) {
        return System.currentTimeMillis() - taskCreateTime >= limitTIme;
    }

}
