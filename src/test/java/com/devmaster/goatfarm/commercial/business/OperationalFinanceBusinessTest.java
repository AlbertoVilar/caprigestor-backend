package com.devmaster.goatfarm.commercial.business;

import com.devmaster.goatfarm.commercial.application.ports.out.InventoryPurchaseCostQueryPort;
import com.devmaster.goatfarm.commercial.application.ports.out.OperationalFinancePersistencePort;
import com.devmaster.goatfarm.commercial.business.bo.OperationalExpenseRequestVO;
import com.devmaster.goatfarm.commercial.enums.OperationalExpenseCategory;
import com.devmaster.goatfarm.commercial.persistence.entity.OperationalExpense;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.farm.application.ports.out.GoatFarmPersistencePort;
import com.devmaster.goatfarm.farm.persistence.entity.GoatFarm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OperationalFinanceBusinessTest {

    @Mock
    private OperationalFinancePersistencePort persistencePort;

    @Mock
    private GoatFarmPersistencePort goatFarmPersistencePort;

    @Mock
    private InventoryPurchaseCostQueryPort inventoryPurchaseCostQueryPort;

    @InjectMocks
    private OperationalFinanceBusiness operationalFinanceBusiness;

    @Test
    void createOperationalExpense_shouldPersistValidExpense() {
        GoatFarm farm = new GoatFarm();
        farm.setId(17L);

        when(goatFarmPersistencePort.findById(17L)).thenReturn(Optional.of(farm));
        when(persistencePort.saveOperationalExpense(any())).thenAnswer(invocation -> {
            OperationalExpense expense = invocation.getArgument(0, OperationalExpense.class);
            expense.setId(9L);
            return expense;
        });

        var response = operationalFinanceBusiness.createOperationalExpense(
                17L,
                new OperationalExpenseRequestVO(
                        OperationalExpenseCategory.VETERINARY,
                        "Consulta e medicamento",
                        new BigDecimal("180.00"),
                        LocalDate.of(2026, 3, 28),
                        "Atendimento preventivo"
                )
        );

        assertThat(response.id()).isEqualTo(9L);
        assertThat(response.category()).isEqualTo(OperationalExpenseCategory.VETERINARY);
        assertThat(response.amount()).isEqualByComparingTo("180.00");
    }

    @Test
    void createOperationalExpense_shouldRejectInvalidAmount() {
        GoatFarm farm = new GoatFarm();
        farm.setId(17L);
        when(goatFarmPersistencePort.findById(17L)).thenReturn(Optional.of(farm));

        InvalidArgumentException exception = assertThrows(
                InvalidArgumentException.class,
                () -> operationalFinanceBusiness.createOperationalExpense(
                        17L,
                        new OperationalExpenseRequestVO(
                                OperationalExpenseCategory.ENERGY,
                                "Conta de luz",
                                BigDecimal.ZERO,
                                LocalDate.of(2026, 3, 28),
                                null
                        )
                )
        );

        assertThat(exception.getMessage()).contains("maior que zero");
    }

    @Test
    void getMonthlySummary_shouldAggregateRevenueAndExpenses() {
        GoatFarm farm = new GoatFarm();
        farm.setId(17L);
        when(goatFarmPersistencePort.findById(17L)).thenReturn(Optional.of(farm));
        when(persistencePort.sumPaidAnimalSalesByFarmIdAndPeriod(17L, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31)))
                .thenReturn(new BigDecimal("1400.00"));
        when(persistencePort.sumPaidMilkSalesByFarmIdAndPeriod(17L, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31)))
                .thenReturn(new BigDecimal("380.00"));
        when(persistencePort.sumOperationalExpensesByFarmIdAndPeriod(17L, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31)))
                .thenReturn(new BigDecimal("250.00"));
        when(inventoryPurchaseCostQueryPort.sumPurchaseCostsByFarmIdAndPeriod(17L, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31)))
                .thenReturn(new BigDecimal("500.00"));

        var summary = operationalFinanceBusiness.getMonthlySummary(17L, 2026, 3);

        assertThat(summary.totalRevenue()).isEqualByComparingTo("1780.00");
        assertThat(summary.totalExpenses()).isEqualByComparingTo("750.00");
        assertThat(summary.balance()).isEqualByComparingTo("1030.00");
        assertThat(summary.inventoryPurchaseCostsTotal()).isEqualByComparingTo("500.00");
    }
}
