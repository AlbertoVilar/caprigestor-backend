package com.devmaster.goatfarm.authority.business;

import com.devmaster.goatfarm.authority.business.usersbusiness.UserBusiness;
import com.devmaster.goatfarm.application.ports.in.EventManagementUseCase;
import com.devmaster.goatfarm.farm.business.farmbusiness.GoatFarmBusiness;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminMaintenanceBusiness {

    private final EventManagementUseCase eventManagementUseCase;
    private final GoatFarmBusiness goatFarmBusiness;
    private final UserBusiness userBusiness;

    public AdminMaintenanceBusiness(EventManagementUseCase eventManagementUseCase, GoatFarmBusiness goatFarmBusiness, UserBusiness userBusiness) {
        this.eventManagementUseCase = eventManagementUseCase;
        this.goatFarmBusiness = goatFarmBusiness;
        this.userBusiness = userBusiness;
    }

    @Transactional
    public void cleanDatabaseAndSetupAdmin(Long adminIdToKeep) {
        eventManagementUseCase.deleteEventsFromOtherUsers(adminIdToKeep);
        goatFarmBusiness.deleteGoatFarmsFromOtherUsers(adminIdToKeep);
        userBusiness.deleteRolesFromOtherUsers(adminIdToKeep);
        userBusiness.deleteOtherUsers(adminIdToKeep);
    }
}


