package com.devmaster.goatfarm.config;

import com.devmaster.goatfarm.authority.persistence.entity.Role;
import com.devmaster.goatfarm.authority.persistence.entity.User;
import com.devmaster.goatfarm.authority.persistence.repository.RoleRepository;
import com.devmaster.goatfarm.authority.persistence.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@Profile("!test")
public class AdminUserInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserInitializer.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean bootstrapAdminEnabled;
    private final boolean resetAdminPassword;
    private final String adminEmail;
    private final String adminName;
    private final String adminCpf;
    private final String adminInitialPassword;

    public AdminUserInitializer(UserRepository userRepository,
                                RoleRepository roleRepository,
                                PasswordEncoder passwordEncoder,
                                @Value("${caprigestor.bootstrap.admin.enabled:true}") boolean bootstrapAdminEnabled,
                                @Value("${caprigestor.bootstrap.admin.reset-password:false}") boolean resetAdminPassword,
                                @Value("${caprigestor.bootstrap.admin.email:albertovilar1@gmail.com}") String adminEmail,
                                @Value("${caprigestor.bootstrap.admin.name:Alberto Vilar}") String adminName,
                                @Value("${caprigestor.bootstrap.admin.cpf:05202259450}") String adminCpf,
                                @Value("${caprigestor.bootstrap.admin.initial-password:132747}") String adminInitialPassword) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.bootstrapAdminEnabled = bootstrapAdminEnabled;
        this.resetAdminPassword = resetAdminPassword;
        this.adminEmail = adminEmail;
        this.adminName = adminName;
        this.adminCpf = adminCpf;
        this.adminInitialPassword = adminInitialPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        logger.info(">>> Iniciando AdminUserInitializer <<<");

        Role adminRole = ensureRoleExists("ROLE_ADMIN");
        Role operatorRole = ensureRoleExists("ROLE_OPERATOR");
        ensureRoleExists("ROLE_FARM_OWNER");
        ensureRoleExists("ROLE_VIEWER");

        Optional<User> adminUserOptional = userRepository.findByEmail(adminEmail);

        if (adminUserOptional.isPresent()) {
            User adminUser = adminUserOptional.get();
            boolean changed = false;

            if (!adminUser.hasRole("ROLE_ADMIN")) {
                adminUser.addRole(adminRole);
                changed = true;
            }
            if (!adminUser.hasRole("ROLE_OPERATOR")) {
                adminUser.addRole(operatorRole);
                changed = true;
            }
            if (resetAdminPassword) {
                adminUser.setPassword(passwordEncoder.encode(adminInitialPassword));
                changed = true;
                logger.warn(">>> Reset de senha bootstrap do admin habilitado. Use apenas em ambiente controlado. <<<");
            }

            if (changed) {
                userRepository.save(adminUser);
                logger.info(">>> Usuário administrativo atualizado com sucesso. <<<");
            } else {
                logger.info(">>> Usuário administrativo já existente e sem alterações de bootstrap. <<<");
            }
            return;
        }

        if (!bootstrapAdminEnabled) {
            logger.warn(">>> Bootstrap do admin desabilitado e usuário '{}' não encontrado. <<<", adminEmail);
            return;
        }

        User adminUser = new User();
        adminUser.setName(adminName);
        adminUser.setEmail(adminEmail);
        adminUser.setCpf(adminCpf);
        adminUser.setPassword(passwordEncoder.encode(adminInitialPassword));
        adminUser.addRole(adminRole);
        adminUser.addRole(operatorRole);

        userRepository.save(adminUser);
        logger.warn(">>> Usuário administrativo bootstrap criado. Altere a senha antes de expor o sistema. <<<");
    }

    private Role ensureRoleExists(String authority) {
        Role role = roleRepository.findByAuthority(authority).orElse(null);
        if (role == null) {
            role = new Role();
            role.setAuthority(authority);
            role = roleRepository.save(role);
            logger.info(">>> Role {} criada <<<", authority);
        }
        return role;
    }
}
