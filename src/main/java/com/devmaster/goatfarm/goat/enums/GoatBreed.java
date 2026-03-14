package com.devmaster.goatfarm.goat.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.text.Normalizer;
import java.util.Locale;

public enum GoatBreed {
    ALPINE("Alpine"),
    ALPINA("Alpina"),
    ALPINA_AMERICANA("Alpina Americana"),
    ALPINA_BRITANICA("Alpina Britânica"),
    ANGLO_NUBIANA("Anglo Nubiana"),
    ANGORA("Angorá"),
    BHUJ("Bhuj"),
    BOER("Boer"),
    CANINDE("Canindé"),
    JAMNAPARI("Jamnapari"),
    KALAHARI("Kalahari"),
    MAMBRINA("Mambrina"),
    MESTICA("Mestiça"),
    MOXOTO("Moxotó"),
    MURCIANA("Murciana"),
    MURCIANA_GRANADINA("Murciana Granadina"),
    SAANEN("Saanen"),
    SAVANA("Savana"),
    SRD("SRD"),
    TOGGENBURG("Toggenburg");

    private final String portugueseValue;

    GoatBreed(String portugueseValue) {
        this.portugueseValue = portugueseValue;
    }

    @JsonValue
    public String getValue() {
        return name();
    }

    public String getLabel() {
        return portugueseValue;
    }

    @JsonCreator
    public static GoatBreed fromValue(String value) {
        if (value == null) {
            return null;
        }

        String token = normalizeToken(value);
        for (GoatBreed breed : GoatBreed.values()) {
            if (normalizeToken(breed.portugueseValue).equals(token)
                    || normalizeToken(breed.name()).equals(token)
                    || normalizeToken(breed.name().replace('_', ' ')).equals(token)) {
                return breed;
            }
        }

        throw new IllegalArgumentException("Valor inválido para GoatBreed: " + value);
    }

    private static String normalizeToken(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("_", " ")
                .replaceAll("\\s+", " ")
                .trim()
                .toUpperCase(Locale.ROOT);
    }
}
