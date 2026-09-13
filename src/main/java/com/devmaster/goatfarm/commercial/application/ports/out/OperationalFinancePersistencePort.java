package com.devmaster.goatfarm.commercial.application.ports.out;

import com.devmaster.goatfarm.commercial.application.model.OperationalExpenseCommand;
import com.devmaster.goatfarm.commercial.application.model.OperationalExpenseRecord;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface OperationalFinancePersistencePort {

    OperationalExpenseRecord saveOperationalExpense(OperationalExpenseCommand command);

    List<OperationalExpenseRecord> findOperationalExpensesByFarmId(Long farmId);

    BigDecimal sumOperationalExpensesByFarmIdAndPeriod(Long farmId, LocalDate fromDate, LocalDate toDate);

    BigDecimal sumPaidAnimalSalesByFarmIdAndPeriod(Long farmId, LocalDate fromDate, LocalDate toDate);

    BigDecimal sumPaidMilkSalesByFarmIdAndPeriod(Long farmId, LocalDate fromDate, LocalDate toDate);
}
