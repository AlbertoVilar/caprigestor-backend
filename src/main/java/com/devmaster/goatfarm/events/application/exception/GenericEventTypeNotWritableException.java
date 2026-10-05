package com.devmaster.goatfarm.events.application.exception;

/** Raised when a specialized domain fact is submitted through generic Events. */
public class GenericEventTypeNotWritableException extends RuntimeException {

    public static final String ERROR_CODE = "GENERIC_EVENT_TYPE_NOT_WRITABLE";

    public GenericEventTypeNotWritableException() {
        super("Este tipo de ocorrência deve ser registrado no módulo especializado correspondente.");
    }
}
