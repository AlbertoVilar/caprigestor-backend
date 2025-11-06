package com.devmaster.goatfarm.goat.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.text.Normalizer;

public enum GoatStatus {
    ATIVO("Ativo"),
    INATIVO("Inativo"),
    FALECIDO("Falecido"),
    VENDIDO("Vendido");

    private final String portugueseValue;

    GoatStatus(String portugueseValue) {
        this.portugueseValue = portugueseValue;
    }

    @JsonCreator
    public static GoatStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        String normalizedInput = normalize(value);
        for (GoatStatus status : GoatStatus.values()) {
            if (normalize(status.portugueseValue).equalsIgnoreCase(normalizedInput)
                    || status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Valor inválido para GoatStatus: " + value);
    }

    private static String normalize(String input) {
        return Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "") // remove acentos
                .trim();
    }
}
