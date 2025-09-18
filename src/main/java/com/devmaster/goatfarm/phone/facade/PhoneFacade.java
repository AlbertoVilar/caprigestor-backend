package com.devmaster.goatfarm.phone.facade;

import com.devmaster.goatfarm.farm.business.bo.GoatFarmRequestVO;
import com.devmaster.goatfarm.phone.business.bo.PhoneRequestVO;
import com.devmaster.goatfarm.phone.business.bo.PhoneResponseVO;
import com.devmaster.goatfarm.phone.business.business.PhoneBusiness;
import com.devmaster.goatfarm.phone.facade.dto.PhoneFacadeResponseDTO;
import com.devmaster.goatfarm.phone.facade.mapper.PhoneFacadeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PhoneFacade {

    @Autowired
    private PhoneBusiness phoneBusiness;
    
    @Autowired
    private PhoneFacadeMapper facadeMapper;

    public PhoneFacadeResponseDTO createPhone(PhoneRequestVO requestVO, Long goatFarmId) {
        PhoneResponseVO responseVO = phoneBusiness.createPhone(requestVO, goatFarmId);
        return facadeMapper.toFacadeDTO(responseVO);
    }

    public PhoneFacadeResponseDTO updatePhone(Long id, PhoneRequestVO requestVO) {
        PhoneResponseVO responseVO = phoneBusiness.updatePhone(id, requestVO);
        return facadeMapper.toFacadeDTO(responseVO);
    }

    public PhoneFacadeResponseDTO findPhoneById(Long id) {
        PhoneResponseVO responseVO = phoneBusiness.findPhoneById(id);
        return facadeMapper.toFacadeDTO(responseVO);
    }

    public List<PhoneFacadeResponseDTO> findAllPhones() {
        List<PhoneResponseVO> responseVOs = phoneBusiness.findAllPhones();
        return facadeMapper.toFacadeDTOList(responseVOs);
    }

    public String deletePhone(Long id) {
        return phoneBusiness.deletePhone(id);
    }
}
