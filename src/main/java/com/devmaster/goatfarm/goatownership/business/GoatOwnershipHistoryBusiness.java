package com.devmaster.goatfarm.goatownership.business;

import com.devmaster.goatfarm.application.exception.AuthorizationDeniedException;
import com.devmaster.goatfarm.authority.application.ports.in.CurrentPrincipalQueryUseCase;
import com.devmaster.goatfarm.authority.application.ports.in.FarmAuthorizationUseCase;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.goat.application.ports.out.GoatPersistencePort;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.application.model.OwnershipHistory;
import com.devmaster.goatfarm.goatownership.application.model.OwnershipHistoryPeriod;
import com.devmaster.goatfarm.goatownership.application.ports.in.GoatOwnershipHistoryQueryUseCase;
import com.devmaster.goatfarm.goatownership.application.ports.out.GoatOwnershipQueryPort;
import com.devmaster.goatfarm.goatownership.domain.GoatOwnershipPeriod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Authorized application read of the canonical ownership ledger.
 *
 * <p>Authorization deliberately uses only the current canonical ownership
 * period. Historical membership never grants continuing access.</p>
 */
@Service
public class GoatOwnershipHistoryBusiness implements GoatOwnershipHistoryQueryUseCase {
    private final GoatPersistencePort goatPersistence;
    private final GoatOwnershipQueryPort ownershipQuery;
    private final FarmAuthorizationUseCase farmAuthorization;
    private final CurrentPrincipalQueryUseCase currentPrincipalQuery;

    public GoatOwnershipHistoryBusiness(GoatPersistencePort goatPersistence,
                                        GoatOwnershipQueryPort ownershipQuery,
                                        FarmAuthorizationUseCase farmAuthorization,
                                        CurrentPrincipalQueryUseCase currentPrincipalQuery) {
        this.goatPersistence = goatPersistence;
        this.ownershipQuery = ownershipQuery;
        this.farmAuthorization = farmAuthorization;
        this.currentPrincipalQuery = currentPrincipalQuery;
    }

    @Override
    @Transactional(readOnly = true)
    public OwnershipHistory findOwnershipHistory(GoatId goatId) {
        requireValidGoatId(goatId);
        goatPersistence.findById(goatId)
                .orElseThrow(() -> new ResourceNotFoundException("Goat not found: " + goatId.value()));

        var principal = currentPrincipalQuery.requireCurrent();
        boolean admin = principal.hasAuthority("ROLE_ADMIN");
        Long authorizedCurrentFarmId = null;
        if (!admin) {
            authorizedCurrentFarmId = ownershipQuery.findCurrentOwnerFarmId(goatId)
                    .orElseThrow(() -> new AuthorizationDeniedException(
                            "current principal cannot read ownership history without a current owner"));
            if (!farmAuthorization.canAdministerFarm(authorizedCurrentFarmId)) {
                throw new AuthorizationDeniedException("current principal cannot administer current goat farm");
            }
        }

        List<GoatOwnershipPeriod> periods = ownershipQuery.findOwnershipHistory(goatId);
        if (periods == null || periods.isEmpty()) {
            throw new BusinessRuleException("ownershipHistory",
                    "Goat exists without canonical ownership history");
        }
        if (!admin && !matchesAuthorizedCurrentOwner(periods, authorizedCurrentFarmId)) {
            throw new AuthorizationDeniedException(
                    "current ownership changed while reading ownership history");
        }
        return new OwnershipHistory(goatId, periods.stream().map(this::toReadModel).toList());
    }

    private boolean matchesAuthorizedCurrentOwner(List<GoatOwnershipPeriod> periods,
                                                  Long authorizedCurrentFarmId) {
        List<GoatOwnershipPeriod> openPeriods = periods.stream()
                .filter(GoatOwnershipPeriod::isOpen)
                .toList();
        return openPeriods.size() == 1
                && openPeriods.get(0).farmId() == authorizedCurrentFarmId;
    }

    private OwnershipHistoryPeriod toReadModel(GoatOwnershipPeriod period) {
        return new OwnershipHistoryPeriod(
                period.farmId(), period.startedAt(), period.endedAt(), period.entryType(), period.exitType(), period.isOpen());
    }

    private void requireValidGoatId(GoatId goatId) {
        if (goatId == null || goatId.value() <= 0) {
            throw new InvalidArgumentException("goatId must be positive");
        }
    }
}
