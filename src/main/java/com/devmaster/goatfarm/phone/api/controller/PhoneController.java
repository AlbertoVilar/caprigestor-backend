package com.devmaster.goatfarm.phone.api.controller;

import com.devmaster.goatfarm.phone.api.dto.PhoneRequestDTO;
import com.devmaster.goatfarm.phone.api.dto.PhoneResponseDTO;
import com.devmaster.goatfarm.phone.business.bo.PhoneRequestVO;
import com.devmaster.goatfarm.phone.business.bo.PhoneResponseVO;
import com.devmaster.goatfarm.phone.converter.PhoneDTOConverter;
import com.devmaster.goatfarm.phone.facade.PhoneFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
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

    @Operation(summary = "Cadastra um novo telefone")
    //@PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<PhoneResponseDTO> createPhone(
            @RequestBody(description = "Dados do telefone a ser cadastrado")
            @org.springframework.web.bind.annotation.RequestBody @Valid PhoneRequestDTO requestDTO) {

        PhoneRequestVO requestVO = PhoneDTOConverter.toVO(requestDTO);
        Long goatFarmId = requestDTO.getGoatFarmId();

        PhoneResponseVO responseVO = phoneFacade.createPhone(requestVO, goatFarmId);
        PhoneResponseDTO responseDTO = PhoneDTOConverter.toDTO(responseVO);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @Operation(summary = "Busca um telefone pelo ID")
    //@PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<PhoneResponseDTO> getPhoneById(
            @Parameter(description = "ID do telefone a ser buscado", example = "1") @PathVariable Long id) {

        PhoneResponseVO responseVO = phoneFacade.findPhoneById(id);
        if (responseVO != null) {
            return ResponseEntity.ok(PhoneDTOConverter.toDTO(responseVO));
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Lista todos os telefones cadastrados")
    //@PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<List<PhoneResponseDTO>> getAllPhones() {
        List<PhoneResponseVO> responseVOs = phoneFacade.findAllPhones();
        return ResponseEntity.ok(responseVOs.stream()
                .map(PhoneDTOConverter::toDTO)
                .collect(Collectors.toList()));
    }

    @Operation(summary = "Atualiza um telefone existente")
    //@PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<PhoneResponseDTO> updatePhone(
            @Parameter(description = "ID do telefone a ser atualizado", example = "1") @PathVariable Long id,
            @RequestBody(description = "Novos dados do telefone")
            @org.springframework.web.bind.annotation.RequestBody @Valid PhoneRequestDTO requestDTO) {

        PhoneRequestVO requestVO = PhoneDTOConverter.toVO(requestDTO);
        PhoneResponseVO responseVO = phoneFacade.updatePhone(id, requestVO);
        return ResponseEntity.ok(PhoneDTOConverter.toDTO(responseVO));
    }

    @Operation(summary = "Remove um telefone existente")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePhone(
            @Parameter(description = "ID do telefone a ser removido", example = "1") @PathVariable Long id) {

        phoneFacade.deletePhone(id);
        return ResponseEntity.noContent().build();
    }
}
