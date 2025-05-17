package com.devmaster.goatfarm.farm.model.repository;

import com.devmaster.goatfarm.farm.model.entity.GoatFarm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GoatFarmRepository extends JpaRepository<GoatFarm, Long> {

    boolean existsByName(String name);

    boolean existsByTod(String tod);

    Page<GoatFarm> findAll(Pageable pageable);

    @Query("SELECT g FROM GoatFarm g WHERE LOWER(g.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<GoatFarm> searchGoatFarmByName(@Param("name") String name, Pageable pageable);


    @EntityGraph(attributePaths = {"owner", "address", "phones"})
    @Query("SELECT g FROM GoatFarm g")
    Page<GoatFarm> searchAllFullFarms(Pageable pageable);


}
