package com.devmaster.goatfarm.authority.application.ports.out;

import com.devmaster.goatfarm.authority.business.bo.AuthorityAccount;

import java.util.Optional;

/**
 * Porta de saída para persistência de usuários.
 */
public interface UserPersistencePort {
    Optional<AuthorityAccount> findByEmail(String email);
    Optional<AuthorityAccount> findByCpf(String cpf);
    Optional<AuthorityAccount> findById(Long userId);
    AuthorityAccount save(AuthorityAccount user);
    void updatePassword(Long userId, String encryptedPassword);
}
