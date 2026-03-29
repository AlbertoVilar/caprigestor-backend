package com.devmaster.goatfarm.health.api.mapper;

import com.devmaster.goatfarm.health.api.dto.GoatWithdrawalStatusDTO;
import com.devmaster.goatfarm.health.api.dto.HealthWithdrawalOriginDTO;
import com.devmaster.goatfarm.health.business.bo.GoatWithdrawalStatusVO;
import com.devmaster.goatfarm.health.business.bo.HealthWithdrawalOriginVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface HealthWithdrawalApiMapper {
    GoatWithdrawalStatusDTO toDTO(GoatWithdrawalStatusVO vo);
    HealthWithdrawalOriginDTO toDTO(HealthWithdrawalOriginVO vo);
}
