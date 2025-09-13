package com.devmaster.goatfarm.config.security;

import com.devmaster.goatfarm.authority.model.entity.User;
import com.devmaster.goatfarm.authority.model.repository.UserRepository;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
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
 * Service para verificação de ownership (propriedade) de recursos.
 * Centraliza a lógica de verificação se um usuário tem permissão para acessar/modificar recursos.
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
     * Obtém o usuário atualmente autenticado
     * @return User autenticado
     * @throws RuntimeException se não há usuário autenticado
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            String email = authentication.getName();
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuário autenticado não encontrado: " + email));
        }
        throw new RuntimeException("Usuário não autenticado");
    }

    /**
     * Verifica se o usuário atual é ADMIN
     * @return true se for ADMIN, false caso contrário
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
     * Verifica se o usuário atual é ADMIN ou OPERATOR
     * @return true se for ADMIN ou OPERATOR, false caso contrário
     */
    public boolean isCurrentUserAdminOrOperator() {
        try {
            User currentUser = getCurrentUser();
            return currentUser.getRoles().stream()
                    .anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN") || role.getAuthority().equals("ROLE_OPERATOR"));
        } catch (RuntimeException e) {
            return false;
        }
    }

    /**
     * Verifica se o usuário atual tem permissão para acessar uma fazenda
     * ADMIN e OPERATOR têm acesso a tudo, FARM_OWNER só à própria fazenda
     * @param farm Fazenda a ser verificada
     * @throws ResourceNotFoundException se não tem permissão
     */
    public void verifyFarmOwnership(GoatFarm farm) {
        User currentUser = getCurrentUser();
        logger.debug("🔍 OWNERSHIP: Verificando acesso para usuário: {}", currentUser.getEmail());
        logger.debug("🔍 OWNERSHIP: Roles do usuário: {}", currentUser.getRoles().stream().map(r -> r.getAuthority()).toList());
        
        // ADMIN e OPERATOR têm acesso a tudo
        if (isCurrentUserAdminOrOperator()) {
            logger.debug("✅ OWNERSHIP: Usuário é ADMIN ou OPERATOR - acesso liberado");
            return;
        }

        // Verificar se o usuário é proprietário da fazenda
        if (farm == null) {
            logger.warn("❌ OWNERSHIP: Fazenda é null");
            throw new ResourceNotFoundException("Fazenda não encontrada");
        }

        logger.debug("🔍 OWNERSHIP: Fazenda ID: {}, Proprietário ID: {}", farm.getId(), (farm.getUser() != null ? farm.getUser().getId() : "null"));
        logger.debug("🔍 OWNERSHIP: Usuário atual ID: {}", currentUser.getId());
        
        if (farm.getUser() == null || !farm.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Acesso negado: Você não tem permissão para acessar esta fazenda");
        }
    }

    /**
     * Verifica se o usuário atual tem permissão para acessar uma fazenda (versão para VO)
     * ADMIN e OPERATOR têm acesso a tudo, FARM_OWNER só à própria fazenda
     * @param farmVO Fazenda VO a ser verificada
     * @throws ResourceNotFoundException se não tem permissão
     */
    public void verifyFarmOwnership(com.devmaster.goatfarm.farm.business.bo.GoatFarmFullResponseVO farmVO) {
        User currentUser = getCurrentUser();
        
        // ADMIN e OPERATOR têm acesso a tudo
        if (isCurrentUserAdminOrOperator()) {
            return;
        }

        // Verificar se o usuário é proprietário da fazenda
        if (farmVO == null) {
            throw new ResourceNotFoundException("Fazenda não encontrada");
        }

        if (farmVO.getUserId() == null || !farmVO.getUserId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Acesso negado: Você não tem permissão para acessar esta fazenda");
        }
    }

    /**
     * Verifica se o usuário atual tem permissão para acessar uma cabra
     * ADMIN e OPERATOR têm acesso a tudo, FARM_OWNER só às cabras da própria fazenda
     * @param goat Cabra a ser verificada
     * @throws ResourceNotFoundException se não tem permissão
     */
    public void verifyGoatOwnership(Goat goat) {
        User currentUser = getCurrentUser();
        
        // ADMIN e OPERATOR têm acesso a tudo
        if (isCurrentUserAdminOrOperator()) {
            return;
        }

        // Verificar se a cabra está associada a uma fazenda
        if (goat == null) {
            throw new ResourceNotFoundException("Cabra não encontrada");
        }

        if (goat.getFarm() == null) {
            throw new ResourceNotFoundException("Cabra não está associada a nenhuma fazenda");
        }

        // Verificar se o usuário é proprietário da fazenda da cabra
        if (!goat.getFarm().getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Acesso negado: Você não tem permissão para acessar cabras desta fazenda");
        }
    }

    /**
     * Verifica se o usuário atual tem permissão para acessar dados de um usuário específico
     * ADMIN e OPERATOR têm acesso a tudo, outros usuários só aos próprios dados
     * @param userId ID do usuário a ser verificado
     * @throws ResourceNotFoundException se não tem permissão
     */
    public void verifyUserAccess(Long userId) {
        User currentUser = getCurrentUser();
        
        // ADMIN e OPERATOR têm acesso a tudo
        if (isCurrentUserAdminOrOperator()) {
            return;
        }

        // Usuário só pode acessar os próprios dados
        if (!currentUser.getId().equals(userId)) {
            throw new ResourceNotFoundException("Acesso negado: Você não tem permissão para acessar dados de outro usuário");
        }
    }

    /**
     * Verifica se o usuário atual pode criar recursos em uma fazenda específica
     * @param farmId ID da fazenda
     * @param farm Entidade da fazenda (opcional, se já carregada)
     * @throws ResourceNotFoundException se não tem permissão
     */
    public void verifyCanCreateInFarm(Long farmId, GoatFarm farm) {
        User currentUser = getCurrentUser();
        
        // ADMIN e OPERATOR podem criar em qualquer fazenda
        if (isCurrentUserAdminOrOperator()) {
            return;
        }

        // Se a fazenda foi fornecida, usar ela
        if (farm != null) {
            verifyFarmOwnership(farm);
            return;
        }

        // Buscar fazenda pelo ID e verificar ownership
        GoatFarm foundFarm = goatFarmRepository.findById(farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Fazenda não encontrada com ID: " + farmId));
        verifyFarmOwnership(foundFarm);
    }
    
    // ========== MÉTODOS DE VERIFICAÇÃO POR ID ==========
    
    /**
     * Verifica ownership de fazenda por ID
     * @param farmId ID da fazenda
     * @throws ResourceNotFoundException se não tem permissão
     */
    public void verifyFarmOwnershipById(Long farmId) {
        if (isCurrentUserAdminOrOperator()) {
            return;
        }
        
        GoatFarm farm = goatFarmRepository.findById(farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Fazenda não encontrada com ID: " + farmId));
        verifyFarmOwnership(farm);
    }
    
    /**
     * Verifica ownership de cabra por número de registro
     * @param registrationNumber Número de registro da cabra
     * @throws ResourceNotFoundException se não tem permissão
     */
    public void verifyOwnershipByGoatId(String registrationNumber) {
        if (isCurrentUserAdminOrOperator()) {
            return;
        }
        
        Goat goat = goatRepository.findById(registrationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Cabra não encontrada com registro: " + registrationNumber));
        verifyGoatOwnership(goat);
    }
    
    /**
     * Verifica ownership de evento por ID
     * @param eventId ID do evento
     * @throws ResourceNotFoundException se não tem permissão
     */
    public void verifyOwnershipByEventId(Long eventId) {
        if (isCurrentUserAdminOrOperator()) {
            return;
        }
        
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado com ID: " + eventId));
        
        if (event.getGoat() == null || event.getGoat().getFarm() == null) {
            throw new ResourceNotFoundException("Evento não está associado a uma fazenda válida");
        }
        
        verifyFarmOwnership(event.getGoat().getFarm());
    }
    
    /**
     * Verifica se o usuário atual tem permissão para acessar um endereço
     * Endereços são acessíveis apenas se estão associados a fazendas do usuário
     * @param address Endereço a ser verificado
     * @throws ResourceNotFoundException se não tem permissão
     */
    public void verifyAddressOwnership(Address address) {
        if (isCurrentUserAdminOrOperator()) {
            return;
        }
        
        if (address == null) {
            throw new ResourceNotFoundException("Endereço não encontrado");
        }
        
        // Buscar fazenda que usa este endereço
        GoatFarm farm = goatFarmRepository.findAll().stream()
                .filter(f -> f.getAddress() != null && f.getAddress().getId().equals(address.getId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Endereço não está associado a nenhuma fazenda"));
        
        verifyFarmOwnership(farm);
    }
    
    /**
     * Verifica se o usuário atual tem permissão para acessar um telefone
     * Telefones são acessíveis apenas se estão associados a fazendas do usuário
     * @param phone Telefone a ser verificado
     * @throws ResourceNotFoundException se não tem permissão
     */
    public void verifyPhoneOwnership(Phone phone) {
        if (isCurrentUserAdminOrOperator()) {
            return;
        }
        
        if (phone == null) {
            throw new ResourceNotFoundException("Telefone não encontrado");
        }
        
        if (phone.getGoatFarm() == null) {
            throw new ResourceNotFoundException("Telefone não está associado a nenhuma fazenda");
        }
        
        verifyFarmOwnership(phone.getGoatFarm());
    }
    
    // ========== MÉTODOS AUXILIARES ==========
    
    /**
     * Verifica se o usuário atual é proprietário de uma fazenda específica
     * @param farmId ID da fazenda
     * @return true se for proprietário, false caso contrário
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
     * Obtém o ID do usuário atual
     * @return ID do usuário autenticado
     */
    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
    
    /**
     * Verifica se o usuário atual tem uma role específica
     * @param roleName Nome da role (ex: "ROLE_ADMIN")
     * @return true se tem a role, false caso contrário
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