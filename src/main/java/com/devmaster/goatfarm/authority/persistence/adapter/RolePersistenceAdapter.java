package com.devmaster.goatfarm.authority.persistence.adapter;

import com.devmaster.goatfarm.authority.application.ports.out.RolePersistencePort;
import com.devmaster.goatfarm.authority.persistence.entity.Role;
import com.devmaster.goatfarm.authority.persistence.repository.RoleRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RolePersistenceAdapter implements RolePersistencePort {
    private final RoleRepository repository;

    public RolePersistenceAdapter(RoleRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Role> findByAuthority(String authority) {
        return repository.findByAuthority(authority);
    }
}