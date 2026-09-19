package com.devmaster.goatfarm.goatownership.business;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.application.model.*;
import com.devmaster.goatfarm.goatownership.application.ports.in.FarmGoatHistoricalReproductionQueryUseCase;
import com.devmaster.goatfarm.goatownership.application.ports.in.FarmGoatRegistryQueryUseCase;
import com.devmaster.goatfarm.goatownership.application.ports.out.FarmGoatHistoricalReproductionQueryPort;
import com.devmaster.goatfarm.reproduction.enums.PregnancyCloseReason;
import com.devmaster.goatfarm.reproduction.enums.PregnancyStatus;
import com.devmaster.goatfarm.reproduction.enums.ReproductiveEventType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Business service coordinating historical reproduction dossier reads.
 * Enforces Registry membership, strict event provenance, and safe field-by-field process projection.
 */
@Service
@Transactional(readOnly = true)
public class FarmGoatHistoricalReproductionQueryBusiness implements FarmGoatHistoricalReproductionQueryUseCase {

    private static final Comparator<HistoricalReproductionProcessItem> PROCESS_COMPARATOR =
            Comparator.comparing(HistoricalReproductionProcessItem::breedingDate, Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(HistoricalReproductionProcessItem::pregnancyId, Comparator.nullsLast(Comparator.reverseOrder()));

    private static final Comparator<HistoricalReproductionEventItem> EVENT_COMPARATOR =
            Comparator.comparing(HistoricalReproductionEventItem::eventDate, Comparator.reverseOrder())
                    .thenComparing(HistoricalReproductionEventItem::id, Comparator.nullsLast(Comparator.reverseOrder()));

    private final FarmGoatRegistryQueryUseCase registryQueryUseCase;
    private final FarmGoatHistoricalReproductionQueryPort queryPort;

    public FarmGoatHistoricalReproductionQueryBusiness(
            FarmGoatRegistryQueryUseCase registryQueryUseCase,
            FarmGoatHistoricalReproductionQueryPort queryPort
    ) {
        this.registryQueryUseCase = Objects.requireNonNull(registryQueryUseCase, "registryQueryUseCase must not be null");
        this.queryPort = Objects.requireNonNull(queryPort, "queryPort must not be null");
    }

    @Override
    public Optional<FarmGoatHistoricalReproductionSnapshot> findHistoricalReproduction(long farmId, GoatId goatId) {
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

        // 1. Load candidate facts from persistence adapter
        List<HistoricalReproductionEventItem> candidateEvents =
                queryPort.findCandidateEventsByGoat(goatId, registrationNumber);
        List<HistoricalReproductionProcessCandidate> candidateProcesses =
                queryPort.findCandidateProcessesByGoat(goatId, registrationNumber);

        // 2. Strict event visibility: event.farmId == farmId
        List<HistoricalReproductionEventItem> visibleEvents = candidateEvents.stream()
                .filter(e -> Objects.equals(e.farmId(), farmId))
                .sorted(EVENT_COMPARATOR)
                .toList();

        // Map events by ID for coverage and closure lookup
        Map<Long, HistoricalReproductionEventItem> eventById = candidateEvents.stream()
                .collect(Collectors.toMap(HistoricalReproductionEventItem::id, Function.identity(), (a, b) -> a));

        // Group closure events by pregnancyId
        Map<Long, List<HistoricalReproductionEventItem>> closureEventsByPregnancy = candidateEvents.stream()
                .filter(e -> e.pregnancyId() != null)
                .filter(e -> e.eventType() == ReproductiveEventType.PREGNANCY_CLOSE)
                .collect(Collectors.groupingBy(HistoricalReproductionEventItem::pregnancyId));

        // 3. Project processes according to safe field-by-field visibility
        List<HistoricalReproductionProcessItem> visibleProcesses = new ArrayList<>();
        for (HistoricalReproductionProcessCandidate candidate : candidateProcesses) {
            projectProcessForFarm(farmId, goatId, candidate, eventById, closureEventsByPregnancy)
                    .ifPresent(visibleProcesses::add);
        }

        visibleProcesses.sort(PROCESS_COMPARATOR);

        return Optional.of(new FarmGoatHistoricalReproductionSnapshot(
                goatId,
                visibleProcesses,
                visibleEvents
        ));
    }

    private Optional<HistoricalReproductionProcessItem> projectProcessForFarm(
            long farmId,
            GoatId goatId,
            HistoricalReproductionProcessCandidate candidate,
            Map<Long, HistoricalReproductionEventItem> eventById,
            Map<Long, List<HistoricalReproductionEventItem>> closureEventsByPregnancy
    ) {
        boolean isOwnPregnancy = Objects.equals(candidate.farmId(), farmId);
        List<HistoricalReproductionEventItem> closuresForPregnancy =
                closureEventsByPregnancy.getOrDefault(candidate.id(), List.of());

        // Check if requesting farm has an explicit PREGNANCY_CLOSE event for this pregnancy
        boolean hasOwnClosure = closuresForPregnancy.stream()
                .anyMatch(e -> Objects.equals(e.farmId(), farmId));

        // Visibility authorization:
        // Case A: requesting farm owns the pregnancy
        // Case D: requesting farm owns an explicit closure event referencing this foreign pregnancy
        if (!isOwnPregnancy && !hasOwnClosure) {
            // Case B: foreign pregnancy, even if referencing requesting farm's coverage, is NOT visible!
            return Optional.empty();
        }

        // Closure correlation evaluation:
        // Invariant: exactly 1 closure event, farmId == requestingFarmId, eventDate == closedAt, closedAt != null
        ClosureCorrelation closureCorrelation = evaluateClosureCorrelation(farmId, goatId, candidate, closuresForPregnancy);

        if (isOwnPregnancy) {
            // Case A: Pregnancy belongs to requesting farm
            MinimalForeignCoverageContext foreignCoverage = null;
            if (candidate.coverageEventId() != null) {
                HistoricalReproductionEventItem coverage = eventById.get(candidate.coverageEventId());
                if (coverage != null && !Objects.equals(coverage.farmId(), farmId)) {
                    // Case C: requesting farm owns pregnancy, coverage was on foreign farm -> minimal neutral foreign coverage context
                    foreignCoverage = new MinimalForeignCoverageContext(
                            coverage.id(),
                            coverage.farmId(),
                            coverage.eventDate(),
                            coverage.breedingType(),
                            coverage.breederRef()
                    );
                }
            }

            PregnancyStatus safeStatus = resolveSafeStatus(candidate, closureCorrelation);

            return Optional.of(new HistoricalReproductionProcessItem(
                    candidate.id(),
                    goatId,
                    candidate.farmId(),
                    candidate.breedingDate(),
                    candidate.confirmDate(),
                    candidate.expectedDueDate(),
                    candidate.coverageEventId(),
                    safeStatus,
                    closureCorrelation.closedAt(),
                    closureCorrelation.closeReason(),
                    foreignCoverage
            ));
        } else {
            // Case D: Foreign pregnancy with requesting-farm-owned closure event
            // Expose ONLY minimal process context + its own closure. Redact confirmDate and expectedDueDate.
            return Optional.of(new HistoricalReproductionProcessItem(
                    candidate.id(),
                    goatId,
                    candidate.farmId(),
                    candidate.breedingDate(),
                    null, // confirmDate redacted
                    null, // expectedDueDate redacted
                    candidate.coverageEventId(),
                    closureCorrelation.status(), // CLOSED
                    closureCorrelation.closedAt(),
                    closureCorrelation.closeReason(),
                    null // foreignCoverageContext not applicable
            ));
        }
    }

    private ClosureCorrelation evaluateClosureCorrelation(
            long farmId,
            GoatId goatId,
            HistoricalReproductionProcessCandidate candidate,
            List<HistoricalReproductionEventItem> closuresForPregnancy
    ) {
        if (closuresForPregnancy.size() != 1) {
            // Missing (0) or ambiguous (>1) closure event -> fail closed
            return ClosureCorrelation.UNEXPOSED;
        }

        HistoricalReproductionEventItem closureEvent = closuresForPregnancy.get(0);

        if (!Objects.equals(closureEvent.farmId(), farmId)) {
            // Closure belongs to another farm -> fail closed
            return ClosureCorrelation.UNEXPOSED;
        }

        if (!Objects.equals(closureEvent.goatId(), goatId)) {
            // GoatId mismatch -> fail closed
            return ClosureCorrelation.UNEXPOSED;
        }

        if (candidate.closedAt() == null || !Objects.equals(closureEvent.eventDate(), candidate.closedAt())) {
            // Date mismatch or missing closedAt -> fail closed
            return ClosureCorrelation.UNEXPOSED;
        }

        // All correlation invariants satisfied
        return new ClosureCorrelation(
                candidate.closedAt(),
                candidate.closeReason(),
                PregnancyStatus.CLOSED
        );
    }

    private PregnancyStatus resolveSafeStatus(
            HistoricalReproductionProcessCandidate candidate,
            ClosureCorrelation closureCorrelation
    ) {
        if (closureCorrelation.isCorrelated()) {
            return PregnancyStatus.CLOSED;
        }

        if (candidate.status() == PregnancyStatus.ACTIVE && candidate.closedAt() == null) {
            // Pregnancy is active and was not closed
            return PregnancyStatus.ACTIVE;
        }

        // Pregnancy is CLOSED in DB, but closure was not safely correlated to requesting farm
        // (e.g. closed by another farm or missing/ambiguous closure event).
        // Must NOT leak CLOSED, and must NOT fabricate ACTIVE. Fail closed (null / unexposed).
        return null;
    }

    private record ClosureCorrelation(
            LocalDate closedAt,
            PregnancyCloseReason closeReason,
            PregnancyStatus status
    ) {
        static final ClosureCorrelation UNEXPOSED = new ClosureCorrelation(null, null, null);

        boolean isCorrelated() {
            return closedAt != null && closeReason != null;
        }
    }
}
