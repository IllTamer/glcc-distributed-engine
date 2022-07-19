package dev.jianmu.engine.provider;

public class ShellWorker extends Worker {

    public ShellWorker(String id, String name) {
        super(id, name, Type.SHELL);
    }

    @Override
    public void runTask(Task task) {
//        "echo \"?\" | /bin/bash"
        // TODO
    }

}
