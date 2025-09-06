package com.devmaster.goatfarm.owner.model.repository;

import com.devmaster.goatfarm.owner.model.entity.Owner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OwnerRepository extends JpaRepository<Owner, Long> {

    boolean existsByCpf(String cpf);
    boolean existsByEmail(String email);


    Page<Owner> findAll(Pageable pageable);

    @Query("SELECT o FROM Owner o WHERE LOWER(o.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Owner> searchOwnerByName(@Param("name") String name, Pageable pageable);

    Optional<Owner> findByCpf(String cpf);
    Optional<Owner> findByEmail(String email);
}