package com.devmaster.goatfarm.genealogy.model.repository;

import com.devmaster.goatfarm.genealogy.model.entity.Genealogy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GenealogyRepository extends JpaRepository<Genealogy, Long> {
    Optional<Genealogy> findByGoatRegistration(String goatRegistration);

    boolean existsByGoatRegistration(String goatRegistration);
}
