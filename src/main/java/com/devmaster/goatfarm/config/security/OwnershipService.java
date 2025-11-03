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
import com.devmaster.goatfarm.address.model.entity.Address;
import com.devmaster.goatfarm.phone.model.entity.Phone;
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

    public void verifyGoatOwnership(String goatId) {
        if (isCurrentUserAdmin()) {
            return;
        }
        Goat goat = goatDAO.findByRegistrationNumber(goatId)
                .orElseThrow(() -> new ResourceNotFoundException("Cabra não encontrada: " + goatId));
        verifyFarmOwnership(goat.getFarm().getId());
    }

    public void verifyEventOwnership(Long eventId) {
        if (isCurrentUserAdmin()) {
            return;
        }
        Event event = eventDAO.findEventById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado: " + eventId));
        verifyGoatOwnership(event.getGoat().getRegistrationNumber());
    }

    public void verifyAddressOwnership(Long addressId) {
        if (isCurrentUserAdmin()) {
            return;
        }
        GoatFarm farm = goatFarmDAO.findByAddressId(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Endereço não está associado a nenhuma fazenda sua."));
        verifyFarmOwnership(farm.getId());
    }

    public void verifyPhoneOwnership(Long phoneId) {
        // Supondo que Phone tenha uma relação com GoatFarm
        // Esta lógica precisaria de um método em PhoneDAO para encontrar a fazenda de um telefone
    }
}
