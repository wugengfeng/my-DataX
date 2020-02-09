package com.alibaba.datax.common.exception;

/**
 * Created by acer on 2020/2/5.
 */
public class ParseConfigException extends RuntimeException{
    public ParseConfigException() {
    }

    public ParseConfigException(String message) {
        super(message);
    }

    public ParseConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParseConfigException(Throwable cause) {
        super(cause);
    }

    public ParseConfigException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
