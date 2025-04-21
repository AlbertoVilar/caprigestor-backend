package com.devmaster.goatfarm.phone.api.controller;

import com.devmaster.goatfarm.phone.api.dto.PhoneRequestDTO;
import com.devmaster.goatfarm.phone.api.dto.PhoneResponseDTO;
import com.devmaster.goatfarm.phone.business.bo.PhoneRequestVO;
import com.devmaster.goatfarm.phone.business.bo.PhoneResponseVO;
import com.devmaster.goatfarm.phone.business.business.PhoneBusiness;
import com.devmaster.goatfarm.phone.converter.PhoneDTOConverter;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/phones")
public class PhoneController {

    @Autowired
    private PhoneBusiness phoneBusiness;

    @PostMapping
    public ResponseEntity<PhoneResponseDTO> createPhone(@Valid @RequestBody PhoneRequestDTO requestDTO) {
        PhoneRequestVO requestVO = PhoneDTOConverter.toVO(requestDTO);
        PhoneResponseVO responseVO = phoneBusiness.createPhone(requestVO);
        return new ResponseEntity<>(PhoneDTOConverter.toDTO(responseVO), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PhoneResponseDTO> getPhoneById(@PathVariable Long id) {
        PhoneResponseVO responseVO = phoneBusiness.findPhoneById(id);
        if (responseVO != null) {
            return ResponseEntity.ok(PhoneDTOConverter.toDTO(responseVO));
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping
    public ResponseEntity<List<PhoneResponseDTO>> getAllPhones() {
        List<PhoneResponseVO> responseVOs = phoneBusiness.findAllPhones();
        return ResponseEntity.ok(responseVOs.stream()
                .map(PhoneDTOConverter::toDTO)
                .collect(Collectors.toList()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PhoneResponseDTO> updatePhone(@PathVariable Long id, @Valid @RequestBody PhoneRequestDTO requestDTO) {
        PhoneRequestVO requestVO = PhoneDTOConverter.toVO(requestDTO);
        PhoneResponseVO responseVO = phoneBusiness.updatePhone(id, requestVO);
        return ResponseEntity.ok(PhoneDTOConverter.toDTO(responseVO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePhone(@PathVariable Long id) {
        phoneBusiness.deletePhone(id);
        return ResponseEntity.noContent().build();
    }
}