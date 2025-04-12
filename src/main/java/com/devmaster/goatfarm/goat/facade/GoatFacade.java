package com.devmaster.goatfarm.goat.facade;

import com.devmaster.goatfarm.farm.business.bo.GoatFarmRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.business.goatbusiness.GoatBusiness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoatFacade {

    @Autowired
    private GoatBusiness goatBusiness;

    // CREATE
    public GoatResponseVO createGoat(GoatRequestVO requestVO, GoatFarmRequestVO goatFarmRequestVO) {
        return goatBusiness.createGoat(requestVO, goatFarmRequestVO);
    }

    // READ
    public GoatResponseVO findGoatByRegistrationNumber(String registrationNumber) {
        return goatBusiness.findGoatByRegistrationNumber(registrationNumber);
    }

    public List<GoatResponseVO> findAllGoats() {
        return goatBusiness.findAllGoats();
    }

    // UPDATE
    public GoatResponseVO updateGoat(GoatRequestVO requestVO) {
        return goatBusiness.updateGoat(requestVO);
    }

    // DELETE
    public void deleteGoatByRegistrationNumber(String registrationNumber) {
        goatBusiness.deleteGoat(registrationNumber);
    }
}
