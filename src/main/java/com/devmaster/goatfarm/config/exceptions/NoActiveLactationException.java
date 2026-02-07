package com.devmaster.goatfarm.config.exceptions;

import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;

public class NoActiveLactationException extends BusinessRuleException {

    public NoActiveLactationException() {
        super("Nao ha lactacao ativa para registrar producao de leite.");
    }

    public NoActiveLactationException(String message) {
        super(message);
    }
}
