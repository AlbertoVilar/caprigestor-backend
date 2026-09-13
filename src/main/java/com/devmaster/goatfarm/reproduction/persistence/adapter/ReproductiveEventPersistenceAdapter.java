package com.devmaster.goatfarm.reproduction.persistence.adapter;

import com.devmaster.goatfarm.reproduction.application.ports.out.ReproductiveEventPersistencePort;
import com.devmaster.goatfarm.reproduction.application.model.PregnancyDiagnosisAlertSnapshot;
import com.devmaster.goatfarm.reproduction.domain.ReproductiveEvent;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReference;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReferenceQueryPort;
import com.devmaster.goatfarm.reproduction.enums.ReproductiveEventType;
import com.devmaster.goatfarm.reproduction.persistence.repository.ReproductiveEventRepository;
import com.devmaster.goatfarm.reproduction.persistence.mapper.ReproductiveEventPersistenceMapper;
import com.devmaster.goatfarm.reproduction.persistence.entity.ReproductiveEventEntity;
import com.devmaster.goatfarm.reproduction.persistence.projection.PregnancyDiagnosisAlertProjection;
import com.devmaster.goatfarm.application.pagination.PageQuery;
import com.devmaster.goatfarm.application.pagination.PageResult;
import com.devmaster.goatfarm.application.pagination.SortDirection;
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
    public PageResult<ReproductiveEvent> findAllByFarmIdAndGoatId(Long farmId, String goatId, PageQuery pageQuery) {
        Pageable pageable = toPageable(pageQuery);
        Optional<Long> technicalId = technicalId(farmId, goatId);
        if (technicalId.isPresent()) {
            Page<ReproductiveEventEntity> technicalPage = repository.findAllByFarmIdAndGoatTechnicalIdOrderByEventDateDescIdDesc(
                    farmId, technicalId.get(), pageable);
            Page<ReproductiveEvent> technical = technicalPage.map(mapper::toDomain);
            if (technical.hasContent()) {
                return toPageResult(technicalPage);
            }
        }
        return toPageResult(repository.findAllByFarmIdAndGoatIdOrderByEventDateDescIdDesc(farmId, goatId, pageable));
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
    public PageResult<PregnancyDiagnosisAlertSnapshot> findPendingPregnancyDiagnosisAlerts(
            Long farmId,
            LocalDate referenceDate,
            int minDays,
            PageQuery pageQuery
    ) {
        LocalDate eligibleThresholdDate = referenceDate.minusDays(minDays);
        return toAlertPageResult(repository.findPendingPregnancyDiagnosisAlerts(
                farmId,
                referenceDate,
                eligibleThresholdDate,
                BLOCKING_DIAGNOSIS_EVENT_TYPES,
                toPageable(pageQuery)
        ));
    }

    private Optional<Long> technicalId(Long farmId, String registrationNumber) {
        if (goatReferenceQueryPort == null || registrationNumber == null) {
            return Optional.empty();
        }
        return goatReferenceQueryPort.findReferenceByRegistrationNumberAndFarmId(registrationNumber, farmId)
                .map(GoatReference::id)
                .map(id -> id.value());
    }

    private Pageable toPageable(PageQuery query) {
        if (query == null) {
            return PageRequest.of(0, 10);
        }
        var orders = query.sort().stream()
                .map(spec -> new org.springframework.data.domain.Sort.Order(
                        spec.direction() == SortDirection.ASC ? org.springframework.data.domain.Sort.Direction.ASC : org.springframework.data.domain.Sort.Direction.DESC,
                        spec.field()))
                .toList();
        return PageRequest.of(query.page(), query.size(), org.springframework.data.domain.Sort.by(orders));
    }

    private PageResult<ReproductiveEvent> toPageResult(Page<ReproductiveEventEntity> page) {
        return new PageResult<>(page.getContent().stream().map(mapper::toDomain).toList(),
                page.getTotalElements(), page.getNumber(), page.getSize());
    }

    private PageResult<PregnancyDiagnosisAlertSnapshot> toAlertPageResult(Page<PregnancyDiagnosisAlertProjection> page) {
        return new PageResult<>(page.getContent().stream()
                .map(p -> new PregnancyDiagnosisAlertSnapshot(p.getGoatTechnicalId(), p.getGoatId(),
                        p.getLastCoverageDate(), p.getLastCheckDate(), p.getEligibleDate()))
                .toList(), page.getTotalElements(), page.getNumber(), page.getSize());
    }

}
