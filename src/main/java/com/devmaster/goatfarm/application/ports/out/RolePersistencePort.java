package com.devmaster.goatfarm.application.ports.out;

import com.devmaster.goatfarm.authority.model.entity.Role;

import java.util.Optional;

/**
 * Porta de saída para persistência de roles/autoridades.
 */
public interface RolePersistencePort {
    Optional<Role> findByAuthority(String authority);
}