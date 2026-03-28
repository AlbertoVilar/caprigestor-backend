package com.devmaster.goatfarm.commercial.application.ports.in;

import com.devmaster.goatfarm.commercial.business.bo.MonthlyOperationalSummaryVO;
import com.devmaster.goatfarm.commercial.business.bo.OperationalExpenseRequestVO;
import com.devmaster.goatfarm.commercial.business.bo.OperationalExpenseResponseVO;

import java.util.List;

public interface OperationalFinanceUseCase {

    OperationalExpenseResponseVO createOperationalExpense(Long farmId, OperationalExpenseRequestVO requestVO);

    List<OperationalExpenseResponseVO> listOperationalExpenses(Long farmId);

    MonthlyOperationalSummaryVO getMonthlySummary(Long farmId, int year, int month);
}
