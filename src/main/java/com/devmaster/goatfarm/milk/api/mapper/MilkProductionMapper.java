package com.devmaster.goatfarm.milk.api.mapper;

import com.devmaster.goatfarm.milk.api.dto.MilkProductionRequestDTO;
import com.devmaster.goatfarm.milk.api.dto.MilkProductionResponseDTO;
import com.devmaster.goatfarm.milk.api.dto.MilkProductionUpdateRequestDTO;
import com.devmaster.goatfarm.milk.business.bo.MilkProductionRequestVO;
import com.devmaster.goatfarm.milk.business.bo.MilkProductionResponseVO;
import com.devmaster.goatfarm.milk.business.bo.MilkProductionUpdateRequestVO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MilkProductionMapper {

    MilkProductionRequestVO toRequestVO(MilkProductionRequestDTO dto);

    MilkProductionUpdateRequestVO toRequestVO(MilkProductionUpdateRequestDTO dto);

    MilkProductionResponseDTO toResponseDTO(MilkProductionResponseVO vo);

    List<MilkProductionResponseDTO> toResponseDTOList(List<MilkProductionResponseVO> vos);
}
