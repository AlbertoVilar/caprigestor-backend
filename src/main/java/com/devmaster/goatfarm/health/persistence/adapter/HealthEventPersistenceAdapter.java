package com.devmaster.goatfarm.health.persistence.adapter;

import com.devmaster.goatfarm.health.application.ports.out.HealthEventPersistencePort;
import com.devmaster.goatfarm.health.application.model.HealthEventRecord;
import com.devmaster.goatfarm.health.application.model.HealthEventWindow;
import com.devmaster.goatfarm.application.pagination.PageQuery;
import com.devmaster.goatfarm.application.pagination.PageResult;
import com.devmaster.goatfarm.application.pagination.SortDirection;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReference;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReferenceQueryPort;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.health.domain.enums.HealthEventStatus;
import com.devmaster.goatfarm.health.domain.enums.HealthEventType;
import com.devmaster.goatfarm.health.persistence.entity.HealthEvent;
import com.devmaster.goatfarm.health.persistence.repository.HealthEventRepository;
import com.devmaster.goatfarm.health.persistence.mapper.HealthEventPersistenceMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class HealthEventPersistenceAdapter implements HealthEventPersistencePort {

    private final HealthEventRepository repository;
    private final GoatReferenceQueryPort goatReferenceQueryPort;
    private final HealthEventPersistenceMapper mapper;

    public HealthEventPersistenceAdapter(HealthEventRepository repository,
                                         HealthEventPersistenceMapper mapper) {
        this(repository, null, mapper);
    }

    @Autowired
    public HealthEventPersistenceAdapter(HealthEventRepository repository,
                                         GoatReferenceQueryPort goatReferenceQueryPort,
                                         HealthEventPersistenceMapper mapper) {
        this.repository = repository;
        this.goatReferenceQueryPort = goatReferenceQueryPort;
        this.mapper = mapper;
    }

    @Override
    public HealthEventRecord save(HealthEventRecord healthEvent) {
        HealthEvent entity = mapper.toEntity(healthEvent);
        populateTechnicalIdentity(entity);
        return mapper.toModel(repository.save(entity));
    }

    @Override
    public Optional<HealthEventRecord> findByIdAndFarmIdAndGoatId(Long id, Long farmId, String goatId) {
        Optional<Long> technicalId = technicalId(farmId, goatId);
        if (technicalId.isPresent()) {
            Optional<HealthEvent> technical = repository.findByIdAndFarmIdAndGoatTechnicalId(id, farmId, technicalId.get());
            if (technical.isPresent()) {
                return technical.map(mapper::toModel);
            }
        }
        return repository.findByIdAndFarmIdAndGoatId(id, farmId, goatId).map(mapper::toModel);
    }

    @Override
    public PageResult<HealthEventRecord> findByFarmIdAndGoatId(
            Long farmId,
            String goatId,
            LocalDate from,
            LocalDate to,
            HealthEventType type,
            HealthEventStatus status,
            PageQuery pageQuery
    ) {
        Pageable pageable = toPageable(pageQuery);
        Optional<Long> technicalId = technicalId(farmId, goatId);
        if (technicalId.isPresent()) {
            Page<HealthEvent> technical = repository.searchByGoatTechnicalId(farmId, technicalId.get(), from, to, type, status, pageable);
            if (technical.hasContent()) {
                return toPageResult(technical);
            }
        }
        return toPageResult(repository.searchByGoat(farmId, goatId, from, to, type, status, pageable));
    }

    @Override
    public PageResult<HealthEventRecord> findByFarmIdAndPeriod(
            Long farmId,
            LocalDate from,
            LocalDate to,
            HealthEventType type,
            HealthEventStatus status,
            PageQuery pageQuery
    ) {
        Pageable pageable = toPageable(pageQuery);
        return toPageResult(repository.searchCalendar(farmId, from, to, type, status, pageable));
    }

    @Override
    public HealthEventWindow findNextScheduledEvents(Long farmId, LocalDate from, LocalDate to,
                                                     HealthEventType type, HealthEventStatus status, int limit) {
        Page<HealthEvent> page = repository.searchCalendar(
                farmId,
                from,
                to,
                type,
                status,
                PageRequest.of(0, limit, Sort.by(Sort.Order.asc("scheduledDate")))
        );
        return new HealthEventWindow(
                page.getContent().stream().map(mapper::toModel).toList(),
                page.getTotalElements()
        );
    }

    @Override
    public List<HealthEventRecord> findPerformedWithWithdrawalByFarmIdAndGoatId(Long farmId, String goatId) {
        Optional<Long> technicalId = technicalId(farmId, goatId);
        if (technicalId.isPresent()) {
            List<HealthEvent> technical = repository.findPerformedWithWithdrawalByFarmIdAndGoatTechnicalId(farmId, technicalId.get());
            if (!technical.isEmpty()) {
                return technical.stream().map(mapper::toModel).toList();
            }
        }
        return repository.findPerformedWithWithdrawalByFarmIdAndGoatId(farmId, goatId).stream().map(mapper::toModel).toList();
    }

    @Override
    public List<HealthEventRecord> findPerformedWithWithdrawalByFarmId(Long farmId) {
        return repository.findPerformedWithWithdrawalByFarmId(farmId).stream().map(mapper::toModel).toList();
    }

    @Override
    public List<HealthEventRecord> findPerformedWithWithdrawalByGoatTechnicalId(GoatId goatId) {
        if (goatId == null) {
            return List.of();
        }
        return repository.findPerformedWithWithdrawalByGoatTechnicalId(goatId.value())
                .stream().map(mapper::toModel).toList();
    }

    private Optional<Long> technicalId(Long farmId, String registrationNumber) {
        if (goatReferenceQueryPort == null || registrationNumber == null) {
            return Optional.empty();
        }
        return goatReferenceQueryPort.findReferenceByRegistrationNumberAndFarmId(registrationNumber, farmId)
                .map(GoatReference::id)
                .map(id -> id.value());
    }

    private void populateTechnicalIdentity(HealthEvent entity) {
        if (entity.getGoatTechnicalId() == null) {
            technicalId(entity.getFarmId(), entity.getGoatId()).ifPresent(entity::setGoatTechnicalId);
        }
    }

    private Pageable toPageable(PageQuery query) {
        var orders = query.sort().stream()
                .map(spec -> new Sort.Order(
                        spec.direction() == SortDirection.ASC ? Sort.Direction.ASC : Sort.Direction.DESC,
                        spec.field()))
                .toList();
        return PageRequest.of(query.page(), query.size(), Sort.by(orders));
    }

    private PageResult<HealthEventRecord> toPageResult(Page<HealthEvent> page) {
        return new PageResult<>(
                page.getContent().stream().map(mapper::toModel).toList(),
                page.getTotalElements(),
                page.getNumber(),
                page.getSize()
        );
    }
}
