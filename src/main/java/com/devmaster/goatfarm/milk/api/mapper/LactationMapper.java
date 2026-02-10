package com.devmaster.goatfarm.milk.api.mapper;

import com.devmaster.goatfarm.milk.api.dto.LactationRequestDTO;
import com.devmaster.goatfarm.milk.api.dto.LactationDryRequestDTO;
import com.devmaster.goatfarm.milk.api.dto.LactationDryOffAlertItemDTO;
import com.devmaster.goatfarm.milk.api.dto.LactationResponseDTO;
import com.devmaster.goatfarm.milk.api.dto.LactationSummaryResponseDTO;
import com.devmaster.goatfarm.milk.business.bo.LactationRequestVO;
import com.devmaster.goatfarm.milk.business.bo.LactationDryRequestVO;
import com.devmaster.goatfarm.milk.business.bo.LactationDryOffAlertVO;
import com.devmaster.goatfarm.milk.business.bo.LactationResponseVO;
import com.devmaster.goatfarm.milk.business.bo.LactationSummaryResponseVO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LactationMapper {

    LactationRequestVO toRequestVO(LactationRequestDTO dto);

    LactationDryRequestVO toDryRequestVO(LactationDryRequestDTO dto);

    LactationResponseDTO toResponseDTO(LactationResponseVO vo);

    List<LactationResponseDTO> toResponseDTOList(List<LactationResponseVO> vos);

    LactationSummaryResponseDTO toSummaryResponseDTO(LactationSummaryResponseVO vo);

    LactationSummaryResponseDTO.LactationSummaryLactationDTO toSummaryLactationDTO(
            LactationSummaryResponseVO.LactationSummaryLactationVO vo
    );

    LactationSummaryResponseDTO.LactationSummaryProductionDTO toSummaryProductionDTO(
            LactationSummaryResponseVO.LactationSummaryProductionVO vo
    );

    LactationSummaryResponseDTO.LactationSummaryPregnancyDTO toSummaryPregnancyDTO(
            LactationSummaryResponseVO.LactationSummaryPregnancyVO vo
    );

    LactationDryOffAlertItemDTO toDryOffAlertItemDTO(LactationDryOffAlertVO vo);
}
