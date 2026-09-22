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

    boolean existsByGoatTechnicalIdAndTargetFarmIsNull(Long goatTechnicalId);

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
              and not exists (
                  select reversal.id
                  from AnimalSaleReversal reversal
                  where reversal.sale.id = a.id
              )
              and (
                  a.targetFarm is null
                  or exists (
                      select transfer.id
                      from com.devmaster.goatfarm.goatownership.persistence.entity.OwnershipTransferEntity transfer
                      where transfer.saleId = a.id
                        and transfer.kind = com.devmaster.goatfarm.goatownership.domain.OwnershipTransferKind.INTERNAL_SALE
                        and transfer.status = com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus.COMPLETED
                  )
              )
            """)
    BigDecimal sumPaidAmountByFarmIdAndPaymentDateBetween(
            @Param("farmId") Long farmId,
            @Param("paymentStatus") SalePaymentStatus paymentStatus,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );
}
