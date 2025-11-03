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
@RequestMapping("/api/phones")
public class PhoneController {

    @Autowired
    private PhoneFacade phoneFacade;

    @Operation(summary = "Cadastra um novo telefone")
    @PostMapping
    public ResponseEntity<PhoneResponseDTO> createPhone(@RequestBody @Valid PhoneRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(phoneFacade.createPhone(requestDTO));
    }

    @Operation(summary = "Busca um telefone pelo ID")
    @GetMapping("/{id}")
    public ResponseEntity<PhoneResponseDTO> getPhoneById(@Parameter(description = "ID do telefone a ser buscado", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(phoneFacade.findPhoneById(id));
    }

    @Operation(summary = "Lista todos os telefones cadastrados")
    @GetMapping
    public ResponseEntity<List<PhoneResponseDTO>> getAllPhones() {
        return ResponseEntity.ok(phoneFacade.findAllPhones());
    }

    @Operation(summary = "Atualiza um telefone existente")
    @PutMapping("/{id}")
    public ResponseEntity<PhoneResponseDTO> updatePhone(@Parameter(description = "ID do telefone a ser atualizado", example = "1") @PathVariable Long id, @RequestBody @Valid PhoneRequestDTO requestDTO) {
        return ResponseEntity.ok(phoneFacade.updatePhone(id, requestDTO));
    }

    @Operation(summary = "Remove um telefone existente")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePhone(@Parameter(description = "ID do telefone a ser removido", example = "1") @PathVariable Long id) {
        phoneFacade.deletePhone(id);
        return ResponseEntity.noContent().build();
    }
}
