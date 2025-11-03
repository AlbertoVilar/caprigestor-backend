package com.devmaster.goatfarm.config.security;

import com.devmaster.goatfarm.authority.dao.UserDAO;
import com.devmaster.goatfarm.authority.model.entity.User;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.config.exceptions.custom.UnauthorizedException;
import com.devmaster.goatfarm.events.dao.EventDao;
import com.devmaster.goatfarm.events.model.entity.Event;
import com.devmaster.goatfarm.farm.dao.GoatFarmDAO;
import com.devmaster.goatfarm.farm.model.entity.GoatFarm;
import com.devmaster.goatfarm.goat.dao.GoatDAO;
import com.devmaster.goatfarm.goat.model.entity.Goat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class OwnershipService {

    private static final Logger logger = LoggerFactory.getLogger(OwnershipService.class);

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private GoatFarmDAO goatFarmDAO;

    @Autowired
    private GoatDAO goatDAO;

    @Autowired
    private EventDao eventDAO;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            String email = authentication.getName();
            return userDAO.findUserByEmail(email)
                    .orElseThrow(() -> new UnauthorizedException("Usuário autenticado não encontrado: " + email));
        }
        throw new UnauthorizedException("Usuário não autenticado");
    }

    public boolean isCurrentUserAdmin() {
        User currentUser = getCurrentUser();
        return currentUser.getRoles().stream()
                .anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));
    }

    public void verifyFarmOwnership(Long farmId) {
        if (isCurrentUserAdmin()) {
            logger.debug("✅ OWNERSHIP: Usuário é ADMIN - acesso liberado para a fazenda {}", farmId);
            return;
        }
        User currentUser = getCurrentUser();
        goatFarmDAO.findByIdAndUserId(farmId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Acesso negado: Você não tem permissão para acessar a fazenda com ID: " + farmId));
    }

    public void verifyGoatOwnership(Long farmId, String goatId) {
        verifyFarmOwnership(farmId);
        goatDAO.findByIdAndFarmId(goatId, farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Cabra " + goatId + " não encontrada na fazenda " + farmId));
    }

    public void verifyEventOwnership(Long farmId, String goatId, Long eventId) {
        verifyGoatOwnership(farmId, goatId);
        Event event = eventDAO.findEventById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado: " + eventId));
        if (!event.getGoat().getRegistrationNumber().equals(goatId)) {
            throw new ResourceNotFoundException("Este evento não pertence à cabra especificada.");
        }
    }
}
