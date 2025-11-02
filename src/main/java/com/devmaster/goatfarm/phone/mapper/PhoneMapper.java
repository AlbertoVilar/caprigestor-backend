package com.devmaster.goatfarm.phone.mapper;

import com.devmaster.goatfarm.phone.api.dto.PhoneRequestDTO;
import com.devmaster.goatfarm.phone.api.dto.PhoneResponseDTO;
import com.devmaster.goatfarm.phone.business.bo.PhoneRequestVO;
import com.devmaster.goatfarm.phone.business.bo.PhoneResponseVO;
import com.devmaster.goatfarm.phone.model.entity.Phone;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import java.util.List;

@Mapper(componentModel = "spring")
public interface PhoneMapper {

    // --- FLUXO DE REQUISIÇÃO (ENTRADA DE DADOS) ---

    /**
     * Converte um DTO de requisição para um VO de requisição.
     * DTO (API) -> VO (Business)
     */
    PhoneRequestVO toRequestVO(PhoneRequestDTO dto);

    /**
     * Converte um VO de requisição para a Entidade JPA.
     * VO (Business) -> Entity (JPA)
     */
    @Mapping(target = "id", ignore = true)
    Phone toEntity(PhoneRequestVO vo);

    @Mapping(target = "id", ignore = true)
    void toEntity(@MappingTarget Phone target, PhoneRequestVO vo);


    // --- FLUXO DE RESPOSTA (SAÍDA DE DADOS) ---

    /**
     * Converte a Entidade JPA para um VO de resposta.
     * Entity (JPA) -> VO (Business)
     */
    PhoneResponseVO toResponseVO(Phone entity);

    /**
     * Converte um VO de resposta para um DTO de resposta.
     * VO (Business) -> DTO (API)
     */
    PhoneResponseDTO toResponseDTO(PhoneResponseVO vo);


    // --- MAPEAMENTO DE LISTAS (RESOLVENDO O ERRO DO .map()) ---

    /**
     * Converte uma lista de DTOs de requisição para uma lista de VOs de requisição.
     * List<DTO> -> List<VO>
     */
    List<PhoneRequestVO> toRequestVOList(List<PhoneRequestDTO> dtos);

    /**
     * Converte uma lista de Entidades para uma lista de VOs de resposta.
     * List<Entity> -> List<VO>
     */
    List<PhoneResponseVO> toResponseVOList(List<Phone> entities);

    /**
     * Converte uma lista de VOs de resposta para uma lista de DTOs de resposta.
     * List<VO> -> List<DTO>
     */
    List<PhoneResponseDTO> toResponseDTOList(List<PhoneResponseVO> vos);
}