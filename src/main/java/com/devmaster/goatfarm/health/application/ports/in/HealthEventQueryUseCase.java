package com.devmaster.goatfarm.health.application.ports.in;

import com.devmaster.goatfarm.health.business.bo.HealthEventResponseVO;
import com.devmaster.goatfarm.application.pagination.PageQuery;
import com.devmaster.goatfarm.application.pagination.PageResult;
import com.devmaster.goatfarm.health.domain.enums.HealthEventStatus;
import com.devmaster.goatfarm.health.domain.enums.HealthEventType;

import java.time.LocalDate;

public interface HealthEventQueryUseCase {
    HealthEventResponseVO getById(Long farmId, String goatId, Long eventId);
    PageResult<HealthEventResponseVO> listByGoat(Long farmId, String goatId, LocalDate from, LocalDate to, HealthEventType type, HealthEventStatus status, PageQuery pageQuery);
    PageResult<HealthEventResponseVO> listCalendar(Long farmId, LocalDate from, LocalDate to, HealthEventType type, HealthEventStatus status, PageQuery pageQuery);
}
