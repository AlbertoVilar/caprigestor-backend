package com.devmaster.goatfarm.farm.mapper;

import com.devmaster.goatfarm.farm.api.dto.GoatFarmFullResponseDTO;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmRequestDTO;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmResponseDTO;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmUpdateFarmDTO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmFullResponseVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmRequestVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmResponseVO;
import com.devmaster.goatfarm.farm.model.entity.GoatFarm;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Mapper(componentModel = "spring")
public interface GoatFarmMapper {

    GoatFarmFullResponseDTO toFullDTO(GoatFarmFullResponseVO vo);

    GoatFarmResponseDTO toResponseDTO(GoatFarmResponseVO vo);

    GoatFarmRequestVO toRequestVO(GoatFarmRequestDTO dto);

    GoatFarmRequestVO toRequestVO(GoatFarmUpdateFarmDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "phones", ignore = true)
    @Mapping(target = "goats", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    GoatFarm toEntity(GoatFarmRequestVO vo);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "phones", ignore = true)
    @Mapping(target = "goats", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget GoatFarm entity, GoatFarmRequestVO vo);

    GoatFarmFullResponseVO toFullResponseVO(GoatFarm entity);

    GoatFarmResponseVO toResponseVO(GoatFarm entity);

    // Auxiliar para conversão de timestamps
    default LocalDateTime map(Instant value) {
        return value == null ? null : LocalDateTime.ofInstant(value, ZoneId.systemDefault());
    }
}