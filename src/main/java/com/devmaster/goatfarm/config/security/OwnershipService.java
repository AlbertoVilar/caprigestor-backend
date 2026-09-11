package com.devmaster.goatfarm.config.security;

import com.devmaster.goatfarm.config.exceptions.custom.UnauthorizedException;
import com.devmaster.goatfarm.authority.application.ports.out.FarmAccessQueryPort;
import org.springframework.security.access.AccessDeniedException;
import com.devmaster.goatfarm.authority.persistence.entity.User;
import com.devmaster.goatfarm.farm.application.ports.out.GoatFarmPersistencePort;
import com.devmaster.goatfarm.farm.application.ports.out.FarmOwnerQueryPort;
import com.devmaster.goatfarm.goat.application.routing.GoatReferenceResolver;
import com.devmaster.goatfarm.authority.application.ports.out.UserPersistencePort;
import com.devmaster.goatfarm.authority.business.bo.AuthenticatedPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OwnershipService {

    private static final Logger logger = LoggerFactory.getLogger(OwnershipService.class);

    private final GoatFarmPersistencePort goatFarmPort;
    private final UserPersistencePort userPort;
    private final GoatReferenceResolver goatReferenceResolver;
    private final FarmAccessQueryPort farmAccessQueryPort;
    private final FarmOwnerQueryPort farmOwnerQueryPort;

    @org.springframework.beans.factory.annotation.Autowired
    public OwnershipService(GoatFarmPersistencePort goatFarmPort, UserPersistencePort userPort,
                            GoatReferenceResolver goatReferenceResolver, FarmAccessQueryPort farmAccessQueryPort,
                            FarmOwnerQueryPort farmOwnerQueryPort) {
        this.goatFarmPort = goatFarmPort;
        this.userPort = userPort;
        this.goatReferenceResolver = goatReferenceResolver;
        this.farmAccessQueryPort = farmAccessQueryPort;
        this.farmOwnerQueryPort = farmOwnerQueryPort;
    }

    /** Compatibility constructor for isolated unit tests and legacy callers. */
    public OwnershipService(GoatFarmPersistencePort goatFarmPort, UserPersistencePort userPort,
                             GoatReferenceResolver goatReferenceResolver, FarmAccessQueryPort farmAccessQueryPort) {
        this(goatFarmPort, userPort, goatReferenceResolver, farmAccessQueryPort, null);
    }

    public void verifyFarmOwnership(Long farmId) {
        var current = getAuthenticatedPrincipal();
        boolean isAdmin = current.hasAuthority("ROLE_ADMIN");
        if (isAdmin) return;
        boolean isFarmOwner = current.hasAuthority("ROLE_FARM_OWNER");
        if (!isFarmOwner) {
            throw new AccessDeniedException("Usuário não possui o papel de proprietário de fazenda.");
        }
        var ownerId = ownerId(farmId)
                .orElseThrow(() -> new UnauthorizedException("Fazenda não encontrada: " + farmId));
        if (!ownerId.equals(current.id())) {
            throw new AccessDeniedException("Usuário não é proprietário desta fazenda.");
        }
    }

    /**
     * Verifies that the authenticated user can perform operational work for the
     * requested farm. Unlike ownership, this deliberately includes a formally
     * linked operator.
     */
    public void verifyFarmManagement(Long farmId) {
        if (!canManageFarm(farmId)) {
            throw new AccessDeniedException("Usuário não pode operar esta fazenda.");
        }
    }

    public void verifyGoatOwnership(Long farmId, String goatId) {
        // Primeiro, verifica se o usuário é dono da fazenda (admin tem bypass)
        verifyFarmOwnership(farmId);
        // Depois, garante que a cabra pertence à fazenda informada
        var goatReference = goatReferenceResolver.resolve(goatId, farmId);
        if (goatReference.isEmpty()) {
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
            var current = getAuthenticatedPrincipal();
            boolean isAdmin = current.hasAuthority("ROLE_ADMIN");
            if (isAdmin) return true;
            return ownerId(farmId).map(current.id()::equals).orElse(false);
        } catch (RuntimeException ex) {
            logger.debug("event=farm_ownership_check_failed farmId={} exception={}",
                    farmId, ex.getClass().getSimpleName());
            return false;
        }
    }

    /**
     * Returns whether the current principal has the administrative capability
     * represented by {@code @FarmOwnerOnly}: ADMIN globally, or the official
     * FARM_OWNER role for the requested farm.
     */
    public boolean canAdministerFarm(Long farmId) {
        try {
            var current = getAuthenticatedPrincipal();
            if (current.hasAuthority("ROLE_ADMIN")) return true;
            if (!current.hasAuthority("ROLE_FARM_OWNER")) return false;
            return ownerId(farmId).map(current.id()::equals).orElse(false);
        } catch (RuntimeException ex) {
            logger.debug("event=farm_administration_check_failed farmId={} exception={}",
                    farmId, ex.getClass().getSimpleName());
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
            var current = getAuthenticatedPrincipal();
            
            // 1. ADMIN tem acesso total
            boolean isAdmin = current.hasAuthority("ROLE_ADMIN");
            if (isAdmin) return true;

            // 2. OPERATOR tem acesso (validado por vínculo)
            boolean isOperator = current.hasAuthority("ROLE_OPERATOR");
            if (isOperator && farmAccessQueryPort.existsOperatorLink(farmId, current.id())) {
                return true;
            }

            // 3. FARM_OWNER deve ser dono da fazenda. A relação direta, sem o
            // papel oficial, não deve conceder capacidade operacional.
            boolean isFarmOwner = current.hasAuthority("ROLE_FARM_OWNER");
            if (!isFarmOwner) {
                logger.debug("event=farm_management_check_denied farmId={} reason=missing_farm_owner_role", farmId);
                return false;
            }
            var ownerId = ownerId(farmId);
            if (ownerId.isEmpty()) {
                logger.debug("event=farm_management_check_denied farmId={} reason=farm_not_found", farmId);
                return false;
            }
            boolean isOwner = ownerId.get().equals(current.id());
            logger.debug("event=farm_management_check farmId={} allowed={}", farmId, isOwner);
            return isOwner;
        } catch (RuntimeException ex) {
            logger.debug("event=farm_management_check_failed farmId={} exception={}",
                    farmId, ex.getClass().getSimpleName());
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

    private AuthenticatedPrincipal getAuthenticatedPrincipal() {
        User current = getAuthenticatedEntity();
        return new AuthenticatedPrincipal(
                current.getId(),
                current.getEmail(),
                current.getName(),
                current.getRoles().stream().map(role -> role.getAuthority()).collect(java.util.stream.Collectors.toSet())
        );
    }

    private java.util.Optional<Long> ownerId(Long farmId) {
        if (farmOwnerQueryPort != null) {
            return farmOwnerQueryPort.findOwnerId(farmId);
        }
        return goatFarmPort.findById(farmId)
                .map(farm -> farm.getUser() == null ? null : farm.getUser().getId());
    }
}
