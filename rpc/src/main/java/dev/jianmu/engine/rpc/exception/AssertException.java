package dev.jianmu.engine.rpc.exception;

/**
 * Assert exception
 * */
public class AssertException extends RuntimeException {

    public AssertException() {
    }

    public AssertException(String message) {
        super(message);
    }

    public AssertException(String message, Throwable cause) {
        super(message, cause);
    }

}
