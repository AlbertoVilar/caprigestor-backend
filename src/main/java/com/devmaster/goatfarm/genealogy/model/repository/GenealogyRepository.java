package com.devmaster.goatfarm.genealogy.model.repository;

import com.devmaster.goatfarm.genealogy.model.entity.Genealogy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GenealogyRepository extends JpaRepository<Genealogy, Long> {
    Optional<Genealogy> findByGoatRegistration(String goatRegistration);

    boolean existsByGoatRegistration(String goatRegistration);

    // NOVO: Busca por número de registro da cabra e ID da fazenda
    @Query("SELECT g FROM Genealogy g WHERE g.goatRegistration = :goatRegistration AND g.goat.farm.id = :farmId")
    Optional<Genealogy> findByGoatRegistrationAndGoatFarmId(@Param("goatRegistration") String goatRegistration, @Param("farmId") Long farmId);
}
