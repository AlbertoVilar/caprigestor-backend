package com.devmaster.goatfarm.commercial.api.mapper;

import com.devmaster.goatfarm.commercial.api.dto.MonthlyOperationalSummaryDTO;
import com.devmaster.goatfarm.commercial.api.dto.OperationalExpenseRequestDTO;
import com.devmaster.goatfarm.commercial.api.dto.OperationalExpenseResponseDTO;
import com.devmaster.goatfarm.commercial.business.bo.MonthlyOperationalSummaryVO;
import com.devmaster.goatfarm.commercial.business.bo.OperationalExpenseRequestVO;
import com.devmaster.goatfarm.commercial.business.bo.OperationalExpenseResponseVO;
import org.springframework.stereotype.Component;

@Component
public class OperationalFinanceApiMapper {

    public OperationalExpenseRequestVO toVO(OperationalExpenseRequestDTO dto) {
        return new OperationalExpenseRequestVO(
                dto.category(),
                dto.description(),
                dto.amount(),
                dto.expenseDate(),
                dto.notes()
        );
    }

    public OperationalExpenseResponseDTO toDTO(OperationalExpenseResponseVO vo) {
        return new OperationalExpenseResponseDTO(
                vo.id(),
                vo.category(),
                vo.description(),
                vo.amount(),
                vo.expenseDate(),
                vo.notes(),
                vo.createdAt()
        );
    }

    public MonthlyOperationalSummaryDTO toDTO(MonthlyOperationalSummaryVO vo) {
        return new MonthlyOperationalSummaryDTO(
                vo.year(),
                vo.month(),
                vo.totalRevenue(),
                vo.totalExpenses(),
                vo.balance(),
                vo.animalSalesRevenue(),
                vo.milkSalesRevenue(),
                vo.operationalExpensesTotal(),
                vo.inventoryPurchaseCostsTotal()
        );
    }
}
