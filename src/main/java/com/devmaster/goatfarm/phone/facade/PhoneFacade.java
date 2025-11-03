package com.devmaster.goatfarm.phone.facade;

import com.devmaster.goatfarm.phone.api.dto.PhoneRequestDTO;
import com.devmaster.goatfarm.phone.api.dto.PhoneResponseDTO;
import com.devmaster.goatfarm.phone.business.business.PhoneBusiness;
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

    public PhoneResponseDTO createPhone(Long farmId, PhoneRequestDTO requestDTO) {
        return phoneMapper.toResponseDTO(phoneBusiness.createPhone(farmId, phoneMapper.toRequestVO(requestDTO)));
    }

    public PhoneResponseDTO updatePhone(Long farmId, Long phoneId, PhoneRequestDTO requestDTO) {
        return phoneMapper.toResponseDTO(phoneBusiness.updatePhone(farmId, phoneId, phoneMapper.toRequestVO(requestDTO)));
    }

    public PhoneResponseDTO findPhoneById(Long farmId, Long phoneId) {
        return phoneMapper.toResponseDTO(phoneBusiness.findPhoneById(farmId, phoneId));
    }

    public List<PhoneResponseDTO> findAllPhonesByFarm(Long farmId) {
        return phoneBusiness.findAllPhonesByFarm(farmId).stream()
                .map(phoneMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public void deletePhone(Long farmId, Long phoneId) {
        phoneBusiness.deletePhone(farmId, phoneId);
    }
}
