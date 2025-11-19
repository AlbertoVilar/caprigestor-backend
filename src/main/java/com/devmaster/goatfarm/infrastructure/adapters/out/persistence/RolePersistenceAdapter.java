package com.devmaster.goatfarm.infrastructure.adapters.out.persistence;

import com.devmaster.goatfarm.application.ports.out.RolePersistencePort;
import com.devmaster.goatfarm.authority.model.entity.Role;
import com.devmaster.goatfarm.authority.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RolePersistenceAdapter implements RolePersistencePort {
    private final RoleRepository repository;

    @Autowired
    public RolePersistenceAdapter(RoleRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Role> findByAuthority(String authority) {
        return repository.findByAuthority(authority);
    }
}