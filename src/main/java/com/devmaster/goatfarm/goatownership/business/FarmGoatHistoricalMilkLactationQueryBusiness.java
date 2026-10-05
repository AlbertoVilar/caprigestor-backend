package com.devmaster.goatfarm.goatownership.business;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatHistoricalLactationItem;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatHistoricalMilkLactationSnapshot;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatHistoricalMilkProductionItem;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatRegistryItem;
import com.devmaster.goatfarm.goatownership.application.ports.in.FarmGoatHistoricalMilkLactationQueryUseCase;
import com.devmaster.goatfarm.goatownership.application.ports.in.FarmGoatRegistryQueryUseCase;
import com.devmaster.goatfarm.goatownership.application.ports.out.FarmGoatHistoricalMilkLactationQueryPort;
import com.devmaster.goatfarm.goatownership.application.ports.out.GoatOwnershipQueryPort;
import com.devmaster.goatfarm.goatownership.domain.GoatOwnershipPeriod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Application business service that coordinates historical milk and lactation dossier reads.
 * Ensures registry membership, canonical ownership history, visibility rules, and deterministic sorting.
 */
@Service
@Transactional(readOnly = true)
public class FarmGoatHistoricalMilkLactationQueryBusiness implements FarmGoatHistoricalMilkLactationQueryUseCase {

    private static final ZoneId DOMAIN_ZONE = ZoneId.of("America/Sao_Paulo");

    private static final Comparator<FarmGoatHistoricalLactationItem> LACTATION_COMPARATOR =
            Comparator.comparing(FarmGoatHistoricalLactationItem::startDate, Comparator.reverseOrder())
                    .thenComparing(FarmGoatHistoricalLactationItem::id, Comparator.nullsLast(Comparator.reverseOrder()));

    private static final Comparator<FarmGoatHistoricalMilkProductionItem> MILK_PRODUCTION_COMPARATOR =
            Comparator.comparing(FarmGoatHistoricalMilkProductionItem::date, Comparator.reverseOrder())
                    .thenComparing(FarmGoatHistoricalMilkProductionItem::shift)
                    .thenComparing(FarmGoatHistoricalMilkProductionItem::id, Comparator.nullsLast(Comparator.reverseOrder()));

    private final FarmGoatRegistryQueryUseCase registryQueryUseCase;
    private final GoatOwnershipQueryPort goatOwnershipQueryPort;
    private final FarmGoatHistoricalMilkLactationQueryPort queryPort;

    public FarmGoatHistoricalMilkLactationQueryBusiness(
            FarmGoatRegistryQueryUseCase registryQueryUseCase,
            GoatOwnershipQueryPort goatOwnershipQueryPort,
            FarmGoatHistoricalMilkLactationQueryPort queryPort
    ) {
        this.registryQueryUseCase = Objects.requireNonNull(registryQueryUseCase, "registryQueryUseCase must not be null");
        this.goatOwnershipQueryPort = Objects.requireNonNull(goatOwnershipQueryPort, "goatOwnershipQueryPort must not be null");
        this.queryPort = Objects.requireNonNull(queryPort, "queryPort must not be null");
    }

    @Override
    public Optional<FarmGoatHistoricalMilkLactationSnapshot> findHistoricalMilkLactation(long farmId, GoatId goatId) {
        if (farmId <= 0) {
            throw new IllegalArgumentException("farmId must be positive");
        }
        if (goatId == null) {
            throw new IllegalArgumentException("goatId must not be null");
        }

        Optional<FarmGoatRegistryItem> registryItemOpt = registryQueryUseCase.findForFarmAndGoat(farmId, goatId);
        if (registryItemOpt.isEmpty()) {
            return Optional.empty();
        }

        String registrationNumber = registryItemOpt.get().registrationNumber();

        // Query historical milk productions strictly scoped to requesting farm
        List<FarmGoatHistoricalMilkProductionItem> rawProductions =
                queryPort.findMilkProductionsByGoatAndFarm(farmId, goatId, registrationNumber);

        // Deterministically sort milk productions: date DESC, shift ASC, id DESC
        List<FarmGoatHistoricalMilkProductionItem> sortedProductions = rawProductions.stream()
                .sorted(MILK_PRODUCTION_COMPARATOR)
                .toList();

        // Query candidate lactations for the goat
        List<FarmGoatHistoricalLactationItem> candidateLactations =
                queryPort.findLactationsByGoat(goatId, registrationNumber);

        // Query canonical ownership history
        List<GoatOwnershipPeriod> ownershipHistory = goatOwnershipQueryPort.findOwnershipHistory(goatId);
        List<GoatOwnershipPeriod> farmOwnershipPeriods = ownershipHistory.stream()
                .filter(p -> p.farmId() == farmId)
                .toList();

        // Set of lactation IDs referenced by this farm's milk productions
        Set<Long> referencedLactationIds = sortedProductions.stream()
                .map(FarmGoatHistoricalMilkProductionItem::lactationId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Filter visible lactations according to canonical rules
        List<FarmGoatHistoricalLactationItem> visibleLactations = candidateLactations.stream()
                .filter(lactation -> isLactationVisible(lactation, farmId, referencedLactationIds, farmOwnershipPeriods))
                .sorted(LACTATION_COMPARATOR)
                .toList();

        return Optional.of(new FarmGoatHistoricalMilkLactationSnapshot(
                goatId,
                visibleLactations,
                sortedProductions
        ));
    }

    private boolean isLactationVisible(
            FarmGoatHistoricalLactationItem lactation,
            long farmId,
            Set<Long> referencedLactationIds,
            List<GoatOwnershipPeriod> farmOwnershipPeriods
    ) {
        // Condition 1: lactation originated on this farm
        if (Objects.equals(lactation.farmId(), farmId)) {
            return true;
        }

        // Condition 2: referenced by at least one milk production of this farm
        if (lactation.id() != null && referencedLactationIds.contains(lactation.id())) {
            return true;
        }

        // Condition 3: overlaps at least one ownership period of this farm (fail closed on transfer-day ambiguity)
        return farmOwnershipPeriods.stream()
                .anyMatch(period -> hasUnambiguousOwnershipOverlap(lactation, period));
    }

    private boolean hasUnambiguousOwnershipOverlap(FarmGoatHistoricalLactationItem lactation, GoatOwnershipPeriod period) {
        LocalDate periodStart = period.startedAt().atZone(DOMAIN_ZONE).toLocalDate();
        LocalDate periodEnd = period.endedAt() != null
                ? period.endedAt().atZone(DOMAIN_ZONE).toLocalDate()
                : null;

        LocalDate lactationStart = lactation.startDate();
        LocalDate lactationEnd = lactation.endDate();

        // Fail-closed transfer-day rule:
        // 1. If ownership has an end boundary, lactation must start strictly before ownership ends
        if (periodEnd != null && !lactationStart.isBefore(periodEnd)) {
            return false;
        }

        // 2. If lactation has an end boundary, ownership must start strictly before lactation ends
        if (lactationEnd != null && !periodStart.isBefore(lactationEnd)) {
            return false;
        }

        return true;
    }
}
