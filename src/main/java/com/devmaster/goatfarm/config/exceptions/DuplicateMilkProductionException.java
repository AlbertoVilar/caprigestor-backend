package com.devmaster.goatfarm.config.exceptions;

public class DuplicateMilkProductionException extends DuplicateEntityException {

    public DuplicateMilkProductionException() {
        super("Producao de leite ja registrada para a mesma data e turno.");
    }

    public DuplicateMilkProductionException(String message) {
        super(message);
    }
}
