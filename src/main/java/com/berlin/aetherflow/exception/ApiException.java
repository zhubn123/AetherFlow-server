package com.berlin.aetherflow.exception;

import lombok.Getter;

import java.io.Serial;

/**
 * 业务可控异常（支持自定义返回码）。
 */
@Getter
public class ApiException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Integer code;

    public ApiException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }

    public static ApiException unauthorized(String message) {
        return new ApiException(ResultCode.UNAUTHORIZED, message);
    }

    public static ApiException forbidden(String message) {
        return new ApiException(ResultCode.FORBIDDEN, message);
    }

    public static ApiException badRequest(String message) {
        return new ApiException(ResultCode.PARAM_ERROR, message);
    }

    public static ApiException business(String message) {
        return new ApiException(ResultCode.BUSINESS_ERROR, message);
    }
}
