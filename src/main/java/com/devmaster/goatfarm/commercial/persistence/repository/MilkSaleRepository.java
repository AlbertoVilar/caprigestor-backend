package com.devmaster.goatfarm.commercial.persistence.repository;

import com.devmaster.goatfarm.commercial.persistence.entity.MilkSale;
import com.devmaster.goatfarm.commercial.enums.SalePaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MilkSaleRepository extends JpaRepository<MilkSale, Long> {

    Optional<MilkSale> findByIdAndFarm_Id(Long id, Long farmId);

    List<MilkSale> findByFarm_IdOrderBySaleDateDescIdDesc(Long farmId);

    @Query("""
            select coalesce(sum(m.totalAmount), 0)
            from MilkSale m
            where m.farm.id = :farmId
              and m.paymentStatus = :paymentStatus
              and m.paymentDate >= :fromDate
              and m.paymentDate <= :toDate
            """)
    BigDecimal sumPaidAmountByFarmIdAndPaymentDateBetween(
            @Param("farmId") Long farmId,
            @Param("paymentStatus") SalePaymentStatus paymentStatus,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );
}
