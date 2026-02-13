package com.devmaster.goatfarm.inventory.business.inventoryservice;

import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.inventory.application.ports.out.InventoryMovementPersistencePort;
import com.devmaster.goatfarm.inventory.business.bo.InventoryMovementCreateRequestVO;
import com.devmaster.goatfarm.inventory.domain.enums.InventoryMovementType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;

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

    private InventoryMovementCreateRequestVO validRequest() {
        return new InventoryMovementCreateRequestVO(
                InventoryMovementType.IN,
                new BigDecimal("1.00"),
                10L,
                null,
                null,
                LocalDate.now(),
                "entrada"
        );
    }
}
