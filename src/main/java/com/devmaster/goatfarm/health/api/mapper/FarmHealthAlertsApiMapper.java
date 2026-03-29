package com.devmaster.goatfarm.health.api.mapper;

import com.devmaster.goatfarm.health.api.dto.FarmHealthAlertsResponseDTO;
import com.devmaster.goatfarm.health.business.bo.FarmHealthAlertItemVO;
import com.devmaster.goatfarm.health.business.bo.FarmHealthAlertsResponseVO;
import com.devmaster.goatfarm.health.business.bo.WithdrawalAlertItemVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FarmHealthAlertsApiMapper {
    FarmHealthAlertsResponseDTO toDTO(FarmHealthAlertsResponseVO vo);
    FarmHealthAlertsResponseDTO.AlertItemDTO toDTO(FarmHealthAlertItemVO vo);
    FarmHealthAlertsResponseDTO.WithdrawalAlertItemDTO toDTO(WithdrawalAlertItemVO vo);
}
