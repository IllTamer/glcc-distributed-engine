package dev.jianmu.engine.provider;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Slf4j
public class ShellWorker extends Worker {

    public ShellWorker(String id, String name) {
        super(id, name, Type.SHELL);
    }

    @Override
    @SneakyThrows({IOException.class, InterruptedException.class})
    public void runTask(Task task) {
        StringBuilder builder = new StringBuilder();
        task.getScript().forEach(s -> builder.append('&').append(s));
        Process process = Runtime.getRuntime().exec(new String[] {"/bin/bash", "-c", builder.deleteCharAt(0).toString()});
        final int status = process.waitFor();
        if (status == 0)
            log.info("Task({}#{}) run succeed", task.getUuid(), task.getTransactionId());
        else {
            try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream(), DEFAULT_CHARSET))
            ) {
                String line;
                StringBuilder errMessage = new StringBuilder();
                while ((line = reader.readLine()) != null)
                    errMessage.append('\n').append(line);
                log.error("Task({}#{}) run failed:{}", task.getUuid(), task.getTransactionId(), errMessage);
            }
        }
    }

}
