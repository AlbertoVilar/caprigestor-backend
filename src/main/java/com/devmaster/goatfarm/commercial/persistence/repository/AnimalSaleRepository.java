package com.devmaster.goatfarm.commercial.persistence.repository;

import com.devmaster.goatfarm.commercial.persistence.entity.AnimalSale;
import com.devmaster.goatfarm.commercial.enums.SalePaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AnimalSaleRepository extends JpaRepository<AnimalSale, Long> {

    boolean existsByGoatRegistrationNumber(String goatRegistrationNumber);

    Optional<AnimalSale> findByIdAndFarm_Id(Long id, Long farmId);

    List<AnimalSale> findByFarm_IdOrderBySaleDateDescIdDesc(Long farmId);

    @Query("""
            select coalesce(sum(a.amount), 0)
            from AnimalSale a
            where a.farm.id = :farmId
              and a.paymentStatus = :paymentStatus
              and a.paymentDate >= :fromDate
              and a.paymentDate <= :toDate
            """)
    BigDecimal sumPaidAmountByFarmIdAndPaymentDateBetween(
            @Param("farmId") Long farmId,
            @Param("paymentStatus") SalePaymentStatus paymentStatus,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );
}
