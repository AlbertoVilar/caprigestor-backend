package com.devmaster.goatfarm.goat.business.goatbusiness;

import com.devmaster.goatfarm.farm.business.bo.GoatFarmRequestVO;
import com.devmaster.goatfarm.farm.model.entity.GoatFarm;
import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.dao.GoatDAO;
import com.devmaster.goatfarm.goat.model.entity.Goat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoatBusiness {

    @Autowired
    private GoatDAO goatDAO;

    // CREATE
    public GoatResponseVO createGoat(GoatRequestVO requestVO, GoatFarmRequestVO goatFarmRequestVO) {
        return goatDAO.createGoat(requestVO, goatFarmRequestVO);
    }

    // READ (BY ID)
    public GoatResponseVO findGoatByRegistrationNumber(String registrationNumber) {
        return goatDAO.findGoatById(registrationNumber);
    }

    // READ (ALL)
    public List<GoatResponseVO> findAllGoats() {
        return goatDAO.findAllGoats();
    }

    // UPDATE
    public GoatResponseVO updateGoat(GoatRequestVO requestVO) {
        return goatDAO.updateGoat(requestVO);
    }


    // DELETE
    public void deleteGoat(String registrationNumber) {
        goatDAO.deleteGoat(registrationNumber);
    }
}
