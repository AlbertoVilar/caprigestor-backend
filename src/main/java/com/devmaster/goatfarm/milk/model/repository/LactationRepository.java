package com.devmaster.goatfarm.milk.model.repository;

import com.devmaster.goatfarm.milk.model.entity.Lactation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LactationRepository extends JpaRepository<Lactation, Long> {
}
