package com.devmaster.goatfarm.application.exception;

/** Indicates that canonical ownership cannot be established for a civil date. */
public class GoatOwnershipNotValidOnDateException extends RuntimeException {

    public static final String ERROR_CODE = "GOAT_OWNERSHIP_NOT_VALID_ON_DATE";

    public enum Reason {
        OWNERSHIP_NOT_UNAMBIGUOUS,
        OWNERSHIP_PERIOD_FROM_ANOTHER_FARM
    }

    private final Reason reason;

    public GoatOwnershipNotValidOnDateException(String message) {
        this(message, Reason.OWNERSHIP_NOT_UNAMBIGUOUS);
    }

    public GoatOwnershipNotValidOnDateException(String message, Reason reason) {
        super(message);
        this.reason = reason;
    }

    public Reason reason() {
        return reason;
    }
}
