package com.devmaster.goatfarm.goat.api.controller;

import com.devmaster.goatfarm.goat.api.dto.GoatRequestDTO;
import com.devmaster.goatfarm.goat.api.dto.GoatResponseDTO;
import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.converter.GoatDTOConverter;
import com.devmaster.goatfarm.goat.facade.GoatFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/goats")
public class GoatController {

    @Autowired
    private GoatFacade goatFacade;

    @Operation(summary = "Verifica as permissões do usuário logado", hidden = true)
    @GetMapping("/debug")
    public ResponseEntity<String> debugAuthorities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return ResponseEntity.status(403).body("Nenhuma autenticação ativa.");
        }

        return ResponseEntity.ok("Authorities reconhecidas: " + authentication.getAuthorities());
    }

    @Operation(summary = "Cadastra uma nova cabra no sistema")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<GoatResponseDTO> createGoat(
            @Valid @RequestBody GoatRequestDTO goatRequestDTO) {

        GoatRequestVO requestVO = GoatDTOConverter.toRequestVO(goatRequestDTO);
        Long farmId = goatRequestDTO.getFarmId();
        Long ownerId = goatRequestDTO.getOwnerId();

        return ResponseEntity.ok(GoatDTOConverter.toResponseDTO(
                goatFacade.createGoat(requestVO, ownerId, farmId)));
    }

    @Operation(summary = "Atualiza os dados de uma cabra existente")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{registrationNumber}")
    public ResponseEntity<GoatResponseDTO> updateGoat(
            @Parameter(description = "Número de registro da cabra", example = "2114517012")
            @PathVariable String registrationNumber,
            @Valid @RequestBody GoatRequestDTO goatRequestDTO) {

        GoatRequestVO requestVO = GoatDTOConverter.toRequestVO(goatRequestDTO);
        return ResponseEntity.ok(GoatDTOConverter.toResponseDTO(
                goatFacade.updateGoat(registrationNumber, requestVO)));
    }

    @Operation(summary = "Busca paginada por cabras usando parte do nome")
    @GetMapping("/name")
    public ResponseEntity<Page<GoatResponseDTO>> searchGoatByName(
            @Parameter(description = "Nome ou parte do nome da cabra", example = "NAIDE")
            @RequestParam(defaultValue = "") String name,
            @Parameter(hidden = true)
            @PageableDefault(size = 12) Pageable pageable) {

        return ResponseEntity.ok(goatFacade.searchGoatByName(name, pageable)
                .map(GoatDTOConverter::toResponseDTO));
    }

    @Operation(summary = "Lista todas as cabras cadastradas com paginação")
    @GetMapping
    public ResponseEntity<Page<GoatResponseDTO>> findAllGoats(
            @PageableDefault(size = 12) Pageable pageable) {

        return ResponseEntity.ok(goatFacade.findAllGoats(pageable)
                .map(GoatDTOConverter::toResponseDTO));
    }

    @Operation(summary = "Busca uma cabra pelo número de registro")
    @GetMapping("/{registrationNumber}")
    public ResponseEntity<GoatResponseDTO> findByRegistrationNumber(
            @Parameter(description = "Número de registro da cabra", example = "2114517012")
            @PathVariable String registrationNumber) {

        return ResponseEntity.ok(GoatDTOConverter.toResponseDTO(
                goatFacade.findGoatByRegistrationNumber(registrationNumber)));
    }

    @Operation(summary = "Remove uma cabra do sistema")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{registrationNumber}")
    public ResponseEntity<Void> deleteGoat(
            @Parameter(description = "Número de registro da cabra a ser removida", example = "2114517012")
            @PathVariable String registrationNumber) {

        goatFacade.deleteGoatByRegistrationNumber(registrationNumber);
        return ResponseEntity.noContent().build();
    }
}
