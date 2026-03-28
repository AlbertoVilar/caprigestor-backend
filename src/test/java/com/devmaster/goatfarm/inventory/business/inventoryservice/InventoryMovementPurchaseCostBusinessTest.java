package com.devmaster.goatfarm.inventory.business.inventoryservice;

import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.inventory.application.ports.out.InventoryMovementPersistencePort;
import com.devmaster.goatfarm.inventory.business.bo.InventoryBalanceSnapshotVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryItemSnapshotVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryMovementCreateRequestVO;
import com.devmaster.goatfarm.inventory.domain.enums.InventoryMovementType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryMovementPurchaseCostBusinessTest {

    @Mock
    private InventoryMovementPersistencePort persistencePort;

    @Mock
    private Clock clock;

    @InjectMocks
    private InventoryMovementBusiness inventoryMovementBusiness;

    @Test
    void createMovement_shouldRejectPurchaseCostOutsideEntryType() {
        InventoryMovementCreateRequestVO request = new InventoryMovementCreateRequestVO(
                InventoryMovementType.OUT,
                new BigDecimal("2.000"),
                10L,
                null,
                null,
                LocalDate.of(2026, 3, 28),
                "baixa",
                new BigDecimal("18.50"),
                new BigDecimal("37.00"),
                LocalDate.of(2026, 3, 28),
                "Fornecedor"
        );

        InvalidArgumentException exception = assertThrows(
                InvalidArgumentException.class,
                () -> inventoryMovementBusiness.createMovement(1L, "purchase-cost-out", request)
        );

        assertThat(exception.getMessage()).contains("type=IN");
    }

    @Test
    void createMovement_shouldRejectPurchaseCostWithoutPurchaseDate() {
        InventoryMovementCreateRequestVO request = new InventoryMovementCreateRequestVO(
                InventoryMovementType.IN,
                new BigDecimal("10.000"),
                10L,
                null,
                null,
                LocalDate.of(2026, 3, 28),
                "entrada",
                new BigDecimal("18.50"),
                null,
                null,
                "Fornecedor"
        );

        InvalidArgumentException exception = assertThrows(
                InvalidArgumentException.class,
                () -> inventoryMovementBusiness.createMovement(1L, "purchase-cost-no-date", request)
        );

        assertThat(exception.getMessage()).contains("purchaseDate");
    }

    @Test
    void createMovement_shouldDeriveMissingTotalCostForPurchaseEntry() {
        stubFixedClock();

        Long farmId = 1L;
        Long itemId = 10L;
        InventoryMovementCreateRequestVO request = new InventoryMovementCreateRequestVO(
                InventoryMovementType.IN,
                new BigDecimal("10.000"),
                itemId,
                null,
                null,
                LocalDate.of(2026, 3, 28),
                "compra de racao",
                new BigDecimal("18.5000"),
                null,
                LocalDate.of(2026, 3, 28),
                "Casa do Campo"
        );

        when(persistencePort.findIdempotency(farmId, "purchase-cost-ok")).thenReturn(Optional.empty());
        when(persistencePort.findItemSnapshot(farmId, itemId)).thenReturn(Optional.of(new InventoryItemSnapshotVO(itemId, false)));
        when(persistencePort.lockItemForUpdate(farmId, itemId)).thenReturn(Optional.of(new InventoryItemSnapshotVO(itemId, false)));
        when(persistencePort.lockBalanceForUpdate(farmId, itemId, null))
                .thenReturn(Optional.of(new InventoryBalanceSnapshotVO(farmId, itemId, null, new BigDecimal("2.000"))));
        when(persistencePort.saveMovement(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(persistencePort.upsertBalance(any()))
                .thenReturn(new InventoryBalanceSnapshotVO(farmId, itemId, null, new BigDecimal("12.000")));
        when(persistencePort.saveIdempotency(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = inventoryMovementBusiness.createMovement(farmId, "purchase-cost-ok", request);

        assertThat(result.response().totalCost()).isEqualByComparingTo("185.00");
        assertThat(result.response().unitCost()).isEqualByComparingTo("18.5000");
        assertThat(result.response().purchaseDate()).isEqualTo(LocalDate.of(2026, 3, 28));
        assertThat(result.response().supplierName()).isEqualTo("Casa do Campo");
    }

    @Test
    void createMovement_shouldRejectInconsistentQuantityUnitCostAndTotalCost() {
        InventoryMovementCreateRequestVO request = new InventoryMovementCreateRequestVO(
                InventoryMovementType.IN,
                new BigDecimal("5.000"),
                10L,
                null,
                null,
                LocalDate.of(2026, 3, 28),
                "compra",
                new BigDecimal("10.0000"),
                new BigDecimal("80.00"),
                LocalDate.of(2026, 3, 28),
                "Fornecedor"
        );

        InvalidArgumentException exception = assertThrows(
                InvalidArgumentException.class,
                () -> inventoryMovementBusiness.createMovement(1L, "purchase-cost-mismatch", request)
        );

        assertThat(exception.getMessage()).contains("quantity x unitCost");
    }

    private void stubFixedClock() {
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(Instant.parse("2026-03-28T14:00:00Z"));
    }
}
