package com.devmaster.goatfarm.goatownership.business;

import com.devmaster.goatfarm.application.exception.AuthorizationDeniedException;
import com.devmaster.goatfarm.application.pagination.PageResult;
import com.devmaster.goatfarm.authority.application.ports.in.FarmAuthorizationUseCase;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.farm.application.ports.out.GoatFarmPersistencePort;
import com.devmaster.goatfarm.goatownership.application.model.OwnershipMovementDirection;
import com.devmaster.goatfarm.goatownership.application.model.OwnershipMovementItem;
import com.devmaster.goatfarm.goatownership.application.model.OwnershipMovementKind;
import com.devmaster.goatfarm.goatownership.application.model.OwnershipMovementPageQuery;
import com.devmaster.goatfarm.goatownership.application.ports.in.OwnershipMovementQueryUseCase;
import com.devmaster.goatfarm.goatownership.application.ports.out.OwnershipMovementQueryPort;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Authorizes and coordinates the read-only ownership movement projection. */
@Service
public class OwnershipMovementQueryBusiness implements OwnershipMovementQueryUseCase {
    private final OwnershipMovementQueryPort movementQuery;
    private final GoatFarmPersistencePort farmPersistence;
    private final FarmAuthorizationUseCase farmAuthorization;

    public OwnershipMovementQueryBusiness(OwnershipMovementQueryPort movementQuery,
                                         GoatFarmPersistencePort farmPersistence,
                                         FarmAuthorizationUseCase farmAuthorization) {
        this.movementQuery = movementQuery;
        this.farmPersistence = farmPersistence;
        this.farmAuthorization = farmAuthorization;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<OwnershipMovementItem> listForFarm(Long farmId,
                                                          OwnershipMovementDirection direction,
                                                          OwnershipTransferStatus status,
                                                          OwnershipMovementKind kind,
                                                          OwnershipMovementPageQuery pageQuery) {
        if (farmId == null || farmId <= 0) {
            throw new InvalidArgumentException("farmId must be positive");
        }
        if (direction == null) {
            throw new InvalidArgumentException("direction is required");
        }
        if (pageQuery == null || pageQuery.page() < 0 || pageQuery.size() < 1 || pageQuery.size() > 100) {
            throw new BusinessRuleException("page must be >= 0 and size must be between 1 and 100");
        }
        farmPersistence.findById(farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Farm not found: " + farmId));
        if (!farmAuthorization.canAdministerFarm(farmId)) {
            throw new AuthorizationDeniedException("current principal cannot administer farm " + farmId);
        }
        return movementQuery.findForFarm(farmId, direction, status, kind, pageQuery);
    }
}
