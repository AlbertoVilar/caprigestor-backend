package com.devmaster.goatfarm.inventory.business.inventoryservice;

import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.inventory.application.ports.out.InventoryMovementQueryPort;
import com.devmaster.goatfarm.inventory.business.bo.InventoryMovementFilterVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryMovementHistoryResponseVO;
import com.devmaster.goatfarm.inventory.domain.enums.InventoryMovementType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryMovementQueryBusinessTest {

    @Mock
    private InventoryMovementQueryPort queryPort;

    @InjectMocks
    private InventoryMovementQueryBusiness business;

    @Test
    void listMovements_shouldRejectInvalidDateRange() {
        InventoryMovementFilterVO filter = new InventoryMovementFilterVO(
                7L,
                null,
                null,
                null,
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 2, 1),
                PageRequest.of(0, 20)
        );

        InvalidArgumentException exception = assertThrows(
                InvalidArgumentException.class,
                () -> business.listMovements(filter)
        );

        assertThat(exception.getMessage()).contains("Data inicial");
        verifyNoInteractions(queryPort);
    }

    @Test
    void listMovements_shouldRejectPageSizeGreaterThan100() {
        InventoryMovementFilterVO filter = new InventoryMovementFilterVO(
                7L,
                null,
                null,
                null,
                null,
                null,
                PageRequest.of(0, 120)
        );

        InvalidArgumentException exception = assertThrows(
                InvalidArgumentException.class,
                () -> business.listMovements(filter)
        );

        assertThat(exception.getMessage()).contains("size");
        verifyNoInteractions(queryPort);
    }

    @Test
    void listMovements_shouldDelegateToQueryPort() {
        InventoryMovementFilterVO filter = new InventoryMovementFilterVO(
                7L,
                101L,
                null,
                InventoryMovementType.OUT,
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 2, 28),
                PageRequest.of(0, 20)
        );

        when(queryPort.listMovements(filter)).thenReturn(new PageImpl<>(List.of(
                new InventoryMovementHistoryResponseVO(
                        9001L,
                        InventoryMovementType.OUT,
                        null,
                        new BigDecimal("2.000"),
                        101L,
                        "Ração Premium",
                        501L,
                        LocalDate.of(2026, 2, 28),
                        "Baixa por aplicação",
                        new BigDecimal("18.750"),
                        OffsetDateTime.parse("2026-02-28T12:15:00Z")
                )
        )));

        var page = business.listMovements(filter);

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent()).extracting(InventoryMovementHistoryResponseVO::itemName)
                .containsExactly("Ração Premium");
        verify(queryPort).listMovements(filter);
    }
}
