package dev.jianmu.engine.provider;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Shell Worker
 * 仅实现 ShellWorker 用于测试
 * */
@Slf4j
public class ShellWorker extends Worker {

    public ShellWorker(String id, String name) {
        super(id, name, Type.SHELL);
    }

    @Override
    public void runTask(Task task) throws Exception {
        log.info("Task({}#{}) run succeed", task.getUuid(), task.getTransactionId());
//        Process process = Runtime.getRuntime().exec(new String[] {"/bin/bash", "-c", task.getScript()});
//        final int status = process.waitFor();
//        if (status == 0)
//            log.info("Task({}#{}) run succeed", task.getUuid(), task.getTransactionId());
//        else {
//            try (
//                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream(), DEFAULT_CHARSET))
//            ) {
//                String line;
//                StringBuilder errMessage = new StringBuilder();
//                while ((line = reader.readLine()) != null)
//                    errMessage.append('\n').append(line);
//                log.error("Task({}#{}) run failed:{}", task.getUuid(), task.getTransactionId(), errMessage);
//            }
//        }
    }

    @Override
    public String parseScript(List<String> scripts) {
        StringBuilder builder = new StringBuilder();
        scripts.forEach(s -> builder.append('&').append(s));
        return builder.deleteCharAt(0).toString();
    }

}
