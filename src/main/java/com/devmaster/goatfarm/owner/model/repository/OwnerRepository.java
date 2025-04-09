package com.devmaster.goatfarm.owner.model.repository;

import com.devmaster.goatfarm.owner.model.entity.Owner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OwnerRepository extends JpaRepository<Owner, Long> {
}
