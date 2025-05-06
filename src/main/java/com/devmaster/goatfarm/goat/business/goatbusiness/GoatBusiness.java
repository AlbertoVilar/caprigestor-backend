package com.devmaster.goatfarm.goat.business.goatbusiness;

import com.devmaster.goatfarm.farm.business.bo.GoatFarmRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.dao.GoatDAO;
import com.devmaster.goatfarm.owner.business.bo.OwnerRequestVO;
import com.devmaster.goatfarm.owner.business.bo.OwnerResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoatBusiness {

    @Autowired
    private GoatDAO goatDAO;

    // CREATE
    public GoatResponseVO createGoat(GoatRequestVO requestVO, Long ownerId, Long farmId) {
        return goatDAO.createGoat(requestVO, ownerId, farmId);
    }

    // READ (BY ID)
    public GoatResponseVO findGoatByRegistrationNumber(String registrationNumber) {
        return goatDAO.findGoatById(registrationNumber);
    }

    // READ (ALL)
    public Page<GoatResponseVO> findAllGoats(Pageable pageable) {
        return goatDAO.findAllGoats(pageable);
    }

    public Page<GoatResponseVO> searchGoatByName(String name, Pageable pageable) {
        return goatDAO.searchGoatByName(name, pageable);
    }

    // UPDATE
    public GoatResponseVO updateGoat(String numRegistration, GoatRequestVO requestVO) {
        return goatDAO.updateGoat(numRegistration, requestVO);
    }


    // DELETE
    public void deleteGoat(String registrationNumber) {
        goatDAO.deleteGoat(registrationNumber);
    }
}
