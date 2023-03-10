package dev.jianmu.engine.api.controller;

import dev.jianmu.engine.api.pojo.ResponseResult;
import dev.jianmu.engine.rpc.exception.AssertException;
import dev.jianmu.engine.rpc.exception.RpcException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;

/**
 * 全局异常处理类
 * */
@Slf4j
@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(AssertException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseResult assertException(AssertException e) {
        log.error("断言异常", e);
        return ResponseResult.error(e.getMessage());
    }

    @ExceptionHandler(SQLException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseResult sqlException(SQLException e) {
        log.error("SQL异常", e);
        return ResponseResult.error("SQL执行错误");
    }

    @ExceptionHandler(RpcException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseResult rpcException(RpcException e) {
        log.error("RPC调度异常", e);
        return ResponseResult.error(e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseResult runtimeException(RuntimeException e) {
        log.error("Unknown runtime exception", e);
        return ResponseResult.error(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseResult doException(Exception e) {
        log.error("Exception", e);
        return ResponseResult.error(e.getMessage());
    }

}
