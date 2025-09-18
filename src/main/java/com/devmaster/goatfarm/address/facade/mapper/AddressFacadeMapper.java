package com.devmaster.goatfarm.address.facade.mapper;

import com.devmaster.goatfarm.address.business.bo.AddressResponseVO;
import com.devmaster.goatfarm.address.facade.dto.AddressFacadeResponseDTO;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Mapper para converter entre AddressResponseVO e AddressFacadeResponseDTO
 * Encapsula a conversão de dados internos (VO) para dados expostos pelo Facade (DTO)
 */
@Mapper(componentModel = "spring")
public interface AddressFacadeMapper {

    /**
     * Converte AddressResponseVO para AddressFacadeResponseDTO
     * @param vo VO interno do business
     * @return DTO para exposição pelo Facade
     */
    AddressFacadeResponseDTO toFacadeDTO(AddressResponseVO vo);

    /**
     * Converte lista de AddressResponseVO para lista de AddressFacadeResponseDTO
     * @param vos Lista de VOs internos
     * @return Lista de DTOs para exposição pelo Facade
     */
    List<AddressFacadeResponseDTO> toFacadeDTOList(List<AddressResponseVO> vos);
}