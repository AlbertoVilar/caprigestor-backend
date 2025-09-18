package com.devmaster.goatfarm.events.facade.mapper;

import com.devmaster.goatfarm.events.business.bo.EventResponseVO;
import com.devmaster.goatfarm.events.facade.dto.EventFacadeResponseDTO;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Mapper para converter entre EventResponseVO e EventFacadeResponseDTO
 * Encapsula a conversão de dados internos (VO) para dados expostos pelo Facade (DTO)
 */
@Mapper(componentModel = "spring")
public interface EventFacadeMapper {

    /**
     * Converte EventResponseVO para EventFacadeResponseDTO
     * @param vo VO interno do business
     * @return DTO para exposição pelo Facade
     */
    EventFacadeResponseDTO toFacadeDTO(EventResponseVO vo);

    /**
     * Converte lista de EventResponseVO para lista de EventFacadeResponseDTO
     * @param vos Lista de VOs internos
     * @return Lista de DTOs para exposição pelo Facade
     */
    List<EventFacadeResponseDTO> toFacadeDTOList(List<EventResponseVO> vos);
}