package com.devmaster.goatfarm.goatownership.business;

import com.devmaster.goatfarm.application.exception.AuthorizationDeniedException;
import com.devmaster.goatfarm.application.pagination.PageResult;
import com.devmaster.goatfarm.authority.application.ports.in.CurrentPrincipalQueryUseCase;
import com.devmaster.goatfarm.authority.application.ports.in.FarmAuthorizationUseCase;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.farm.application.ports.out.GoatFarmPersistencePort;
import com.devmaster.goatfarm.goatownership.application.model.OwnershipTransferDirection;
import com.devmaster.goatfarm.goatownership.application.model.OwnershipTransferPageQuery;
import com.devmaster.goatfarm.goatownership.application.ports.in.GoatOwnershipTransferQueryUseCase;
import com.devmaster.goatfarm.goatownership.application.ports.out.OwnershipTransferPersistencePort;
import com.devmaster.goatfarm.goatownership.application.ports.out.OwnershipTransferQueryPort;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransfer;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransferKind;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus;
import com.devmaster.goatfarm.authority.business.bo.AuthenticatedPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Orchestrates authorized ownership transfer reads without exposing persistence details. */
@Service
public class GoatOwnershipTransferQueryBusiness implements GoatOwnershipTransferQueryUseCase {
    private final OwnershipTransferPersistencePort transferPersistence;
    private final OwnershipTransferQueryPort transferQuery;
    private final GoatFarmPersistencePort farmPersistence;
    private final FarmAuthorizationUseCase farmAuthorization;
    private final CurrentPrincipalQueryUseCase currentPrincipalQuery;

    public GoatOwnershipTransferQueryBusiness(OwnershipTransferPersistencePort transferPersistence,
                                              OwnershipTransferQueryPort transferQuery,
                                              GoatFarmPersistencePort farmPersistence,
                                              FarmAuthorizationUseCase farmAuthorization,
                                              CurrentPrincipalQueryUseCase currentPrincipalQuery) {
        this.transferPersistence = transferPersistence;
        this.transferQuery = transferQuery;
        this.farmPersistence = farmPersistence;
        this.farmAuthorization = farmAuthorization;
        this.currentPrincipalQuery = currentPrincipalQuery;
    }

    @Override
    @Transactional(readOnly = true)
    public OwnershipTransfer findAuthorizedById(Long transferId) {
        requirePositive(transferId, "transferId");
        OwnershipTransfer transfer = transferPersistence.findById(transferId)
                .orElseThrow(() -> new ResourceNotFoundException("Ownership transfer not found: " + transferId));
        requireInternalTransfer(transfer);
        if (!canRead(transfer)) {
            throw new AuthorizationDeniedException("current principal cannot read ownership transfer " + transferId);
        }
        return transfer;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<OwnershipTransfer> listForFarm(Long farmId,
                                                      OwnershipTransferDirection direction,
                                                      OwnershipTransferStatus status,
                                                      OwnershipTransferPageQuery pageQuery) {
        requirePositive(farmId, "farmId");
        if (direction == null) {
            throw new InvalidArgumentException("direction is required");
        }
        validatePage(pageQuery);
        farmPersistence.findById(farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Farm not found: " + farmId));
        if (!farmAuthorization.canAdministerFarm(farmId)) {
            throw new AuthorizationDeniedException("current principal cannot administer farm " + farmId);
        }
        return transferQuery.findForFarm(farmId, direction, status, pageQuery);
    }

    private boolean canRead(OwnershipTransfer transfer) {
        AuthenticatedPrincipal principal = currentPrincipalQuery.requireCurrent();
        if (principal.hasAuthority("ROLE_ADMIN")) {
            return true;
        }
        return (transfer.sourceFarmId() != null && farmAuthorization.canAdministerFarm(transfer.sourceFarmId()))
                || farmAuthorization.canAdministerFarm(transfer.targetFarmId());
    }

    private void requireInternalTransfer(OwnershipTransfer transfer) {
        if (transfer.kind() != OwnershipTransferKind.INTERNAL_TRANSFER) {
            throw new ResourceNotFoundException("Ownership transfer not found: " + transfer.id());
        }
    }

    private void validatePage(OwnershipTransferPageQuery pageQuery) {
        if (pageQuery == null || pageQuery.page() < 0 || pageQuery.size() < 1 || pageQuery.size() > 100) {
            throw new BusinessRuleException("page must be >= 0 and size must be between 1 and 100");
        }
    }

    private void requirePositive(Long value, String field) {
        if (value == null || value <= 0) {
            throw new InvalidArgumentException(field + " must be positive");
        }
    }
}
