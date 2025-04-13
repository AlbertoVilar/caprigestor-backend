package com.devmaster.goatfarm.goat.facade;

import com.devmaster.goatfarm.farm.business.bo.GoatFarmRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.business.goatbusiness.GoatBusiness;
import com.devmaster.goatfarm.owner.business.bo.OwnerRequestVO;
import com.devmaster.goatfarm.owner.business.bo.OwnerResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
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

    public List<GoatResponseVO> findAllGoats() {
        return goatBusiness.findAllGoats();
    }

    // UPDATE
    public GoatResponseVO updateGoat(GoatRequestVO goatRequestVO) {
        return goatBusiness.updateGoat(goatRequestVO);
    }

    // DELETE
    public void deleteGoatByRegistrationNumber(String registrationNumber) {
        goatBusiness.deleteGoat(registrationNumber);
    }
}
