package dev.jianmu.engine.api.exception;

/**
 * 发布任务异常
 * */
public class PublishException extends RuntimeException {

    public PublishException() {
    }

    public PublishException(String message) {
        super(message);
    }

    public PublishException(String message, Throwable cause) {
        super(message, cause);
    }

}
