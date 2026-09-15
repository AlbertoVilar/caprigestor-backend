package com.devmaster.goatfarm.health.business.healthservice;

import com.devmaster.goatfarm.application.core.business.common.EntityFinder;
import com.devmaster.goatfarm.application.core.business.validation.GoatGenderValidator;
import com.devmaster.goatfarm.application.exception.AuthorizationDeniedException;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.authority.application.ports.in.FarmAuthorizationUseCase;
import com.devmaster.goatfarm.goat.application.routing.GoatReferenceResolver;
import com.devmaster.goatfarm.goatownership.application.ports.in.GoatOwnershipGuardUseCase;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReference;
import com.devmaster.goatfarm.health.application.ports.in.HealthEventCommandUseCase;
import com.devmaster.goatfarm.health.application.ports.in.HealthEventQueryUseCase;
import com.devmaster.goatfarm.health.application.model.HealthEventRecord;
import com.devmaster.goatfarm.health.application.ports.out.HealthEventPersistencePort;
import com.devmaster.goatfarm.application.pagination.PageQuery;
import com.devmaster.goatfarm.application.pagination.PageResult;
import com.devmaster.goatfarm.health.business.bo.HealthEventCancelRequestVO;
import com.devmaster.goatfarm.health.business.bo.HealthEventCreateRequestVO;
import com.devmaster.goatfarm.health.business.bo.HealthEventDoneRequestVO;
import com.devmaster.goatfarm.health.business.bo.HealthEventResponseVO;
import com.devmaster.goatfarm.health.business.bo.HealthEventUpdateRequestVO;
import com.devmaster.goatfarm.health.business.mapper.HealthEventBusinessMapper;
import com.devmaster.goatfarm.health.domain.enums.HealthEventStatus;
import com.devmaster.goatfarm.health.domain.enums.HealthEventType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class HealthEventBusiness implements HealthEventCommandUseCase, HealthEventQueryUseCase {

    private final HealthEventPersistencePort persistencePort;
    private final GoatReferenceResolver goatReferenceResolver;
    private final GoatGenderValidator goatGenderValidator;
    private final HealthEventBusinessMapper mapper;
    private final EntityFinder entityFinder;
    private final FarmAuthorizationUseCase ownershipService;
    private final GoatOwnershipGuardUseCase goatOwnershipGuard;

    public HealthEventBusiness(
            HealthEventPersistencePort persistencePort,
            GoatReferenceResolver goatReferenceResolver,
            GoatGenderValidator goatGenderValidator,
            HealthEventBusinessMapper mapper,
            EntityFinder entityFinder,
            FarmAuthorizationUseCase ownershipService,
            GoatOwnershipGuardUseCase goatOwnershipGuard
    ) {
        this.persistencePort = persistencePort;
        this.goatReferenceResolver = goatReferenceResolver;
        this.goatGenderValidator = goatGenderValidator;
        this.mapper = mapper;
        this.entityFinder = entityFinder;
        this.ownershipService = ownershipService;
        this.goatOwnershipGuard = goatOwnershipGuard;
    }

    @Override
    @Transactional
    public HealthEventResponseVO create(Long farmId, String goatId, HealthEventCreateRequestVO request) {
        GoatReference goat = entityFinder.findOrThrow(
                () -> goatReferenceResolver.resolveGlobal(goatId),
                "Cabra não encontrada."
        );
        goatOwnershipGuard.requireCurrentFarm(goat.id(), farmId);
        requireProjectionMatchesFarm(goat, farmId);

        // Controle de acesso da fazenda é feito no Controller via @CanManageFarm.
        goatGenderValidator.requireActive(farmId, goatId);

        var record = mapper.toRecord(request);
        record.setFarmId(farmId);
        record.setGoatId(goatId);

        // Invariável de domínio: criação é sempre AGENDADO
        record.setStatus(HealthEventStatus.AGENDADO);

        var saved = persistencePort.save(record);
        return mapper.toResponseVO(saved);
    }

    private void requireProjectionMatchesFarm(GoatReference goat, Long farmId) {
        if (goat.farmId() == null || !goat.farmId().equals(farmId)) {
            throw new AuthorizationDeniedException(
                    "A projeção legada da cabra está divergente do contexto da fazenda informado.");
        }
    }

    @Override
    @Transactional
    public HealthEventResponseVO update(Long farmId, String goatId, Long eventId, HealthEventUpdateRequestVO request) {
        goatGenderValidator.requireActive(farmId, goatId);
        var healthEvent = findEventOrThrow(farmId, goatId, eventId);

        // Regra de negócio: só pode editar enquanto estiver AGENDADO
        if (healthEvent.getStatus() != HealthEventStatus.AGENDADO) {
            throw new BusinessRuleException("Não é permitido alterar um evento de saúde já realizado ou cancelado.");
        }

        mapper.updateRecord(healthEvent, request);

        var saved = persistencePort.save(healthEvent);
        return mapper.toResponseVO(saved);
    }

    @Override
    @Transactional
    public HealthEventResponseVO markAsDone(Long farmId, String goatId, Long eventId, HealthEventDoneRequestVO request) {
        goatGenderValidator.requireActive(farmId, goatId);
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
        goatGenderValidator.requireActive(farmId, goatId);
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
    @Transactional
    public HealthEventResponseVO reopen(Long farmId, String goatId, Long eventId) {
        ownershipService.verifyFarmOwnership(farmId);
        goatGenderValidator.requireActive(farmId, goatId);
        var healthEvent = findEventOrThrow(farmId, goatId, eventId);
        HealthEventStatus currentStatus = healthEvent.getStatus();

        if (currentStatus == HealthEventStatus.AGENDADO) {
            throw new BusinessRuleException("O evento já está agendado.");
        }
        if (currentStatus != HealthEventStatus.REALIZADO && currentStatus != HealthEventStatus.CANCELADO) {
            throw new BusinessRuleException("Não é possível reabrir um evento neste status.");
        }

        healthEvent.setStatus(HealthEventStatus.AGENDADO);
        healthEvent.setPerformedAt(null);

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
    public PageResult<HealthEventResponseVO> listCalendar(
            Long farmId,
            LocalDate from,
            LocalDate to,
            HealthEventType type,
            HealthEventStatus status,
            PageQuery pageQuery
    ) {
        var healthEvents = persistencePort.findByFarmIdAndPeriod(
                farmId,
                from,
                to,
                type,
                status,
                pageQuery
        );

        return healthEvents.map(mapper::toResponseVO);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<HealthEventResponseVO> listByGoat(
            Long farmId,
            String goatId,
            LocalDate from,
            LocalDate to,
            HealthEventType type,
            HealthEventStatus status,
            PageQuery pageQuery
    ) {
        entityFinder.findOrThrow(
                () -> goatReferenceResolver.resolve(goatId, farmId),
                "Cabra não encontrada no capril informado. goatId=" + goatId + ", farmId=" + farmId
        );

        var healthEvents = persistencePort.findByFarmIdAndGoatId(
                farmId,
                goatId,
                from,
                to,
                type,
                status,
                pageQuery
        );

        return healthEvents.map(mapper::toResponseVO);
    }

    private HealthEventRecord findEventOrThrow(Long farmId, String goatId, Long eventId) {
        return entityFinder.findOrThrow(
                () -> persistencePort.findByIdAndFarmIdAndGoatId(eventId, farmId, goatId),
                "Evento de saúde não encontrado. eventId=" + eventId + ", goatId=" + goatId + ", farmId=" + farmId
        );
    }
}
