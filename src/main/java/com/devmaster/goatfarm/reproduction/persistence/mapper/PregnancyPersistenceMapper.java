package com.devmaster.goatfarm.reproduction.persistence.mapper;

import com.devmaster.goatfarm.reproduction.domain.Pregnancy;
import com.devmaster.goatfarm.reproduction.persistence.entity.PregnancyEntity;
import org.springframework.stereotype.Component;

@Component
public class PregnancyPersistenceMapper {
    public Pregnancy toDomain(PregnancyEntity e) {
        return Pregnancy.rehydrate(e.getId(), e.getFarmId(), e.getGoatId(), e.getGoatTechnicalId(), e.getStatus(), e.getBreedingDate(), e.getConfirmDate(), e.getExpectedDueDate(), e.getClosedAt(), e.getCloseReason(), e.getNotes(), e.getCoverageEventId(), e.getCreatedAt(), e.getUpdatedAt());
    }
    public PregnancyEntity toEntity(Pregnancy d) {
        PregnancyEntity e = PregnancyEntity.builder().id(d.getId()).farmId(d.getFarmId()).goatId(d.getGoatId()).goatTechnicalId(d.getGoatTechnicalId()).status(d.getStatus()).breedingDate(d.getBreedingDate()).confirmDate(d.getConfirmDate()).expectedDueDate(d.getExpectedDueDate()).closedAt(d.getClosedAt()).closeReason(d.getCloseReason()).notes(d.getNotes()).coverageEventId(d.getCoverageEventId()).createdAt(d.getCreatedAt()).updatedAt(d.getUpdatedAt()).build();
        return e;
    }
}
