package com.devmaster.goatfarm.reproduction.persistence.projection;

import java.time.LocalDate;

public interface PregnancyDiagnosisAlertProjection {
    String getGoatId();

    LocalDate getLastCoverageDate();

    LocalDate getLastCheckDate();

    LocalDate getEligibleDate();
}
