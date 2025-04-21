package com.devmaster.goatfarm.owner.model.repository;

import com.devmaster.goatfarm.owner.model.entity.Owner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OwnerRepository extends JpaRepository<Owner, Long> {

    boolean existsByCpf(String cpf);
    boolean existsByEmail(String email);

}