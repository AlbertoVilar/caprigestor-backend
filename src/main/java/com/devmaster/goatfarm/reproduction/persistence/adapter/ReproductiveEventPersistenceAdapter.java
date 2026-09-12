package com.devmaster.goatfarm.reproduction.persistence.adapter;

import com.devmaster.goatfarm.reproduction.application.ports.out.ReproductiveEventPersistencePort;
import com.devmaster.goatfarm.reproduction.application.model.PregnancyDiagnosisAlertSnapshot;
import com.devmaster.goatfarm.reproduction.domain.ReproductiveEvent;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReference;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReferenceQueryPort;
import com.devmaster.goatfarm.reproduction.enums.ReproductiveEventType;
import com.devmaster.goatfarm.reproduction.persistence.repository.ReproductiveEventRepository;
import com.devmaster.goatfarm.reproduction.persistence.mapper.ReproductiveEventPersistenceMapper;
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
    private final ReproductiveEventPersistenceMapper mapper;

    public ReproductiveEventPersistenceAdapter(ReproductiveEventRepository repository) {
        this(repository, null, new ReproductiveEventPersistenceMapper());
    }

    public ReproductiveEventPersistenceAdapter(ReproductiveEventRepository repository,
                                               GoatReferenceQueryPort goatReferenceQueryPort) {
        this(repository, goatReferenceQueryPort, new ReproductiveEventPersistenceMapper());
    }

    @Autowired
    public ReproductiveEventPersistenceAdapter(ReproductiveEventRepository repository,
                                               GoatReferenceQueryPort goatReferenceQueryPort,
                                               ReproductiveEventPersistenceMapper mapper) {
        this.repository = repository;
        this.goatReferenceQueryPort = goatReferenceQueryPort;
        this.mapper = mapper;
    }

    @Override
    public ReproductiveEvent save(ReproductiveEvent entity) {
        var persistence = mapper.toEntity(entity);
        if (persistence.getGoatTechnicalId() == null) {
            technicalId(entity.getFarmId(), entity.getGoatId()).ifPresent(persistence::setGoatTechnicalId);
        }
        return mapper.toDomain(repository.save(persistence));
    }

    @Override
    public Page<ReproductiveEvent> findAllByFarmIdAndGoatId(Long farmId, String goatId, Pageable pageable) {
        Optional<Long> technicalId = technicalId(farmId, goatId);
        if (technicalId.isPresent()) {
            Page<ReproductiveEvent> technical = repository.findAllByFarmIdAndGoatTechnicalIdOrderByEventDateDescIdDesc(
                    farmId, technicalId.get(), pageable).map(mapper::toDomain);
            if (technical.hasContent()) {
                return technical;
            }
        }
        return repository.findAllByFarmIdAndGoatIdOrderByEventDateDescIdDesc(farmId, goatId, pageable).map(mapper::toDomain);
    }

    @Override
    public Optional<ReproductiveEvent> findByIdAndFarmIdAndGoatId(Long eventId, Long farmId, String goatId) {
        Optional<Long> technicalId = technicalId(farmId, goatId);
        if (technicalId.isPresent()) {
            Optional<ReproductiveEvent> technical = repository.findByIdAndFarmIdAndGoatTechnicalId(eventId, farmId, technicalId.get()).map(mapper::toDomain);
            if (technical.isPresent()) {
                return technical;
            }
        }
        return repository.findByIdAndFarmIdAndGoatId(eventId, farmId, goatId).map(mapper::toDomain);
    }

    @Override
    public Optional<ReproductiveEvent> findLatestCoverageByFarmIdAndGoatIdOnOrBefore(Long farmId, String goatId, LocalDate date) {
        Optional<Long> technicalId = technicalId(farmId, goatId);
        if (technicalId.isPresent()) {
            Optional<ReproductiveEvent> technical = repository.findTopByFarmIdAndGoatTechnicalIdAndEventTypeAndEventDateLessThanEqualOrderByEventDateDescIdDesc(
                    farmId, technicalId.get(), ReproductiveEventType.COVERAGE, date).map(mapper::toDomain);
            if (technical.isPresent()) {
                return technical;
            }
        }
        return repository.findTopByFarmIdAndGoatIdAndEventTypeAndEventDateLessThanEqualOrderByEventDateDescIdDesc(
                farmId,
                goatId,
                ReproductiveEventType.COVERAGE,
                date
        ).map(mapper::toDomain);
    }

    @Override
    public Optional<ReproductiveEvent> findLatestEffectiveCoverageByFarmIdAndGoatIdOnOrBefore(Long farmId, String goatId, LocalDate date) {
        Optional<Long> technicalId = technicalId(farmId, goatId);
        if (technicalId.isPresent()) {
            Optional<ReproductiveEvent> technical = repository.findLatestEffectiveCoverageOnOrBeforeByTechnicalId(
                    farmId, technicalId.get(), date, PageRequest.of(0, 1)).stream().map(mapper::toDomain).findFirst();
            if (technical.isPresent()) {
                return technical;
            }
        }
        return repository.findLatestEffectiveCoverageOnOrBefore(
                farmId,
                goatId,
                date,
                PageRequest.of(0, 1)
        ).stream().map(mapper::toDomain).findFirst();
    }

    @Override
    public Optional<ReproductiveEvent> findLatestPregnancyCheckByFarmIdAndGoatIdOnOrBefore(Long farmId, String goatId, LocalDate date) {
        Optional<Long> technicalId = technicalId(farmId, goatId);
        if (technicalId.isPresent()) {
            Optional<ReproductiveEvent> technical = repository.findTopByFarmIdAndGoatTechnicalIdAndEventTypeAndEventDateLessThanEqualOrderByEventDateDescIdDesc(
                    farmId, technicalId.get(), ReproductiveEventType.PREGNANCY_CHECK, date).map(mapper::toDomain);
            if (technical.isPresent()) {
                return technical;
            }
        }
        return repository.findTopByFarmIdAndGoatIdAndEventTypeAndEventDateLessThanEqualOrderByEventDateDescIdDesc(
                farmId,
                goatId,
                ReproductiveEventType.PREGNANCY_CHECK,
                date
        ).map(mapper::toDomain);
    }

    @Override
    public Optional<ReproductiveEvent> findCoverageCorrectionByRelatedEventId(Long farmId, String goatId, Long relatedEventId) {
        Optional<Long> technicalId = technicalId(farmId, goatId);
        if (technicalId.isPresent()) {
            Optional<ReproductiveEvent> technical = repository.findTopByFarmIdAndGoatTechnicalIdAndEventTypeAndRelatedEventIdOrderByEventDateDescIdDesc(
                    farmId, technicalId.get(), ReproductiveEventType.COVERAGE_CORRECTION, relatedEventId).map(mapper::toDomain);
            if (technical.isPresent()) {
                return technical;
            }
        }
        return repository.findTopByFarmIdAndGoatIdAndEventTypeAndRelatedEventIdOrderByEventDateDescIdDesc(
                farmId,
                goatId,
                ReproductiveEventType.COVERAGE_CORRECTION,
                relatedEventId
        ).map(mapper::toDomain);
    }

    @Override
    public Optional<ReproductiveEvent> findLatestByFarmIdAndGoatIdAndEventType(Long farmId, String goatId, ReproductiveEventType eventType) {
        Optional<Long> technicalId = technicalId(farmId, goatId);
        if (technicalId.isPresent()) {
            Optional<ReproductiveEvent> technical = repository.findTopByFarmIdAndGoatTechnicalIdAndEventTypeOrderByEventDateDescIdDesc(
                    farmId, technicalId.get(), eventType).map(mapper::toDomain);
            if (technical.isPresent()) {
                return technical;
            }
        }
        return repository.findTopByFarmIdAndGoatIdAndEventTypeOrderByEventDateDescIdDesc(
                farmId,
                goatId,
                eventType
        ).map(mapper::toDomain);
    }

    @Override
    public Page<PregnancyDiagnosisAlertSnapshot> findPendingPregnancyDiagnosisAlerts(
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
        ).map(p -> new PregnancyDiagnosisAlertSnapshot(p.getGoatTechnicalId(), p.getGoatId(), p.getLastCoverageDate(), p.getLastCheckDate(), p.getEligibleDate()));
    }

    private Optional<Long> technicalId(Long farmId, String registrationNumber) {
        if (goatReferenceQueryPort == null || registrationNumber == null) {
            return Optional.empty();
        }
        return goatReferenceQueryPort.findReferenceByRegistrationNumberAndFarmId(registrationNumber, farmId)
                .map(GoatReference::id)
                .map(id -> id.value());
    }

}
