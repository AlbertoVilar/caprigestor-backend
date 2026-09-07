package com.devmaster.goatfarm.phone.api.controller;

import com.devmaster.goatfarm.phone.api.dto.PhoneRequestDTO;
import com.devmaster.goatfarm.phone.api.dto.PhoneResponseDTO;
import com.devmaster.goatfarm.phone.application.ports.in.PhoneManagementUseCase;
import com.devmaster.goatfarm.phone.api.mapper.PhoneMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/goatfarms/{farmId}/phones")
public class PhoneController {

    private final PhoneManagementUseCase phoneUseCase;
    private final PhoneMapper phoneMapper;

    public PhoneController(PhoneManagementUseCase phoneUseCase, PhoneMapper phoneMapper) {
        this.phoneUseCase = phoneUseCase;
        this.phoneMapper = phoneMapper;
    }

    @Operation(summary = "Cadastra um novo telefone para uma fazenda")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or (hasAuthority('ROLE_FARM_OWNER') and @ownershipService.isFarmOwner(#farmId))")
    @PostMapping
    public ResponseEntity<PhoneResponseDTO> createPhone(@PathVariable Long farmId, @RequestBody @Valid PhoneRequestDTO requestDTO) {
        var responseVO = phoneUseCase.createPhone(farmId, phoneMapper.toRequestVO(requestDTO));
        return ResponseEntity.status(HttpStatus.CREATED).body(phoneMapper.toResponseDTO(responseVO));
    }

    @Operation(summary = "Busca um telefone pelo ID dentro de uma fazenda")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or (hasAuthority('ROLE_FARM_OWNER') and @ownershipService.isFarmOwner(#farmId))")
    @GetMapping("/{phoneId}")
    public ResponseEntity<PhoneResponseDTO> getPhoneById(
            @PathVariable Long farmId,
            @Parameter(description = "ID do telefone a ser buscado", example = "1") @PathVariable Long phoneId) {
        var responseVO = phoneUseCase.findPhoneById(farmId, phoneId);
        return ResponseEntity.ok(phoneMapper.toResponseDTO(responseVO));
    }

    @Operation(summary = "Lista todos os telefones de uma fazenda")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or (hasAuthority('ROLE_FARM_OWNER') and @ownershipService.isFarmOwner(#farmId))")
    @GetMapping
    public ResponseEntity<List<PhoneResponseDTO>> findAllPhonesByFarm(@PathVariable Long farmId) {
        var voList = phoneUseCase.findAllPhonesByFarm(farmId);
        return ResponseEntity.ok(phoneMapper.toResponseDTOList(voList));
    }

    @Operation(summary = "Atualiza um telefone existente em uma fazenda")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or (hasAuthority('ROLE_FARM_OWNER') and @ownershipService.isFarmOwner(#farmId))")
    @PutMapping("/{phoneId}")
    public ResponseEntity<PhoneResponseDTO> updatePhone(
            @PathVariable Long farmId,
            @Parameter(description = "ID do telefone a ser atualizado", example = "1") @PathVariable Long phoneId,
            @RequestBody @Valid PhoneRequestDTO requestDTO) {
        var responseVO = phoneUseCase.updatePhone(farmId, phoneId, phoneMapper.toRequestVO(requestDTO));
        return ResponseEntity.ok(phoneMapper.toResponseDTO(responseVO));
    }

    @Operation(summary = "Remove um telefone existente de uma fazenda")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or (hasAuthority('ROLE_FARM_OWNER') and @ownershipService.isFarmOwner(#farmId))")
    @DeleteMapping("/{phoneId}")
    public ResponseEntity<Void> deletePhone(
            @PathVariable Long farmId,
            @Parameter(description = "ID do telefone a ser removido", example = "1") @PathVariable Long phoneId) {
        phoneUseCase.deletePhone(farmId, phoneId);
        return ResponseEntity.noContent().build();
    }
}
