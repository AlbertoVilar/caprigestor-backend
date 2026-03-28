package com.devmaster.goatfarm.commercial.persistence.adapter;

import com.devmaster.goatfarm.commercial.application.ports.out.OperationalFinancePersistencePort;
import com.devmaster.goatfarm.commercial.enums.SalePaymentStatus;
import com.devmaster.goatfarm.commercial.persistence.entity.OperationalExpense;
import com.devmaster.goatfarm.commercial.persistence.repository.AnimalSaleRepository;
import com.devmaster.goatfarm.commercial.persistence.repository.MilkSaleRepository;
import com.devmaster.goatfarm.commercial.persistence.repository.OperationalExpenseRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
public class OperationalFinancePersistenceAdapter implements OperationalFinancePersistencePort {

    private final OperationalExpenseRepository operationalExpenseRepository;
    private final AnimalSaleRepository animalSaleRepository;
    private final MilkSaleRepository milkSaleRepository;

    public OperationalFinancePersistenceAdapter(
            OperationalExpenseRepository operationalExpenseRepository,
            AnimalSaleRepository animalSaleRepository,
            MilkSaleRepository milkSaleRepository
    ) {
        this.operationalExpenseRepository = operationalExpenseRepository;
        this.animalSaleRepository = animalSaleRepository;
        this.milkSaleRepository = milkSaleRepository;
    }

    @Override
    public OperationalExpense saveOperationalExpense(OperationalExpense operationalExpense) {
        return operationalExpenseRepository.save(operationalExpense);
    }

    @Override
    public List<OperationalExpense> findOperationalExpensesByFarmId(Long farmId) {
        return operationalExpenseRepository.findByFarm_IdOrderByExpenseDateDescIdDesc(farmId);
    }

    @Override
    public BigDecimal sumOperationalExpensesByFarmIdAndPeriod(Long farmId, LocalDate fromDate, LocalDate toDate) {
        return operationalExpenseRepository.sumAmountByFarmIdAndExpenseDateBetween(farmId, fromDate, toDate);
    }

    @Override
    public BigDecimal sumPaidAnimalSalesByFarmIdAndPeriod(Long farmId, LocalDate fromDate, LocalDate toDate) {
        return animalSaleRepository.sumPaidAmountByFarmIdAndPaymentDateBetween(farmId, SalePaymentStatus.PAID, fromDate, toDate);
    }

    @Override
    public BigDecimal sumPaidMilkSalesByFarmIdAndPeriod(Long farmId, LocalDate fromDate, LocalDate toDate) {
        return milkSaleRepository.sumPaidAmountByFarmIdAndPaymentDateBetween(farmId, SalePaymentStatus.PAID, fromDate, toDate);
    }
}
