package com.devmaster.goatfarm.authority.business;

import com.devmaster.goatfarm.authority.application.ports.out.RolePersistencePort;
import com.devmaster.goatfarm.authority.application.ports.out.UserPersistencePort;
import com.devmaster.goatfarm.authority.persistence.entity.Role;
import com.devmaster.goatfarm.authority.persistence.entity.User;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AdminBusiness {

    private static final Logger logger = LoggerFactory.getLogger(AdminBusiness.class);

    private final UserPersistencePort userPort;
    private final RolePersistencePort rolePort;
    private final PasswordEncoder passwordEncoder;
    private final AdminMaintenanceBusiness adminMaintenanceBusiness;

    public AdminBusiness(UserPersistencePort userPort, RolePersistencePort rolePort, PasswordEncoder passwordEncoder, AdminMaintenanceBusiness adminMaintenanceBusiness) {
        this.userPort = userPort;
        this.rolePort = rolePort;
        this.passwordEncoder = passwordEncoder;
        this.adminMaintenanceBusiness = adminMaintenanceBusiness;
    }

    public boolean updateUserPassword(String email, String newPassword) {
        User user = userPort.findByEmail(email).orElse(null);
        if (user != null) {
            String encrypted = passwordEncoder.encode(newPassword);
            userPort.updatePassword(user.getId(), encrypted);
            return true;
        } else {
            logger.warn("Usuário com email {} não encontrado para atualização de senha", email);
            return false;
        }
    }

    public User getUserByEmail(String email) {
        User user = userPort.findByEmail(email).orElse(null);
        if (user == null) {
            logger.warn("Usuário com email {} não encontrado", email);
            return null;
        }
        return user;
    }

    @Transactional
    public boolean cleanDatabaseAndSetupAdmin() {
        try {
            User adminUser = userPort.findByEmail("alberto.vilar@example.com").orElse(null);
            if (adminUser == null) {
                logger.error("Usuário admin não encontrado");
                return false;
            }

            adminUser.setPassword(passwordEncoder.encode("132747"));
            userPort.save(adminUser);

            adminMaintenanceBusiness.cleanDatabaseAndSetupAdmin(adminUser.getId());

            ensureRoleExists("ROLE_ADMIN");
            ensureRoleExists("ROLE_OPERATOR");

            adminUser.getRoles().clear();
            adminUser.getRoles().add(ensureRoleExists("ROLE_ADMIN"));
            adminUser.getRoles().add(ensureRoleExists("ROLE_OPERATOR"));
            userPort.save(adminUser);

            logger.info("Banco limpo e usuário admin configurado com sucesso");
            return true;
        } catch (Exception e) {
            logger.error("Erro ao limpar banco e configurar admin: {}", e.getMessage(), e);
            return false;
        }
    }

    private Role ensureRoleExists(String authority) {
        return rolePort.findByAuthority(authority)
                .orElseGet(() -> {
                    // Se a role não existir, não a cria aqui, apenas loga e lança erro
                    logger.error("Role {} não encontrada e criação automática não suportada nesta camada", authority);
                    throw new ResourceNotFoundException("Role não encontrada: " + authority);
                });
    }
}