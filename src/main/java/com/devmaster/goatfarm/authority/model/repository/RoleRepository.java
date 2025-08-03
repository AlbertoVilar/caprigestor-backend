package com.devmaster.goatfarm.authority.model.repository;

import com.devmaster.goatfarm.authority.model.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    // Busca a role pelo campo "authority" (ou "name", se for o seu atributo)
    Optional<Role> findByAuthority(String authority);
}
