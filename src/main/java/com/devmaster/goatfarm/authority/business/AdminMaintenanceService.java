package com.devmaster.goatfarm.authority.business;

import com.devmaster.goatfarm.application.ports.in.AdminMaintenanceUseCase;
import org.springframework.stereotype.Service;

@Service
public class AdminMaintenanceService implements AdminMaintenanceUseCase {

    private final AdminMaintenanceBusiness adminMaintenanceBusiness;
    private final AdminBusiness adminBusiness;

    public AdminMaintenanceService(AdminMaintenanceBusiness adminMaintenanceBusiness,
                                   AdminBusiness adminBusiness) {
        this.adminMaintenanceBusiness = adminMaintenanceBusiness;
        this.adminBusiness = adminBusiness;
    }

    @Override
    public void cleanDatabaseAndSetupAdmin(Long adminId) {
        adminMaintenanceBusiness.cleanDatabaseAndSetupAdmin(adminId);
    }

    @Override
    public void cleanDatabaseAndSetupAdminAuto() {
        adminBusiness.cleanDatabaseAndSetupAdmin();
    }
}