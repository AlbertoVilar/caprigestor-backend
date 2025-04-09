package com.devmaster.goatfarm.phone.api.controller;

import com.devmaster.goatfarm.phone.api.dto.PhoneRequestDTO;
import com.devmaster.goatfarm.phone.api.dto.PhoneResponseDTO;
import com.devmaster.goatfarm.phone.business.bo.PhoneRequestVO;
import com.devmaster.goatfarm.phone.business.bo.PhoneResponseVO;
import com.devmaster.goatfarm.phone.business.business.PhoneBusiness;
import com.devmaster.goatfarm.phone.converter.PhoneDTOConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/phones")
public class PhoneController {

    @Autowired
    private PhoneBusiness phoneBusiness;

    @PostMapping
    public PhoneResponseDTO createPhone(@RequestBody PhoneRequestDTO requestDTO) {
        PhoneRequestVO requestVO = PhoneDTOConverter.toVO(requestDTO);
        PhoneResponseVO responseVO = phoneBusiness.createPhone(requestVO);
        return PhoneDTOConverter.toDTO(responseVO);
    }

    @GetMapping("/{id}")
    public PhoneResponseDTO getPhoneById(@PathVariable Long id) {
        PhoneResponseVO responseVO = phoneBusiness.findPhoneById(id);
        return PhoneDTOConverter.toDTO(responseVO);
    }

    @GetMapping
    public List<PhoneResponseDTO> getAllPhones() {
        List<PhoneResponseVO> responseVOs = phoneBusiness.findAllPhones();
        return responseVOs.stream()
                .map(PhoneDTOConverter::toDTO)
                .collect(Collectors.toList());
    }

    @PutMapping("/{id}")
    public PhoneResponseDTO updatePhone(@PathVariable Long id, @RequestBody PhoneRequestDTO requestDTO) {
        PhoneRequestVO requestVO = PhoneDTOConverter.toVO(requestDTO);
        PhoneResponseVO responseVO = phoneBusiness.updatePhone(id, requestVO);
        return PhoneDTOConverter.toDTO(responseVO);
    }

    @DeleteMapping("/{id}")
    public String deletePhone(@PathVariable Long id) {
        return phoneBusiness.deletePhone(id);
    }
}
