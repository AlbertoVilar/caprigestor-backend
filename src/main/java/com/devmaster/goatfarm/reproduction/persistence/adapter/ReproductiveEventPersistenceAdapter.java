package com.devmaster.goatfarm.reproduction.persistence.adapter;

import com.devmaster.goatfarm.reproduction.application.ports.out.ReproductiveEventPersistencePort;
import com.devmaster.goatfarm.reproduction.enums.ReproductiveEventType;
import com.devmaster.goatfarm.reproduction.persistence.entity.ReproductiveEvent;
import com.devmaster.goatfarm.reproduction.persistence.repository.ReproductiveEventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

@Component
public class ReproductiveEventPersistenceAdapter implements ReproductiveEventPersistencePort {

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
}
