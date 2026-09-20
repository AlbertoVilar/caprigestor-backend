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
    boolean existsByFarm_IdAndGoatTechnicalId(Long farmId, Long goatTechnicalId);

    @Query("select case when count(a) > 0 then true else false end from AnimalSale a "
            + "where a.farm.id = :farmId and a.goatTechnicalId = :goatTechnicalId "
            + "and a.targetFarm is not null and exists (select t.id from OwnershipTransferEntity t "
            + "where t.saleId = a.id and t.status in (com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus.REQUESTED, "
            + "com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus.ACCEPTED))")
    boolean existsActiveOwnershipSaleByFarmIdAndGoatTechnicalId(@Param("farmId") Long farmId,
                                                                  @Param("goatTechnicalId") Long goatTechnicalId);

    Optional<AnimalSale> findByIdAndFarm_Id(Long id, Long farmId);

    List<AnimalSale> findByFarm_IdOrderBySaleDateDescIdDesc(Long farmId);

    List<AnimalSale> findByTargetFarm_IdOrderBySaleDateDescIdDesc(Long targetFarmId);

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
