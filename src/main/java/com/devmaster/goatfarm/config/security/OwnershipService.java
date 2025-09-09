package com.devmaster.goatfarm.config.security;

import com.devmaster.goatfarm.authority.model.entity.User;
import com.devmaster.goatfarm.authority.model.repository.UserRepository;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.farm.model.entity.GoatFarm;
import com.devmaster.goatfarm.goat.model.entity.Goat;
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

    @Autowired
    private UserRepository userRepository;

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
     * Verifica se o usuário atual tem permissão para acessar uma fazenda
     * ADMIN tem acesso a tudo, FARM_OWNER só à própria fazenda
     * @param farm Fazenda a ser verificada
     * @throws ResourceNotFoundException se não tem permissão
     */
    public void verifyFarmOwnership(GoatFarm farm) {
        User currentUser = getCurrentUser();
        
        // ADMIN tem acesso a tudo
        if (isCurrentUserAdmin()) {
            return;
        }

        // Verificar se o usuário é proprietário da fazenda
        if (farm == null) {
            throw new ResourceNotFoundException("Fazenda não encontrada");
        }

        if (farm.getUser() == null || !farm.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Acesso negado: Você não tem permissão para acessar esta fazenda");
        }
    }

    /**
     * Verifica se o usuário atual tem permissão para acessar uma fazenda (versão para VO)
     * ADMIN tem acesso a tudo, FARM_OWNER só à própria fazenda
     * @param farmVO Fazenda VO a ser verificada
     * @throws ResourceNotFoundException se não tem permissão
     */
    public void verifyFarmOwnership(com.devmaster.goatfarm.farm.business.bo.GoatFarmFullResponseVO farmVO) {
        User currentUser = getCurrentUser();
        
        // ADMIN tem acesso a tudo
        if (isCurrentUserAdmin()) {
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
     * ADMIN tem acesso a tudo, FARM_OWNER só às cabras da própria fazenda
     * @param goat Cabra a ser verificada
     * @throws ResourceNotFoundException se não tem permissão
     */
    public void verifyGoatOwnership(Goat goat) {
        User currentUser = getCurrentUser();
        
        // ADMIN tem acesso a tudo
        if (isCurrentUserAdmin()) {
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
     * ADMIN tem acesso a tudo, outros usuários só aos próprios dados
     * @param userId ID do usuário a ser verificado
     * @throws ResourceNotFoundException se não tem permissão
     */
    public void verifyUserAccess(Long userId) {
        User currentUser = getCurrentUser();
        
        // ADMIN tem acesso a tudo
        if (isCurrentUserAdmin()) {
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
        
        // ADMIN pode criar em qualquer fazenda
        if (isCurrentUserAdmin()) {
            return;
        }

        // Se a fazenda foi fornecida, usar ela
        if (farm != null) {
            verifyFarmOwnership(farm);
            return;
        }

        // Caso contrário, verificar pelo ID (seria necessário injetar repository)
        throw new RuntimeException("Verificação de ownership por ID não implementada. Forneça a entidade da fazenda.");
    }
}