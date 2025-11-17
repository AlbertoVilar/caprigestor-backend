package com.devmaster.goatfarm.application.ports.out;

import com.devmaster.goatfarm.authority.model.entity.User;

import java.util.Optional;

/**
 * Porta de saída para persistência de usuários.
 */
public interface UserPersistencePort {
    Optional<User> findByEmail(String email);
    Optional<User> findByCpf(String cpf);
    Optional<User> findById(Long userId);
    User save(User user);
    void updatePassword(Long userId, String encryptedPassword);
    void deleteRolesFromOtherUsers(Long adminId);
    void deleteOtherUsers(Long adminId);
}