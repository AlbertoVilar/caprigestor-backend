package com.devmaster.goatfarm.farm.model.repository;

import com.devmaster.goatfarm.farm.model.entity.GoatFarm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GoatFarmRepository extends JpaRepository<GoatFarm, Long> {

    boolean existsByName(String name);

    boolean existsByTod(String tod);
}
