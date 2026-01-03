package com.devmaster.goatfarm.authority.business;

import com.devmaster.goatfarm.address.business.AddressBusiness;
import com.devmaster.goatfarm.authority.business.usersbusiness.UserBusiness;
import com.devmaster.goatfarm.application.ports.in.EventManagementUseCase;
import com.devmaster.goatfarm.farm.business.farmbusiness.GoatFarmBusiness;
import com.devmaster.goatfarm.goat.business.goatbusiness.GoatBusiness;
import com.devmaster.goatfarm.phone.business.business.PhoneBusiness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminMaintenanceBusiness {

    private final EventManagementUseCase eventManagementUseCase;
    private final GoatBusiness goatBusiness;
    private final PhoneBusiness phoneBusiness;
    private final GoatFarmBusiness goatFarmBusiness;
    private final AddressBusiness addressBusiness;
    private final UserBusiness userBusiness;

    public AdminMaintenanceBusiness(EventManagementUseCase eventManagementUseCase, GoatBusiness goatBusiness, PhoneBusiness phoneBusiness, GoatFarmBusiness goatFarmBusiness, AddressBusiness addressBusiness, UserBusiness userBusiness) {
        this.eventManagementUseCase = eventManagementUseCase;
        this.goatBusiness = goatBusiness;
        this.phoneBusiness = phoneBusiness;
        this.goatFarmBusiness = goatFarmBusiness;
        this.addressBusiness = addressBusiness;
        this.userBusiness = userBusiness;
    }

    @Transactional
    public void cleanDatabaseAndSetupAdmin(Long adminIdToKeep) {
        eventManagementUseCase.deleteEventsFromOtherUsers(adminIdToKeep);
        // goatBusiness.deleteGoatsFromOtherUsers(adminIdToKeep); // TODO: Implementar método
        goatFarmBusiness.deleteGoatFarmsFromOtherUsers(adminIdToKeep);
        // phoneBusiness.deletePhonesFromOtherUsers(adminIdToKeep); // TODO: Implementar método
        // addressBusiness.deleteAddressesFromOtherUsers(adminIdToKeep); // TODO: Implementar método
        userBusiness.deleteRolesFromOtherUsers(adminIdToKeep);
        userBusiness.deleteOtherUsers(adminIdToKeep);
    }
}


