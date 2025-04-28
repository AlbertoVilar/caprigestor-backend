package com.devmaster.goatfarm.goat.facade;

import com.devmaster.goatfarm.farm.business.bo.GoatFarmRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.business.goatbusiness.GoatBusiness;
import com.devmaster.goatfarm.owner.business.bo.OwnerRequestVO;
import com.devmaster.goatfarm.owner.business.bo.OwnerResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoatFacade {

    private final GoatBusiness goatBusiness;

    @Autowired
    public GoatFacade(GoatBusiness goatBusiness) {
        this.goatBusiness = goatBusiness;
    }

    // CREATE
    public GoatResponseVO createGoat(GoatRequestVO requestVO, Long ownerId, Long farmId) {
        return goatBusiness.createGoat(requestVO, ownerId, farmId);
    }
    // READ
    public GoatResponseVO findGoatByRegistrationNumber(String registrationNumber) {
        return goatBusiness.findGoatByRegistrationNumber(registrationNumber);
    }

    public Page<GoatResponseVO> findAllGoats(Pageable pageable) {
        return goatBusiness.findAllGoats(pageable);
    }

    public Page<GoatResponseVO> searchGoatByName(String name, Pageable pageable) {

        return goatBusiness.searchGoatByName(name, pageable);
    }

    // UPDATE
    public GoatResponseVO updateGoat(String numRegistration, GoatRequestVO goatRequestVO) {
        return goatBusiness.updateGoat(numRegistration, goatRequestVO);
    }

    // DELETE
    public void deleteGoatByRegistrationNumber(String registrationNumber) {
        goatBusiness.deleteGoat(registrationNumber);
    }
}
