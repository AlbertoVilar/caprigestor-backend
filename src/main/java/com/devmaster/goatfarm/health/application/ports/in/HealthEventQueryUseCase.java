package com.devmaster.goatfarm.health.application.ports.in;

import com.devmaster.goatfarm.health.business.bo.HealthEventResponseVO;
import com.devmaster.goatfarm.health.domain.enums.HealthEventStatus;
import com.devmaster.goatfarm.health.domain.enums.HealthEventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface HealthEventQueryUseCase {
    HealthEventResponseVO getById(Long farmId, String goatId, Long eventId);
    Page<HealthEventResponseVO> listByGoat(Long farmId, String goatId, LocalDate from, LocalDate to, HealthEventType type, HealthEventStatus status, Pageable pageable);
    Page<HealthEventResponseVO> listCalendar(Long farmId, LocalDate from, LocalDate to, HealthEventType type, HealthEventStatus status, Pageable pageable);
}
