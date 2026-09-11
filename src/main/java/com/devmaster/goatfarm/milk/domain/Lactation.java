package com.devmaster.goatfarm.milk.domain;

import com.devmaster.goatfarm.milk.enums.LactationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Framework-free aggregate that models the lifecycle of one lactation.
 * Cross-aggregate rules (goat, pregnancy and uniqueness) remain in the
 * application service; this type owns only its intrinsic state transitions.
 */
public final class Lactation {

    private final Long id;
    private final Long farmId;
    private final String goatId;
    private final Long goatTechnicalId;
    private LactationStatus status;
    private final LocalDate startDate;
    private LocalDate endDate;
    private final LocalDate pregnancyStartDate;
    private LocalDate dryStartDate;
    private final Integer dryAtPregnancyDays;
    private final Integer restDays;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Lactation(
            Long id,
            Long farmId,
            String goatId,
            Long goatTechnicalId,
            LactationStatus status,
            LocalDate startDate,
            LocalDate endDate,
            LocalDate pregnancyStartDate,
            LocalDate dryStartDate,
            Integer dryAtPregnancyDays,
            Integer restDays,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.farmId = farmId;
        this.goatId = goatId;
        this.goatTechnicalId = goatTechnicalId;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.pregnancyStartDate = pregnancyStartDate;
        this.dryStartDate = dryStartDate;
        this.dryAtPregnancyDays = dryAtPregnancyDays;
        this.restDays = restDays;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Lactation open(Long farmId, String goatId, LocalDate startDate) {
        if (farmId == null || goatId == null || startDate == null) {
            throw new IllegalArgumentException("farmId, goatId e startDate sao obrigatorios");
        }
        return new Lactation(
                null, farmId, goatId, null, LactationStatus.ACTIVE, startDate,
                null, null, null, 90, 60, null, null
        );
    }

    public static Lactation rehydrate(
            Long id,
            Long farmId,
            String goatId,
            Long goatTechnicalId,
            LactationStatus status,
            LocalDate startDate,
            LocalDate endDate,
            LocalDate pregnancyStartDate,
            LocalDate dryStartDate,
            Integer dryAtPregnancyDays,
            Integer restDays,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new Lactation(
                id, farmId, goatId, goatTechnicalId, status, startDate, endDate,
                pregnancyStartDate, dryStartDate, dryAtPregnancyDays, restDays,
                createdAt, updatedAt
        );
    }

    public void dry(LocalDate dryEndDate) {
        if (status != LactationStatus.ACTIVE) {
            throw new IllegalStateException("Lactacao nao esta ativa.");
        }
        if (dryEndDate == null) {
            throw new IllegalArgumentException("Data de fim da lactacao e obrigatoria.");
        }
        if (dryEndDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Data de fim da lactacao nao pode ser anterior a data de inicio.");
        }
        status = LactationStatus.DRY;
        endDate = dryEndDate;
        dryStartDate = dryEndDate;
    }

    public void resume() {
        if (status != LactationStatus.DRY) {
            throw new IllegalStateException("Apenas lactacoes secadas podem ser retomadas.");
        }
        status = LactationStatus.ACTIVE;
        endDate = null;
        dryStartDate = null;
    }

    public Long getId() { return id; }
    public Long getFarmId() { return farmId; }
    public String getGoatId() { return goatId; }
    public Long getGoatTechnicalId() { return goatTechnicalId; }
    public LactationStatus getStatus() { return status; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public LocalDate getPregnancyStartDate() { return pregnancyStartDate; }
    public LocalDate getDryStartDate() { return dryStartDate; }
    public Integer getDryAtPregnancyDays() { return dryAtPregnancyDays; }
    public Integer getRestDays() { return restDays; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
