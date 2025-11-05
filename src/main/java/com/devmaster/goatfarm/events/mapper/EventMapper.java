package com.devmaster.goatfarm.events.mapper;

import com.devmaster.goatfarm.events.api.dto.EventRequestDTO;
import com.devmaster.goatfarm.events.api.dto.EventResponseDTO;
import com.devmaster.goatfarm.events.business.bo.EventRequestVO;
import com.devmaster.goatfarm.events.business.bo.EventResponseVO;
import com.devmaster.goatfarm.events.model.entity.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;
@Mapper(componentModel = "spring")
public interface EventMapper {

    EventRequestVO toRequestVO(EventRequestDTO dto);
    
    EventResponseDTO toResponseDTO(EventResponseVO vo);
    
    List<EventResponseDTO> toResponseDTOList(List<EventResponseVO> voList);

    // Mapeamentos necessários pelo EventApplicationService
    Event toEntity(EventRequestVO vo);

    void updateEvent(@MappingTarget Event target, EventRequestVO vo);

    EventResponseVO toResponseVO(Event entity);
}