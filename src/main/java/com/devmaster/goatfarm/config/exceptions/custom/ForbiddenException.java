package com.devmaster.goatfarm.config.exceptions.custom;

public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String msg) {
        super(msg);
    }

    public ForbiddenException(String msg, Throwable cause) {
        super(msg, cause);
    }
}