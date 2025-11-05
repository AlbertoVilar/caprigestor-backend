package com.devmaster.goatfarm.authority.business;

import com.devmaster.goatfarm.authority.dao.RoleDAO;
import com.devmaster.goatfarm.authority.dao.UserDAO;
import com.devmaster.goatfarm.authority.mapper.RoleMapper;
import com.devmaster.goatfarm.authority.model.entity.Role;
import com.devmaster.goatfarm.authority.model.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AdminBusiness {

    private static final Logger logger = LoggerFactory.getLogger(AdminBusiness.class);

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private RoleDAO roleDAO;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AdminMaintenanceBusiness adminMaintenanceBusiness;

    public boolean updateUserPassword(String email, String newPassword) {
        User user = userDAO.findUserByEmail(email).orElse(null);
        if (user != null) {
            user.setPassword(passwordEncoder.encode(newPassword));
            userDAO.save(user);
            return true;
        } else {
            logger.warn("Usuário com email {} não encontrado para atualização de senha", email);
            return false;
        }
    }

    public User getUserByEmail(String email) {
        User user = userDAO.findUserByEmail(email).orElse(null);
        if (user == null) {
            logger.warn("Usuário com email {} não encontrado", email);
            return null;
        }
        return user;
    }

    @Transactional
    public boolean cleanDatabaseAndSetupAdmin() {
        try {
            User adminUser = userDAO.findUserByEmail("alberto.vilar@example.com").orElse(null);
            if (adminUser == null) {
                logger.error("Usuário admin não encontrado");
                return false;
            }

            adminUser.setPassword(passwordEncoder.encode("132747"));
            userDAO.save(adminUser);

            adminMaintenanceBusiness.cleanDatabaseAndSetupAdmin(adminUser.getId());

            ensureRoleExists("ROLE_ADMIN");
            ensureRoleExists("ROLE_OPERATOR");

            adminUser.getRoles().clear();
            adminUser.getRoles().add(ensureRoleExists("ROLE_ADMIN"));
            adminUser.getRoles().add(ensureRoleExists("ROLE_OPERATOR"));
            userDAO.save(adminUser);

            logger.info("Banco limpo e usuário admin configurado com sucesso");
            return true;
        } catch (Exception e) {
            logger.error("Erro ao limpar banco e configurar admin: {}", e.getMessage(), e);
            return false;
        }
    }

    private Role ensureRoleExists(String authority) {
        return roleDAO.findByAuthority(authority)
                .orElseGet(() -> {
                    Role role = roleMapper.toEntity(authority);
                    Role saved = roleDAO.save(role);
                    logger.info("Role {} criada", authority);
                    return saved;
                });
    }
}
