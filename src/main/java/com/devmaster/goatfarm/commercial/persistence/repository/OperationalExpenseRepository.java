package com.devmaster.goatfarm.commercial.persistence.repository;

import com.devmaster.goatfarm.commercial.persistence.entity.OperationalExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface OperationalExpenseRepository extends JpaRepository<OperationalExpense, Long> {

    List<OperationalExpense> findByFarm_IdOrderByExpenseDateDescIdDesc(Long farmId);

    @Query("""
            select coalesce(sum(e.amount), 0)
            from OperationalExpense e
            where e.farm.id = :farmId
              and e.expenseDate >= :fromDate
              and e.expenseDate <= :toDate
            """)
    BigDecimal sumAmountByFarmIdAndExpenseDateBetween(
            @Param("farmId") Long farmId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );
}
