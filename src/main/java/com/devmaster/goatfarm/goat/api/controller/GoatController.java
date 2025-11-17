package com.devmaster.goatfarm.goat.api.controller;

import com.devmaster.goatfarm.goat.api.dto.GoatRequestDTO;
import com.devmaster.goatfarm.goat.api.dto.GoatResponseDTO;
import com.devmaster.goatfarm.application.ports.in.GoatManagementUseCase;
import com.devmaster.goatfarm.goat.mapper.GoatMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/goatfarms/{farmId}/goats")
@Tag(name = "Goat API", description = "Gerenciamento de cabras na fazenda")
public class GoatController {

    private final GoatManagementUseCase goatUseCase;
    private final GoatMapper goatMapper;

    @Autowired
    public GoatController(GoatManagementUseCase goatUseCase, GoatMapper goatMapper) {
        this.goatUseCase = goatUseCase;
        this.goatMapper = goatMapper;
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_OPERATOR')")
    @PostMapping
    @Operation(summary = "Cadastra uma nova cabra em uma fazenda")
    public ResponseEntity<GoatResponseDTO> createGoat(@PathVariable Long farmId, @Valid @RequestBody GoatRequestDTO goatRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(goatMapper.toResponseDTO(
                        goatUseCase.createGoat(farmId, goatMapper.toRequestVO(goatRequestDTO))
                ));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_OPERATOR')")
    @PutMapping("/{goatId}")
    @Operation(summary = "Atualiza os dados de uma cabra existente em uma fazenda")
    public ResponseEntity<GoatResponseDTO> updateGoat(@PathVariable Long farmId, @PathVariable String goatId, @Valid @RequestBody GoatRequestDTO goatRequestDTO) {
        return ResponseEntity.ok(
                goatMapper.toResponseDTO(
                        goatUseCase.updateGoat(farmId, goatId, goatMapper.toRequestVO(goatRequestDTO))
                )
        );
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_OPERATOR')")
    @DeleteMapping("/{goatId}")
    @Operation(summary = "Remove uma cabra de uma fazenda")
    public ResponseEntity<Void> deleteGoat(@PathVariable Long farmId, @PathVariable String goatId) {
        goatUseCase.deleteGoat(farmId, goatId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{goatId}")
    @Operation(summary = "Busca uma cabra pelo ID dentro de uma fazenda")
    public ResponseEntity<GoatResponseDTO> findGoatById(@PathVariable Long farmId, @PathVariable String goatId) {
        return ResponseEntity.ok(
                goatMapper.toResponseDTO(
                        goatUseCase.findGoatById(farmId, goatId)
                )
        );
    }

    @GetMapping
    @Operation(summary = "Lista todas as cabras de uma fazenda")
    public ResponseEntity<Page<GoatResponseDTO>> findAllGoatsByFarm(@PathVariable Long farmId, @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok(
                goatUseCase.findAllGoatsByFarm(farmId, pageable).map(goatMapper::toResponseDTO)
        );
    }

    @GetMapping("/search")
    @Operation(summary = "Busca cabras por nome dentro de uma fazenda")
    public ResponseEntity<Page<GoatResponseDTO>> findGoatsByNameAndFarm(
            @PathVariable Long farmId,
            @RequestParam String name,
            @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok(
                goatUseCase.findGoatsByNameAndFarm(farmId, name, pageable).map(goatMapper::toResponseDTO)
        );
    }
}