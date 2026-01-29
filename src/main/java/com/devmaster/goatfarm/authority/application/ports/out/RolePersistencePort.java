package com.devmaster.goatfarm.authority.application.ports.out;

import com.devmaster.goatfarm.authority.persistence.entity.Role;

import java.util.Optional;

/**
 * Porta de saída para persistência de roles/autoridades.
 */
public interface RolePersistencePort {
    Optional<Role> findByAuthority(String authority);
}