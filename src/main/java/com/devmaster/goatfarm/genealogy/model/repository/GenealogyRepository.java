package com.devmaster.goatfarm.genealogy.model.repository;


import com.devmaster.goatfarm.genealogy.model.entity.Genealogy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GenealogyRepository extends JpaRepository<Genealogy, Long> {

    // Find by Name
    Genealogy findByGoatName(String goatName);

    boolean existsByGoatRegistration(String goatRegistrationNumber);

    Optional<Genealogy> findByGoatRegistration(String goatRegistrationNumber);
}
