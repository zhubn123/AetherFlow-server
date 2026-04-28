package com.berlin.aetherflow.exception;

import jakarta.validation.ConstraintViolationException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public Result<?> handleApiException(ApiException e, HttpServletRequest request) {
        log.warn("请求路径：{}，业务异常：{}", request.getRequestURI(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String message = e.getBindingResult().getAllErrors().stream()
                .map(error -> error.getDefaultMessage() == null ? "参数错误" : error.getDefaultMessage())
                .collect(Collectors.joining("；"));
        log.warn("请求路径：{}，参数校验失败：{}", request.getRequestURI(), message);
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), message);
    }

    @ExceptionHandler(BindException.class)
    public Result<?> handleBindException(BindException e, HttpServletRequest request) {
        String message = e.getBindingResult().getAllErrors().stream()
                .map(error -> error.getDefaultMessage() == null ? "参数错误" : error.getDefaultMessage())
                .collect(Collectors.joining("；"));
        log.warn("请求路径：{}，绑定校验失败：{}", request.getRequestURI(), message);
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Result<?> handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
        String message = e.getConstraintViolations().stream()
                .map(violation -> violation.getMessage() == null ? "参数错误" : violation.getMessage())
                .collect(Collectors.joining("；"));
        log.warn("请求路径：{}，约束校验失败：{}", request.getRequestURI(), message);
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), message);
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e, HttpServletRequest request) {
        log.error("请求路径：{}", request.getRequestURI(), e);
        return Result.fail(ResultCode.SYSTEM_ERROR.getCode(), "系统内部错误: " + e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public Result<?> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        log.error("请求路径：{}", request.getRequestURI(), e);
        return Result.fail(ResultCode.BUSINESS_ERROR.getCode(), e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<?> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        log.error("请求路径：{}", request.getRequestURI(), e);
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), "参数错误: " + e.getMessage());
    }
}
