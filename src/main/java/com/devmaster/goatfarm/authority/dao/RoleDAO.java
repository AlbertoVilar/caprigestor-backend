package com.devmaster.goatfarm.authority.dao;

import com.devmaster.goatfarm.authority.model.entity.Role;
import com.devmaster.goatfarm.authority.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class RoleDAO {

    private final RoleRepository repository;

    @Autowired
    public RoleDAO(RoleRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Optional<Role> findByAuthority(String authority) {
        return repository.findByAuthority(authority);
    }

    @Transactional
    public Role save(Role role) {
        return repository.save(role);
    }
}
