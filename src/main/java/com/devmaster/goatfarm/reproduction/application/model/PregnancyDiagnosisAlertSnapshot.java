package com.devmaster.goatfarm.reproduction.application.model;

import java.time.LocalDate;

/** Persistence-free read model for pending diagnosis alerts. */
public record PregnancyDiagnosisAlertSnapshot(Long goatTechnicalId, String goatId, LocalDate lastCoverageDate,
                                               LocalDate lastCheckDate, LocalDate eligibleDate) { }
