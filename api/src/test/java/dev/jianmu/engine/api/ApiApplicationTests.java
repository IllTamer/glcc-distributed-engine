package dev.jianmu.engine.api;

import dev.jianmu.engine.api.config.application.RegisterApplication;
import dev.jianmu.engine.api.service.TaskService;
import dev.jianmu.engine.register.util.CronParser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ApiApplicationTests {

    @Autowired
    private TaskService taskService;

    @Test
    void invoke() throws Exception {
        System.out.println(taskService.queryAllTimeoutWaiting(RegisterApplication.LONGEST_EXECUTION_SECONDS));
    }

    public static void main(String[] args) {
        System.out.println(CronParser.parse("5s9s1m7s0s"));
    }

}
