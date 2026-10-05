package com.devmaster.goatfarm.reproduction.application.ports.out;

import com.devmaster.goatfarm.reproduction.domain.ReproductiveEvent;
import com.devmaster.goatfarm.reproduction.application.model.PregnancyDiagnosisAlertSnapshot;
import com.devmaster.goatfarm.reproduction.enums.ReproductiveEventType;
import com.devmaster.goatfarm.application.pagination.PageQuery;
import com.devmaster.goatfarm.application.pagination.PageResult;

import java.time.LocalDate;
import java.util.Optional;

public interface ReproductiveEventPersistencePort {
    ReproductiveEvent save(ReproductiveEvent entity);

    PageResult<ReproductiveEvent> findAllByFarmIdAndGoatId(Long farmId, String goatId, PageQuery pageQuery);

    Optional<ReproductiveEvent> findLatestCoverageByFarmIdAndGoatIdOnOrBefore(Long farmId, String goatId, LocalDate date);

    Optional<ReproductiveEvent> findByIdAndFarmIdAndGoatId(Long eventId, Long farmId, String goatId);

    Optional<ReproductiveEvent> findLatestEffectiveCoverageByFarmIdAndGoatIdOnOrBefore(Long farmId, String goatId, LocalDate date);

    Optional<ReproductiveEvent> findLatestPregnancyCheckByFarmIdAndGoatIdOnOrBefore(Long farmId, String goatId, LocalDate date);

    Optional<ReproductiveEvent> findCoverageCorrectionByRelatedEventId(Long farmId, String goatId, Long relatedEventId);

    Optional<ReproductiveEvent> findLatestByFarmIdAndGoatIdAndEventType(Long farmId, String goatId, ReproductiveEventType eventType);

    PageResult<PregnancyDiagnosisAlertSnapshot> findPendingPregnancyDiagnosisAlerts(
            Long farmId,
            LocalDate referenceDate,
            int minDays,
            PageQuery pageQuery
    );
}
