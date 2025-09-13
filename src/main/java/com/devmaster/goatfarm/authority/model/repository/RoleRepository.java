package com.devmaster.goatfarm.authority.model.repository;

import com.devmaster.goatfarm.authority.model.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    // Find role by "authority" field (or "name", if that's your attribute)
    Optional<Role> findByAuthority(String authority);
}
