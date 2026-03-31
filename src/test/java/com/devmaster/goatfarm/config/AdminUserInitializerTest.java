package com.devmaster.goatfarm.config;

import com.devmaster.goatfarm.authority.persistence.entity.Role;
import com.devmaster.goatfarm.authority.persistence.entity.User;
import com.devmaster.goatfarm.authority.persistence.repository.RoleRepository;
import com.devmaster.goatfarm.authority.persistence.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserInitializerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldNotResetExistingAdminPasswordWhenResetFlagIsDisabled() throws Exception {
        Role adminRole = new Role(1L, "ROLE_ADMIN");
        Role operatorRole = new Role(2L, "ROLE_OPERATOR");
        Role farmOwnerRole = new Role(3L, "ROLE_FARM_OWNER");
        Role viewerRole = new Role(4L, "ROLE_VIEWER");

        when(roleRepository.findByAuthority("ROLE_ADMIN")).thenReturn(Optional.of(adminRole));
        when(roleRepository.findByAuthority("ROLE_OPERATOR")).thenReturn(Optional.of(operatorRole));
        when(roleRepository.findByAuthority("ROLE_FARM_OWNER")).thenReturn(Optional.of(farmOwnerRole));
        when(roleRepository.findByAuthority("ROLE_VIEWER")).thenReturn(Optional.of(viewerRole));

        User admin = new User();
        admin.setEmail("albertovilar1@gmail.com");
        admin.setPassword("encoded-existing");
        admin.addRole(adminRole);
        admin.addRole(operatorRole);

        when(userRepository.findByEmail("albertovilar1@gmail.com")).thenReturn(Optional.of(admin));

        AdminUserInitializer initializer = new AdminUserInitializer(
                userRepository,
                roleRepository,
                passwordEncoder,
                true,
                false,
                "albertovilar1@gmail.com",
                "Alberto Vilar",
                "05202259450",
                "132747"
        );

        initializer.run();

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any(User.class));
        assertThat(admin.getPassword()).isEqualTo("encoded-existing");
    }

    @Test
    void shouldCreateBootstrapAdminOnlyWhenEnabled() throws Exception {
        Role adminRole = new Role(1L, "ROLE_ADMIN");
        Role operatorRole = new Role(2L, "ROLE_OPERATOR");
        Role farmOwnerRole = new Role(3L, "ROLE_FARM_OWNER");
        Role viewerRole = new Role(4L, "ROLE_VIEWER");

        when(roleRepository.findByAuthority("ROLE_ADMIN")).thenReturn(Optional.of(adminRole));
        when(roleRepository.findByAuthority("ROLE_OPERATOR")).thenReturn(Optional.of(operatorRole));
        when(roleRepository.findByAuthority("ROLE_FARM_OWNER")).thenReturn(Optional.of(farmOwnerRole));
        when(roleRepository.findByAuthority("ROLE_VIEWER")).thenReturn(Optional.of(viewerRole));
        when(userRepository.findByEmail("bootstrap@local")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("super-secret")).thenReturn("encoded-secret");

        AdminUserInitializer initializer = new AdminUserInitializer(
                userRepository,
                roleRepository,
                passwordEncoder,
                true,
                false,
                "bootstrap@local",
                "Bootstrap Admin",
                "12345678901",
                "super-secret"
        );

        initializer.run();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo("bootstrap@local");
        assertThat(saved.getPassword()).isEqualTo("encoded-secret");
        assertThat(saved.hasRole("ROLE_ADMIN")).isTrue();
        assertThat(saved.hasRole("ROLE_OPERATOR")).isTrue();
    }
}
