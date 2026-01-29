package com.devmaster.goatfarm.authority.persistence.adapter;

import com.devmaster.goatfarm.authority.application.ports.out.UserPersistencePort;
import com.devmaster.goatfarm.authority.persistence.entity.User;
import com.devmaster.goatfarm.authority.persistence.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserPersistenceAdapter implements UserPersistencePort {
    private final UserRepository repository;

    public UserPersistenceAdapter(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    @Override
    public Optional<User> findByCpf(String cpf) {
        return repository.findByCpf(cpf);
    }

    @Override
    public Optional<User> findById(Long userId) {
        return repository.findById(userId);
    }

    @Override
    public User save(User user) {
        return repository.save(user);
    }

    @Override
    public void updatePassword(Long userId, String encryptedPassword) {
        repository.findById(userId).ifPresent(user -> {
            user.setPassword(encryptedPassword);
            repository.save(user);
        });
    }

    @Override
    public void deleteRolesFromOtherUsers(Long adminId) {
        repository.deleteRolesFromOtherUsers(adminId);
    }

    @Override
    public void deleteOtherUsers(Long adminId) {
        repository.deleteOtherUsers(adminId);
    }
}