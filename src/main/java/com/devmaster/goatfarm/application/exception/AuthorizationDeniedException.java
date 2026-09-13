package com.devmaster.goatfarm.application.exception;

/** Signals that an application operation is not authorized for the current principal. */
public class AuthorizationDeniedException extends RuntimeException {

    public AuthorizationDeniedException(String message) {
        super(message);
    }
}
