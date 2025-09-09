package com.devmaster.goatfarm.authority.model.repository;

import com.devmaster.goatfarm.authority.model.entity.Authority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthorityRepository extends JpaRepository<Authority, Long> {

    Optional<Authority> findByName(String name);
    
    boolean existsByName(String name);
}