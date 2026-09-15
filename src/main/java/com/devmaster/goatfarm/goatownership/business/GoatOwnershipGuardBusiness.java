package com.devmaster.goatfarm.goatownership.business;

import com.devmaster.goatfarm.application.exception.AuthorizationDeniedException;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.application.ports.in.GoatOwnershipGuardUseCase;
import com.devmaster.goatfarm.goatownership.application.ports.out.GoatOwnershipQueryPort;
import com.devmaster.goatfarm.goatownership.domain.GoatOwnershipPeriod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/** Canonical ownership policies exposed to other application modules. */
@Service
public class GoatOwnershipGuardBusiness implements GoatOwnershipGuardUseCase {

    private final GoatOwnershipQueryPort ownershipQuery;

    public GoatOwnershipGuardBusiness(GoatOwnershipQueryPort ownershipQuery) {
        this.ownershipQuery = ownershipQuery;
    }

    @Override
    @Transactional(readOnly = true)
    public void requireCurrentFarm(GoatId goatId, long expectedFarmId) {
        List<GoatOwnershipPeriod> periods = loadConsistentHistory(goatId);
        GoatOwnershipPeriod current = periods.stream()
                .filter(GoatOwnershipPeriod::isOpen)
                .findFirst()
                .orElseThrow(() -> new AuthorizationDeniedException(
                        "A cabra não possui ownership canônico aberto."));
        if (current.farmId() != expectedFarmId) {
            throw new AuthorizationDeniedException(
                    "A fazenda informada não corresponde ao owner canônico atual da cabra.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void requireLastAssociatedFarm(GoatId goatId, long expectedFarmId) {
        List<GoatOwnershipPeriod> periods = loadConsistentHistory(goatId);
        GoatOwnershipPeriod last = periods.stream()
                .max(Comparator.comparing(GoatOwnershipPeriod::startedAt)
                        .thenComparing(period -> period.endedAt() == null
                                ? java.time.Instant.MAX : period.endedAt()))
                .orElseThrow(() -> new BusinessRuleException("ownership",
                        "O animal não possui histórico canônico de ownership para esta operação."));
        if (last.farmId() != expectedFarmId) {
            throw new AuthorizationDeniedException(
                    "A fazenda informada não corresponde à última fazenda canônica associada ao animal.");
        }
    }

    private List<GoatOwnershipPeriod> loadConsistentHistory(GoatId goatId) {
        requirePositive(goatId);
        List<GoatOwnershipPeriod> periods = ownershipQuery.findOwnershipHistory(goatId);
        if (periods == null || periods.isEmpty()) {
            throw new BusinessRuleException("ownership",
                    "O animal não possui histórico canônico de ownership para esta operação.");
        }
        if (periods.stream().anyMatch(period -> !goatId.equals(period.goatId()))) {
            throw new BusinessRuleException("ownership",
                    "O histórico canônico contém um período de outro GoatId.");
        }
        try {
            GoatOwnershipPeriod.ensureConsistent(periods);
        } catch (IllegalArgumentException ex) {
            throw new BusinessRuleException("ownership",
                    "O histórico canônico de ownership é inconsistente.");
        }
        return periods;
    }

    private void requirePositive(GoatId goatId) {
        if (goatId == null || goatId.value() <= 0) {
            throw new InvalidArgumentException("goatId must be positive");
        }
    }
}
