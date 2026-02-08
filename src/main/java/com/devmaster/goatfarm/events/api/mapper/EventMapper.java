package com.devmaster.goatfarm.events.api.mapper;

import com.devmaster.goatfarm.events.api.dto.EventRequestDTO;
import com.devmaster.goatfarm.events.api.dto.EventResponseDTO;
import com.devmaster.goatfarm.events.business.bo.EventRequestVO;
import com.devmaster.goatfarm.events.business.bo.EventResponseVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
@Mapper(componentModel = "spring")
public interface EventMapper {

    EventRequestVO toRequestVO(EventRequestDTO dto);
    
    // Ajuste de nomes: eventId (VO) -> id (DTO)
    @Mapping(source = "eventId", target = "id")
    EventResponseDTO toResponseDTO(EventResponseVO vo);
    
    List<EventResponseDTO> toResponseDTOList(List<EventResponseVO> voList);

    // Somente DTO <-> VO. Mapeamentos de Entity ficam na camada business.
}
