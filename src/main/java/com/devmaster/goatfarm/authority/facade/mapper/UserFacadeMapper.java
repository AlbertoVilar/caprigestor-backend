package com.devmaster.goatfarm.authority.facade.mapper;

import com.devmaster.goatfarm.authority.business.bo.UserResponseVO;
import com.devmaster.goatfarm.authority.facade.dto.UserFacadeResponseDTO;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Mapper para converter entre UserResponseVO e UserFacadeResponseDTO
 * Encapsula a conversão de dados internos (VO) para dados expostos pelo Facade (DTO)
 */
@Mapper(componentModel = "spring")
public interface UserFacadeMapper {

    /**
     * Converte UserResponseVO para UserFacadeResponseDTO
     * @param vo VO interno do business
     * @return DTO para exposição pelo Facade
     */
    UserFacadeResponseDTO toFacadeDTO(UserResponseVO vo);

    /**
     * Converte lista de UserResponseVO para lista de UserFacadeResponseDTO
     * @param vos Lista de VOs internos
     * @return Lista de DTOs para exposição pelo Facade
     */
    List<UserFacadeResponseDTO> toFacadeDTOList(List<UserResponseVO> vos);
}