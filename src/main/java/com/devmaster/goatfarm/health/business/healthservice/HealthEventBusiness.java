package com.devmaster.goatfarm.health.business.healthservice;

import com.devmaster.goatfarm.application.core.business.common.EntityFinder;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.goat.application.ports.out.GoatPersistencePort;
import com.devmaster.goatfarm.health.application.ports.in.HealthEventCommandUseCase;
import com.devmaster.goatfarm.health.application.ports.in.HealthEventQueryUseCase;
import com.devmaster.goatfarm.health.application.ports.out.HealthEventPersistencePort;
import com.devmaster.goatfarm.health.business.bo.HealthEventCancelRequestVO;
import com.devmaster.goatfarm.health.business.bo.HealthEventCreateRequestVO;
import com.devmaster.goatfarm.health.business.bo.HealthEventDoneRequestVO;
import com.devmaster.goatfarm.health.business.bo.HealthEventResponseVO;
import com.devmaster.goatfarm.health.business.bo.HealthEventUpdateRequestVO;
import com.devmaster.goatfarm.health.business.mapper.HealthEventBusinessMapper;
import com.devmaster.goatfarm.health.domain.enums.HealthEventStatus;
import com.devmaster.goatfarm.health.domain.enums.HealthEventType;
import com.devmaster.goatfarm.health.persistence.entity.HealthEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class HealthEventBusiness implements HealthEventCommandUseCase, HealthEventQueryUseCase {

    private final HealthEventPersistencePort persistencePort;
    private final GoatPersistencePort goatPersistencePort;
    private final HealthEventBusinessMapper mapper;
    private final EntityFinder entityFinder;

    public HealthEventBusiness(
            HealthEventPersistencePort persistencePort,
            GoatPersistencePort goatPersistencePort,
            HealthEventBusinessMapper mapper,
            EntityFinder entityFinder
    ) {
        this.persistencePort = persistencePort;
        this.goatPersistencePort = goatPersistencePort;
        this.mapper = mapper;
        this.entityFinder = entityFinder;
    }

    @Override
    @Transactional
    public HealthEventResponseVO create(Long farmId, String goatId, HealthEventCreateRequestVO request) {
        // Controle de acesso deve ser feito no Controller via OwnershipService.canManageFarm(farmId)

        entityFinder.findOrThrow(
                () -> goatPersistencePort.findByIdAndFarmId(goatId, farmId),
                "Cabra não encontrada no capril informado. goatId=" + goatId + ", farmId=" + farmId
        );

        var entity = mapper.toEntity(request);
        entity.setFarmId(farmId);
        entity.setGoatId(goatId);

        // Invariável de domínio: criação é sempre AGENDADO
        entity.setStatus(HealthEventStatus.AGENDADO);

        var saved = persistencePort.save(entity);
        return mapper.toResponseVO(saved);
    }

    @Override
    @Transactional
    public HealthEventResponseVO update(Long farmId, String goatId, Long eventId, HealthEventUpdateRequestVO request) {
        var healthEvent = findEventOrThrow(farmId, goatId, eventId);

        // Regra de negócio: só pode editar enquanto estiver AGENDADO
        if (healthEvent.getStatus() != HealthEventStatus.AGENDADO) {
            throw new BusinessRuleException("Não é permitido alterar um evento de saúde já realizado ou cancelado.");
        }

        mapper.updateEntity(healthEvent, request);

        var saved = persistencePort.save(healthEvent);
        return mapper.toResponseVO(saved);
    }

    @Override
    @Transactional
    public HealthEventResponseVO markAsDone(Long farmId, String goatId, Long eventId, HealthEventDoneRequestVO request) {
        var healthEvent = findEventOrThrow(farmId, goatId, eventId);

        if (healthEvent.getStatus() != HealthEventStatus.AGENDADO) {
            throw new BusinessRuleException("Apenas eventos agendados podem ser marcados como realizados.");
        }

        // Aplicar dados do request
        healthEvent.setPerformedAt(request.performedAt());
        healthEvent.setResponsible(request.responsible());
        healthEvent.setNotes(request.notes());

        healthEvent.setStatus(HealthEventStatus.REALIZADO);

        var saved = persistencePort.save(healthEvent);
        return mapper.toResponseVO(saved);
    }

    @Override
    @Transactional
    public HealthEventResponseVO cancel(Long farmId, String goatId, Long eventId, HealthEventCancelRequestVO request) {
        var healthEvent = findEventOrThrow(farmId, goatId, eventId);

        if (healthEvent.getStatus() != HealthEventStatus.AGENDADO) {
            throw new BusinessRuleException("Apenas eventos agendados podem ser cancelados.");
        }

        // Aplicar motivo do cancelamento
        healthEvent.setNotes(request.notes());

        healthEvent.setStatus(HealthEventStatus.CANCELADO);

        var saved = persistencePort.save(healthEvent);
        return mapper.toResponseVO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public HealthEventResponseVO getById(Long farmId, String goatId, Long eventId) {
        var healthEvent = findEventOrThrow(farmId, goatId, eventId);
        return mapper.toResponseVO(healthEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HealthEventResponseVO> listCalendar(
            Long farmId,
            LocalDate from,
            LocalDate to,
            HealthEventType type,
            HealthEventStatus status,
            Pageable pageable
    ) {
        var healthEvents = persistencePort.findByFarmIdAndPeriod(
                farmId,
                from,
                to,
                type,
                status,
                pageable
        );

        return healthEvents.map(mapper::toResponseVO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HealthEventResponseVO> listByGoat(
            Long farmId,
            String goatId,
            LocalDate from,
            LocalDate to,
            HealthEventType type,
            HealthEventStatus status,
            Pageable pageable
    ) {
        entityFinder.findOrThrow(
                () -> goatPersistencePort.findByIdAndFarmId(goatId, farmId),
                "Cabra não encontrada no capril informado. goatId=" + goatId + ", farmId=" + farmId
        );

        var healthEvents = persistencePort.findByFarmIdAndGoatId(
                farmId,
                goatId,
                from,
                to,
                type,
                status,
                pageable
        );

        return healthEvents.map(mapper::toResponseVO);
    }

    private HealthEvent findEventOrThrow(Long farmId, String goatId, Long eventId) {
        return entityFinder.findOrThrow(
                () -> persistencePort.findByIdAndFarmIdAndGoatId(eventId, farmId, goatId),
                "Evento de saúde não encontrado. eventId=" + eventId + ", goatId=" + goatId + ", farmId=" + farmId
        );
    }
}
