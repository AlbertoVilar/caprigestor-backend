package com.devmaster.goatfarm.application.exception;

/** Signals a persistence conflict without exposing infrastructure-specific exceptions. */
public class PersistenceConflictException extends RuntimeException {

    public PersistenceConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
