package com.devmaster.goatfarm.reproduction.persistence.adapter;

import com.devmaster.goatfarm.reproduction.application.ports.out.ReproductiveEventPersistencePort;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReference;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReferenceQueryPort;
import com.devmaster.goatfarm.reproduction.enums.ReproductiveEventType;
import com.devmaster.goatfarm.reproduction.persistence.entity.ReproductiveEvent;
import com.devmaster.goatfarm.reproduction.persistence.projection.PregnancyDiagnosisAlertProjection;
import com.devmaster.goatfarm.reproduction.persistence.repository.ReproductiveEventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

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
    private final GoatReferenceQueryPort goatReferenceQueryPort;

    public ReproductiveEventPersistenceAdapter(ReproductiveEventRepository repository) {
        this(repository, null);
    }

    @Autowired
    public ReproductiveEventPersistenceAdapter(ReproductiveEventRepository repository,
                                               GoatReferenceQueryPort goatReferenceQueryPort) {
        this.repository = repository;
        this.goatReferenceQueryPort = goatReferenceQueryPort;
    }

    @Override
    public ReproductiveEvent save(ReproductiveEvent entity) {
        populateTechnicalIdentity(entity);
        return repository.save(entity);
    }

    @Override
    public Page<ReproductiveEvent> findAllByFarmIdAndGoatId(Long farmId, String goatId, Pageable pageable) {
        Optional<Long> technicalId = technicalId(farmId, goatId);
        if (technicalId.isPresent()) {
            Page<ReproductiveEvent> technical = repository.findAllByFarmIdAndGoatTechnicalIdOrderByEventDateDescIdDesc(
                    farmId, technicalId.get(), pageable);
            if (technical.hasContent()) {
                return technical;
            }
        }
        return repository.findAllByFarmIdAndGoatIdOrderByEventDateDescIdDesc(farmId, goatId, pageable);
    }

    @Override
    public Optional<ReproductiveEvent> findByIdAndFarmIdAndGoatId(Long eventId, Long farmId, String goatId) {
        Optional<Long> technicalId = technicalId(farmId, goatId);
        if (technicalId.isPresent()) {
            Optional<ReproductiveEvent> technical = repository.findByIdAndFarmIdAndGoatTechnicalId(eventId, farmId, technicalId.get());
            if (technical.isPresent()) {
                return technical;
            }
        }
        return repository.findByIdAndFarmIdAndGoatId(eventId, farmId, goatId);
    }

    @Override
    public Optional<ReproductiveEvent> findLatestCoverageByFarmIdAndGoatIdOnOrBefore(Long farmId, String goatId, LocalDate date) {
        Optional<Long> technicalId = technicalId(farmId, goatId);
        if (technicalId.isPresent()) {
            Optional<ReproductiveEvent> technical = repository.findTopByFarmIdAndGoatTechnicalIdAndEventTypeAndEventDateLessThanEqualOrderByEventDateDescIdDesc(
                    farmId, technicalId.get(), ReproductiveEventType.COVERAGE, date);
            if (technical.isPresent()) {
                return technical;
            }
        }
        return repository.findTopByFarmIdAndGoatIdAndEventTypeAndEventDateLessThanEqualOrderByEventDateDescIdDesc(
                farmId,
                goatId,
                ReproductiveEventType.COVERAGE,
                date
        );
    }

    @Override
    public Optional<ReproductiveEvent> findLatestEffectiveCoverageByFarmIdAndGoatIdOnOrBefore(Long farmId, String goatId, LocalDate date) {
        Optional<Long> technicalId = technicalId(farmId, goatId);
        if (technicalId.isPresent()) {
            Optional<ReproductiveEvent> technical = repository.findLatestEffectiveCoverageOnOrBeforeByTechnicalId(
                    farmId, technicalId.get(), date, PageRequest.of(0, 1)).stream().findFirst();
            if (technical.isPresent()) {
                return technical;
            }
        }
        return repository.findLatestEffectiveCoverageOnOrBefore(
                farmId,
                goatId,
                date,
                PageRequest.of(0, 1)
        ).stream().findFirst();
    }

    @Override
    public Optional<ReproductiveEvent> findLatestPregnancyCheckByFarmIdAndGoatIdOnOrBefore(Long farmId, String goatId, LocalDate date) {
        Optional<Long> technicalId = technicalId(farmId, goatId);
        if (technicalId.isPresent()) {
            Optional<ReproductiveEvent> technical = repository.findTopByFarmIdAndGoatTechnicalIdAndEventTypeAndEventDateLessThanEqualOrderByEventDateDescIdDesc(
                    farmId, technicalId.get(), ReproductiveEventType.PREGNANCY_CHECK, date);
            if (technical.isPresent()) {
                return technical;
            }
        }
        return repository.findTopByFarmIdAndGoatIdAndEventTypeAndEventDateLessThanEqualOrderByEventDateDescIdDesc(
                farmId,
                goatId,
                ReproductiveEventType.PREGNANCY_CHECK,
                date
        );
    }

    @Override
    public Optional<ReproductiveEvent> findCoverageCorrectionByRelatedEventId(Long farmId, String goatId, Long relatedEventId) {
        Optional<Long> technicalId = technicalId(farmId, goatId);
        if (technicalId.isPresent()) {
            Optional<ReproductiveEvent> technical = repository.findTopByFarmIdAndGoatTechnicalIdAndEventTypeAndRelatedEventIdOrderByEventDateDescIdDesc(
                    farmId, technicalId.get(), ReproductiveEventType.COVERAGE_CORRECTION, relatedEventId);
            if (technical.isPresent()) {
                return technical;
            }
        }
        return repository.findTopByFarmIdAndGoatIdAndEventTypeAndRelatedEventIdOrderByEventDateDescIdDesc(
                farmId,
                goatId,
                ReproductiveEventType.COVERAGE_CORRECTION,
                relatedEventId
        );
    }

    @Override
    public Optional<ReproductiveEvent> findLatestByFarmIdAndGoatIdAndEventType(Long farmId, String goatId, ReproductiveEventType eventType) {
        Optional<Long> technicalId = technicalId(farmId, goatId);
        if (technicalId.isPresent()) {
            Optional<ReproductiveEvent> technical = repository.findTopByFarmIdAndGoatTechnicalIdAndEventTypeOrderByEventDateDescIdDesc(
                    farmId, technicalId.get(), eventType);
            if (technical.isPresent()) {
                return technical;
            }
        }
        return repository.findTopByFarmIdAndGoatIdAndEventTypeOrderByEventDateDescIdDesc(
                farmId,
                goatId,
                eventType
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

    private Optional<Long> technicalId(Long farmId, String registrationNumber) {
        if (goatReferenceQueryPort == null || registrationNumber == null) {
            return Optional.empty();
        }
        return goatReferenceQueryPort.findReferenceByRegistrationNumberAndFarmId(registrationNumber, farmId)
                .map(GoatReference::id)
                .map(id -> id.value());
    }

    private void populateTechnicalIdentity(ReproductiveEvent entity) {
        if (entity.getGoatTechnicalId() == null) {
            technicalId(entity.getFarmId(), entity.getGoatId()).ifPresent(entity::setGoatTechnicalId);
        }
    }
}
