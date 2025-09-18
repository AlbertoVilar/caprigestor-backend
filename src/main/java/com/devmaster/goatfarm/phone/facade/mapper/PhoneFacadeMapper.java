package com.devmaster.goatfarm.phone.facade.mapper;

import com.devmaster.goatfarm.phone.business.bo.PhoneResponseVO;
import com.devmaster.goatfarm.phone.facade.dto.PhoneFacadeResponseDTO;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Mapper para converter entre PhoneResponseVO e PhoneFacadeResponseDTO
 * Encapsula a conversão de dados internos (VO) para dados expostos pelo Facade (DTO)
 */
@Mapper(componentModel = "spring")
public interface PhoneFacadeMapper {

    /**
     * Converte PhoneResponseVO para PhoneFacadeResponseDTO
     * @param vo VO interno do business
     * @return DTO para exposição pelo Facade
     */
    PhoneFacadeResponseDTO toFacadeDTO(PhoneResponseVO vo);

    /**
     * Converte lista de PhoneResponseVO para lista de PhoneFacadeResponseDTO
     * @param vos Lista de VOs internos
     * @return Lista de DTOs para exposição pelo Facade
     */
    List<PhoneFacadeResponseDTO> toFacadeDTOList(List<PhoneResponseVO> vos);
}