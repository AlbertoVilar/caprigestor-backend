package com.devmaster.goatfarm.health.domain.enums;

public enum AdministrationRoute {
    IM("Intramuscular"),
    SC("Subcutânea"),
    IV("Intravenosa"),
    VO("Oral"),
    TOPICA("Tópica/Pour-on"),
    INTRAMAMARIA("Intramamária"),
    OUTRO("Outro");

    private final String description;

    AdministrationRoute(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
