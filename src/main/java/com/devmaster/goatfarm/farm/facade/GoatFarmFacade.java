package com.devmaster.goatfarm.farm.facade;

import com.devmaster.goatfarm.address.business.bo.AddressRequestVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmFullResponseVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmRequestVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmResponseVO;
import com.devmaster.goatfarm.farm.business.farmbusiness.GoatFarmBusiness;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.owner.business.bo.OwnerRequestVO;
import com.devmaster.goatfarm.phone.business.bo.PhoneRequestVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoatFarmFacade {

    @Autowired
    private GoatFarmBusiness farmBusiness;

    public GoatFarmResponseVO createGoatFarm(GoatFarmRequestVO requestVO) {

        return farmBusiness.createGoatFarm(requestVO);
    }

    public GoatFarmFullResponseVO updateGoatFarm(Long id,
                                                 GoatFarmRequestVO requestVO,
                                                 OwnerRequestVO ownerVO,
                                                 AddressRequestVO addressVO,
                                                 List<PhoneRequestVO> phoneVOs) {
        return farmBusiness.updateGoatFarm(id, requestVO, ownerVO, addressVO, phoneVOs);
    }



    public GoatFarmFullResponseVO findGoatFarmById(Long id) {

        return farmBusiness.findGoatFarmById(id);
    }

    public Page<GoatFarmFullResponseVO> searchGoatFarmByName(String name, Pageable pageable) {

        return farmBusiness.searchGoatFarmByName(name, pageable);
    }

    public Page<GoatFarmFullResponseVO> findAllGoatFarm(Pageable pageable) {

        return farmBusiness.findAllGoatFarm(pageable);
    }

    public String deleteGoatFarm(Long id) {
        return farmBusiness.deleteGoatFarm(id);
    }

}
