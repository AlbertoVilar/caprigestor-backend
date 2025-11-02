package com.devmaster.goatfarm.authority.business;

import com.devmaster.goatfarm.authority.dao.RoleDAO;
import com.devmaster.goatfarm.authority.dao.UserDAO;
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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AdminMaintenanceBusiness adminMaintenanceBusiness;

    public boolean updateUserPassword(String email, String newPassword) {
        try {
            // Validação de entrada
            if (email == null || email.trim().isEmpty()) {
                throw new IllegalArgumentException("Email não pode ser vazio");
            }
            if (newPassword == null || newPassword.trim().isEmpty()) {
                throw new IllegalArgumentException("Nova senha não pode ser vazia");
            }

            User user = userDAO.findUserByEmail(email).orElse(null);
            if (user == null) {
                return false;
            }

            user.setPassword(passwordEncoder.encode(newPassword));
            userDAO.save(user);

            logger.info("[ADMIN] Senha atualizada para usuário: {}", email);
            return true;
        } catch (Exception e) {
            logger.error("[ADMIN] Erro ao atualizar senha: {}", e.getMessage());
            throw new RuntimeException("Erro ao atualizar senha: " + e.getMessage(), e);
        }
    }

    public User getUserByEmail(String email) {
        try {
            // Validação de entrada
            if (email == null || email.trim().isEmpty()) {
                throw new IllegalArgumentException("Email não pode ser vazio");
            }

            return userDAO.findUserByEmail(email).orElse(null);
        } catch (Exception e) {
            logger.error("[ADMIN] Erro ao buscar usuário: {}", e.getMessage());
            throw new RuntimeException("Erro ao buscar usuário: " + e.getMessage(), e);
        }
    }

    public boolean cleanDatabaseAndSetupAdmin() {
        try {
            // Encontra o usuário admin
            User adminUser = userDAO.findUserByEmail("albertovilar1@gmail.com").orElse(null);
            if (adminUser == null) {
                throw new IllegalStateException("Usuário admin não encontrado!");
            }

            // Atualiza o CPF e senha do admin
            adminUser.setCpf("05202259450");
            adminUser.setPassword(passwordEncoder.encode("password"));
            userDAO.save(adminUser);
            
            logger.info("[ADMIN] Senha atualizada para: password");

            // Limpa o banco em etapas para evitar violação de chave estrangeira
            adminMaintenanceBusiness.cleanDatabaseAndSetupAdmin(adminUser.getId());
            
            // Recriar as roles se não existirem
            Role adminRole = ensureRoleExists("ROLE_ADMIN");
            Role operatorRole = ensureRoleExists("ROLE_OPERATOR");
            
            // Adicionar as roles ao usuário admin
            adminUser.addRole(adminRole);
            adminUser.addRole(operatorRole);
            logger.info("[ADMIN] Roles ROLE_ADMIN e ROLE_OPERATOR adicionadas ao usuário");
            
            userDAO.save(adminUser);
            logger.info("[ADMIN] Roles do usuário admin recriadas");

            logger.info("[ADMIN] Banco limpo! Mantido apenas: {} com CPF: {}", adminUser.getEmail(), adminUser.getCpf());
            
            return true;
        } catch (Exception e) {
            logger.error("[ADMIN] Erro ao limpar banco: {}", e.getMessage());
            throw new RuntimeException("Erro ao limpar banco: " + e.getMessage(), e);
        }
    }

    private Role ensureRoleExists(String authority) {
        Role role = roleDAO.findByAuthority(authority).orElse(null);
        if (role == null) {
            role = new Role();
            role.setAuthority(authority);
            role = roleDAO.save(role);
            logger.info("[ADMIN] Role {} criada", authority);
        }
        return role;
    }
}