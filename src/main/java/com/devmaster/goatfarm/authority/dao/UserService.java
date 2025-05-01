package com.devmaster.goatfarm.authority.dao;

import com.devmaster.goatfarm.authority.api.projection.UserDetailsProjection;
import com.devmaster.goatfarm.authority.model.entity.Role;
import com.devmaster.goatfarm.authority.model.entity.User;
import com.devmaster.goatfarm.authority.model.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService implements UserDetailsService {


    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        List<UserDetailsProjection> result = repository.searchUserAndRolesByEmail(username);
        if(result.isEmpty()) {
            throw new UsernameNotFoundException("User not found.");
        }

        User user = new User();
        user.setEmail(username); // O username (email) já é passado como parâmetro
        user.setPassword(result.get(0).getPassword()); // Pega a senha do primeiro elemento

        for (UserDetailsProjection userP : result) {
            user.addRole(new Role(userP.getRoleId(), userP.getAuthority()));
        }
        return user;
    }
}
