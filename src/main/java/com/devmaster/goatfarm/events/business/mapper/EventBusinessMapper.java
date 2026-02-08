package com.devmaster.goatfarm.events.business.mapper;

import com.devmaster.goatfarm.events.business.bo.EventRequestVO;
import com.devmaster.goatfarm.events.business.bo.EventResponseVO;
import com.devmaster.goatfarm.events.persistence.entity.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EventBusinessMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "goat", ignore = true)
    Event toEntity(EventRequestVO vo);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "goat", ignore = true)
    void updateEntity(@MappingTarget Event target, EventRequestVO vo);

    @Mapping(source = "id", target = "eventId")
    @Mapping(source = "goat.registrationNumber", target = "goatId")
    @Mapping(source = "goat.name", target = "goatName")
    EventResponseVO toResponseVO(Event entity);
}
