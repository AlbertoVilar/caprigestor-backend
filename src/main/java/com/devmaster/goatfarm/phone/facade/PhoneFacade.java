package com.devmaster.goatfarm.phone.facade;

import com.devmaster.goatfarm.farm.business.bo.GoatFarmRequestVO;
import com.devmaster.goatfarm.phone.business.bo.PhoneRequestVO;
import com.devmaster.goatfarm.phone.business.bo.PhoneResponseVO;
import com.devmaster.goatfarm.phone.business.business.PhoneBusiness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PhoneFacade {

    @Autowired
    private PhoneBusiness phoneBusiness;

    public PhoneResponseVO createPhone(PhoneRequestVO requestVO, Long goatFarmId) {
        return phoneBusiness.createPhone(requestVO, goatFarmId);
    }

    public PhoneResponseVO updatePhone(Long id, PhoneRequestVO requestVO) {
        return phoneBusiness.updatePhone(id, requestVO);
    }

    public PhoneResponseVO findPhoneById(Long id) {
        return phoneBusiness.findPhoneById(id);
    }

    public List<PhoneResponseVO> findAllPhones() {
        return phoneBusiness.findAllPhones();
    }

    public String deletePhone(Long id) {
        return phoneBusiness.deletePhone(id);
    }
}
