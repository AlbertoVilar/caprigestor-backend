package com.devmaster.goatfarm.commercial.application.ports.out;

import com.devmaster.goatfarm.commercial.persistence.entity.OperationalExpense;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface OperationalFinancePersistencePort {

    OperationalExpense saveOperationalExpense(OperationalExpense operationalExpense);

    List<OperationalExpense> findOperationalExpensesByFarmId(Long farmId);

    BigDecimal sumOperationalExpensesByFarmIdAndPeriod(Long farmId, LocalDate fromDate, LocalDate toDate);

    BigDecimal sumPaidAnimalSalesByFarmIdAndPeriod(Long farmId, LocalDate fromDate, LocalDate toDate);

    BigDecimal sumPaidMilkSalesByFarmIdAndPeriod(Long farmId, LocalDate fromDate, LocalDate toDate);
}
