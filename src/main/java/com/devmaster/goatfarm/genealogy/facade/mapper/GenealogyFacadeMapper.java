package com.devmaster.goatfarm.genealogy.facade.mapper;

import com.devmaster.goatfarm.genealogy.business.bo.GenealogyResponseVO;
import com.devmaster.goatfarm.genealogy.facade.dto.GenealogyFacadeResponseDTO;
import org.mapstruct.Mapper;

/**
 * Mapper para converter entre GenealogyResponseVO e GenealogyFacadeResponseDTO
 * Encapsula a conversão de dados internos (VO) para dados expostos pelo Facade (DTO)
 */
@Mapper(componentModel = "spring")
public interface GenealogyFacadeMapper {

    /**
     * Converte GenealogyResponseVO para GenealogyFacadeResponseDTO
     * @param vo VO interno do business
     * @return DTO para exposição pelo Facade
     */
    GenealogyFacadeResponseDTO toFacadeDTO(GenealogyResponseVO vo);
}