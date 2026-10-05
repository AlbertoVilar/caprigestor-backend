package com.devmaster.goatfarm.authority.persistence.adapter;

import com.devmaster.goatfarm.authority.application.ports.out.UserPersistencePort;
import com.devmaster.goatfarm.authority.business.bo.AuthorityAccount;
import com.devmaster.goatfarm.authority.persistence.entity.User;
import com.devmaster.goatfarm.authority.persistence.repository.UserRepository;
import com.devmaster.goatfarm.authority.persistence.repository.RoleRepository;
import com.devmaster.goatfarm.authority.persistence.mapper.AuthorityPersistenceMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class UserPersistenceAdapter implements UserPersistencePort {
    private final UserRepository repository;
    private final RoleRepository roleRepository;
    private final AuthorityPersistenceMapper mapper;

    public UserPersistenceAdapter(UserRepository repository, RoleRepository roleRepository,
                                  AuthorityPersistenceMapper mapper) {
        this.repository = repository;
        this.roleRepository = roleRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<AuthorityAccount> findByEmail(String email) {
        return repository.findByEmail(email).map(mapper::toAccount);
    }

    @Override
    public Optional<AuthorityAccount> findByCpf(String cpf) {
        return repository.findByCpf(cpf).map(mapper::toAccount);
    }

    @Override
    public Optional<AuthorityAccount> findById(Long userId) {
        return repository.findById(userId).map(mapper::toAccount);
    }

    @Override
    public AuthorityAccount save(AuthorityAccount account) {
        User entity = mapper.toEntity(account);
        account.roles().stream().map(roleRepository::findByAuthority)
                .flatMap(Optional::stream).forEach(entity::addRole);
        return mapper.toAccount(repository.save(entity));
    }

    @Override
    public void updatePassword(Long userId, String encryptedPassword) {
        repository.findById(userId).ifPresent(user -> {
            user.setPassword(encryptedPassword);
            repository.save(user);
        });
    }

}
