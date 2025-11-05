package com.devmaster.goatfarm.config.security;

import com.devmaster.goatfarm.authority.dao.UserDAO; import com.devmaster.goatfarm.authority.model.entity.User;
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
    private UserDAO userDAO; 
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userDAO.findUserByUsername(username);
        if (user == null) {
            // Evita problemas de encoding em ambientes que não tratam UTF-8
            throw new UsernameNotFoundException("Usuario nao encontrado: " + username);
        }
        return user;
    }
}

