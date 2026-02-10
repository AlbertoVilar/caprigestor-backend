package com.devmaster.goatfarm.milk.persistence.projection;

import java.time.LocalDate;

public interface LactationDryOffAlertProjection {
    Long getLactationId();

    String getGoatId();

    Integer getDryAtPregnancyDays();

    LocalDate getStartDatePregnancy();

    LocalDate getBreedingDate();

    LocalDate getConfirmDate();

    LocalDate getDryOffDate();
}
