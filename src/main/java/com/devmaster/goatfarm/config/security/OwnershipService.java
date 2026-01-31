package com.devmaster.goatfarm.config.security;

import com.devmaster.goatfarm.config.exceptions.custom.UnauthorizedException;
import com.devmaster.goatfarm.authority.persistence.repository.FarmOperatorRepository;
import org.springframework.security.access.AccessDeniedException;
import com.devmaster.goatfarm.authority.persistence.entity.User;
import com.devmaster.goatfarm.farm.application.ports.out.GoatFarmPersistencePort;
import com.devmaster.goatfarm.goat.application.ports.out.GoatPersistencePort;
import com.devmaster.goatfarm.authority.application.ports.out.UserPersistencePort;
import org.springframework.stereotype.Service;

@Service
public class OwnershipService {

    private final GoatFarmPersistencePort goatFarmPort;
    private final UserPersistencePort userPort;
    private final GoatPersistencePort goatPort;
    private final FarmOperatorRepository farmOperatorRepository;

    public OwnershipService(GoatFarmPersistencePort goatFarmPort, UserPersistencePort userPort, GoatPersistencePort goatPort, FarmOperatorRepository farmOperatorRepository) {
        this.goatFarmPort = goatFarmPort;
        this.userPort = userPort;
        this.goatPort = goatPort;
        this.farmOperatorRepository = farmOperatorRepository;
    }

    public void verifyFarmOwnership(Long farmId) {
        var current = getAuthenticatedEntity();
        boolean isAdmin = current.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equals(r.getAuthority()));
        if (isAdmin) return;
        var farm = goatFarmPort.findById(farmId)
                .orElseThrow(() -> new UnauthorizedException("Fazenda não encontrada: " + farmId));
        if (farm.getUser() == null || !farm.getUser().getId().equals(current.getId())) {
            throw new AccessDeniedException("Usuário não é proprietário desta fazenda.");
        }
    }

    public void verifyGoatOwnership(Long farmId, String goatId) {
        // Primeiro, verifica se o usuário é dono da fazenda (admin tem bypass)
        verifyFarmOwnership(farmId);
        // Depois, garante que a cabra pertence à fazenda informada
        var goatOpt = goatPort.findByIdAndFarmId(goatId, farmId);
        if (goatOpt.isEmpty()) {
            throw new AccessDeniedException("Cabra não pertence à fazenda informada.");
        }
    }

    public User getCurrentUser() {
        return getAuthenticatedEntity();
    }

    public boolean isCurrentUserAdmin() {
        var current = getAuthenticatedEntity();
        return current.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equals(r.getAuthority()));
    }

    public boolean isFarmOwner(Long farmId) {
        try {
            var current = getAuthenticatedEntity();
            boolean isAdmin = current.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equals(r.getAuthority()));
            if (isAdmin) return true;
            var farmOpt = goatFarmPort.findById(farmId);
            if (farmOpt.isEmpty() || farmOpt.get().getUser() == null) {
                return false;
            }
            return farmOpt.get().getUser().getId().equals(current.getId());
        } catch (RuntimeException ex) {
            return false;
        }
    }

    /**
     * Verifica se o usuário pode gerenciar a fazenda.
     * Retorna true se:
     * - OWNER (dono da fazenda)
     * - ADMIN (administrador do sistema)
     * - OPERATOR (operador com permissão)
     */
    public boolean canManageFarm(Long farmId) {
        try {
            var current = getAuthenticatedEntity();
            
            // 1. ADMIN tem acesso total
            boolean isAdmin = current.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equals(r.getAuthority()));
            if (isAdmin) return true;

            // 2. OPERATOR tem acesso (validado por vínculo)
            boolean isOperator = current.getRoles().stream().anyMatch(r -> "ROLE_OPERATOR".equals(r.getAuthority()));
            if (isOperator && farmOperatorRepository.existsByFarmIdAndUserId(farmId, current.getId())) {
                return true;
            }

            // 3. OWNER deve ser dono da fazenda
            var farmOpt = goatFarmPort.findById(farmId);
            if (farmOpt.isEmpty() || farmOpt.get().getUser() == null) {
                return false;
            }
            return farmOpt.get().getUser().getId().equals(current.getId());
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private User getAuthenticatedEntity() {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            String email = authentication.getName();
            return userPort.findByEmail(email)
                    .orElseThrow(() -> new UnauthorizedException("Usuário autenticado não encontrado: " + email));
        }
        throw new UnauthorizedException("Usuário não autenticado");
    }
}

