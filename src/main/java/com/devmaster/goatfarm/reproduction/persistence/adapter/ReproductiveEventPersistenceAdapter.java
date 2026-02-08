package com.devmaster.goatfarm.reproduction.persistence.adapter;

import com.devmaster.goatfarm.reproduction.application.ports.out.ReproductiveEventPersistencePort;
import com.devmaster.goatfarm.reproduction.enums.ReproductiveEventType;
import com.devmaster.goatfarm.reproduction.persistence.entity.ReproductiveEvent;
import com.devmaster.goatfarm.reproduction.persistence.projection.PregnancyDiagnosisAlertProjection;
import com.devmaster.goatfarm.reproduction.persistence.repository.ReproductiveEventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class ReproductiveEventPersistenceAdapter implements ReproductiveEventPersistencePort {

    private static final List<String> BLOCKING_DIAGNOSIS_EVENT_TYPES = List.of(
            ReproductiveEventType.PREGNANCY_CHECK.name(),
            ReproductiveEventType.PREGNANCY_CLOSE.name()
    );

    private final ReproductiveEventRepository repository;

    public ReproductiveEventPersistenceAdapter(ReproductiveEventRepository repository) {
        this.repository = repository;
    }

    @Override
    public ReproductiveEvent save(ReproductiveEvent entity) {
        return repository.save(entity);
    }

    @Override
    public Page<ReproductiveEvent> findAllByFarmIdAndGoatId(Long farmId, String goatId, Pageable pageable) {
        return repository.findAllByFarmIdAndGoatIdOrderByEventDateDescIdDesc(farmId, goatId, pageable);
    }

    @Override
    public Optional<ReproductiveEvent> findByIdAndFarmIdAndGoatId(Long eventId, Long farmId, String goatId) {
        return repository.findByIdAndFarmIdAndGoatId(eventId, farmId, goatId);
    }

    @Override
    public Optional<ReproductiveEvent> findLatestCoverageByFarmIdAndGoatIdOnOrBefore(Long farmId, String goatId, LocalDate date) {
        return repository.findTopByFarmIdAndGoatIdAndEventTypeAndEventDateLessThanEqualOrderByEventDateDescIdDesc(
                farmId,
                goatId,
                ReproductiveEventType.COVERAGE,
                date
        );
    }

    @Override
    public Optional<ReproductiveEvent> findLatestEffectiveCoverageByFarmIdAndGoatIdOnOrBefore(Long farmId, String goatId, LocalDate date) {
        return repository.findLatestEffectiveCoverageOnOrBefore(farmId, goatId, date);
    }

    @Override
    public Optional<ReproductiveEvent> findLatestPregnancyCheckByFarmIdAndGoatIdOnOrBefore(Long farmId, String goatId, LocalDate date) {
        return repository.findTopByFarmIdAndGoatIdAndEventTypeAndEventDateLessThanEqualOrderByEventDateDescIdDesc(
                farmId,
                goatId,
                ReproductiveEventType.PREGNANCY_CHECK,
                date
        );
    }

    @Override
    public Optional<ReproductiveEvent> findCoverageCorrectionByRelatedEventId(Long farmId, String goatId, Long relatedEventId) {
        return repository.findTopByFarmIdAndGoatIdAndEventTypeAndRelatedEventIdOrderByEventDateDescIdDesc(
                farmId,
                goatId,
                ReproductiveEventType.COVERAGE_CORRECTION,
                relatedEventId
        );
    }

    @Override
    public Page<PregnancyDiagnosisAlertProjection> findPendingPregnancyDiagnosisAlerts(
            Long farmId,
            LocalDate referenceDate,
            int minDays,
            Pageable pageable
    ) {
        LocalDate eligibleThresholdDate = referenceDate.minusDays(minDays);
        return repository.findPendingPregnancyDiagnosisAlerts(
                farmId,
                referenceDate,
                eligibleThresholdDate,
                BLOCKING_DIAGNOSIS_EVENT_TYPES,
                pageable
        );
    }
}
