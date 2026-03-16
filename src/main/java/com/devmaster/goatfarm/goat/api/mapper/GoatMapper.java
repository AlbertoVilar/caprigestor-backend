package com.devmaster.goatfarm.goat.api.mapper;

import com.devmaster.goatfarm.goat.api.dto.GoatRequestDTO;
import com.devmaster.goatfarm.goat.api.dto.GoatHerdSummaryDTO;
import com.devmaster.goatfarm.goat.api.dto.GoatResponseDTO;
import com.devmaster.goatfarm.goat.api.dto.GoatExitRequestDTO;
import com.devmaster.goatfarm.goat.api.dto.GoatExitResponseDTO;
import com.devmaster.goatfarm.goat.business.bo.GoatExitRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatExitResponseVO;
import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatHerdSummaryVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GoatMapper {
    GoatResponseDTO toResponseDTO(GoatResponseVO vo);

    GoatHerdSummaryDTO toHerdSummaryDTO(GoatHerdSummaryVO vo);

    GoatRequestVO toRequestVO(GoatRequestDTO dto);

    GoatExitRequestVO toExitRequestVO(GoatExitRequestDTO dto);

    GoatExitResponseDTO toExitResponseDTO(GoatExitResponseVO vo);
}
