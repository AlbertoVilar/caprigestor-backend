package com.devmaster.goatfarm.config.security;

import com.devmaster.goatfarm.authority.application.ports.out.UserPersistencePort;
import com.devmaster.goatfarm.authority.business.bo.AuthorityAccount;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    private final UserPersistencePort userPort;

    public CustomUserDetailsService(UserPersistencePort userPort) {
        this.userPort = userPort;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AuthorityAccount user = userPort.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado: " + username));
        return org.springframework.security.core.userdetails.User.withUsername(user.email())
                .password(user.encodedPassword())
                .authorities(user.roles().toArray(String[]::new))
                .build();
    }
}

