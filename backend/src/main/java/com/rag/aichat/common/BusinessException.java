package com.rag.aichat.common;

public class BusinessException extends RuntimeException {

    private final int code;
    private final int statusCode;

    public BusinessException(int statusCode, int code, String message) {
        super(message);
        this.code = code;
        this.statusCode = statusCode;
    }

    public BusinessException(int statusCode, int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.statusCode = statusCode;
    }

    public int getCode() {
        return code;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
