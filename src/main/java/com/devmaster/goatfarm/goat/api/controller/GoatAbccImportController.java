package com.devmaster.goatfarm.goat.api.controller;

import com.devmaster.goatfarm.goat.api.dto.GoatAbccConfirmRequestDTO;
import com.devmaster.goatfarm.goat.api.dto.GoatAbccPreviewRequestDTO;
import com.devmaster.goatfarm.goat.api.dto.GoatAbccPreviewResponseDTO;
import com.devmaster.goatfarm.goat.api.dto.GoatAbccSearchRequestDTO;
import com.devmaster.goatfarm.goat.api.dto.GoatAbccSearchResponseDTO;
import com.devmaster.goatfarm.goat.api.dto.GoatResponseDTO;
import com.devmaster.goatfarm.goat.api.mapper.GoatAbccImportMapper;
import com.devmaster.goatfarm.goat.api.mapper.GoatMapper;
import com.devmaster.goatfarm.goat.application.ports.in.GoatAbccImportUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/goatfarms/{farmId}/goats/imports/abcc", "/api/goatfarms/{farmId}/goats/imports/abcc"})
@Tag(name = "Goat ABCC Import API", description = "Importação opcional de cabras via ABCC pública. Caminho canônico /api/v1; legado /api em descontinuação.")
public class GoatAbccImportController {

    private final GoatAbccImportUseCase goatAbccImportUseCase;
    private final GoatAbccImportMapper goatAbccImportMapper;
    private final GoatMapper goatMapper;

    public GoatAbccImportController(
            GoatAbccImportUseCase goatAbccImportUseCase,
            GoatAbccImportMapper goatAbccImportMapper,
            GoatMapper goatMapper
    ) {
        this.goatAbccImportUseCase = goatAbccImportUseCase;
        this.goatAbccImportMapper = goatAbccImportMapper;
        this.goatMapper = goatMapper;
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or ((hasAuthority('ROLE_OPERATOR') or hasAuthority('ROLE_FARM_OWNER')) and @ownershipService.isFarmOwner(#farmId))")
    @PostMapping("/search")
    @Operation(summary = "Busca animais na ABCC pública por filtros mínimos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Busca ABCC executada com sucesso."),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para importar nesta fazenda."),
            @ApiResponse(responseCode = "422", description = "Falha de validação ou regra de negócio.")
    })
    public ResponseEntity<GoatAbccSearchResponseDTO> search(
            @PathVariable("farmId") Long farmId,
            @Valid @RequestBody GoatAbccSearchRequestDTO requestDTO
    ) {
        var responseVO = goatAbccImportUseCase.search(farmId, goatAbccImportMapper.toSearchRequestVO(requestDTO));
        return ResponseEntity.ok(goatAbccImportMapper.toSearchResponseDTO(responseVO));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or ((hasAuthority('ROLE_OPERATOR') or hasAuthority('ROLE_FARM_OWNER')) and @ownershipService.isFarmOwner(#farmId))")
    @PostMapping("/preview")
    @Operation(summary = "Consulta preview detalhado de um animal da ABCC pública")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Preview ABCC retornado com sucesso."),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para importar nesta fazenda."),
            @ApiResponse(responseCode = "422", description = "Falha de validação ou regra de negócio.")
    })
    public ResponseEntity<GoatAbccPreviewResponseDTO> preview(
            @PathVariable("farmId") Long farmId,
            @Valid @RequestBody GoatAbccPreviewRequestDTO requestDTO
    ) {
        var responseVO = goatAbccImportUseCase.preview(farmId, goatAbccImportMapper.toPreviewRequestVO(requestDTO));
        return ResponseEntity.ok(goatAbccImportMapper.toPreviewResponseDTO(responseVO));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or ((hasAuthority('ROLE_OPERATOR') or hasAuthority('ROLE_FARM_OWNER')) and @ownershipService.isFarmOwner(#farmId))")
    @PostMapping("/confirm")
    @Operation(summary = "Confirma importação ABCC e cria a cabra na fazenda")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Importação confirmada e cabra criada com sucesso."),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para importar nesta fazenda."),
            @ApiResponse(responseCode = "409", description = "Conflito de unicidade (registro já existente)."),
            @ApiResponse(responseCode = "422", description = "Falha de validação ou regra de negócio.")
    })
    public ResponseEntity<GoatResponseDTO> confirm(
            @PathVariable("farmId") Long farmId,
            @Valid @RequestBody GoatAbccConfirmRequestDTO requestDTO
    ) {
        var created = goatAbccImportUseCase.confirm(
                farmId,
                requestDTO.getExternalId(),
                goatMapper.toRequestVO(requestDTO.getGoat())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(goatMapper.toResponseDTO(created));
    }
}

