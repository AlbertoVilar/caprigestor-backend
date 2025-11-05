package com.devmaster.goatfarm.authority.facade;

import com.devmaster.goatfarm.authority.business.AdminBusiness;
import com.devmaster.goatfarm.authority.business.AdminMaintenanceBusiness;
import org.springframework.stereotype.Service;

@Service
public class AdminFacade {

    private final AdminBusiness adminBusiness;
    private final AdminMaintenanceBusiness adminMaintenanceBusiness;

    public AdminFacade(AdminBusiness adminBusiness, AdminMaintenanceBusiness adminMaintenanceBusiness) {
        this.adminBusiness = adminBusiness;
        this.adminMaintenanceBusiness = adminMaintenanceBusiness;
    }

    public void cleanDatabaseAndSetupAdmin(Long adminId) {
        adminMaintenanceBusiness.cleanDatabaseAndSetupAdmin(adminId);
    }

    public void cleanDatabaseAndSetupAdminAuto() {
        adminBusiness.cleanDatabaseAndSetupAdmin();
    }
}
