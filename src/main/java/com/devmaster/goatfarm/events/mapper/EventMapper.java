package com.devmaster.goatfarm.events.mapper;

import com.devmaster.goatfarm.events.api.dto.EventRequestDTO;
import com.devmaster.goatfarm.events.api.dto.EventResponseDTO;
import com.devmaster.goatfarm.events.business.bo.EventRequestVO;
import com.devmaster.goatfarm.events.business.bo.EventResponseVO;
import com.devmaster.goatfarm.events.model.entity.Event;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * Mapper para conversões entre DTOs, VOs e Entities do Event
 * Utiliza MapStruct para geração automática das implementações
 */
@Mapper(componentModel = "spring")
public interface EventMapper {

    // --- FLUXO DE REQUISIÇÃO (ENTRADA DE DADOS) ---

    /**
     * Converte um DTO de requisição para um VO de requisição.
     * DTO (API) -> VO (Business)
     */
    EventRequestVO toRequestVO(EventRequestDTO dto);
    
    /**
     * Converte um DTO de requisição para um VO de requisição (alias para compatibilidade).
     * DTO (API) -> VO (Business)
     */
    EventRequestVO toVO(EventRequestDTO dto);

    /**
     * Converte um VO de requisição para a Entidade JPA.
     * VO (Business) -> Entity (JPA)
     */
    Event toEntity(EventRequestVO vo);


    // --- FLUXO DE RESPOSTA (SAÍDA DE DADOS) ---

    /**
     * Converte a Entidade JPA para um VO de resposta.
     * Entity (JPA) -> VO (Business)
     */
    EventResponseVO toResponseVO(Event entity);

    /**
     * Converte um VO de resposta para um DTO de resposta.
     * VO (Business) -> DTO (API)
     */
    EventResponseDTO responseDTO(EventResponseVO vo);


    // --- MAPEAMENTO DE LISTAS ---

    /**
     * Converte uma lista de Entidades para uma lista de VOs de resposta.
     * List<Entity> -> List<VO>
     */
    List<EventResponseVO> toResponseVOList(List<Event> entities);

    /**
     * Converte uma lista de VOs de resposta para uma lista de DTOs de resposta.
     * List<VO> -> List<DTO>
     */
    List<EventResponseDTO> toResponseDTOList(List<EventResponseVO> vos);

    /**
     * Atualiza a entidade Event com os dados do VO de requisição.
     * Mantém associações existentes como Goat.
     */
    void updateEvent(@MappingTarget Event event, EventRequestVO vo);
}