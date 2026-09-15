package com.devmaster.goatfarm.goatownership.business;

import com.devmaster.goatfarm.application.exception.AuthorizationDeniedException;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.authority.application.ports.in.FarmAuthorizationUseCase;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.application.model.GoatOwnershipLockState;
import com.devmaster.goatfarm.goatownership.application.model.TerminalOwnershipExitCommand;
import com.devmaster.goatfarm.goatownership.application.ports.in.GoatOwnershipExitUseCase;
import com.devmaster.goatfarm.goatownership.application.ports.out.GoatOwnershipLockPort;
import com.devmaster.goatfarm.goatownership.application.ports.out.GoatOwnershipPeriodPersistencePort;
import com.devmaster.goatfarm.goatownership.application.ports.out.GoatOwnershipQueryPort;
import com.devmaster.goatfarm.goatownership.application.ports.out.OwnershipTransferPersistencePort;
import com.devmaster.goatfarm.goatownership.domain.GoatOwnershipPeriod;
import com.devmaster.goatfarm.goatownership.domain.OwnershipExitType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Transactional application core for terminal ownership exits.
 *
 * <p>This capability intentionally changes only the canonical ownership
 * ledger. Goat lifecycle metadata, commercial records and audit integration
 * remain in their own reviewed waves.</p>
 */
@Service
public class GoatOwnershipExitBusiness implements GoatOwnershipExitUseCase {
    private final FarmAuthorizationUseCase farmAuthorization;
    private final GoatOwnershipLockPort ownershipLock;
    private final GoatOwnershipPeriodPersistencePort periodPersistence;
    private final GoatOwnershipQueryPort ownershipQuery;
    private final OwnershipTransferPersistencePort transferPersistence;
    private final Clock clock;

    public GoatOwnershipExitBusiness(FarmAuthorizationUseCase farmAuthorization,
                                     GoatOwnershipLockPort ownershipLock,
                                     GoatOwnershipPeriodPersistencePort periodPersistence,
                                     GoatOwnershipQueryPort ownershipQuery,
                                     OwnershipTransferPersistencePort transferPersistence,
                                     Clock clock) {
        this.farmAuthorization = farmAuthorization;
        this.ownershipLock = ownershipLock;
        this.periodPersistence = periodPersistence;
        this.ownershipQuery = ownershipQuery;
        this.transferPersistence = transferPersistence;
        this.clock = clock;
    }

    @Override
    @Transactional
    public GoatOwnershipPeriod closeTerminalOwnership(TerminalOwnershipExitCommand command) {
        validateCommand(command);

        GoatId goatId = command.goatId();
        GoatOwnershipLockState lock = ownershipLock.lockGoatOwnership(goatId)
                .orElseThrow(() -> new ResourceNotFoundException("Goat not found: " + goatId.value()));
        GoatOwnershipPeriod current = lock.openPeriod()
                .orElseThrow(() -> new BusinessRuleException("Goat has no canonical open ownership period"));

        if (current.farmId() != command.expectedSourceFarmId()) {
            throw new AuthorizationDeniedException("canonical ownership source does not match the expected farm");
        }
        if (!farmAuthorization.canAdministerFarm(current.farmId())) {
            throw new AuthorizationDeniedException("current principal cannot administer canonical source farm");
        }
        transferPersistence.findPendingByGoatId(goatId).ifPresent(pending -> {
            throw new BusinessRuleException("another ownership transfer is already pending for this GoatId");
        });

        Instant effectiveAt = Instant.now(clock);
        if (!effectiveAt.isAfter(current.startedAt())) {
            throw new BusinessRuleException("effectiveAt must be after ownership period start");
        }

        current.close(effectiveAt, command.exitType());
        List<GoatOwnershipPeriod> existingHistory = ownershipQuery.findOwnershipHistory(goatId);
        if (existingHistory == null || existingHistory.isEmpty()) {
            throw new BusinessRuleException("Goat ownership history is missing");
        }
        List<GoatOwnershipPeriod> history = new ArrayList<>(existingHistory);
        replacePeriod(history, current);
        GoatOwnershipPeriod.ensureConsistent(history);
        return periodPersistence.save(current);
    }

    private void validateCommand(TerminalOwnershipExitCommand command) {
        if (command == null || command.goatId() == null || command.goatId().value() <= 0) {
            throw new InvalidArgumentException("goatId", "GoatId must be a positive number");
        }
        if (command.expectedSourceFarmId() == null || command.expectedSourceFarmId() <= 0) {
            throw new InvalidArgumentException("expectedSourceFarmId", "expectedSourceFarmId must be positive");
        }
        if (!isSupported(command.exitType())) {
            throw new BusinessRuleException("exitType", "unsupported terminal ownership exit type");
        }
    }

    private boolean isSupported(OwnershipExitType exitType) {
        return exitType == OwnershipExitType.EXTERNAL_SALE
                || exitType == OwnershipExitType.DONATION
                || exitType == OwnershipExitType.DEATH
                || exitType == OwnershipExitType.RETIREMENT;
    }

    private void replacePeriod(List<GoatOwnershipPeriod> history, GoatOwnershipPeriod replacement) {
        for (int index = 0; index < history.size(); index++) {
            GoatOwnershipPeriod candidate = history.get(index);
            if (candidate.id() != null && candidate.id().equals(replacement.id())) {
                history.set(index, replacement);
                return;
            }
        }
        throw new BusinessRuleException("locked open ownership period is absent from ownership history");
    }
}
