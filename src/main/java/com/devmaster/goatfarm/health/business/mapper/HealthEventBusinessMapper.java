package com.devmaster.goatfarm.health.business.mapper;

import com.devmaster.goatfarm.health.business.bo.HealthEventCreateRequestVO;
import com.devmaster.goatfarm.health.business.bo.HealthEventResponseVO;
import com.devmaster.goatfarm.health.business.bo.HealthEventUpdateRequestVO;
import com.devmaster.goatfarm.health.domain.enums.HealthEventStatus;
import com.devmaster.goatfarm.health.persistence.entity.HealthEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.LocalDate;

@Mapper(componentModel = "spring")
public interface HealthEventBusinessMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "farmId", ignore = true)
    @Mapping(target = "goatId", ignore = true)
    @Mapping(target = "status", expression = "java(com.devmaster.goatfarm.health.domain.enums.HealthEventStatus.AGENDADO)")
    @Mapping(target = "performedAt", ignore = true)
    @Mapping(target = "responsible", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    HealthEvent toEntity(HealthEventCreateRequestVO vo);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "farmId", ignore = true)
    @Mapping(target = "goatId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "performedAt", ignore = true)
    @Mapping(target = "responsible", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget HealthEvent entity, HealthEventUpdateRequestVO vo);

    @Mapping(target = "overdue", expression = "java(isOverdue(entity))")
    HealthEventResponseVO toResponseVO(HealthEvent entity);

    default boolean isOverdue(HealthEvent entity) {
        if (entity.getScheduledDate() == null) return false;
        if (entity.getStatus() == HealthEventStatus.REALIZADO || entity.getStatus() == HealthEventStatus.CANCELADO) {
            return false;
        }
        return entity.getScheduledDate().isBefore(LocalDate.now());
    }
}
