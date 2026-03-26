package com.devmaster.goatfarm.commercial.persistence.repository;

import com.devmaster.goatfarm.commercial.persistence.entity.MilkSale;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MilkSaleRepository extends JpaRepository<MilkSale, Long> {

    Optional<MilkSale> findByIdAndFarm_Id(Long id, Long farmId);

    List<MilkSale> findByFarm_IdOrderBySaleDateDescIdDesc(Long farmId);
}
