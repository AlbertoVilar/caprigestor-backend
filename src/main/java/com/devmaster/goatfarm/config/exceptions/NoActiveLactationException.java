package com.devmaster.goatfarm.config.exceptions;

import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;

public class NoActiveLactationException extends InvalidArgumentException {

    public NoActiveLactationException() {
        super("Nao ha lactacao ativa para registrar producao de leite.");
    }

    public NoActiveLactationException(String message) {
        super(message);
    }
}
