package com.yg.exception;

import org.slf4j.helpers.MessageFormatter;

public class BizRuntimeException extends RuntimeException {
    private static final long serialVersionUID = 8905531579475605587L;
    private String errorCode;
    private String message;

    public BizRuntimeException() {
    }

    public BizRuntimeException(String message, Throwable e) {
        super(e);
        this.message = message;
    }

    public BizRuntimeException(String message) {
        super(message);
    }

    public BizRuntimeException(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    public BizRuntimeException message(String message) {
        this.message = message;
        return this;
    }

    public BizRuntimeException message(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
        return this;
    }

    public String getErrorCode() {
        return this.errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return this.message != null ? this.message : super.getMessage();
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static BizRuntimeException throwException(String msgPattern, Object... obj) {
        String message = MessageFormatter.arrayFormat(msgPattern, obj).getMessage();
        return new BizRuntimeException(message);
    }
}
