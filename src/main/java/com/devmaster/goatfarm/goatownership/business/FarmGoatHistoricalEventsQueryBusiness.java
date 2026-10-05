package com.devmaster.goatfarm.goatownership.business;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatHistoricalEventsSnapshot;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatRegistryItem;
import com.devmaster.goatfarm.goatownership.application.model.HistoricalOperationalEventItem;
import com.devmaster.goatfarm.goatownership.application.ports.in.FarmGoatHistoricalEventsQueryUseCase;
import com.devmaster.goatfarm.goatownership.application.ports.in.FarmGoatRegistryQueryUseCase;
import com.devmaster.goatfarm.goatownership.application.ports.out.FarmGoatHistoricalEventsQueryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Coordinates historical event dossier reads. Registry membership establishes
 * the requesting context; immutable recording provenance establishes visibility.
 */
@Service
@Transactional(readOnly = true)
public class FarmGoatHistoricalEventsQueryBusiness implements FarmGoatHistoricalEventsQueryUseCase {

    private static final Comparator<HistoricalOperationalEventItem> EVENT_COMPARATOR =
            Comparator.comparing(HistoricalOperationalEventItem::date, Comparator.reverseOrder())
                    .thenComparing(HistoricalOperationalEventItem::id, Comparator.reverseOrder());

    private final FarmGoatRegistryQueryUseCase registryQueryUseCase;
    private final FarmGoatHistoricalEventsQueryPort queryPort;

    public FarmGoatHistoricalEventsQueryBusiness(
            FarmGoatRegistryQueryUseCase registryQueryUseCase,
            FarmGoatHistoricalEventsQueryPort queryPort
    ) {
        this.registryQueryUseCase = Objects.requireNonNull(registryQueryUseCase, "registryQueryUseCase must not be null");
        this.queryPort = Objects.requireNonNull(queryPort, "queryPort must not be null");
    }

    @Override
    public Optional<FarmGoatHistoricalEventsSnapshot> findHistoricalEvents(long farmId, GoatId goatId) {
        if (farmId <= 0) {
            throw new IllegalArgumentException("farmId must be positive");
        }
        if (goatId == null) {
            throw new IllegalArgumentException("goatId must not be null");
        }

        Optional<FarmGoatRegistryItem> registryItem = registryQueryUseCase.findForFarmAndGoat(farmId, goatId);
        if (registryItem.isEmpty()) {
            return Optional.empty();
        }

        List<HistoricalOperationalEventItem> events = queryPort.findHistoricalEvents(goatId, farmId);
        List<HistoricalOperationalEventItem> visibleEvents = events == null ? List.of() : events.stream()
                .filter(Objects::nonNull)
                .filter(event -> goatId.equals(event.goatId()))
                .filter(event -> Long.valueOf(farmId).equals(event.recordingFarmId()))
                .sorted(EVENT_COMPARATOR)
                .toList();

        return Optional.of(new FarmGoatHistoricalEventsSnapshot(goatId, visibleEvents));
    }
}
