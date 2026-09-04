package com.devmaster.goatfarm.inventory.business.inventoryservice;

import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.inventory.application.ports.out.InventoryMovementPersistencePort;
import com.devmaster.goatfarm.inventory.business.bo.InventoryBalanceSnapshotVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryIdempotencyVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryItemSnapshotVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryMovementCreateRequestVO;
import com.devmaster.goatfarm.inventory.domain.enums.InventoryMovementType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
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
        assertThat(result.response().subtotalCost()).isEqualByComparingTo("185.00");
        assertThat(result.response().freightCost()).isEqualByComparingTo("0.00");
        assertThat(result.response().discountAmount()).isEqualByComparingTo("0.00");
        assertThat(result.response().purchaseDate()).isEqualTo(LocalDate.of(2026, 3, 28));
        assertThat(result.response().supplierName()).isEqualTo("Casa do Campo");

        ArgumentCaptor<InventoryIdempotencyVO> captor = ArgumentCaptor.forClass(InventoryIdempotencyVO.class);
        verify(persistencePort).saveIdempotency(captor.capture());
        String legacyCanonical = "IN|10|10|||2026-03-28|compra de racao|18.5|185|2026-03-28|Casa do Campo";
        assertThat(captor.getValue().requestHash()).isEqualTo(sha256(legacyCanonical));
    }

    @Test
    void createMovement_shouldCalculateFinalCostWithFreightAndDiscount() {
        stubFixedClock();

        Long farmId = 1L;
        Long itemId = 10L;
        InventoryMovementCreateRequestVO request = new InventoryMovementCreateRequestVO(
                InventoryMovementType.IN,
                new BigDecimal("32.143"),
                itemId,
                null,
                null,
                LocalDate.of(2026, 8, 1),
                "compra de insumo",
                new BigDecimal("112.0000"),
                null,
                new BigDecimal("45.50"),
                new BigDecimal("12.25"),
                LocalDate.of(2026, 8, 1),
                "Durrancho"
        );

        when(persistencePort.findIdempotency(farmId, "purchase-cost-breakdown")).thenReturn(Optional.empty());
        when(persistencePort.findItemSnapshot(farmId, itemId)).thenReturn(Optional.of(new InventoryItemSnapshotVO(itemId, false)));
        when(persistencePort.lockItemForUpdate(farmId, itemId)).thenReturn(Optional.of(new InventoryItemSnapshotVO(itemId, false)));
        when(persistencePort.lockBalanceForUpdate(farmId, itemId, null))
                .thenReturn(Optional.of(new InventoryBalanceSnapshotVO(farmId, itemId, null, BigDecimal.ZERO)));
        when(persistencePort.saveMovement(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(persistencePort.upsertBalance(any()))
                .thenReturn(new InventoryBalanceSnapshotVO(farmId, itemId, null, new BigDecimal("32.143")));
        when(persistencePort.saveIdempotency(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = inventoryMovementBusiness.createMovement(farmId, "purchase-cost-breakdown", request);

        assertThat(result.response().unitCost()).isEqualByComparingTo("112.0000");
        assertThat(result.response().subtotalCost()).isEqualByComparingTo("3600.02");
        assertThat(result.response().freightCost()).isEqualByComparingTo("45.50");
        assertThat(result.response().discountAmount()).isEqualByComparingTo("12.25");
        assertThat(result.response().totalCost()).isEqualByComparingTo("3633.27");
    }

    @Test
    void createMovement_shouldDeriveMerchandiseUnitCostFromLegacyTotalWithFreightAndDiscount() {
        stubFixedClock();

        Long farmId = 1L;
        Long itemId = 10L;
        InventoryMovementCreateRequestVO request = new InventoryMovementCreateRequestVO(
                InventoryMovementType.IN,
                new BigDecimal("10.000"),
                itemId,
                null,
                null,
                LocalDate.of(2026, 8, 1),
                "compra",
                null,
                new BigDecimal("205.00"),
                new BigDecimal("25.00"),
                new BigDecimal("5.00"),
                LocalDate.of(2026, 8, 1),
                "Fornecedor"
        );

        when(persistencePort.findIdempotency(farmId, "purchase-total-only")).thenReturn(Optional.empty());
        when(persistencePort.findItemSnapshot(farmId, itemId)).thenReturn(Optional.of(new InventoryItemSnapshotVO(itemId, false)));
        when(persistencePort.lockItemForUpdate(farmId, itemId)).thenReturn(Optional.of(new InventoryItemSnapshotVO(itemId, false)));
        when(persistencePort.lockBalanceForUpdate(farmId, itemId, null))
                .thenReturn(Optional.of(new InventoryBalanceSnapshotVO(farmId, itemId, null, BigDecimal.ZERO)));
        when(persistencePort.saveMovement(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(persistencePort.upsertBalance(any()))
                .thenReturn(new InventoryBalanceSnapshotVO(farmId, itemId, null, new BigDecimal("10.000")));
        when(persistencePort.saveIdempotency(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = inventoryMovementBusiness.createMovement(farmId, "purchase-total-only", request);

        assertThat(result.response().unitCost()).isEqualByComparingTo("18.5000");
        assertThat(result.response().subtotalCost()).isEqualByComparingTo("185.00");
        assertThat(result.response().totalCost()).isEqualByComparingTo("205.00");
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

        assertThat(exception.getMessage()).contains("subtotal + freightCost - discountAmount");
    }

    @Test
    void createMovement_shouldRejectDiscountThatMakesFinalCostNonPositive() {
        InventoryMovementCreateRequestVO request = new InventoryMovementCreateRequestVO(
                InventoryMovementType.IN,
                new BigDecimal("5.000"),
                10L,
                null,
                null,
                LocalDate.of(2026, 8, 1),
                "compra",
                new BigDecimal("10.0000"),
                null,
                BigDecimal.ZERO,
                new BigDecimal("50.00"),
                LocalDate.of(2026, 8, 1),
                "Fornecedor"
        );

        InvalidArgumentException exception = assertThrows(
                InvalidArgumentException.class,
                () -> inventoryMovementBusiness.createMovement(1L, "purchase-invalid-discount", request)
        );

        assertThat(exception.getMessage()).contains("desconto");
    }

    private void stubFixedClock() {
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(Instant.parse("2026-03-28T14:00:00Z"));
    }

    private String sha256(String value) {
        try {
            return java.util.HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))
            );
        } catch (java.security.NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
