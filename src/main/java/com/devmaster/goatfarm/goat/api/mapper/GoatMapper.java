package com.devmaster.goatfarm.goat.api.mapper;

import com.devmaster.goatfarm.goat.api.dto.GoatRequestDTO;
import com.devmaster.goatfarm.goat.api.dto.GoatResponseDTO;
import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GoatMapper {
    GoatResponseDTO toResponseDTO(GoatResponseVO vo);

    GoatRequestVO toRequestVO(GoatRequestDTO dto);
}
