package com.devmaster.goatfarm.authority.dao;

import com.devmaster.goatfarm.authority.business.bo.UserRequestVO;
import com.devmaster.goatfarm.authority.business.bo.UserResponseVO;
import com.devmaster.goatfarm.authority.conveter.UserEntityConverter;
import com.devmaster.goatfarm.authority.model.entity.Role;
import com.devmaster.goatfarm.authority.model.entity.User;
import com.devmaster.goatfarm.authority.model.repository.RoleRepository;
import com.devmaster.goatfarm.authority.model.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserDAO {

    private final UserRepository repository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserDAO(UserRepository repository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    protected User authenticated() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Jwt jwtPrincipal = (Jwt) authentication.getPrincipal();
            String username = jwtPrincipal.getClaim("username");
            return repository.findByEmail(username).orElseThrow();
        } catch (Exception e) {
            throw new RuntimeException("Email não encontrado");
        }
    }

    @Transactional(readOnly = true)
    public UserResponseVO getMe() {
        User user = authenticated();
        return UserEntityConverter.toVO(user);
    }

    @Transactional(readOnly = true)
    public User findUserByUsername(String username) {
        return repository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com email: " + username));
    }

    @Transactional
    public UserResponseVO saveUser(UserRequestVO vo) {
        User user = UserEntityConverter.fromVO(vo);

        // Resolver roles já salvas no banco
        if (vo.getRoles() != null && !vo.getRoles().isEmpty()) {
            user.getRoles().clear(); // evitar acúmulo ou roles duplicadas

            vo.getRoles().forEach(roleName -> {
                Optional<Role> optionalRole = roleRepository.findByAuthority(roleName);
                if (optionalRole.isEmpty()) {
                    throw new RuntimeException("Role não encontrada: " + roleName);
                }
                user.addRole(optionalRole.get());
            });
        }

        // Criptografar senha antes de salvar
        user.setPassword(passwordEncoder.encode(vo.getPassword()));

        User savedUser = repository.save(user);
        return UserEntityConverter.toVO(savedUser);
    }
}
