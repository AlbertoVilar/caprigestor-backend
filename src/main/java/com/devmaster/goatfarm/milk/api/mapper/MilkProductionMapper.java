package com.devmaster.goatfarm.milk.api.mapper;

import com.devmaster.goatfarm.milk.api.dto.MilkProductionRequestDTO;
import com.devmaster.goatfarm.milk.api.dto.MilkProductionResponseDTO;
import com.devmaster.goatfarm.milk.api.dto.MilkProductionUpdateRequestDTO;
import com.devmaster.goatfarm.milk.business.bo.MilkProductionRequestVO;
import com.devmaster.goatfarm.milk.business.bo.MilkProductionResponseVO;
import com.devmaster.goatfarm.milk.business.bo.MilkProductionUpdateRequestVO;
import com.devmaster.goatfarm.milk.persistence.entity.MilkProduction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MilkProductionMapper {

    MilkProductionRequestVO toRequestVO(MilkProductionRequestDTO dto);

    MilkProductionUpdateRequestVO toRequestVO(MilkProductionUpdateRequestDTO dto);

    MilkProductionResponseDTO toResponseDTO(MilkProductionResponseVO vo);

    List<MilkProductionResponseDTO> toResponseDTOList(List<MilkProductionResponseVO> vos);

    MilkProductionResponseVO toResponseVO(MilkProduction entity);

    List<MilkProductionResponseVO> toResponseVOList(List<MilkProduction> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "farmId", ignore = true)
    @Mapping(target = "goatId", ignore = true)
    @Mapping(target = "lactation", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "canceledAt", ignore = true)
    @Mapping(target = "canceledReason", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    MilkProduction toEntity(MilkProductionRequestVO vo);
}
