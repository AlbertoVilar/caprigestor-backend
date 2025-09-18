package com.devmaster.goatfarm.farm.mapper;

import com.devmaster.goatfarm.farm.business.bo.GoatFarmFullRequestVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmFullResponseVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmRequestVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmResponseVO;
import com.devmaster.goatfarm.farm.model.entity.GoatFarm;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmRequestDTO;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmResponseDTO;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmFullResponseDTO;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmFullRequestDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GoatFarmMapper {

    GoatFarm toEntity(GoatFarmRequestDTO dto);

    GoatFarmResponseDTO toResponseDTO(GoatFarm entity);

    GoatFarmFullResponseDTO toFullDTO(GoatFarmFullResponseVO vo);

    GoatFarmRequestVO toRequestVO(GoatFarmRequestDTO dto);

    GoatFarmFullRequestVO toFullRequestVO(GoatFarmFullRequestDTO dto);

    GoatFarmResponseVO toResponseVO(GoatFarm entity);

}