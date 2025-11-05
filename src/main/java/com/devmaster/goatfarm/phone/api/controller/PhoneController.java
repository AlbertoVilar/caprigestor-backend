package com.devmaster.goatfarm.phone.api.controller;

import com.devmaster.goatfarm.phone.api.dto.PhoneRequestDTO;
import com.devmaster.goatfarm.phone.api.dto.PhoneResponseDTO;
import com.devmaster.goatfarm.phone.facade.PhoneFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goatfarms/{farmId}/phones")
public class PhoneController {

    @Autowired
    private PhoneFacade phoneFacade;

    @Operation(summary = "Cadastra um novo telefone para uma fazenda")
    @PostMapping
    public ResponseEntity<PhoneResponseDTO> createPhone(@PathVariable Long farmId, @RequestBody @Valid PhoneRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(phoneFacade.createPhone(farmId, requestDTO));
    }

    @Operation(summary = "Busca um telefone pelo ID dentro de uma fazenda")
    @GetMapping("/{phoneId}")
    public ResponseEntity<PhoneResponseDTO> getPhoneById(
            @PathVariable Long farmId,
            @Parameter(description = "ID do telefone a ser buscado", example = "1") @PathVariable Long phoneId) {
        return ResponseEntity.ok(phoneFacade.findPhoneById(farmId, phoneId));
    }

    @Operation(summary = "Lista todos os telefones de uma fazenda")
    @GetMapping
    public ResponseEntity<List<PhoneResponseDTO>> findAllPhonesByFarm(@PathVariable Long farmId) {
        return ResponseEntity.ok(phoneFacade.findAllPhonesByFarm(farmId));
    }

    @Operation(summary = "Atualiza um telefone existente em uma fazenda")
    @PutMapping("/{phoneId}")
    public ResponseEntity<PhoneResponseDTO> updatePhone(
            @PathVariable Long farmId,
            @Parameter(description = "ID do telefone a ser atualizado", example = "1") @PathVariable Long phoneId,
            @RequestBody @Valid PhoneRequestDTO requestDTO) {
        return ResponseEntity.ok(phoneFacade.updatePhone(farmId, phoneId, requestDTO));
    }

    @Operation(summary = "Remove um telefone existente de uma fazenda")
    @DeleteMapping("/{phoneId}")
    public ResponseEntity<Void> deletePhone(
            @PathVariable Long farmId,
            @Parameter(description = "ID do telefone a ser removido", example = "1") @PathVariable Long phoneId) {
        phoneFacade.deletePhone(farmId, phoneId);
        return ResponseEntity.noContent().build();
    }
}
