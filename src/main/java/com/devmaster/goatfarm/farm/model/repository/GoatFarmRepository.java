package com.devmaster.goatfarm.farm.model.repository;

import com.devmaster.goatfarm.address.model.entity.Address;
import com.devmaster.goatfarm.farm.model.entity.GoatFarm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GoatFarmRepository extends JpaRepository<GoatFarm, Long> {

    boolean existsByName(String name);

    boolean existsByTod(String tod);

    // Buscar todas as fazendas com relacionamentos completos
    @EntityGraph(attributePaths = {"user", "address", "phones"})
    @Query("SELECT g FROM GoatFarm g")
    Page<GoatFarm> searchAllFullFarms(Pageable pageable);

    // Buscar por nome com relacionamentos completos
    @EntityGraph(attributePaths = {"user", "address", "phones"})
    @Query("SELECT g FROM GoatFarm g WHERE LOWER(g.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<GoatFarm> searchGoatFarmByName(@Param("name") String name, Pageable pageable);

    // Buscar por ID com relacionamentos completos
    @EntityGraph(attributePaths = {"user", "address", "phones"})
    @Query("SELECT g FROM GoatFarm g WHERE g.id = :id")
    Optional<GoatFarm> findById(@Param("id") Long id);

}
