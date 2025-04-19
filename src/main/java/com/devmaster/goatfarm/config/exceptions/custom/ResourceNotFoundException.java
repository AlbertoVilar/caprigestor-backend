package com.devmaster.goatfarm.config.exceptions.custom;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String msg) {
        super(msg);
    }
}
