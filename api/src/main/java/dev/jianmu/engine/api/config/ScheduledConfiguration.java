package dev.jianmu.engine.api.config;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Configuration
public class ScheduledConfiguration implements SchedulingConfigurer {

    //自定义线程池名称
    public static final String THREAD_NAME_WITH_SCHEDULE = "custom-schedule-";

    public static int SCHEDULE_CORE_SIZE = 10;

    private final ScheduledExecutorService scheduledThreadPool;

    public ScheduledConfiguration(ScheduledExecutorService scheduledThreadPool) {
        this.scheduledThreadPool = scheduledThreadPool;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        scheduledTaskRegistrar.setScheduler(scheduledThreadPool);
        log.info("Custom scheduler thread-pool configured, core_size: " + SCHEDULE_CORE_SIZE);
    }

    /**
     * Spring的@Scheduled的自定义周期性线程池
     */
    @Bean("scheduledThreadPool")
    public static ScheduledExecutorService buildScheduledThreadPool() {
        return new ScheduledThreadPoolExecutor(
                SCHEDULE_CORE_SIZE,
                new CustomThreadFactory(THREAD_NAME_WITH_SCHEDULE)
        );
    }

    public static class CustomThreadFactory implements ThreadFactory {

        final AtomicInteger poolNumber = new AtomicInteger(1);
        final ThreadGroup group;
        final AtomicInteger threadNumber = new AtomicInteger(1);
        final String namePrefix;

        public CustomThreadFactory(String prefix) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            namePrefix = prefix +
                    poolNumber.getAndIncrement() +
                    "-thread-";
        }

        @Override
        public Thread newThread(@NotNull Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }

    }

}
