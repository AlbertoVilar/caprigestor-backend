package com.devmaster.goatfarm.health.persistence.mapper;

import com.devmaster.goatfarm.health.application.model.HealthEventRecord;
import com.devmaster.goatfarm.health.persistence.entity.HealthEvent;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface HealthEventPersistenceMapper {

    HealthEventRecord toModel(HealthEvent entity);

    HealthEvent toEntity(HealthEventRecord model);
}
