package com.devmaster.goatfarm.health.persistence.adapter;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.application.model.HistoricalHealthEventItem;
import com.devmaster.goatfarm.health.domain.enums.AdministrationRoute;
import com.devmaster.goatfarm.health.domain.enums.DoseUnit;
import com.devmaster.goatfarm.health.domain.enums.HealthEventStatus;
import com.devmaster.goatfarm.health.domain.enums.HealthEventType;
import com.devmaster.goatfarm.health.persistence.entity.HealthEvent;
import com.devmaster.goatfarm.health.persistence.repository.HealthEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FarmGoatHistoricalHealthPersistenceAdapterTest {

    @Mock
    private HealthEventRepository repository;

    private FarmGoatHistoricalHealthPersistenceAdapter adapter;

    private static final GoatId GOAT_ID = GoatId.of(42L);
    private static final Long FARM_ID = 7L;

    @BeforeEach
    void setUp() {
        adapter = new FarmGoatHistoricalHealthPersistenceAdapter(repository);
    }

    private HealthEvent createEntity(Long id, Long goatTechnicalId, Long farmId, HealthEventType type, HealthEventStatus status) {
        HealthEvent entity = new HealthEvent();
        entity.setId(id);
        entity.setGoatTechnicalId(goatTechnicalId);
        entity.setGoatId("RG-42");
        entity.setFarmId(farmId);
        entity.setType(type);
        entity.setStatus(status);
        entity.setTitle("Vacinação Anti-rábica");
        entity.setDescription("Dose de reforço anual");
        entity.setScheduledDate(LocalDate.of(2026, 3, 10));
        entity.setPerformedAt(LocalDateTime.of(2026, 3, 10, 9, 30));
        entity.setResponsible("Dra. Paula");
        entity.setNotes("Animal calmo");
        entity.setProductName("Rabivac");
        entity.setActiveIngredient("Vírus inativado");
        entity.setDose(new BigDecimal("2.000"));
        entity.setDoseUnit(DoseUnit.ML);
        entity.setRoute(AdministrationRoute.SC);
        entity.setBatchNumber("RV-2026-A");
        entity.setWithdrawalMilkDays(0);
        entity.setWithdrawalMeatDays(21);
        return entity;
    }

    @Test
    @DisplayName("Queries repository with exact technical id and farm id, mapping all fields correctly")
    void queriesRepositoryAndMapsCorrectly() {
        HealthEvent entity = createEntity(101L, GOAT_ID.value(), FARM_ID, HealthEventType.VACINA, HealthEventStatus.REALIZADO);
        when(repository.findHistoricalHealthEvents(GOAT_ID.value(), FARM_ID)).thenReturn(List.of(entity));

        List<HistoricalHealthEventItem> items = adapter.findHistoricalHealthEvents(GOAT_ID, FARM_ID);

        assertThat(items).hasSize(1);
        HistoricalHealthEventItem item = items.get(0);
        assertThat(item.id()).isEqualTo(101L);
        assertThat(item.goatId()).isEqualTo(GOAT_ID);
        assertThat(item.farmId()).isEqualTo(FARM_ID);
        assertThat(item.type()).isEqualTo(HealthEventType.VACINA);
        assertThat(item.status()).isEqualTo(HealthEventStatus.REALIZADO);
        assertThat(item.title()).isEqualTo("Vacinação Anti-rábica");
        assertThat(item.description()).isEqualTo("Dose de reforço anual");
        assertThat(item.scheduledDate()).isEqualTo(LocalDate.of(2026, 3, 10));
        assertThat(item.performedAt()).isEqualTo(LocalDateTime.of(2026, 3, 10, 9, 30));
        assertThat(item.responsible()).isEqualTo("Dra. Paula");
        assertThat(item.notes()).isEqualTo("Animal calmo");
        assertThat(item.productName()).isEqualTo("Rabivac");
        assertThat(item.activeIngredient()).isEqualTo("Vírus inativado");
        assertThat(item.dose()).isEqualByComparingTo(new BigDecimal("2.000"));
        assertThat(item.doseUnit()).isEqualTo(DoseUnit.ML);
        assertThat(item.route()).isEqualTo(AdministrationRoute.SC);
        assertThat(item.batchNumber()).isEqualTo("RV-2026-A");
        assertThat(item.withdrawalMilkDays()).isEqualTo(0);
        assertThat(item.withdrawalMeatDays()).isEqualTo(21);

        verify(repository).findHistoricalHealthEvents(GOAT_ID.value(), FARM_ID);
    }

    @Test
    @DisplayName("Null goatId returns empty list without querying repository")
    void nullGoatIdReturnsEmpty() {
        List<HistoricalHealthEventItem> items = adapter.findHistoricalHealthEvents(null, FARM_ID);

        assertThat(items).isEmpty();
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("Invalid farmId returns empty list without querying repository")
    void invalidFarmIdReturnsEmpty() {
        List<HistoricalHealthEventItem> zero = adapter.findHistoricalHealthEvents(GOAT_ID, 0L);
        List<HistoricalHealthEventItem> negative = adapter.findHistoricalHealthEvents(GOAT_ID, -1L);

        assertThat(zero).isEmpty();
        assertThat(negative).isEmpty();
        verifyNoInteractions(repository);
    }
}
