package com.devmaster.goatfarm.commercial.persistence.repository;

import com.devmaster.goatfarm.commercial.persistence.entity.AnimalSale;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnimalSaleRepository extends JpaRepository<AnimalSale, Long> {

    boolean existsByGoatRegistrationNumber(String goatRegistrationNumber);

    Optional<AnimalSale> findByIdAndFarm_Id(Long id, Long farmId);

    List<AnimalSale> findByFarm_IdOrderBySaleDateDescIdDesc(Long farmId);
}
