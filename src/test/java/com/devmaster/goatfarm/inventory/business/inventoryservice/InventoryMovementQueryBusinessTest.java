package com.devmaster.goatfarm.inventory.business.inventoryservice;

import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.inventory.application.ports.out.InventoryMovementQueryPort;
import com.devmaster.goatfarm.application.pagination.PageQuery;
import com.devmaster.goatfarm.application.pagination.PageResult;
import com.devmaster.goatfarm.inventory.business.bo.InventoryMovementFilterVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryMovementHistoryResponseVO;
import com.devmaster.goatfarm.inventory.domain.enums.InventoryMovementType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
                LocalDate.of(2026, 2, 1)
        );
        PageQuery page = new PageQuery(0, 20, List.of());

        InvalidArgumentException exception = assertThrows(
                InvalidArgumentException.class,
                () -> business.listMovements(filter, page)
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
                null
        );
        PageQuery page = new PageQuery(0, 120, List.of());

        InvalidArgumentException exception = assertThrows(
                InvalidArgumentException.class,
                () -> business.listMovements(filter, page)
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
                LocalDate.of(2026, 2, 28)
        );
        PageQuery pageQuery = new PageQuery(0, 20, List.of());

        when(queryPort.listMovements(filter, pageQuery)).thenReturn(new PageResult<>(List.of(
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
        ), 1, 0, 20));

        var page = business.listMovements(filter, pageQuery);

        assertThat(page.totalElements()).isEqualTo(1);
        assertThat(page.content()).extracting(InventoryMovementHistoryResponseVO::itemName)
                .containsExactly("Ração Premium");
        verify(queryPort).listMovements(filter, pageQuery);
    }
}
