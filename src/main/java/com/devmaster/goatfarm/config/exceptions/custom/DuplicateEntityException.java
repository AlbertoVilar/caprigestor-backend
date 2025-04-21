package com.devmaster.goatfarm.config.exceptions.custom;

public class DuplicateEntityException extends RuntimeException {

    public DuplicateEntityException(String msg) {
        super(msg);
    }
}
