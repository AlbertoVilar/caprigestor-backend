package com.devmaster.goatfarm.config.security;

import com.devmaster.goatfarm.config.exceptions.custom.UnauthorizedException;
import com.devmaster.goatfarm.authority.dao.UserDAO;
import com.devmaster.goatfarm.authority.model.entity.User;
import com.devmaster.goatfarm.farm.dao.GoatFarmDAO;
import com.devmaster.goatfarm.goat.dao.GoatDAO;
import org.springframework.stereotype.Service;

@Service
public class OwnershipService {

    private final GoatFarmDAO goatFarmDAO;
    private final UserDAO userDAO;
    private final GoatDAO goatDAO;

    public OwnershipService(GoatFarmDAO goatFarmDAO, UserDAO userDAO, GoatDAO goatDAO) {
        this.goatFarmDAO = goatFarmDAO;
        this.userDAO = userDAO;
        this.goatDAO = goatDAO;
    }

    public void verifyFarmOwnership(Long farmId) {
        var current = userDAO.getAuthenticatedEntity();
        boolean isAdmin = current.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equals(r.getAuthority()));
        if (isAdmin) return;

        var farm = goatFarmDAO.findFarmEntityById(farmId);
        if (farm.getUser() == null || !farm.getUser().getId().equals(current.getId())) {
            throw new UnauthorizedException("Usuário não é proprietário desta fazenda.");
        }
    }

    public void verifyGoatOwnership(Long farmId, String goatId) {
        // Primeiro, verifica se o usuário é dono da fazenda (admin tem bypass)
        verifyFarmOwnership(farmId);
        // Depois, garante que a cabra pertence à fazenda informada
        var goatOpt = goatDAO.findByIdAndFarmId(goatId, farmId);
        if (goatOpt.isEmpty()) {
            throw new UnauthorizedException("Cabra não pertence à fazenda informada.");
        }
    }

    public User getCurrentUser() {
        return userDAO.getAuthenticatedEntity();
    }

    public boolean isCurrentUserAdmin() {
        var current = userDAO.getAuthenticatedEntity();
        return current.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equals(r.getAuthority()));
    }
}
