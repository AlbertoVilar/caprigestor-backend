package com.devmaster.goatfarm.milk.mapper;

import com.devmaster.goatfarm.milk.api.dto.LactationRequestDTO;
import com.devmaster.goatfarm.milk.api.dto.LactationResponseDTO;
import com.devmaster.goatfarm.milk.api.dto.LactationSummaryResponseDTO;
import com.devmaster.goatfarm.milk.business.bo.LactationRequestVO;
import com.devmaster.goatfarm.milk.business.bo.LactationResponseVO;
import com.devmaster.goatfarm.milk.business.bo.LactationSummaryResponseVO;
import com.devmaster.goatfarm.milk.model.entity.Lactation;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LactationMapper {

    LactationRequestVO toRequestVO(LactationRequestDTO dto);

    LactationResponseDTO toResponseDTO(LactationResponseVO vo);

    List<LactationResponseDTO> toResponseDTOList(List<LactationResponseVO> vos);

    LactationSummaryResponseDTO toSummaryResponseDTO(LactationSummaryResponseVO vo);

    LactationResponseVO toResponseVO(Lactation entity);

    List<LactationResponseVO> toResponseVOList(List<Lactation> entities);
}
