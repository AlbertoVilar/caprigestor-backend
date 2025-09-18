package com.devmaster.goatfarm.genealogy.mapper;

import com.devmaster.goatfarm.genealogy.api.dto.GenealogyRequestDTO;
import com.devmaster.goatfarm.genealogy.api.dto.GenealogyResponseDTO;
import com.devmaster.goatfarm.genealogy.business.bo.GenealogyResponseVO;
import com.devmaster.goatfarm.genealogy.model.entity.Genealogy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface GenealogyMapper {
    GenealogyMapper INSTANCE = Mappers.getMapper(GenealogyMapper.class);

    @Mapping(target = "id", ignore = true)
    Genealogy toEntity(GenealogyRequestDTO dto);

    GenealogyResponseVO toResponseVO(Genealogy entity);

    GenealogyResponseDTO toResponseDTO(GenealogyResponseVO vo);
}
