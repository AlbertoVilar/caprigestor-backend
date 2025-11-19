package com.devmaster.goatfarm.config.security;

import com.devmaster.goatfarm.application.ports.out.UserPersistencePort; import com.devmaster.goatfarm.authority.model.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserPersistencePort userPort; 
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userPort.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado: " + username));
        return user;
    }
}

