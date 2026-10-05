package com.devmaster.goatfarm.authority.application.ports.in;

/** Farm-scoped authorization policy exposed to application and adapter layers. */
public interface FarmAuthorizationUseCase {
    void verifyFarmOwnership(Long farmId);
    void verifyFarmManagement(Long farmId);
    boolean isFarmOwner(Long farmId);
    boolean canAdministerFarm(Long farmId);
    boolean canManageFarm(Long farmId);
}
