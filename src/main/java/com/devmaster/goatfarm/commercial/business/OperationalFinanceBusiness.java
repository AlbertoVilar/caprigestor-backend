package com.devmaster.goatfarm.commercial.business;

import com.devmaster.goatfarm.authority.application.ports.in.FarmAuthorizationUseCase;
import com.devmaster.goatfarm.commercial.application.model.OperationalExpenseCommand;
import com.devmaster.goatfarm.commercial.application.model.OperationalExpenseRecord;
import com.devmaster.goatfarm.commercial.application.ports.in.OperationalFinanceUseCase;
import com.devmaster.goatfarm.commercial.application.ports.out.InventoryPurchaseCostQueryPort;
import com.devmaster.goatfarm.commercial.application.ports.out.OperationalFinancePersistencePort;
import com.devmaster.goatfarm.commercial.business.bo.MonthlyOperationalSummaryVO;
import com.devmaster.goatfarm.commercial.business.bo.OperationalExpenseRequestVO;
import com.devmaster.goatfarm.commercial.business.bo.OperationalExpenseResponseVO;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.farm.application.ports.out.GoatFarmPersistencePort;
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
    private final FarmAuthorizationUseCase ownershipService;

    public OperationalFinanceBusiness(
            OperationalFinancePersistencePort persistencePort,
            GoatFarmPersistencePort goatFarmPersistencePort,
            InventoryPurchaseCostQueryPort inventoryPurchaseCostQueryPort,
            FarmAuthorizationUseCase ownershipService
    ) {
        this.persistencePort = persistencePort;
        this.goatFarmPersistencePort = goatFarmPersistencePort;
        this.inventoryPurchaseCostQueryPort = inventoryPurchaseCostQueryPort;
        this.ownershipService = ownershipService;
    }

    @Override
    @Transactional
    public OperationalExpenseResponseVO createOperationalExpense(Long farmId, OperationalExpenseRequestVO requestVO) {
        ownershipService.verifyFarmOwnership(farmId);
        ensureFarmExists(farmId);
        validateRequest(requestVO);

        OperationalExpenseRecord saved = persistencePort.saveOperationalExpense(
                new OperationalExpenseCommand(
                        null,
                        farmId,
                        requestVO.category(),
                        normalizeText(requestVO.description()),
                        requestVO.amount().setScale(2, RoundingMode.HALF_UP),
                        requestVO.expenseDate(),
                        normalizeText(requestVO.notes())
                )
        );

        return toResponseVO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OperationalExpenseResponseVO> listOperationalExpenses(Long farmId) {
        ownershipService.verifyFarmManagement(farmId);
        ensureFarmExists(farmId);
        return persistencePort.findOperationalExpensesByFarmId(farmId).stream().map(this::toResponseVO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MonthlyOperationalSummaryVO getMonthlySummary(Long farmId, int year, int month) {
        ownershipService.verifyFarmManagement(farmId);
        ensureFarmExists(farmId);

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

    private void ensureFarmExists(Long farmId) {
        if (farmId == null) {
            throw new InvalidArgumentException("farmId", "farmId e obrigatorio.");
        }

        goatFarmPersistencePort.findById(farmId)
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

    private OperationalExpenseResponseVO toResponseVO(OperationalExpenseRecord record) {
        return new OperationalExpenseResponseVO(
                record.id(),
                record.category(),
                record.description(),
                record.amount(),
                record.expenseDate(),
                record.notes(),
                record.createdAt()
        );
    }
}
