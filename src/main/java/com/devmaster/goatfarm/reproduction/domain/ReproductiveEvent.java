package com.devmaster.goatfarm.reproduction.domain;

import com.devmaster.goatfarm.reproduction.enums.BreedingType;
import com.devmaster.goatfarm.reproduction.enums.PregnancyCheckResult;
import com.devmaster.goatfarm.reproduction.enums.ReproductiveEventType;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Immutable historical reproduction fact. */
public final class ReproductiveEvent {
    private final Long id, farmId, goatTechnicalId, pregnancyId, relatedEventId;
    private final String goatId, breederRef, notes;
    private final ReproductiveEventType eventType;
    private final LocalDate eventDate, correctedEventDate, checkScheduledDate;
    private final BreedingType breedingType;
    private final PregnancyCheckResult checkResult;
    private final LocalDateTime createdAt, updatedAt;

    private ReproductiveEvent(Builder b) {
        id=b.id; farmId=b.farmId; goatId=b.goatId; goatTechnicalId=b.goatTechnicalId; eventType=b.eventType; eventDate=b.eventDate;
        breedingType=b.breedingType; breederRef=b.breederRef; notes=b.notes; pregnancyId=b.pregnancyId; relatedEventId=b.relatedEventId;
        correctedEventDate=b.correctedEventDate; checkScheduledDate=b.checkScheduledDate; checkResult=b.checkResult;
        createdAt=b.createdAt; updatedAt=b.updatedAt;
    }
    public static ReproductiveEvent rehydrate(Long id, Long farmId, String goatId, Long goatTechnicalId, ReproductiveEventType eventType,
                                              LocalDate eventDate, BreedingType breedingType, String breederRef, String notes,
                                              Long pregnancyId, Long relatedEventId, LocalDate correctedEventDate,
                                              LocalDate checkScheduledDate, PregnancyCheckResult checkResult,
                                              LocalDateTime createdAt, LocalDateTime updatedAt) {
        return builder().id(id).farmId(farmId).goatId(goatId).goatTechnicalId(goatTechnicalId).eventType(eventType).eventDate(eventDate)
                .breedingType(breedingType).breederRef(breederRef).notes(notes).pregnancyId(pregnancyId).relatedEventId(relatedEventId)
                .correctedEventDate(correctedEventDate).checkScheduledDate(checkScheduledDate).checkResult(checkResult)
                .createdAt(createdAt).updatedAt(updatedAt).build();
    }
    public Long getId(){return id;} public Long getFarmId(){return farmId;} public String getGoatId(){return goatId;}
    public Long getGoatTechnicalId(){return goatTechnicalId;} public ReproductiveEventType getEventType(){return eventType;}
    public LocalDate getEventDate(){return eventDate;} public BreedingType getBreedingType(){return breedingType;}
    public String getBreederRef(){return breederRef;} public String getNotes(){return notes;} public Long getPregnancyId(){return pregnancyId;}
    public Long getRelatedEventId(){return relatedEventId;} public LocalDate getCorrectedEventDate(){return correctedEventDate;}
    public LocalDate getCheckScheduledDate(){return checkScheduledDate;} public PregnancyCheckResult getCheckResult(){return checkResult;}
    public LocalDateTime getCreatedAt(){return createdAt;} public LocalDateTime getUpdatedAt(){return updatedAt;}
    public static Builder builder(){return new Builder();}
    public static final class Builder {
        private Long id,farmId,goatTechnicalId,pregnancyId,relatedEventId; private String goatId,breederRef,notes;
        private ReproductiveEventType eventType; private LocalDate eventDate,correctedEventDate,checkScheduledDate; private BreedingType breedingType;
        private PregnancyCheckResult checkResult; private LocalDateTime createdAt,updatedAt;
        public Builder id(Long v){id=v;return this;} public Builder farmId(Long v){farmId=v;return this;} public Builder goatId(String v){goatId=v;return this;}
        public Builder goatTechnicalId(Long v){goatTechnicalId=v;return this;} public Builder eventType(ReproductiveEventType v){eventType=v;return this;}
        public Builder eventDate(LocalDate v){eventDate=v;return this;} public Builder breedingType(BreedingType v){breedingType=v;return this;}
        public Builder breederRef(String v){breederRef=v;return this;} public Builder notes(String v){notes=v;return this;}
        public Builder pregnancyId(Long v){pregnancyId=v;return this;} public Builder relatedEventId(Long v){relatedEventId=v;return this;}
        public Builder correctedEventDate(LocalDate v){correctedEventDate=v;return this;} public Builder checkScheduledDate(LocalDate v){checkScheduledDate=v;return this;}
        public Builder checkResult(PregnancyCheckResult v){checkResult=v;return this;} public Builder createdAt(LocalDateTime v){createdAt=v;return this;}
        public Builder updatedAt(LocalDateTime v){updatedAt=v;return this;} public ReproductiveEvent build(){return new ReproductiveEvent(this);}
    }
}
