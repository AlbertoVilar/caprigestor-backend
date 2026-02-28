package com.devmaster.goatfarm.inventory.business.inventoryservice;

import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.inventory.application.ports.out.InventoryBalanceQueryPort;
import com.devmaster.goatfarm.inventory.business.bo.InventoryBalanceFilterVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryBalanceResponseVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryBalanceQueryBusinessTest {

    @Mock
    private InventoryBalanceQueryPort queryPort;

    @InjectMocks
    private InventoryBalanceQueryBusiness business;

    @Test
    void listBalances_shouldRejectPageSizeGreaterThan100() {
        InventoryBalanceFilterVO filter = new InventoryBalanceFilterVO(
                7L,
                null,
                null,
                true,
                PageRequest.of(0, 101)
        );

        InvalidArgumentException exception = assertThrows(
                InvalidArgumentException.class,
                () -> business.listBalances(filter)
        );

        assertThat(exception.getMessage()).contains("size");
        verifyNoInteractions(queryPort);
    }

    @Test
    void listBalances_shouldDelegateToQueryPort() {
        InventoryBalanceFilterVO filter = new InventoryBalanceFilterVO(
                7L,
                101L,
                null,
                true,
                PageRequest.of(0, 20)
        );

        when(queryPort.listBalances(filter)).thenReturn(new PageImpl<>(List.of(
                new InventoryBalanceResponseVO(101L, "Ração Premium", true, 501L, new BigDecimal("18.750"))
        )));

        var page = business.listBalances(filter);

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent()).extracting(InventoryBalanceResponseVO::itemName)
                .containsExactly("Ração Premium");
        verify(queryPort).listBalances(filter);
    }
}
