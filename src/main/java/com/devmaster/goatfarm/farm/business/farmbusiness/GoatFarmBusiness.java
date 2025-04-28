package com.devmaster.goatfarm.farm.business.farmbusiness;

import com.devmaster.goatfarm.farm.business.bo.GoatFarmRequestVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmResponseVO;
import com.devmaster.goatfarm.farm.dao.GoatFarmDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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

    public Page<GoatFarmResponseVO> searchGoatFarmByName(String name, Pageable pageable) {

        return goatFarmDAO.searchGoatFarmByName(name, pageable);
    }

    public Page<GoatFarmResponseVO> findAllGoatFarm(Pageable pageable) {

        return goatFarmDAO.findAllGoatFarm(pageable);
    }

    public String deleteGoatFarm(Long id) {
      return goatFarmDAO.deleteGoatFarm(id);
    }
}
