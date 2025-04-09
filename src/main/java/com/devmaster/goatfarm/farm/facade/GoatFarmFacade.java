package com.devmaster.goatfarm.farm.facade;

import com.devmaster.goatfarm.farm.business.bo.GoatFarmRequestVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmResponseVO;
import com.devmaster.goatfarm.farm.business.farmbusiness.GoatFarmBusiness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoatFarmFacade {

    @Autowired
    private GoatFarmBusiness farmBusiness;

    public GoatFarmResponseVO createGoatFarm(GoatFarmRequestVO requestVO) {

        return farmBusiness.createGoatFarm(requestVO);
    }

    public GoatFarmResponseVO updateGoatFarm(Long id, GoatFarmRequestVO requestVO) {

        return farmBusiness.updateGoatFarm(id, requestVO);
    }

    public GoatFarmResponseVO findGoatFarmById(Long id) {

        return farmBusiness.findGoatFarmById(id);
    }

    public List<GoatFarmResponseVO> findALLGoatFarm() {

        return farmBusiness.FindALLGoatFarm();
    }

    public String deleteGoatFarm(Long id) {
        return farmBusiness.deleteGoatFarm(id);
    }
}
