package com.devmaster.goatfarm.goatownership.business;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatHistoricalHealthSnapshot;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatRegistryItem;
import com.devmaster.goatfarm.goatownership.application.model.HistoricalHealthEventItem;
import com.devmaster.goatfarm.goatownership.application.ports.in.FarmGoatHistoricalHealthQueryUseCase;
import com.devmaster.goatfarm.goatownership.application.ports.in.FarmGoatRegistryQueryUseCase;
import com.devmaster.goatfarm.goatownership.application.ports.out.FarmGoatHistoricalHealthQueryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Application service that coordinates historical health dossier queries.
 * Validates registry membership and ensures strict explicit farm attribution.
 */
@Service
@Transactional(readOnly = true)
public class FarmGoatHistoricalHealthQueryBusiness implements FarmGoatHistoricalHealthQueryUseCase {

    private static final Comparator<HistoricalHealthEventItem> HEALTH_EVENT_COMPARATOR =
            Comparator.comparing(HistoricalHealthEventItem::scheduledDate, Comparator.reverseOrder())
                    .thenComparing(HistoricalHealthEventItem::id, Comparator.reverseOrder());

    private final FarmGoatRegistryQueryUseCase registryQueryUseCase;
    private final FarmGoatHistoricalHealthQueryPort queryPort;

    public FarmGoatHistoricalHealthQueryBusiness(
            FarmGoatRegistryQueryUseCase registryQueryUseCase,
            FarmGoatHistoricalHealthQueryPort queryPort
    ) {
        this.registryQueryUseCase = Objects.requireNonNull(registryQueryUseCase, "registryQueryUseCase must not be null");
        this.queryPort = Objects.requireNonNull(queryPort, "queryPort must not be null");
    }

    @Override
    public Optional<FarmGoatHistoricalHealthSnapshot> findHistoricalHealth(long farmId, GoatId goatId) {
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

        List<HistoricalHealthEventItem> rawEvents = queryPort.findHistoricalHealthEvents(goatId, farmId);
        List<HistoricalHealthEventItem> sortedEvents = rawEvents == null
                ? List.of()
                : rawEvents.stream()
                .filter(event -> event != null
                        && Objects.equals(event.farmId(), farmId)
                        && Objects.equals(event.goatId(), goatId))
                .sorted(HEALTH_EVENT_COMPARATOR)
                .toList();

        return Optional.of(new FarmGoatHistoricalHealthSnapshot(goatId, sortedEvents));
    }
}
