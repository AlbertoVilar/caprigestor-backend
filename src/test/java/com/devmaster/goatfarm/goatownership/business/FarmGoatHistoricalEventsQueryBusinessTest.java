package com.devmaster.goatfarm.goatownership.business;

import com.devmaster.goatfarm.events.enums.EventType;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatHistoricalEventsSnapshot;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatRegistryDisposition;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatRegistryItem;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatRegistryRole;
import com.devmaster.goatfarm.goatownership.application.model.HistoricalOperationalEventItem;
import com.devmaster.goatfarm.goatownership.application.ports.in.FarmGoatRegistryQueryUseCase;
import com.devmaster.goatfarm.goatownership.application.ports.out.FarmGoatHistoricalEventsQueryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FarmGoatHistoricalEventsQueryBusinessTest {

    private static final long FARM_A = 10L;
    private static final long FARM_B = 20L;
    private static final GoatId GOAT = GoatId.of(42L);
    private static final GoatId SAME_RG_OTHER_GOAT = GoatId.of(43L);

    @Mock private FarmGoatRegistryQueryUseCase registryQueryUseCase;
    @Mock private FarmGoatHistoricalEventsQueryPort queryPort;

    private FarmGoatHistoricalEventsQueryBusiness business;

    @BeforeEach
    void setUp() {
        business = new FarmGoatHistoricalEventsQueryBusiness(registryQueryUseCase, queryPort);
    }

    @Test
    void formerOwnerSeesOnlyItsAttributedEventsAndNotFutureEventsFromCurrentOwner() {
        when(registryQueryUseCase.findForFarmAndGoat(FARM_A, GOAT)).thenReturn(Optional.of(registryItem(FARM_A)));
        when(queryPort.findHistoricalEvents(GOAT, FARM_A)).thenReturn(List.of(
                event(1L, GOAT, FARM_A, LocalDate.of(2026, 1, 5)),
                event(2L, GOAT, FARM_B, LocalDate.of(2026, 3, 5))
        ));

        FarmGoatHistoricalEventsSnapshot snapshot = business.findHistoricalEvents(FARM_A, GOAT).orElseThrow();

        assertThat(snapshot.events()).extracting(HistoricalOperationalEventItem::id).containsExactly(1L);
        assertThat(snapshot.events()).allMatch(item -> item.recordingFarmId().equals(FARM_A));
    }

    @Test
    void sameRegistrationButDifferentStructuralGoatIdIsExcludedFailClosed() {
        when(registryQueryUseCase.findForFarmAndGoat(FARM_A, GOAT)).thenReturn(Optional.of(registryItem(FARM_A)));
        when(queryPort.findHistoricalEvents(GOAT, FARM_A)).thenReturn(List.of(
                event(1L, GOAT, FARM_A, LocalDate.of(2026, 1, 5)),
                event(2L, SAME_RG_OTHER_GOAT, FARM_A, LocalDate.of(2026, 1, 6))
        ));

        FarmGoatHistoricalEventsSnapshot snapshot = business.findHistoricalEvents(FARM_A, GOAT).orElseThrow();

        assertThat(snapshot.events()).extracting(HistoricalOperationalEventItem::id).containsExactly(1L);
    }

    @Test
    void relatedFarmWithNoAttributedEventsReceivesAnEmptySnapshot() {
        when(registryQueryUseCase.findForFarmAndGoat(FARM_A, GOAT)).thenReturn(Optional.of(registryItem(FARM_A)));
        when(queryPort.findHistoricalEvents(GOAT, FARM_A)).thenReturn(List.of());

        assertThat(business.findHistoricalEvents(FARM_A, GOAT).orElseThrow().events()).isEmpty();
    }

    @Test
    void unrelatedFarmReceivesNoSnapshot() {
        when(registryQueryUseCase.findForFarmAndGoat(FARM_B, GOAT)).thenReturn(Optional.empty());

        assertThat(business.findHistoricalEvents(FARM_B, GOAT)).isEmpty();
    }

    private FarmGoatRegistryItem registryItem(long farmId) {
        return new FarmGoatRegistryItem(GOAT, "RG-SHARED", "Estrela", GoatStatus.ATIVO, farmId, "Farm",
                Set.of(FarmGoatRegistryRole.FORMER_OWNER), FarmGoatRegistryDisposition.TRANSFERRED, FARM_B);
    }

    private HistoricalOperationalEventItem event(Long id, GoatId goatId, long farmId, LocalDate date) {
        return new HistoricalOperationalEventItem(id, goatId, farmId, EventType.VACINACAO, date,
                "Vaccination", "Barn", "Vet", "Completed");
    }
}
