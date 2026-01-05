package com.devmaster.goatfarm.milk.model.repository;

import com.devmaster.goatfarm.milk.model.entity.MilkProduction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MilkProductionRepository extends JpaRepository<MilkProduction, Long> {
}
