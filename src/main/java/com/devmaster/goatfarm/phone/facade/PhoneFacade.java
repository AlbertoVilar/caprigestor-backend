package com.devmaster.goatfarm.phone.facade;

import com.devmaster.goatfarm.phone.api.dto.PhoneRequestDTO;
import com.devmaster.goatfarm.phone.api.dto.PhoneResponseDTO;
import com.devmaster.goatfarm.phone.business.business.PhoneBusiness;
import com.devmaster.goatfarm.phone.business.bo.PhoneResponseVO;
import com.devmaster.goatfarm.phone.mapper.PhoneMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PhoneFacade {

    @Autowired
    private PhoneBusiness phoneBusiness;

    @Autowired
    private PhoneMapper phoneMapper;

    public PhoneResponseDTO createPhone(PhoneRequestDTO requestDTO) {
        PhoneResponseVO responseVO = phoneBusiness.createPhone(phoneMapper.toRequestVO(requestDTO), requestDTO.getGoatFarmId());
        return phoneMapper.toResponseDTO(responseVO);
    }

    public PhoneResponseDTO updatePhone(Long id, PhoneRequestDTO requestDTO) {
        PhoneResponseVO responseVO = phoneBusiness.updatePhone(id, phoneMapper.toRequestVO(requestDTO));
        return phoneMapper.toResponseDTO(responseVO);
    }

    public PhoneResponseDTO findPhoneById(Long id) {
        PhoneResponseVO responseVO = phoneBusiness.findPhoneById(id);
        return phoneMapper.toResponseDTO(responseVO);
    }

    public List<PhoneResponseDTO> findAllPhones() {
        return phoneBusiness.findAllPhones().stream()
                .map(phoneMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public void deletePhone(Long id) {
        phoneBusiness.deletePhone(id);
    }
}
