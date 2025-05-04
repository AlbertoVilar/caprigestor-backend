package com.devmaster.goatfarm.authority.dao;

import com.devmaster.goatfarm.authority.api.projection.UserDetailsProjection;
import com.devmaster.goatfarm.authority.business.bo.UserResponseVO;
import com.devmaster.goatfarm.authority.conveter.UserEntityConverter;
import com.devmaster.goatfarm.authority.model.entity.Role;
import com.devmaster.goatfarm.authority.model.entity.User;
import com.devmaster.goatfarm.authority.model.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserDAO implements UserDetailsService {


    private final UserRepository repository;

    public UserDAO(UserRepository repository) {
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

    protected User authenticated() {

        String username = null;
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Jwt jwtPrincipal = (Jwt) authentication.getPrincipal();
            username = jwtPrincipal.getClaim("username");

            User user = repository.findByEmail(username).get();

            return user;

        } catch (Exception e) {
            throw new RuntimeException("Email não encontrado");
        }

    }


    @Transactional(readOnly = true)
    public UserResponseVO getMe() {
        User user = authenticated();

        return UserEntityConverter.toVO(user);
    }
}
