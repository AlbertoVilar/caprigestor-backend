package com.devmaster.goatfarm.commercial.business;

import com.devmaster.goatfarm.commercial.application.ports.in.OperationalFinanceUseCase;
import com.devmaster.goatfarm.commercial.application.ports.out.InventoryPurchaseCostQueryPort;
import com.devmaster.goatfarm.commercial.application.ports.out.OperationalFinancePersistencePort;
import com.devmaster.goatfarm.commercial.business.bo.MonthlyOperationalSummaryVO;
import com.devmaster.goatfarm.commercial.business.bo.OperationalExpenseRequestVO;
import com.devmaster.goatfarm.commercial.business.bo.OperationalExpenseResponseVO;
import com.devmaster.goatfarm.commercial.persistence.entity.OperationalExpense;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.farm.application.ports.out.GoatFarmPersistencePort;
import com.devmaster.goatfarm.farm.persistence.entity.GoatFarm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class OperationalFinanceBusiness implements OperationalFinanceUseCase {

    private final OperationalFinancePersistencePort persistencePort;
    private final GoatFarmPersistencePort goatFarmPersistencePort;
    private final InventoryPurchaseCostQueryPort inventoryPurchaseCostQueryPort;

    public OperationalFinanceBusiness(
            OperationalFinancePersistencePort persistencePort,
            GoatFarmPersistencePort goatFarmPersistencePort,
            InventoryPurchaseCostQueryPort inventoryPurchaseCostQueryPort
    ) {
        this.persistencePort = persistencePort;
        this.goatFarmPersistencePort = goatFarmPersistencePort;
        this.inventoryPurchaseCostQueryPort = inventoryPurchaseCostQueryPort;
    }

    @Override
    @Transactional
    public OperationalExpenseResponseVO createOperationalExpense(Long farmId, OperationalExpenseRequestVO requestVO) {
        GoatFarm farm = resolveFarm(farmId);
        validateRequest(requestVO);

        OperationalExpense saved = persistencePort.saveOperationalExpense(
                OperationalExpense.builder()
                        .farm(farm)
                        .category(requestVO.category())
                        .description(normalizeText(requestVO.description()))
                        .amount(requestVO.amount().setScale(2, RoundingMode.HALF_UP))
                        .expenseDate(requestVO.expenseDate())
                        .notes(normalizeText(requestVO.notes()))
                        .build()
        );

        return toResponseVO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OperationalExpenseResponseVO> listOperationalExpenses(Long farmId) {
        resolveFarm(farmId);
        return persistencePort.findOperationalExpensesByFarmId(farmId).stream().map(this::toResponseVO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MonthlyOperationalSummaryVO getMonthlySummary(Long farmId, int year, int month) {
        resolveFarm(farmId);

        YearMonth yearMonth = resolveYearMonth(year, month);
        LocalDate fromDate = yearMonth.atDay(1);
        LocalDate toDate = yearMonth.atEndOfMonth();

        BigDecimal animalSalesRevenue = normalizeMoney(
                persistencePort.sumPaidAnimalSalesByFarmIdAndPeriod(farmId, fromDate, toDate)
        );
        BigDecimal milkSalesRevenue = normalizeMoney(
                persistencePort.sumPaidMilkSalesByFarmIdAndPeriod(farmId, fromDate, toDate)
        );
        BigDecimal operationalExpensesTotal = normalizeMoney(
                persistencePort.sumOperationalExpensesByFarmIdAndPeriod(farmId, fromDate, toDate)
        );
        BigDecimal inventoryPurchaseCostsTotal = normalizeMoney(
                inventoryPurchaseCostQueryPort.sumPurchaseCostsByFarmIdAndPeriod(farmId, fromDate, toDate)
        );

        BigDecimal totalRevenue = animalSalesRevenue.add(milkSalesRevenue);
        BigDecimal totalExpenses = operationalExpensesTotal.add(inventoryPurchaseCostsTotal);
        BigDecimal balance = totalRevenue.subtract(totalExpenses).setScale(2, RoundingMode.HALF_UP);

        return new MonthlyOperationalSummaryVO(
                yearMonth.getYear(),
                yearMonth.getMonthValue(),
                totalRevenue,
                totalExpenses,
                balance,
                animalSalesRevenue,
                milkSalesRevenue,
                operationalExpensesTotal,
                inventoryPurchaseCostsTotal
        );
    }

    private GoatFarm resolveFarm(Long farmId) {
        if (farmId == null) {
            throw new InvalidArgumentException("farmId", "farmId e obrigatorio.");
        }

        return goatFarmPersistencePort.findById(farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Fazenda nao encontrada."));
    }

    private void validateRequest(OperationalExpenseRequestVO requestVO) {
        if (requestVO == null) {
            throw new InvalidArgumentException("request", "Payload da requisicao e obrigatorio.");
        }
        if (requestVO.category() == null) {
            throw new InvalidArgumentException("category", "Categoria da despesa e obrigatoria.");
        }
        if (normalizeText(requestVO.description()) == null) {
            throw new InvalidArgumentException("description", "Descricao da despesa e obrigatoria.");
        }
        if (requestVO.amount() == null || requestVO.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidArgumentException("amount", "Valor da despesa deve ser maior que zero.");
        }
        if (requestVO.expenseDate() == null) {
            throw new InvalidArgumentException("expenseDate", "expenseDate e obrigatoria.");
        }
    }

    private YearMonth resolveYearMonth(int year, int month) {
        try {
            return YearMonth.of(year, month);
        } catch (RuntimeException exception) {
            throw new InvalidArgumentException("month", "Periodo mensal invalido.");
        }
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().replaceAll("\\s+", " ");
        return normalized.isBlank() ? null : normalized;
    }

    private BigDecimal normalizeMoney(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }

    private OperationalExpenseResponseVO toResponseVO(OperationalExpense entity) {
        return new OperationalExpenseResponseVO(
                entity.getId(),
                entity.getCategory(),
                entity.getDescription(),
                entity.getAmount(),
                entity.getExpenseDate(),
                entity.getNotes(),
                entity.getCreatedAt()
        );
    }
}
