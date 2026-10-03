package com.devmaster.goatfarm.goatownership.business;

import com.devmaster.goatfarm.application.exception.AuthorizationDeniedException;
import com.devmaster.goatfarm.application.exception.GoatOwnershipNotValidOnDateException;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.application.ports.in.GoatOwnershipGuardUseCase;
import com.devmaster.goatfarm.goatownership.application.ports.out.GoatOwnershipQueryPort;
import com.devmaster.goatfarm.goatownership.domain.GoatOwnershipPeriod;
import com.devmaster.goatfarm.goatownership.domain.OwnershipEntryType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.Instant;

/** Canonical ownership policies exposed to other application modules. */
@Service
public class GoatOwnershipGuardBusiness implements GoatOwnershipGuardUseCase {

    private static final ZoneId OWNERSHIP_CALENDAR_ZONE = ZoneId.of("America/Sao_Paulo");

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

    @Override
    @Transactional(readOnly = true)
    public void requireUnambiguousOwnershipOnDate(GoatId goatId, long expectedFarmId, LocalDate date) {
        if (date == null) {
            throw new InvalidArgumentException("date must not be null");
        }

        List<GoatOwnershipPeriod> periods = loadConsistentHistory(goatId);
        Instant dayStart = date.atStartOfDay(OWNERSHIP_CALENDAR_ZONE).toInstant();
        Instant nextDayStart = date.plusDays(1).atStartOfDay(OWNERSHIP_CALENDAR_ZONE).toInstant();

        if (isUnambiguousFirstCanonicalOwnership(periods, expectedFarmId, date, dayStart, nextDayStart)) {
            return;
        }

        List<GoatOwnershipPeriod> periodsCoveringWholeDay = periods.stream()
                .filter(period -> !period.startedAt().isAfter(dayStart))
                .filter(period -> period.endedAt() == null || !period.endedAt().isBefore(nextDayStart))
                .toList();

        if (periodsCoveringWholeDay.size() != 1
                || periodsCoveringWholeDay.get(0).farmId() != expectedFarmId) {
            boolean anotherFarmIntersectsDay = periods.stream()
                    .filter(period -> period.farmId() != expectedFarmId)
                    .anyMatch(period -> period.startedAt().isBefore(nextDayStart)
                            && (period.endedAt() == null || period.endedAt().isAfter(dayStart)));
            throw new GoatOwnershipNotValidOnDateException(
                    "A fazenda não possui ownership canônico inequívoco durante todo o dia informado.",
                    anotherFarmIntersectsDay
                            ? GoatOwnershipNotValidOnDateException.Reason.OWNERSHIP_PERIOD_FROM_ANOTHER_FARM
                            : GoatOwnershipNotValidOnDateException.Reason.OWNERSHIP_NOT_UNAMBIGUOUS);
        }
    }

    private boolean isUnambiguousFirstCanonicalOwnership(
            List<GoatOwnershipPeriod> periods,
            long expectedFarmId,
            LocalDate date,
            Instant dayStart,
            Instant nextDayStart
    ) {
        if (periods.size() != 1) {
            return false;
        }

        GoatOwnershipPeriod initial = periods.get(0);
        return initial.isOpen()
                && initial.farmId() == expectedFarmId
                && initial.startedAt().isAfter(dayStart)
                && initial.startedAt().isBefore(nextDayStart)
                && initial.startedAt().atZone(OWNERSHIP_CALENDAR_ZONE).toLocalDate().equals(date)
                && isAllowedInitialEntryType(initial.entryType());
    }

    private boolean isAllowedInitialEntryType(OwnershipEntryType entryType) {
        return entryType == OwnershipEntryType.BIRTH
                || entryType == OwnershipEntryType.MANUAL_IMPORT
                || entryType == OwnershipEntryType.ABCC_IMPORT;
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
