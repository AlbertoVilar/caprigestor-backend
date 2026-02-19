package com.devmaster.goatfarm.inventory.business.inventoryservice;

import com.devmaster.goatfarm.config.exceptions.DuplicateEntityException;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.inventory.business.bo.InventoryBalanceSnapshotVO;
import com.devmaster.goatfarm.inventory.application.ports.out.InventoryMovementPersistencePort;
import com.devmaster.goatfarm.inventory.business.bo.InventoryIdempotencyVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryItemSnapshotVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryMovementCreateRequestVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryMovementResultVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryMovementResponseVO;
import com.devmaster.goatfarm.inventory.domain.enums.InventoryAdjustDirection;
import com.devmaster.goatfarm.inventory.domain.enums.InventoryMovementType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryMovementBusinessTest {

    @Mock
    private InventoryMovementPersistencePort persistencePort;

    @Mock
    private Clock clock;

    @InjectMocks
    private InventoryMovementBusiness inventoryMovementBusiness;

    @Test
    void createMovement_shouldThrowInvalidArgument_whenFarmIdIsNull() {
        InvalidArgumentException exception = assertThrows(InvalidArgumentException.class,
                () -> inventoryMovementBusiness.createMovement(null, "idem-key", validRequest()));

        assertThat(exception.getMessage()).contains("farmId e obrigatorio.");
        verifyNoInteractions(persistencePort);
    }

    @Test
    void createMovement_shouldThrowInvalidArgument_whenIdempotencyKeyIsBlank() {
        InvalidArgumentException exception = assertThrows(InvalidArgumentException.class,
                () -> inventoryMovementBusiness.createMovement(1L, "   ", validRequest()));

        assertThat(exception.getMessage()).contains("Idempotency-Key e obrigatorio.");
        verifyNoInteractions(persistencePort);
    }

    @Test
    void createMovement_shouldThrowInvalidArgument_whenRequestIsNull() {
        InvalidArgumentException exception = assertThrows(InvalidArgumentException.class,
                () -> inventoryMovementBusiness.createMovement(1L, "idem-key", null));

        assertThat(exception.getMessage()).contains("Payload da requisicao e obrigatorio.");
        verifyNoInteractions(persistencePort);
    }

    @Test
    void createMovement_shouldThrowInvalidArgument_whenQuantityIsNull() {
        InventoryMovementCreateRequestVO request = new InventoryMovementCreateRequestVO(
                InventoryMovementType.IN,
                null,
                10L,
                null,
                null,
                LocalDate.now(),
                "entrada"
        );

        InvalidArgumentException exception = assertThrows(InvalidArgumentException.class,
                () -> inventoryMovementBusiness.createMovement(1L, "idem-key", request));

        assertThat(exception.getMessage()).contains("Quantidade deve ser maior que zero.");
        verifyNoInteractions(persistencePort);
    }

    @Test
    void createMovement_shouldThrowInvalidArgument_whenQuantityIsZero() {
        InventoryMovementCreateRequestVO request = new InventoryMovementCreateRequestVO(
                InventoryMovementType.IN,
                BigDecimal.ZERO,
                10L,
                null,
                null,
                LocalDate.now(),
                "entrada"
        );

        InvalidArgumentException exception = assertThrows(InvalidArgumentException.class,
                () -> inventoryMovementBusiness.createMovement(1L, "idem-key", request));

        assertThat(exception.getMessage()).contains("Quantidade deve ser maior que zero.");
        verifyNoInteractions(persistencePort);
    }

    @Test
    void createMovement_shouldThrowInvalidArgument_whenAdjustTypeWithoutDirection() {
        InventoryMovementCreateRequestVO request = new InventoryMovementCreateRequestVO(
                InventoryMovementType.ADJUST,
                new BigDecimal("2.50"),
                10L,
                null,
                null,
                LocalDate.now(),
                "ajuste"
        );

        InvalidArgumentException exception = assertThrows(InvalidArgumentException.class,
                () -> inventoryMovementBusiness.createMovement(1L, "idem-key", request));

        assertThat(exception.getMessage()).contains("adjustDirection e obrigatorio quando o tipo e ADJUST.");
        verifyNoInteractions(persistencePort);
    }

    @Test
    void createMovement_shouldThrowInvalidArgument_whenInTypeWithAdjustDirection() {
        InventoryMovementCreateRequestVO request = new InventoryMovementCreateRequestVO(
                InventoryMovementType.IN,
                new BigDecimal("2.50"),
                10L,
                null,
                InventoryAdjustDirection.INCREASE,
                LocalDate.now(),
                "entrada"
        );

        InvalidArgumentException exception = assertThrows(InvalidArgumentException.class,
                () -> inventoryMovementBusiness.createMovement(1L, "idem-key", request));

        assertThat(exception.getMessage()).contains("adjustDirection deve ser nulo quando o tipo e IN ou OUT.");
        verifyNoInteractions(persistencePort);
    }

    @Test
    void createMovement_shouldThrowInvalidArgument_whenOutTypeWithAdjustDirection() {
        InventoryMovementCreateRequestVO request = new InventoryMovementCreateRequestVO(
                InventoryMovementType.OUT,
                new BigDecimal("2.50"),
                10L,
                null,
                InventoryAdjustDirection.DECREASE,
                LocalDate.now(),
                "saida"
        );

        InvalidArgumentException exception = assertThrows(InvalidArgumentException.class,
                () -> inventoryMovementBusiness.createMovement(1L, "idem-key", request));

        assertThat(exception.getMessage()).contains("adjustDirection deve ser nulo quando o tipo e IN ou OUT.");
        verifyNoInteractions(persistencePort);
    }

    @Test
    void replay_when_same_idempotency_key_and_same_payload_returns_previous_response() {
        Long farmId = 1L;
        String idempotencyKey = "idem-key-1";
        InventoryMovementCreateRequestVO request = validRequest();
        String requestHash = hashRequest(request);

        InventoryMovementResponseVO replayResponse = new InventoryMovementResponseVO(
                99L,
                InventoryMovementType.IN,
                new BigDecimal("1.00"),
                10L,
                null,
                request.movementDate(),
                new BigDecimal("15.50"),
                OffsetDateTime.now()
        );

        when(persistencePort.findIdempotency(farmId, idempotencyKey))
                .thenReturn(Optional.of(new InventoryIdempotencyVO(farmId, idempotencyKey, requestHash, replayResponse)));

        InventoryMovementResultVO result = inventoryMovementBusiness.createMovement(farmId, idempotencyKey, request);

        assertThat(result.replayed()).isTrue();
        assertThat(result.response()).isSameAs(replayResponse);
        verify(persistencePort).findIdempotency(farmId, idempotencyKey);
    }

    @Test
    void conflict_when_same_idempotency_key_and_different_payload_throws_409_exception() {
        Long farmId = 1L;
        String idempotencyKey = "idem-key-1";
        InventoryMovementCreateRequestVO request = validRequest();

        InventoryMovementResponseVO replayResponse = new InventoryMovementResponseVO(
                99L,
                InventoryMovementType.IN,
                new BigDecimal("1.00"),
                10L,
                null,
                request.movementDate(),
                new BigDecimal("15.50"),
                OffsetDateTime.now()
        );

        when(persistencePort.findIdempotency(farmId, idempotencyKey))
                .thenReturn(Optional.of(new InventoryIdempotencyVO(farmId, idempotencyKey, "hash-diferente", replayResponse)));

        DuplicateEntityException exception = assertThrows(DuplicateEntityException.class,
                () -> inventoryMovementBusiness.createMovement(farmId, idempotencyKey, request));

        assertThat(exception.getMessage()).contains("Idempotency-Key").contains("payload diferente");
        verify(persistencePort).findIdempotency(farmId, idempotencyKey);
    }

    @Test
    void replay_when_reason_has_extra_whitespace_should_still_match_same_hash() {
        Long farmId = 1L;
        String idempotencyKey = "idem-1";
        InventoryMovementCreateRequestVO requestA = new InventoryMovementCreateRequestVO(
                InventoryMovementType.OUT,
                new BigDecimal("2.50"),
                10L,
                null,
                null,
                LocalDate.of(2026, 2, 13),
                "ra\u00e7\u00e3o  premium"
        );
        InventoryMovementCreateRequestVO requestB = new InventoryMovementCreateRequestVO(
                InventoryMovementType.OUT,
                new BigDecimal("2.50"),
                10L,
                null,
                null,
                LocalDate.of(2026, 2, 13),
                "ra\u00e7\u00e3o premium"
        );

        String requestHash = hashRequest(requestA);
        InventoryMovementResponseVO replayResponse = new InventoryMovementResponseVO(
                101L,
                InventoryMovementType.OUT,
                new BigDecimal("2.50"),
                10L,
                null,
                requestB.movementDate(),
                new BigDecimal("12.00"),
                OffsetDateTime.now()
        );

        when(persistencePort.findIdempotency(farmId, idempotencyKey))
                .thenReturn(Optional.of(new InventoryIdempotencyVO(farmId, idempotencyKey, requestHash, replayResponse)));

        InventoryMovementResultVO result = inventoryMovementBusiness.createMovement(farmId, idempotencyKey, requestB);

        assertThat(result.replayed()).isTrue();
        assertThat(result.response()).isSameAs(replayResponse);
        verify(persistencePort).findIdempotency(farmId, idempotencyKey);
    }

    @Test
    void shouldThrowInvalidArgument_whenTrackLotTrue_andLotIdIsNull() {
        Long farmId = 1L;
        String idempotencyKey = "k-1";
        Long itemId = 10L;
        InventoryMovementCreateRequestVO request = new InventoryMovementCreateRequestVO(
                InventoryMovementType.IN,
                new BigDecimal("1.0"),
                itemId,
                null,
                null,
                LocalDate.of(2026, 2, 13),
                "entrada"
        );

        when(persistencePort.findIdempotency(farmId, idempotencyKey)).thenReturn(Optional.empty());
        when(persistencePort.findItemSnapshot(farmId, itemId)).thenReturn(Optional.of(new InventoryItemSnapshotVO(itemId, true)));

        InvalidArgumentException exception = assertThrows(InvalidArgumentException.class,
                () -> inventoryMovementBusiness.createMovement(farmId, idempotencyKey, request));

        assertThat(exception.getMessage()).contains("lotId");
        verify(persistencePort, never()).lockBalanceForUpdate(any(), any(), any());
        verify(persistencePort, never()).saveMovement(any());
        verify(persistencePort, never()).saveIdempotency(any());
    }

    @Test
    void shouldThrowInvalidArgument_whenTrackLotFalse_andLotIdIsProvided() {
        Long farmId = 1L;
        String idempotencyKey = "k-1";
        Long itemId = 10L;
        InventoryMovementCreateRequestVO request = new InventoryMovementCreateRequestVO(
                InventoryMovementType.IN,
                new BigDecimal("1.0"),
                itemId,
                99L,
                null,
                LocalDate.of(2026, 2, 13),
                "entrada"
        );

        when(persistencePort.findIdempotency(farmId, idempotencyKey)).thenReturn(Optional.empty());
        when(persistencePort.findItemSnapshot(farmId, itemId)).thenReturn(Optional.of(new InventoryItemSnapshotVO(itemId, false)));

        InvalidArgumentException exception = assertThrows(InvalidArgumentException.class,
                () -> inventoryMovementBusiness.createMovement(farmId, idempotencyKey, request));

        assertThat(exception.getMessage()).contains("lotId");
        verify(persistencePort, never()).lockBalanceForUpdate(any(), any(), any());
        verify(persistencePort, never()).saveMovement(any());
        verify(persistencePort, never()).saveIdempotency(any());
    }

    @Test
    void shouldThrowResourceNotFound_whenItemSnapshotNotFound() {
        Long farmId = 1L;
        String idempotencyKey = "k-1";
        Long itemId = 10L;
        InventoryMovementCreateRequestVO request = new InventoryMovementCreateRequestVO(
                InventoryMovementType.IN,
                new BigDecimal("1.0"),
                itemId,
                null,
                null,
                LocalDate.of(2026, 2, 13),
                "entrada"
        );

        when(persistencePort.findIdempotency(farmId, idempotencyKey)).thenReturn(Optional.empty());
        when(persistencePort.findItemSnapshot(farmId, itemId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> inventoryMovementBusiness.createMovement(farmId, idempotencyKey, request));

        verify(persistencePort, never()).lockBalanceForUpdate(any(), any(), any());
        verify(persistencePort, never()).saveMovement(any());
        verify(persistencePort, never()).saveIdempotency(any());
    }

    @Test
    void shouldThrowBusinessRule_whenOutWouldMakeBalanceNegative() {
        Long farmId = 1L;
        String idempotencyKey = "out-1";
        Long itemId = 10L;

        InventoryMovementCreateRequestVO request = new InventoryMovementCreateRequestVO(
                InventoryMovementType.OUT,
                new BigDecimal("10.0"),
                itemId,
                null,
                null,
                LocalDate.of(2026, 2, 13),
                "saida"
        );

        when(persistencePort.findIdempotency(farmId, idempotencyKey)).thenReturn(Optional.empty());
        when(persistencePort.findItemSnapshot(farmId, itemId)).thenReturn(Optional.of(new InventoryItemSnapshotVO(itemId, false)));
        when(persistencePort.lockItemForUpdate(farmId, itemId)).thenReturn(Optional.of(new InventoryItemSnapshotVO(itemId, false)));
        when(persistencePort.lockBalanceForUpdate(farmId, itemId, null))
                .thenReturn(Optional.of(new InventoryBalanceSnapshotVO(farmId, itemId, null, new BigDecimal("5.0"))));

        BusinessRuleException exception = assertThrows(BusinessRuleException.class,
                () -> inventoryMovementBusiness.createMovement(farmId, idempotencyKey, request));

        assertThat(exception.getMessage()).contains("Saldo insuficiente");
        verify(persistencePort).lockItemForUpdate(farmId, itemId);
        verify(persistencePort).lockBalanceForUpdate(farmId, itemId, null);
        verify(persistencePort, never()).saveMovement(any());
        verify(persistencePort, never()).upsertBalance(any());
        verify(persistencePort, never()).saveIdempotency(any());
    }

    @Test
    void shouldThrowBusinessRule_whenAdjustDecreaseWouldMakeBalanceNegative() {
        Long farmId = 1L;
        String idempotencyKey = "adjust-1";
        Long itemId = 10L;

        InventoryMovementCreateRequestVO request = new InventoryMovementCreateRequestVO(
                InventoryMovementType.ADJUST,
                new BigDecimal("4.0"),
                itemId,
                null,
                InventoryAdjustDirection.DECREASE,
                LocalDate.of(2026, 2, 13),
                "ajuste"
        );

        when(persistencePort.findIdempotency(farmId, idempotencyKey)).thenReturn(Optional.empty());
        when(persistencePort.findItemSnapshot(farmId, itemId)).thenReturn(Optional.of(new InventoryItemSnapshotVO(itemId, false)));
        when(persistencePort.lockItemForUpdate(farmId, itemId)).thenReturn(Optional.of(new InventoryItemSnapshotVO(itemId, false)));
        when(persistencePort.lockBalanceForUpdate(farmId, itemId, null))
                .thenReturn(Optional.of(new InventoryBalanceSnapshotVO(farmId, itemId, null, new BigDecimal("3.0"))));

        BusinessRuleException exception = assertThrows(BusinessRuleException.class,
                () -> inventoryMovementBusiness.createMovement(farmId, idempotencyKey, request));

        assertThat(exception.getMessage()).contains("Ajuste (DECREASE)");
        verify(persistencePort).lockItemForUpdate(farmId, itemId);
        verify(persistencePort).lockBalanceForUpdate(eq(farmId), eq(itemId), eq(null));
        verify(persistencePort, never()).saveMovement(any());
        verify(persistencePort, never()).upsertBalance(any());
        verify(persistencePort, never()).saveIdempotency(any());
    }

    @Test
    void shouldPersistMovementAndBalanceAndIdempotency_whenInMovementIsValid() {
        stubFixedClock();

        Long farmId = 1L;
        String idempotencyKey = "in-1";
        Long itemId = 10L;
        Long movementId = 700L;
        LocalDate movementDate = LocalDate.of(2026, 2, 13);
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-02-13T10:15:30Z");

        InventoryMovementCreateRequestVO request = new InventoryMovementCreateRequestVO(
                InventoryMovementType.IN,
                new BigDecimal("10.0"),
                itemId,
                null,
                null,
                movementDate,
                "entrada manual"
        );

        when(persistencePort.findIdempotency(farmId, idempotencyKey)).thenReturn(Optional.empty());
        when(persistencePort.findItemSnapshot(farmId, itemId)).thenReturn(Optional.of(new InventoryItemSnapshotVO(itemId, false)));
        when(persistencePort.lockItemForUpdate(farmId, itemId)).thenReturn(Optional.of(new InventoryItemSnapshotVO(itemId, false)));
        when(persistencePort.lockBalanceForUpdate(farmId, itemId, null))
                .thenReturn(Optional.of(new InventoryBalanceSnapshotVO(farmId, itemId, null, new BigDecimal("5.0"))));

        when(persistencePort.saveMovement(any())).thenAnswer(invocation -> {
            var vo = invocation.getArgument(0, com.devmaster.goatfarm.inventory.business.bo.InventoryMovementPersistedVO.class);
            return new com.devmaster.goatfarm.inventory.business.bo.InventoryMovementPersistedVO(
                    movementId,
                    vo.farmId(),
                    vo.type(),
                    vo.adjustDirection(),
                    vo.quantity(),
                    vo.itemId(),
                    vo.lotId(),
                    vo.movementDate(),
                    vo.reason(),
                    vo.resultingBalance(),
                    createdAt
            );
        });

        when(persistencePort.upsertBalance(any()))
                .thenReturn(new InventoryBalanceSnapshotVO(farmId, itemId, null, new BigDecimal("15.0")));
        when(persistencePort.saveIdempotency(any())).thenAnswer(invocation -> invocation.getArgument(0));

        InventoryMovementResultVO result = inventoryMovementBusiness.createMovement(farmId, idempotencyKey, request);

        assertThat(result.replayed()).isFalse();
        assertThat(result.response().movementId()).isEqualTo(movementId);
        assertThat(result.response().resultingBalance()).isEqualByComparingTo("15.0");
        assertThat(result.response().itemId()).isEqualTo(itemId);
        assertThat(result.response().lotId()).isNull();
        assertThat(result.response().createdAt()).isEqualTo(createdAt);

        verify(persistencePort).lockBalanceForUpdate(farmId, itemId, null);
        verify(persistencePort).saveMovement(any());
        verify(persistencePort).upsertBalance(new InventoryBalanceSnapshotVO(farmId, itemId, null, new BigDecimal("15.0")));
        verify(persistencePort).saveIdempotency(any());
    }

    @Test
    void shouldRespectLockOrder_itemThenBalance() {
        stubFixedClock();

        Long farmId = 1L;
        String idempotencyKey = "in-lock-order";
        Long itemId = 10L;
        LocalDate movementDate = LocalDate.of(2026, 2, 13);
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-02-13T10:15:30Z");

        InventoryMovementCreateRequestVO request = new InventoryMovementCreateRequestVO(
                InventoryMovementType.IN,
                new BigDecimal("2.0"),
                itemId,
                null,
                null,
                movementDate,
                "entrada"
        );

        when(persistencePort.findIdempotency(farmId, idempotencyKey)).thenReturn(Optional.empty());
        when(persistencePort.findItemSnapshot(farmId, itemId)).thenReturn(Optional.of(new InventoryItemSnapshotVO(itemId, false)));
        when(persistencePort.lockItemForUpdate(farmId, itemId)).thenReturn(Optional.of(new InventoryItemSnapshotVO(itemId, false)));
        when(persistencePort.lockBalanceForUpdate(farmId, itemId, null))
                .thenReturn(Optional.of(new InventoryBalanceSnapshotVO(farmId, itemId, null, new BigDecimal("3.0"))));
        when(persistencePort.saveMovement(any())).thenAnswer(invocation -> {
            var vo = invocation.getArgument(0, com.devmaster.goatfarm.inventory.business.bo.InventoryMovementPersistedVO.class);
            return new com.devmaster.goatfarm.inventory.business.bo.InventoryMovementPersistedVO(
                    900L,
                    vo.farmId(),
                    vo.type(),
                    vo.adjustDirection(),
                    vo.quantity(),
                    vo.itemId(),
                    vo.lotId(),
                    vo.movementDate(),
                    vo.reason(),
                    vo.resultingBalance(),
                    createdAt
            );
        });
        when(persistencePort.upsertBalance(any()))
                .thenReturn(new InventoryBalanceSnapshotVO(farmId, itemId, null, new BigDecimal("5.0")));
        when(persistencePort.saveIdempotency(any())).thenAnswer(invocation -> invocation.getArgument(0));

        inventoryMovementBusiness.createMovement(farmId, idempotencyKey, request);

        var inOrder = inOrder(persistencePort);
        inOrder.verify(persistencePort).lockItemForUpdate(farmId, itemId);
        inOrder.verify(persistencePort).lockBalanceForUpdate(farmId, itemId, null);
        inOrder.verify(persistencePort).saveMovement(any());
        inOrder.verify(persistencePort).upsertBalance(any());
        inOrder.verify(persistencePort).saveIdempotency(any());
    }

    private void stubFixedClock() {
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(Instant.parse("2026-02-13T10:15:30Z"));
    }

    private InventoryMovementCreateRequestVO validRequest() {
        return new InventoryMovementCreateRequestVO(
                InventoryMovementType.IN,
                new BigDecimal("1.00"),
                10L,
                null,
                null,
                LocalDate.of(2026, 2, 13),
                " entrada padrao "
        );
    }

    private String hashRequest(InventoryMovementCreateRequestVO request) {
        String quantityNormalized = request.quantity().stripTrailingZeros().toPlainString();
        String lotIdOrEmpty = request.lotId() == null ? "" : request.lotId().toString();
        String adjustDirectionOrEmpty = request.adjustDirection() == null ? "" : request.adjustDirection().name();
        String movementDateOrEmpty = request.movementDate() == null ? "" : request.movementDate().toString();
        String reasonTrimOrEmpty = request.reason() == null
                ? ""
                : request.reason().trim().replaceAll("\\s+", " ");

        String canonical = request.type().name()
                + "|" + quantityNormalized
                + "|" + request.itemId()
                + "|" + lotIdOrEmpty
                + "|" + adjustDirectionOrEmpty
                + "|" + movementDateOrEmpty
                + "|" + reasonTrimOrEmpty;

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(canonical.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo SHA-256 nao disponivel para teste.", e);
        }
    }
}

