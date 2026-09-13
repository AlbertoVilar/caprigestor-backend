package com.devmaster.goatfarm.health.application.ports.out;

import com.devmaster.goatfarm.health.domain.enums.HealthEventStatus;
import com.devmaster.goatfarm.health.domain.enums.HealthEventType;
import com.devmaster.goatfarm.health.application.model.HealthEventRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HealthEventPersistencePort {

    HealthEventRecord save(HealthEventRecord healthEvent);
    Optional<HealthEventRecord> findByIdAndFarmIdAndGoatId(Long id, Long farmId, String goatId);
    Page<HealthEventRecord> findByFarmIdAndGoatId(Long farmId, String goatId, LocalDate from, LocalDate to,
                                                  HealthEventType type, HealthEventStatus status, Pageable pageable);

    Page<HealthEventRecord> findByFarmIdAndPeriod(Long farmId, LocalDate from, LocalDate to,
                                                  HealthEventType type, HealthEventStatus status, Pageable pageable);

    List<HealthEventRecord> findPerformedWithWithdrawalByFarmIdAndGoatId(Long farmId, String goatId);

    List<HealthEventRecord> findPerformedWithWithdrawalByFarmId(Long farmId);

}
