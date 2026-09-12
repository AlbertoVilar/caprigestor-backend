package com.devmaster.goatfarm.reproduction.domain;

import com.devmaster.goatfarm.reproduction.enums.PregnancyCloseReason;
import com.devmaster.goatfarm.reproduction.enums.PregnancyStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Framework-free reproduction aggregate. Persistence concerns stay in adapters. */
public final class Pregnancy {
    private final Long id;
    private final Long farmId;
    private final String goatId;
    private final Long goatTechnicalId;
    private PregnancyStatus status;
    private final LocalDate breedingDate;
    private final LocalDate confirmDate;
    private final LocalDate expectedDueDate;
    private LocalDate closedAt;
    private PregnancyCloseReason closeReason;
    private String notes;
    private final Long coverageEventId;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Pregnancy(Builder b) {
        this.id = b.id; this.farmId = b.farmId; this.goatId = b.goatId; this.goatTechnicalId = b.goatTechnicalId;
        this.status = b.status; this.breedingDate = b.breedingDate; this.confirmDate = b.confirmDate;
        this.expectedDueDate = b.expectedDueDate; this.closedAt = b.closedAt; this.closeReason = b.closeReason;
        this.notes = b.notes; this.coverageEventId = b.coverageEventId; this.createdAt = b.createdAt; this.updatedAt = b.updatedAt;
    }

    public static Pregnancy confirmed(Long farmId, String goatId, LocalDate breedingDate, LocalDate confirmDate,
                                      LocalDate expectedDueDate, String notes, Long coverageEventId) {
        return builder().farmId(farmId).goatId(goatId).status(PregnancyStatus.ACTIVE).breedingDate(breedingDate)
                .confirmDate(confirmDate).expectedDueDate(expectedDueDate).notes(notes).coverageEventId(coverageEventId).build();
    }

    public static Pregnancy rehydrate(Long id, Long farmId, String goatId, Long goatTechnicalId, PregnancyStatus status,
                                      LocalDate breedingDate, LocalDate confirmDate, LocalDate expectedDueDate,
                                      LocalDate closedAt, PregnancyCloseReason closeReason, String notes, Long coverageEventId,
                                      LocalDateTime createdAt, LocalDateTime updatedAt) {
        return builder().id(id).farmId(farmId).goatId(goatId).goatTechnicalId(goatTechnicalId).status(status)
                .breedingDate(breedingDate).confirmDate(confirmDate).expectedDueDate(expectedDueDate).closedAt(closedAt)
                .closeReason(closeReason).notes(notes).coverageEventId(coverageEventId).createdAt(createdAt).updatedAt(updatedAt).build();
    }

    public void close(PregnancyCloseReason reason, LocalDate date) {
        this.status = PregnancyStatus.CLOSED; this.closedAt = date; this.closeReason = reason;
    }

    public void updateNotes(String notes) { this.notes = notes; }

    public Long getId() { return id; }
    public Long getFarmId() { return farmId; }
    public String getGoatId() { return goatId; }
    public Long getGoatTechnicalId() { return goatTechnicalId; }
    public PregnancyStatus getStatus() { return status; }
    public LocalDate getBreedingDate() { return breedingDate; }
    public LocalDate getConfirmDate() { return confirmDate; }
    public LocalDate getExpectedDueDate() { return expectedDueDate; }
    public LocalDate getClosedAt() { return closedAt; }
    public PregnancyCloseReason getCloseReason() { return closeReason; }
    public String getNotes() { return notes; }
    public Long getCoverageEventId() { return coverageEventId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public static Builder builder() { return new Builder(); }
    public static final class Builder {
        private Long id, farmId, goatTechnicalId, coverageEventId; private String goatId, notes;
        private PregnancyStatus status; private LocalDate breedingDate, confirmDate, expectedDueDate, closedAt;
        private PregnancyCloseReason closeReason; private LocalDateTime createdAt, updatedAt;
        public Builder id(Long v){id=v;return this;} public Builder farmId(Long v){farmId=v;return this;}
        public Builder goatId(String v){goatId=v;return this;} public Builder goatTechnicalId(Long v){goatTechnicalId=v;return this;}
        public Builder status(PregnancyStatus v){status=v;return this;} public Builder breedingDate(LocalDate v){breedingDate=v;return this;}
        public Builder confirmDate(LocalDate v){confirmDate=v;return this;} public Builder expectedDueDate(LocalDate v){expectedDueDate=v;return this;}
        public Builder closedAt(LocalDate v){closedAt=v;return this;} public Builder closeReason(PregnancyCloseReason v){closeReason=v;return this;}
        public Builder notes(String v){notes=v;return this;} public Builder coverageEventId(Long v){coverageEventId=v;return this;}
        public Builder createdAt(LocalDateTime v){createdAt=v;return this;} public Builder updatedAt(LocalDateTime v){updatedAt=v;return this;}
        public Pregnancy build(){return new Pregnancy(this);}
    }
}
