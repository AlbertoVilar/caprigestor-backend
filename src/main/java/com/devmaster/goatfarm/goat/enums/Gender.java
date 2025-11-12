package com.devmaster.goatfarm.goat.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.text.Normalizer;

public enum Gender {
    MACHO("Macho"),
    FEMEA("Fêmea");

    private final String portugueseValue;

    Gender(String portugueseValue) {
        this.portugueseValue = portugueseValue;
    }

    @JsonCreator
    public static Gender fromValue(String value) {
        if (value == null) {
            return null;
        }
        String normalizedInput = normalize(value);
        for (Gender gender : Gender.values()) {
            if (normalize(gender.portugueseValue).equalsIgnoreCase(normalizedInput)
                    || gender.name().equalsIgnoreCase(value)) {
                return gender;
            }
        }
        throw new IllegalArgumentException("Valor inválido para Gender: " + value);
    }

    private static String normalize(String input) {
        return Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "") // remove acentos
                .trim();
    }
}

