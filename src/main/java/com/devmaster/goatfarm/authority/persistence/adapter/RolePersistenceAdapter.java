package com.devmaster.goatfarm.authority.persistence.adapter;

import com.devmaster.goatfarm.authority.application.ports.out.RolePersistencePort;
import com.devmaster.goatfarm.authority.business.bo.AuthorityRole;
import com.devmaster.goatfarm.authority.persistence.entity.Role;
import com.devmaster.goatfarm.authority.persistence.repository.RoleRepository;
import com.devmaster.goatfarm.authority.persistence.mapper.AuthorityPersistenceMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RolePersistenceAdapter implements RolePersistencePort {
    private final RoleRepository repository;
    private final AuthorityPersistenceMapper mapper;

    public RolePersistenceAdapter(RoleRepository repository, AuthorityPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<AuthorityRole> findByAuthority(String authority) {
        return repository.findByAuthority(authority).map(mapper::toRole);
    }
}
