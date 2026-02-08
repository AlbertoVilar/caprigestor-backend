package com.devmaster.goatfarm.reproduction.application.ports.out;

import com.devmaster.goatfarm.reproduction.persistence.entity.ReproductiveEvent;
import com.devmaster.goatfarm.reproduction.persistence.projection.PregnancyDiagnosisAlertProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Optional;

public interface ReproductiveEventPersistencePort {
    ReproductiveEvent save(ReproductiveEvent entity);

    Page<ReproductiveEvent> findAllByFarmIdAndGoatId(Long farmId, String goatId, Pageable pageable);

    Optional<ReproductiveEvent> findLatestCoverageByFarmIdAndGoatIdOnOrBefore(Long farmId, String goatId, LocalDate date);

    Optional<ReproductiveEvent> findByIdAndFarmIdAndGoatId(Long eventId, Long farmId, String goatId);

    Optional<ReproductiveEvent> findLatestEffectiveCoverageByFarmIdAndGoatIdOnOrBefore(Long farmId, String goatId, LocalDate date);

    Optional<ReproductiveEvent> findLatestPregnancyCheckByFarmIdAndGoatIdOnOrBefore(Long farmId, String goatId, LocalDate date);

    Optional<ReproductiveEvent> findCoverageCorrectionByRelatedEventId(Long farmId, String goatId, Long relatedEventId);

    Page<PregnancyDiagnosisAlertProjection> findPendingPregnancyDiagnosisAlerts(
            Long farmId,
            LocalDate referenceDate,
            int minDays,
            Pageable pageable
    );
}
