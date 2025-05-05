package com.devmaster.goatfarm.phone.api.controller;

import com.devmaster.goatfarm.goat.converter.GoatDTOConverter;
import com.devmaster.goatfarm.phone.api.dto.PhoneRequestDTO;
import com.devmaster.goatfarm.phone.api.dto.PhoneResponseDTO;
import com.devmaster.goatfarm.phone.business.bo.PhoneRequestVO;
import com.devmaster.goatfarm.phone.business.bo.PhoneResponseVO;
import com.devmaster.goatfarm.phone.business.business.PhoneBusiness;
import com.devmaster.goatfarm.phone.converter.PhoneDTOConverter;
import com.devmaster.goatfarm.phone.facade.PhoneFacade;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/phones")
public class PhoneController {

    @Autowired
    private PhoneFacade phoneFacade;

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<PhoneResponseDTO> createPhone(@Valid @RequestBody PhoneRequestDTO requestDTO) {
        // Converter o DTO para VO
        PhoneRequestVO requestVO = PhoneDTOConverter.toVO(requestDTO);

        // Obter o ID do GoatFarm do DTO
        Long goatFarmId = requestDTO.getGoatFarmId(); // Supondo que o DTO tenha o goatFarmId

        // Criar o telefone usando a fachada, passando o requestVO e o ID do GoatFarm
        PhoneResponseVO responseVO = phoneFacade.createPhone(requestVO, goatFarmId);

        // Converter a resposta de VO para DTO
        PhoneResponseDTO responseDTO = PhoneDTOConverter.toDTO(responseVO);

        // Retornar a resposta com o status HTTP 200 (OK)
        return ResponseEntity.ok(responseDTO);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<PhoneResponseDTO> getPhoneById(@PathVariable Long id) {
        PhoneResponseVO responseVO = phoneFacade.findPhoneById(id);
        if (responseVO != null) {
            return ResponseEntity.ok(PhoneDTOConverter.toDTO(responseVO));
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<List<PhoneResponseDTO>> getAllPhones() {
        List<PhoneResponseVO> responseVOs = phoneFacade.findAllPhones();
        return ResponseEntity.ok(responseVOs.stream()
                .map(PhoneDTOConverter::toDTO)
                .collect(Collectors.toList()));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<PhoneResponseDTO> updatePhone(@PathVariable Long id, @Valid @RequestBody PhoneRequestDTO requestDTO) {
        PhoneRequestVO requestVO = PhoneDTOConverter.toVO(requestDTO);
        PhoneResponseVO responseVO = phoneFacade.updatePhone(id, requestVO);
        return ResponseEntity.ok(PhoneDTOConverter.toDTO(responseVO));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePhone(@PathVariable Long id) {
        phoneFacade.deletePhone(id);
        return ResponseEntity.noContent().build();
    }
}