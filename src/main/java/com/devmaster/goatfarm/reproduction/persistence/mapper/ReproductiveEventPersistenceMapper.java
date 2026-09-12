package com.devmaster.goatfarm.reproduction.persistence.mapper;

import com.devmaster.goatfarm.reproduction.domain.ReproductiveEvent;
import com.devmaster.goatfarm.reproduction.persistence.entity.ReproductiveEventEntity;
import org.springframework.stereotype.Component;

@Component
public class ReproductiveEventPersistenceMapper {
    public ReproductiveEvent toDomain(ReproductiveEventEntity e) {
        return ReproductiveEvent.rehydrate(e.getId(), e.getFarmId(), e.getGoatId(), e.getGoatTechnicalId(), e.getEventType(), e.getEventDate(), e.getBreedingType(), e.getBreederRef(), e.getNotes(), e.getPregnancyId(), e.getRelatedEventId(), e.getCorrectedEventDate(), e.getCheckScheduledDate(), e.getCheckResult(), e.getCreatedAt(), e.getUpdatedAt());
    }
    public ReproductiveEventEntity toEntity(ReproductiveEvent d) {
        return ReproductiveEventEntity.builder().id(d.getId()).farmId(d.getFarmId()).goatId(d.getGoatId()).goatTechnicalId(d.getGoatTechnicalId()).eventType(d.getEventType()).eventDate(d.getEventDate()).breedingType(d.getBreedingType()).breederRef(d.getBreederRef()).notes(d.getNotes()).pregnancyId(d.getPregnancyId()).relatedEventId(d.getRelatedEventId()).correctedEventDate(d.getCorrectedEventDate()).checkScheduledDate(d.getCheckScheduledDate()).checkResult(d.getCheckResult()).createdAt(d.getCreatedAt()).updatedAt(d.getUpdatedAt()).build();
    }
}
