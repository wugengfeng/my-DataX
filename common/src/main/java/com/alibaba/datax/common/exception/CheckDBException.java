package com.alibaba.datax.common.exception;

/**
 * Created by acer on 2020/2/5.
 */
public class CheckDBException extends RuntimeException {
    public CheckDBException() {
    }

    public CheckDBException(String message) {
        super(message);
    }

    public CheckDBException(String message, Throwable cause) {
        super(message, cause);
    }

    public CheckDBException(Throwable cause) {
        super(cause);
    }

    public CheckDBException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
