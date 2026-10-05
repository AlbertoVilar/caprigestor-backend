package com.devmaster.goatfarm.config.security;

import com.devmaster.goatfarm.authority.application.ports.in.CurrentPrincipalQueryUseCase;
import com.devmaster.goatfarm.authority.application.ports.in.FarmAuthorizationUseCase;
import com.devmaster.goatfarm.authority.application.ports.out.FarmAccessQueryPort;
import com.devmaster.goatfarm.authority.business.bo.AuthenticatedPrincipal;
import com.devmaster.goatfarm.config.exceptions.custom.UnauthorizedException;
import com.devmaster.goatfarm.farm.application.ports.out.FarmOwnerQueryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

/** Central farm policy kept as a bean for SpEL compatibility. */
@Service("ownershipService")
public class OwnershipService implements FarmAuthorizationUseCase {
    private static final Logger logger = LoggerFactory.getLogger(OwnershipService.class);
    private final CurrentPrincipalQueryUseCase currentPrincipalQuery;
    private final FarmAccessQueryPort farmAccessQueryPort;
    private final FarmOwnerQueryPort farmOwnerQueryPort;

    public OwnershipService(CurrentPrincipalQueryUseCase currentPrincipalQuery,
                             FarmAccessQueryPort farmAccessQueryPort,
                             FarmOwnerQueryPort farmOwnerQueryPort) {
        this.currentPrincipalQuery = currentPrincipalQuery;
        this.farmAccessQueryPort = farmAccessQueryPort;
        this.farmOwnerQueryPort = farmOwnerQueryPort;
    }

    @Override
    public void verifyFarmOwnership(Long farmId) {
        AuthenticatedPrincipal current = currentPrincipalQuery.requireCurrent();
        if (current.hasAuthority("ROLE_ADMIN")) return;
        if (!current.hasAuthority("ROLE_FARM_OWNER")) {
            throw new AccessDeniedException("Usuário não possui o papel de proprietário de fazenda.");
        }
        Long ownerId = farmOwnerQueryPort.findOwnerId(farmId)
                .orElseThrow(() -> new UnauthorizedException("Fazenda não encontrada: " + farmId));
        if (!ownerId.equals(current.id())) {
            throw new AccessDeniedException("Usuário não é proprietário desta fazenda.");
        }
    }

    @Override
    public void verifyFarmManagement(Long farmId) {
        if (!canManageFarm(farmId)) throw new AccessDeniedException("Usuário não pode operar esta fazenda.");
    }

    @Override
    public boolean isFarmOwner(Long farmId) {
        try {
            AuthenticatedPrincipal current = currentPrincipalQuery.requireCurrent();
            return current.hasAuthority("ROLE_ADMIN")
                    || farmOwnerQueryPort.findOwnerId(farmId).map(current.id()::equals).orElse(false);
        } catch (RuntimeException ex) {
            logger.debug("event=farm_ownership_check_failed farmId={} exception={}", farmId, ex.getClass().getSimpleName());
            return false;
        }
    }

    @Override
    public boolean canAdministerFarm(Long farmId) {
        try {
            AuthenticatedPrincipal current = currentPrincipalQuery.requireCurrent();
            if (current.hasAuthority("ROLE_ADMIN")) return true;
            return current.hasAuthority("ROLE_FARM_OWNER")
                    && farmOwnerQueryPort.findOwnerId(farmId).map(current.id()::equals).orElse(false);
        } catch (RuntimeException ex) {
            logger.debug("event=farm_administration_check_failed farmId={} exception={}", farmId, ex.getClass().getSimpleName());
            return false;
        }
    }

    @Override
    public boolean canManageFarm(Long farmId) {
        try {
            AuthenticatedPrincipal current = currentPrincipalQuery.requireCurrent();
            if (current.hasAuthority("ROLE_ADMIN")) return true;
            if (current.hasAuthority("ROLE_OPERATOR") && farmAccessQueryPort.existsOperatorLink(farmId, current.id())) return true;
            return current.hasAuthority("ROLE_FARM_OWNER")
                    && farmOwnerQueryPort.findOwnerId(farmId).map(current.id()::equals).orElse(false);
        } catch (RuntimeException ex) {
            logger.debug("event=farm_management_check_failed farmId={} exception={}", farmId, ex.getClass().getSimpleName());
            return false;
        }
    }
}
