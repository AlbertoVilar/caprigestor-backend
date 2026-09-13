package com.devmaster.goatfarm.health.application.ports.out;

import com.devmaster.goatfarm.health.domain.enums.HealthEventStatus;
import com.devmaster.goatfarm.health.domain.enums.HealthEventType;
import com.devmaster.goatfarm.health.application.model.HealthEventRecord;
import com.devmaster.goatfarm.application.pagination.PageQuery;
import com.devmaster.goatfarm.application.pagination.PageResult;
import com.devmaster.goatfarm.health.application.model.HealthEventWindow;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HealthEventPersistencePort {

    HealthEventRecord save(HealthEventRecord healthEvent);
    Optional<HealthEventRecord> findByIdAndFarmIdAndGoatId(Long id, Long farmId, String goatId);
    PageResult<HealthEventRecord> findByFarmIdAndGoatId(Long farmId, String goatId, LocalDate from, LocalDate to,
                                                        HealthEventType type, HealthEventStatus status, PageQuery pageQuery);

    PageResult<HealthEventRecord> findByFarmIdAndPeriod(Long farmId, LocalDate from, LocalDate to,
                                                        HealthEventType type, HealthEventStatus status, PageQuery pageQuery);

    HealthEventWindow findNextScheduledEvents(Long farmId, LocalDate from, LocalDate to,
                                              HealthEventType type, HealthEventStatus status, int limit);

    List<HealthEventRecord> findPerformedWithWithdrawalByFarmIdAndGoatId(Long farmId, String goatId);

    List<HealthEventRecord> findPerformedWithWithdrawalByFarmId(Long farmId);

}
