package com.devmaster.goatfarm.config.security;

import com.devmaster.goatfarm.authority.model.entity.User;
import com.devmaster.goatfarm.authority.model.repository.UserRepository;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.config.exceptions.custom.UnauthorizedException;
import com.devmaster.goatfarm.farm.model.entity.GoatFarm;
import com.devmaster.goatfarm.farm.model.repository.GoatFarmRepository;
import com.devmaster.goatfarm.goat.model.entity.Goat;
import com.devmaster.goatfarm.goat.model.repository.GoatRepository;
import com.devmaster.goatfarm.events.model.entity.Event;
import com.devmaster.goatfarm.events.model.repository.EventRepository;
import com.devmaster.goatfarm.address.model.entity.Address;
import com.devmaster.goatfarm.phone.model.entity.Phone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Service para verificaÃ§Ã£o de ownership (propriedade) de recursos.
 * Centraliza a lÃ³gica de verificaÃ§Ã£o se um usuÃ¡rio tem permissÃ£o para acessar/modificar recursos.
 */
@Service
public class OwnershipService {

    private static final Logger logger = LoggerFactory.getLogger(OwnershipService.class);

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private GoatFarmRepository goatFarmRepository;
    
    @Autowired
    private GoatRepository goatRepository;
    
    @Autowired
    private EventRepository eventRepository;

    /**
     * ObtÃ©m o usuÃ¡rio atualmente autenticado
     * @return User autenticado
     * @throws UnauthorizedException se nÃ£o hÃ¡ usuÃ¡rio autenticado
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            String email = authentication.getName();
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new UnauthorizedException("UsuÃ¡rio autenticado nÃ£o encontrado: " + email));
        }
        throw new UnauthorizedException("UsuÃ¡rio nÃ£o autenticado");
    }

    /**
     * ObtÃ©m o usuÃ¡rio atualmente autenticado ou null se nÃ£o estiver autenticado
     * @return User autenticado ou null
     */
    public User getCurrentUserOrNull() {
        try {
            return getCurrentUser();
        } catch (RuntimeException e) {
            return null;
        }
    }

    /**
     * Verifica se o usuÃ¡rio atual Ã© ADMIN
     * @return true se for ADMIN, false caso contrÃ¡rio
     */
    public boolean isCurrentUserAdmin() {
        try {
            User currentUser = getCurrentUser();
            return currentUser.getRoles().stream()
                    .anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));
        } catch (RuntimeException e) {
            return false;
        }
    }

    /**
     * Verifica se o usuÃ¡rio atual Ã© ADMIN ou OPERATOR
     * @return true se for ADMIN ou OPERATOR, false caso contrÃ¡rio
     */
    public boolean isCurrentUserAdminOrOperator() {
        return isCurrentUserAdmin() || isCurrentUserOperator();
    }

    /**
     * Verifica se o usuÃ¡rio atual Ã© OPERATOR (dono de fazenda)
     * @return true se for OPERATOR, false caso contrÃ¡rio
     */
    public boolean isCurrentUserOperator() {
        try {
            User currentUser = getCurrentUser();
            return currentUser.getRoles().stream()
                    .anyMatch(role -> role.getAuthority().equals("ROLE_OPERATOR"));
        } catch (RuntimeException e) {
            return false;
        }
    }

    /**
     * Verifica se o usuÃ¡rio atual tem permissÃ£o para acessar uma fazenda
     * ADMIN tem acesso a tudo, OPERATOR sÃ³ Ã  prÃ³pria fazenda
     * @param farm Fazenda a ser verificada
     * @throws ResourceNotFoundException se nÃ£o tem permissÃ£o
     */
    public void verifyFarmOwnership(GoatFarm farm) {
        User currentUser = getCurrentUser();
        logger.debug("ðŸ” OWNERSHIP: Verificando acesso para usuÃ¡rio: {}", currentUser.getEmail());
        logger.debug("ðŸ” OWNERSHIP: Roles do usuÃ¡rio: {}", currentUser.getRoles().stream().map(r -> r.getAuthority()).toList());
        
                if (isCurrentUserAdmin()) {
            logger.debug("âœ… OWNERSHIP: UsuÃ¡rio Ã© ADMIN - acesso liberado");
            return;
        }

                if (farm == null) {
            logger.warn("âŒ OWNERSHIP: Fazenda Ã© null");
            throw new ResourceNotFoundException("Fazenda nÃ£o encontrada");
        }

        logger.debug("ðŸ” OWNERSHIP: Fazenda ID: {}, ProprietÃ¡rio ID: {}", farm.getId(), (farm.getUser() != null ? farm.getUser().getId() : "null"));
        logger.debug("ðŸ” OWNERSHIP: UsuÃ¡rio atual ID: {}", currentUser.getId());
        
        if (farm.getUser() == null || !farm.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Acesso negado: VocÃª nÃ£o tem permissÃ£o para acessar esta fazenda");
        }
    }

    /**
     * Verifica se o usuÃ¡rio atual tem permissÃ£o para acessar uma fazenda (versÃ£o para VO)
     * ADMIN tem acesso a tudo, OPERATOR sÃ³ Ã  prÃ³pria fazenda
     * @param farmVO Fazenda VO a ser verificada
     * @throws ResourceNotFoundException se nÃ£o tem permissÃ£o
     */
    public void verifyFarmOwnership(com.devmaster.goatfarm.farm.business.bo.GoatFarmFullResponseVO farmVO) {
        User currentUser = getCurrentUser();
        
                if (isCurrentUserAdmin()) {
            return;
        }

                if (farmVO == null) {
            throw new ResourceNotFoundException("Fazenda nÃ£o encontrada");
        }

        if (farmVO.getUserId() == null || !farmVO.getUserId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Acesso negado: VocÃª nÃ£o tem permissÃ£o para acessar esta fazenda");
        }
    }

    /**
     * Verifica se o usuÃ¡rio atual tem permissÃ£o para acessar uma cabra
     * ADMIN tem acesso a tudo, OPERATOR sÃ³ Ã s cabras da prÃ³pria fazenda
     * @param goat Cabra a ser verificada
     * @throws ResourceNotFoundException se nÃ£o tem permissÃ£o
     */
    public void verifyGoatOwnership(Goat goat) {
        User currentUser = getCurrentUser();
        
                if (isCurrentUserAdmin()) {
            return;
        }

                if (goat == null) {
            throw new ResourceNotFoundException("Cabra nÃ£o encontrada");
        }

        if (goat.getFarm() == null) {
            throw new ResourceNotFoundException("Cabra nÃ£o estÃ¡ associada a nenhuma fazenda");
        }

                if (!goat.getFarm().getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Acesso negado: VocÃª nÃ£o tem permissÃ£o para acessar cabras desta fazenda");
        }
    }

    /**
     * Verifica se o usuÃ¡rio atual tem permissÃ£o para acessar dados de um usuÃ¡rio especÃ­fico
     * ADMIN e OPERATOR tÃªm acesso a tudo, outros usuÃ¡rios sÃ³ aos prÃ³prios dados
     * @param userId ID do usuÃ¡rio a ser verificado
     * @throws ResourceNotFoundException se nÃ£o tem permissÃ£o
     */
    public void verifyUserAccess(Long userId) {
        User currentUser = getCurrentUser();
        
                if (isCurrentUserAdminOrOperator()) {
            return;
        }

                if (!currentUser.getId().equals(userId)) {
            throw new ResourceNotFoundException("Acesso negado: VocÃª nÃ£o tem permissÃ£o para acessar dados de outro usuÃ¡rio");
        }
    }

    /**
     * Verifica se o usuÃ¡rio atual pode criar recursos em uma fazenda especÃ­fica
     * @param farmId ID da fazenda
     * @param farm Entidade da fazenda (opcional, se jÃ¡ carregada)
     * @throws ResourceNotFoundException se nÃ£o tem permissÃ£o
     */
    public void verifyCanCreateInFarm(Long farmId, GoatFarm farm) {
        User currentUser = getCurrentUser();
        
                if (isCurrentUserAdminOrOperator()) {
            return;
        }

                if (farm != null) {
            verifyFarmOwnership(farm);
            return;
        }

                GoatFarm foundFarm = goatFarmRepository.findById(farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Fazenda nÃ£o encontrada com ID: " + farmId));
        verifyFarmOwnership(foundFarm);
    }
    
        
    /**
     * Verifica ownership de fazenda por ID
     * @param farmId ID da fazenda
     * @throws ResourceNotFoundException se nÃ£o tem permissÃ£o
     */
    public void verifyFarmOwnershipById(Long farmId) {
        if (isCurrentUserAdminOrOperator()) {
            return;
        }
        
        GoatFarm farm = goatFarmRepository.findById(farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Fazenda nÃ£o encontrada com ID: " + farmId));
        verifyFarmOwnership(farm);
    }
    
    /**
     * Verifica ownership de cabra por nÃºmero de registro
     * @param registrationNumber NÃºmero de registro da cabra
     * @throws ResourceNotFoundException se nÃ£o tem permissÃ£o
     */
    public void verifyOwnershipByGoatId(String registrationNumber) {
        if (isCurrentUserAdminOrOperator()) {
            return;
        }
        
        Goat goat = goatRepository.findById(registrationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Cabra nÃ£o encontrada com registro: " + registrationNumber));
        verifyGoatOwnership(goat);
    }
    
    /**
     * Verifica ownership de evento por ID
     * @param eventId ID do evento
     * @throws ResourceNotFoundException se nÃ£o tem permissÃ£o
     */
    public void verifyOwnershipByEventId(Long eventId) {
        if (isCurrentUserAdminOrOperator()) {
            return;
        }
        
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento nÃ£o encontrado com ID: " + eventId));
        
        if (event.getGoat() == null || event.getGoat().getFarm() == null) {
            throw new ResourceNotFoundException("Evento nÃ£o estÃ¡ associado a uma fazenda vÃ¡lida");
        }
        
        verifyFarmOwnership(event.getGoat().getFarm());
    }
    
    /**
     * Verifica se o usuÃ¡rio atual tem permissÃ£o para acessar um endereÃ§o
     * EndereÃ§os sÃ£o acessÃ­veis apenas se estÃ£o associados a fazendas do usuÃ¡rio
     * @param address EndereÃ§o a ser verificado
     * @throws ResourceNotFoundException se nÃ£o tem permissÃ£o
     */
    public void verifyAddressOwnership(Address address) {
        if (isCurrentUserAdminOrOperator()) {
            return;
        }
        
        if (address == null) {
            throw new ResourceNotFoundException("EndereÃ§o nÃ£o encontrado");
        }
        
                GoatFarm farm = goatFarmRepository.findAll().stream()
                .filter(f -> f.getAddress() != null && f.getAddress().getId().equals(address.getId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("EndereÃ§o nÃ£o estÃ¡ associado a nenhuma fazenda"));
        
        verifyFarmOwnership(farm);
    }
    
    /**
     * Verifica se o usuÃ¡rio atual tem permissÃ£o para acessar um telefone
     * Telefones sÃ£o acessÃ­veis apenas se estÃ£o associados a fazendas do usuÃ¡rio
     * @param phone Telefone a ser verificado
     * @throws ResourceNotFoundException se nÃ£o tem permissÃ£o
     */
    public void verifyPhoneOwnership(Phone phone) {
        if (isCurrentUserAdminOrOperator()) {
            return;
        }
        
        if (phone == null) {
            throw new ResourceNotFoundException("Telefone nÃ£o encontrado");
        }
        
        if (phone.getGoatFarm() == null) {
            throw new ResourceNotFoundException("Telefone nÃ£o estÃ¡ associado a nenhuma fazenda");
        }
        
        verifyFarmOwnership(phone.getGoatFarm());
    }
    
        
    /**
     * Verifica se o usuÃ¡rio atual Ã© proprietÃ¡rio de uma fazenda especÃ­fica
     * @param farmId ID da fazenda
     * @return true se for proprietÃ¡rio, false caso contrÃ¡rio
     */
    public boolean isOwnerOfFarm(Long farmId) {
        try {
            verifyFarmOwnershipById(farmId);
            return true;
        } catch (ResourceNotFoundException e) {
            return false;
        }
    }
    
    /**
     * ObtÃ©m o ID do usuÃ¡rio atual
     * @return ID do usuÃ¡rio autenticado
     */
    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
    
    /**
     * Verifica se o usuÃ¡rio atual tem uma role especÃ­fica
     * @param roleName Nome da role (ex: "ROLE_ADMIN")
     * @return true se tem a role, false caso contrÃ¡rio
     */
    public boolean hasRole(String roleName) {
        try {
            User currentUser = getCurrentUser();
            return currentUser.getRoles().stream()
                    .anyMatch(role -> role.getAuthority().equals(roleName));
        } catch (RuntimeException e) {
            return false;
        }
    }
}
