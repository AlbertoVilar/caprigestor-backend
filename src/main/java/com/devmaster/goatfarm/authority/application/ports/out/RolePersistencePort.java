package com.devmaster.goatfarm.authority.application.ports.out;

import com.devmaster.goatfarm.authority.business.bo.AuthorityRole;

import java.util.Optional;

/**
 * Porta de saída para persistência de roles/autoridades.
 */
public interface RolePersistencePort {
    Optional<AuthorityRole> findByAuthority(String authority);
}
