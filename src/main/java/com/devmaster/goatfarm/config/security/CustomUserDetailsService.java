package com.devmaster.goatfarm.config.security;

import com.devmaster.goatfarm.authority.dao.UserDAO; // Importa o UserDAO
import com.devmaster.goatfarm.authority.model.entity.User;
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
    private UserDAO userDAO; // Usa o UserDAO

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Chama o método do DAO, que já retorna a entidade User
        User user = userDAO.findUserByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("Usuário não encontrado: " + username);
        }
        return user;
    }
}
