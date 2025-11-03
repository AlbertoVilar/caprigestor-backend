package com.devmaster.goatfarm.goat.mapper;

import com.devmaster.goatfarm.goat.api.dto.GoatRequestDTO;
import com.devmaster.goatfarm.goat.api.dto.GoatResponseDTO;
import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.model.entity.Goat;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface GoatMapper {

    GoatResponseVO toResponseVO(Goat entity);

    GoatResponseDTO toResponseDTO(GoatResponseVO vo);

    GoatRequestVO toRequestVO(GoatRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "farm", ignore = true)
    @Mapping(target = "father", ignore = true)
    @Mapping(target = "mother", ignore = true)
    Goat toEntity(GoatRequestVO vo);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "farm", ignore = true)
    void updateEntity(@MappingTarget Goat entity, GoatRequestVO vo, Goat father, Goat mother);
}
