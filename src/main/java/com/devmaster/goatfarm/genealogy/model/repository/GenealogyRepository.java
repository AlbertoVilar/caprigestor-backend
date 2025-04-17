package com.devmaster.goatfarm.genealogy.model.repository;


import com.devmaster.goatfarm.genealogy.model.entity.Genealogy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GenealogyRepository extends JpaRepository<Genealogy, Long> {

    // Find by Name
    Genealogy findByGoatName(String goatName);

    Genealogy findByGoatRegistration(String goatRegistration);
}
