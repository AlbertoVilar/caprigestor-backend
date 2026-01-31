package com.devmaster.goatfarm.health.application.ports.out;

import com.devmaster.goatfarm.health.domain.enums.HealthEventStatus;
import com.devmaster.goatfarm.health.domain.enums.HealthEventType;
import com.devmaster.goatfarm.health.persistence.entity.HealthEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Optional;

public interface HealthEventPersistencePort {

    HealthEvent save(HealthEvent healthEvent);
    Optional<HealthEvent> findByIdAndFarmIdAndGoatId(Long id, Long farmId, String goatId);
    Page<HealthEvent> findByFarmIdAndGoatId(Long farmId, String goatId, LocalDate from, LocalDate to,
                                            HealthEventType type, HealthEventStatus status, Pageable pageable);

    Page<HealthEvent> findByFarmIdAndPeriod(Long farmId, LocalDate from, LocalDate to,
                                            HealthEventType type, HealthEventStatus status, Pageable pageable);

}
