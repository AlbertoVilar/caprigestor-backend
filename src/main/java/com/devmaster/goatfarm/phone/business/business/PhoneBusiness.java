package com.devmaster.goatfarm.phone.business.business;

import com.devmaster.goatfarm.farm.business.bo.GoatFarmRequestVO;
import com.devmaster.goatfarm.phone.business.bo.PhoneRequestVO;
import com.devmaster.goatfarm.phone.business.bo.PhoneResponseVO;
import com.devmaster.goatfarm.phone.dao.PhoneDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PhoneBusiness {

    @Autowired
    private PhoneDAO phoneDAO;

    public PhoneResponseVO createPhone(PhoneRequestVO requestVO, Long goatFarmId) {
        return phoneDAO.createPhone(requestVO, goatFarmId);
    }

    public PhoneResponseVO updatePhone(Long id, PhoneRequestVO requestVO) {
        return phoneDAO.updatePhone(id, requestVO);
    }

    public PhoneResponseVO findPhoneById(Long id) {
        return phoneDAO.findPhoneById(id);
    }

    public List<PhoneResponseVO> findAllPhones() {
        return phoneDAO.findAllPhones();
    }

    public String deletePhone(Long id) {
        return phoneDAO.deletePhone(id);
    }
}

