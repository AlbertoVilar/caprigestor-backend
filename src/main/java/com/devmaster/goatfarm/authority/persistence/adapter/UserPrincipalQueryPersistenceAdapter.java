package com.devmaster.goatfarm.authority.persistence.adapter;

import com.devmaster.goatfarm.authority.application.ports.out.UserPrincipalQueryPort;
import com.devmaster.goatfarm.authority.business.bo.AuthenticatedPrincipal;
import com.devmaster.goatfarm.authority.persistence.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Persistence adapter that exposes only the application principal projection. */
@Component
public class UserPrincipalQueryPersistenceAdapter implements UserPrincipalQueryPort {
    private final UserRepository repository;

    public UserPrincipalQueryPersistenceAdapter(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<AuthenticatedPrincipal> findByEmail(String email) {
        return repository.findByEmail(email).map(user -> new AuthenticatedPrincipal(
                user.getId(), user.getEmail(), user.getName(),
                user.getRoles().stream().map(role -> role.getAuthority()).collect(java.util.stream.Collectors.toSet())
        ));
    }
}
