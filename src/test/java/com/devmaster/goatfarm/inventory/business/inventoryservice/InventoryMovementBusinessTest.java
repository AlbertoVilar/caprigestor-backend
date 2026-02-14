package com.devmaster.goatfarm.inventory.business.inventoryservice;

import com.devmaster.goatfarm.config.exceptions.DuplicateEntityException;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.inventory.application.ports.out.InventoryMovementPersistencePort;
import com.devmaster.goatfarm.inventory.business.bo.InventoryIdempotencyVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryMovementCreateRequestVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryMovementResponseVO;
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
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryMovementBusinessTest {

    @Mock
    private InventoryMovementPersistencePort persistencePort;

    @InjectMocks
    private InventoryMovementBusiness inventoryMovementBusiness;

    @Test
    void createMovement_shouldThrowInvalidArgument_whenFarmIdIsNull() {
        InvalidArgumentException exception = assertThrows(InvalidArgumentException.class,
                () -> inventoryMovementBusiness.createMovement(null, "idem-key", validRequest()));

        assertThat(exception.getMessage()).contains("farmId é obrigatório.");
        verifyNoInteractions(persistencePort);
    }

    @Test
    void createMovement_shouldThrowInvalidArgument_whenIdempotencyKeyIsBlank() {
        InvalidArgumentException exception = assertThrows(InvalidArgumentException.class,
                () -> inventoryMovementBusiness.createMovement(1L, "   ", validRequest()));

        assertThat(exception.getMessage()).contains("Idempotency-Key é obrigatório.");
        verifyNoInteractions(persistencePort);
    }

    @Test
    void createMovement_shouldThrowInvalidArgument_whenRequestIsNull() {
        InvalidArgumentException exception = assertThrows(InvalidArgumentException.class,
                () -> inventoryMovementBusiness.createMovement(1L, "idem-key", null));

        assertThat(exception.getMessage()).contains("Payload da requisição é obrigatório.");
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

        assertThat(exception.getMessage()).contains("adjustDirection é obrigatório quando o tipo é ADJUST.");
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
                .thenReturn(Optional.of(new InventoryIdempotencyVO(idempotencyKey, requestHash, replayResponse)));

        InventoryMovementResponseVO result = inventoryMovementBusiness.createMovement(farmId, idempotencyKey, request);

        assertThat(result).isSameAs(replayResponse);
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
                .thenReturn(Optional.of(new InventoryIdempotencyVO(idempotencyKey, "hash-diferente", replayResponse)));

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
                .thenReturn(Optional.of(new InventoryIdempotencyVO(idempotencyKey, requestHash, replayResponse)));

        InventoryMovementResponseVO result = inventoryMovementBusiness.createMovement(farmId, idempotencyKey, requestB);

        assertThat(result).isSameAs(replayResponse);
        verify(persistencePort).findIdempotency(farmId, idempotencyKey);
    }

    private InventoryMovementCreateRequestVO validRequest() {
        return new InventoryMovementCreateRequestVO(
                InventoryMovementType.IN,
                new BigDecimal("1.00"),
                10L,
                null,
                null,
                LocalDate.of(2026, 2, 13),
                " entrada padrão "
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
            throw new IllegalStateException("Algoritmo SHA-256 não disponível para teste.", e);
        }
    }
}
