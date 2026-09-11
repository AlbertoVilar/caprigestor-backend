package com.devmaster.goatfarm.milk.persistence.mapper;

import com.devmaster.goatfarm.milk.domain.Lactation;
import com.devmaster.goatfarm.milk.persistence.entity.LactationEntity;
import org.springframework.stereotype.Component;

/** Translates the persistence representation without leaking JPA into the domain. */
@Component
public class LactationPersistenceMapper {

    public Lactation toDomain(LactationEntity entity) {
        if (entity == null) {
            return null;
        }
        return Lactation.rehydrate(
                entity.getId(), entity.getFarmId(), entity.getGoatId(), entity.getGoatTechnicalId(),
                entity.getStatus(), entity.getStartDate(), entity.getEndDate(), entity.getPregnancyStartDate(),
                entity.getDryStartDate(), entity.getDryAtPregnancyDays(), entity.getRestDays(),
                entity.getCreatedAt(), entity.getUpdatedAt()
        );
    }

    public LactationEntity toEntity(Lactation domain) {
        if (domain == null) {
            return null;
        }
        LactationEntity entity = new LactationEntity();
        entity.setId(domain.getId());
        entity.setFarmId(domain.getFarmId());
        entity.setGoatId(domain.getGoatId());
        entity.setGoatTechnicalId(domain.getGoatTechnicalId());
        entity.setStatus(domain.getStatus());
        entity.setStartDate(domain.getStartDate());
        entity.setEndDate(domain.getEndDate());
        entity.setPregnancyStartDate(domain.getPregnancyStartDate());
        entity.setDryStartDate(domain.getDryStartDate());
        entity.setDryAtPregnancyDays(domain.getDryAtPregnancyDays());
        entity.setRestDays(domain.getRestDays());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
}
