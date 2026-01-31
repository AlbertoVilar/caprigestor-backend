package com.devmaster.goatfarm.health.api.mapper;

import com.devmaster.goatfarm.health.api.dto.*;
import com.devmaster.goatfarm.health.business.bo.*;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface HealthEventApiMapper {
    HealthEventCreateRequestVO toCreateVO(HealthEventCreateRequestDTO dto);
    HealthEventUpdateRequestVO toUpdateVO(HealthEventUpdateRequestDTO dto);
    HealthEventDoneRequestVO toDoneVO(HealthEventDoneRequestDTO dto);
    HealthEventCancelRequestVO toCancelVO(HealthEventCancelRequestDTO dto);
    HealthEventResponseDTO toDTO(HealthEventResponseVO vo);
}
