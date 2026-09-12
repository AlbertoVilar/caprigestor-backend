package com.devmaster.goatfarm.goat.business;

import com.devmaster.goatfarm.audit.application.ports.in.OperationalAuditUseCase;
import com.devmaster.goatfarm.audit.business.bo.OperationalAuditRecordVO;
import com.devmaster.goatfarm.audit.enums.OperationalAuditActionType;
import com.devmaster.goatfarm.authority.business.bo.AuthenticatedPrincipal;
import com.devmaster.goatfarm.authority.application.ports.in.CurrentPrincipalQueryUseCase;
import com.devmaster.goatfarm.config.exceptions.DuplicateEntityException;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.authority.application.ports.in.FarmAuthorizationUseCase;
import com.devmaster.goatfarm.goat.application.ports.in.GoatRegistrationRectificationUseCase;
import com.devmaster.goatfarm.goat.application.ports.out.GoatPersistencePort;
import com.devmaster.goatfarm.goat.application.ports.out.GoatRegistrationHistoryPersistencePort;
import com.devmaster.goatfarm.goat.application.routing.GoatReferenceResolver;
import com.devmaster.goatfarm.goat.business.bo.GoatRegistrationHistoryResponseVO;
import com.devmaster.goatfarm.goat.business.bo.GoatRegistrationRectificationRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatRegistrationRectificationResponseVO;
import com.devmaster.goatfarm.goat.domain.Goat;
import com.devmaster.goatfarm.goat.domain.GoatRegistrationHistory;
import com.devmaster.goatfarm.goat.domain.RegistrationIdentity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** Application service for an explicit, audited correction of a Goat identity. */
@Service
public class GoatRegistrationRectificationBusiness implements GoatRegistrationRectificationUseCase {

    private final GoatPersistencePort goatPersistencePort;
    private final GoatReferenceResolver goatReferenceResolver;
    private final GoatRegistrationHistoryPersistencePort historyPersistencePort;
    private final FarmAuthorizationUseCase ownershipService;
    private final OperationalAuditUseCase operationalAuditUseCase;
    private final CurrentPrincipalQueryUseCase currentPrincipalQuery;

    public GoatRegistrationRectificationBusiness(
            GoatPersistencePort goatPersistencePort,
            GoatReferenceResolver goatReferenceResolver,
            GoatRegistrationHistoryPersistencePort historyPersistencePort,
            FarmAuthorizationUseCase ownershipService,
            OperationalAuditUseCase operationalAuditUseCase,
            CurrentPrincipalQueryUseCase currentPrincipalQuery
    ) {
        this.goatPersistencePort = goatPersistencePort;
        this.goatReferenceResolver = goatReferenceResolver;
        this.historyPersistencePort = historyPersistencePort;
        this.ownershipService = ownershipService;
        this.operationalAuditUseCase = operationalAuditUseCase;
        this.currentPrincipalQuery = currentPrincipalQuery;
    }

    @Override
    @Transactional
    public GoatRegistrationRectificationResponseVO rectify(
            Long farmId,
            String goatRouteToken,
            GoatRegistrationRectificationRequestVO request
    ) {
        ownershipService.verifyFarmOwnership(farmId);
        validateRequest(request);

        Goat goat = resolveGoat(farmId, goatRouteToken);
        RegistrationIdentity previous = goat.registrationIdentity();
        RegistrationIdentity corrected = RegistrationIdentity.fromTodAndToe(request.tod(), request.toe());

        if (previous.equals(corrected)) {
            throw new BusinessRuleException("registrationNumber",
                    "A identidade informada é igual à identidade atual; nenhuma retificação foi realizada.");
        }
        if (goatPersistencePort.existsByRegistrationNumber(corrected.registrationNumber())) {
            throw new DuplicateEntityException("registrationNumber",
                    "Número de registro já existe para outro animal.");
        }

        goat.rectifyRegistration(corrected);
        Goat saved = goatPersistencePort.save(goat);
        AuthenticatedPrincipal actor = currentPrincipalQuery.requireCurrent();
        LocalDateTime changedAt = LocalDateTime.now();

        GoatRegistrationHistory history = historyPersistencePort.save(new GoatRegistrationHistory(
                null,
                saved.id(),
                farmId,
                previous,
                corrected,
                request.source(),
                normalizeRequired(request.evidenceReference(), "evidenceReference", "A referência da evidência é obrigatória."),
                normalizeRequired(request.reason(), "reason", "O motivo da retificação é obrigatório."),
                actor.id(),
                changedAt
        ));

        operationalAuditUseCase.record(new OperationalAuditRecordVO(
                farmId,
                saved.id() == null ? null : saved.id().value(),
                corrected.registrationNumber(),
                OperationalAuditActionType.GOAT_REGISTRATION_RECTIFIED,
                corrected.registrationNumber(),
                "Identidade registral retificada de " + previous.registrationNumber()
                        + " para " + corrected.registrationNumber() + ". Histórico "
                        + (history.id() == null ? "registrado" : "#" + history.id()) + "."
        ));

        return new GoatRegistrationRectificationResponseVO(
                saved.id() == null ? null : saved.id().value(),
                previous.registrationNumber(), previous.tod(), previous.toe(),
                corrected.registrationNumber(), corrected.tod(), corrected.toe(),
                request.source(), history.createdAt() == null ? changedAt : history.createdAt()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoatRegistrationHistoryResponseVO> history(Long farmId, String goatRouteToken) {
        ownershipService.verifyFarmOwnership(farmId);
        Goat goat = resolveGoat(farmId, goatRouteToken);
        return historyPersistencePort.findByFarmIdAndGoatId(farmId, goat.id()).stream()
                .map(this::toResponse)
                .toList();
    }

    private Goat resolveGoat(Long farmId, String routeToken) {
        return goatReferenceResolver.resolve(routeToken, farmId)
                .flatMap(reference -> goatPersistencePort.findByIdAndFarmId(reference.id(), farmId))
                .orElseThrow(() -> new ResourceNotFoundException("Cabra não encontrada nesta fazenda."));
    }

    private void validateRequest(GoatRegistrationRectificationRequestVO request) {
        if (request == null) {
            throw new InvalidArgumentException("registration", "Dados da retificação são obrigatórios.");
        }
        normalizeRequired(request.tod(), "tod", "O TOD corrigido é obrigatório.");
        normalizeRequired(request.toe(), "toe", "O TOE corrigido é obrigatório.");
        if (request.source() == null) {
            throw new InvalidArgumentException("source", "A origem da retificação é obrigatória.");
        }
        normalizeRequired(request.evidenceReference(), "evidenceReference", "A referência da evidência é obrigatória.");
        normalizeRequired(request.reason(), "reason", "O motivo da retificação é obrigatório.");
    }

    private String normalizeRequired(String value, String field, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidArgumentException(field, message);
        }
        return value.trim();
    }

    private GoatRegistrationHistoryResponseVO toResponse(GoatRegistrationHistory history) {
        return new GoatRegistrationHistoryResponseVO(
                history.id(),
                history.goatId() == null ? null : history.goatId().value(),
                history.farmId(),
                history.oldIdentity().registrationNumber(), history.oldIdentity().tod(), history.oldIdentity().toe(),
                history.newIdentity().registrationNumber(), history.newIdentity().tod(), history.newIdentity().toe(),
                history.source(), history.evidenceReference(), history.reason(), history.actorUserId(), history.createdAt()
        );
    }
}
