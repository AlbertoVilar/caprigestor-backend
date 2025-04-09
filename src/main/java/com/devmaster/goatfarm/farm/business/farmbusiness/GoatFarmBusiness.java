package com.devmaster.goatfarm.farm.business.farmbusiness;

import com.devmaster.goatfarm.farm.business.bo.GoatFarmRequestVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmResponseVO;
import com.devmaster.goatfarm.farm.dao.GoatFarmDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoatFarmBusiness {

    @Autowired
    private GoatFarmDAO goatFarmDAO;

    public GoatFarmResponseVO createGoatFarm(GoatFarmRequestVO requestVO) {

        return goatFarmDAO.createGoatFarm(requestVO);
    }

    public GoatFarmResponseVO updateGoatFarm(Long id, GoatFarmRequestVO requestVO) {

        return goatFarmDAO.updateGoatFarm(id, requestVO);
    }

    public GoatFarmResponseVO findGoatFarmById(Long id) {

           return goatFarmDAO.findGoatFarmById(id);
    }

    public List<GoatFarmResponseVO> FindALLGoatFarm() {

        return goatFarmDAO.findAllGoatFarm();
    }

    public String deleteGoatFarm(Long id) {
      return goatFarmDAO.deleteGoatFarm(id);
    }
}
