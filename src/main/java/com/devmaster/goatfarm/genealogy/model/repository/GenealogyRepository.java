package com.devmaster.goatfarm.genealogy.model.repository;


import com.devmaster.goatfarm.genealogy.model.entity.Genealogy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GenealogyRepository extends JpaRepository<Genealogy, Long> {

    // Find by Name
    Genealogy findByGoatName(String goatName);

    boolean existsByGoatRegistration(String goatRegistrationNumber);

    Optional<Genealogy> findByGoatRegistration(String goatRegistrationNumber);
    
    // Query customizada para testar
    @Query("SELECT g FROM Genealogy g WHERE g.goatRegistration = :registration")
    Optional<Genealogy> findByGoatRegistrationCustom(@Param("registration") String registration);
    
    // Query nativa para testar
    @Query(value = "SELECT * FROM genealogia WHERE registro_animal = ?1", nativeQuery = true)
    Optional<Genealogy> findByGoatRegistrationNative(String registration);
}
