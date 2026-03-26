package com.devmaster.goatfarm.goat.api.controller;

import com.devmaster.goatfarm.goat.api.dto.GoatRequestDTO;
import com.devmaster.goatfarm.goat.api.dto.GoatHerdSummaryDTO;
import com.devmaster.goatfarm.goat.api.dto.GoatResponseDTO;
import com.devmaster.goatfarm.goat.api.dto.GoatExitRequestDTO;
import com.devmaster.goatfarm.goat.api.dto.GoatExitResponseDTO;
import com.devmaster.goatfarm.goat.application.ports.in.GoatManagementUseCase;
import com.devmaster.goatfarm.goat.api.mapper.GoatMapper;
import com.devmaster.goatfarm.goat.enums.GoatBreed;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/goatfarms/{farmId}/goats", "/api/goatfarms/{farmId}/goats"})
@Tag(name = "Goat API", description = "Gerenciamento de cabras da fazenda. Caminho canônico /api/v1; legado /api em descontinuação.")
public class GoatController {

    private final GoatManagementUseCase goatUseCase;
    private final GoatMapper goatMapper;

    public GoatController(GoatManagementUseCase goatUseCase, GoatMapper goatMapper) {
        this.goatUseCase = goatUseCase;
        this.goatMapper = goatMapper;
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or ((hasAuthority('ROLE_OPERATOR') or hasAuthority('ROLE_FARM_OWNER')) and @ownershipService.isFarmOwner(#farmId))")
    @PostMapping
    @Operation(summary = "Cadastra uma nova cabra em uma fazenda")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cabra criada com sucesso."),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para cadastrar cabras nesta fazenda."),
            @ApiResponse(responseCode = "409", description = "Conflito de unicidade (registro já existente)."),
            @ApiResponse(responseCode = "422", description = "Falha de validação dos dados informados.")
    })
    public ResponseEntity<GoatResponseDTO> createGoat(@PathVariable("farmId") Long farmId, @Valid @RequestBody GoatRequestDTO goatRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(goatMapper.toResponseDTO(
                        goatUseCase.createGoat(farmId, goatMapper.toRequestVO(goatRequestDTO))
                ));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or ((hasAuthority('ROLE_OPERATOR') or hasAuthority('ROLE_FARM_OWNER')) and @ownershipService.isFarmOwner(#farmId))")
    @PutMapping("/{goatId}")
    @Operation(summary = "Atualiza os dados de uma cabra existente em uma fazenda")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cabra atualizada com sucesso."),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para atualizar cabras nesta fazenda."),
            @ApiResponse(responseCode = "404", description = "Cabra não encontrada."),
            @ApiResponse(responseCode = "422", description = "Falha de validação dos dados informados.")
    })
    public ResponseEntity<GoatResponseDTO> updateGoat(@PathVariable("farmId") Long farmId, @PathVariable("goatId") String goatId, @Valid @RequestBody GoatRequestDTO goatRequestDTO) {
        return ResponseEntity.ok(
                goatMapper.toResponseDTO(
                        goatUseCase.updateGoat(farmId, goatId, goatMapper.toRequestVO(goatRequestDTO))
                )
        );
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or ((hasAuthority('ROLE_OPERATOR') or hasAuthority('ROLE_FARM_OWNER')) and @ownershipService.isFarmOwner(#farmId))")
    @PatchMapping("/{goatId}/exit")
    @Operation(summary = "Registra saida controlada do animal do rebanho")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Saida registrada com sucesso."),
            @ApiResponse(responseCode = "403", description = "Usuario sem permissao para operar nesta fazenda."),
            @ApiResponse(responseCode = "404", description = "Cabra nao encontrada."),
            @ApiResponse(responseCode = "422", description = "Falha de validacao/regra de negocio.")
    })
    public ResponseEntity<GoatExitResponseDTO> exitGoat(
            @PathVariable("farmId") Long farmId,
            @PathVariable("goatId") String goatId,
            @Valid @RequestBody GoatExitRequestDTO requestDTO) {
        return ResponseEntity.ok(
                goatMapper.toExitResponseDTO(
                        goatUseCase.exitGoat(farmId, goatId, goatMapper.toExitRequestVO(requestDTO))
                )
        );
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or ((hasAuthority('ROLE_OPERATOR') or hasAuthority('ROLE_FARM_OWNER')) and @ownershipService.isFarmOwner(#farmId))")
    @DeleteMapping("/{goatId}")
    @Operation(summary = "Remove uma cabra de uma fazenda")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cabra removida com sucesso."),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para remover cabras nesta fazenda."),
            @ApiResponse(responseCode = "404", description = "Cabra não encontrada.")
    })
    public ResponseEntity<Void> deleteGoat(@PathVariable("farmId") Long farmId, @PathVariable("goatId") String goatId) {
        goatUseCase.deleteGoat(farmId, goatId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{goatId}")
    @Operation(summary = "Busca uma cabra pelo ID dentro de uma fazenda")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cabra encontrada com sucesso."),
            @ApiResponse(responseCode = "404", description = "Cabra não encontrada.")
    })
    public ResponseEntity<GoatResponseDTO> findGoatById(@PathVariable("farmId") Long farmId, @PathVariable("goatId") String goatId) {
        return ResponseEntity.ok(
                goatMapper.toResponseDTO(
                        goatUseCase.findGoatById(farmId, goatId)
                )
        );
    }

    @GetMapping("/{goatId}/offspring")
    @Operation(summary = "Lista as crias locais vinculadas ao animal")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Crias retornadas com sucesso."),
            @ApiResponse(responseCode = "404", description = "Cabra nÃ£o encontrada.")
    })
    public ResponseEntity<List<GoatResponseDTO>> listOffspring(@PathVariable("farmId") Long farmId, @PathVariable("goatId") String goatId) {
        return ResponseEntity.ok(
                goatUseCase.listOffspring(farmId, goatId).stream()
                        .map(goatMapper::toResponseDTO)
                        .toList()
        );
    }

    @GetMapping
    @Operation(summary = "Lista todas as cabras de uma fazenda")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listagem executada com sucesso."),
            @ApiResponse(responseCode = "400", description = "Parâmetros de paginação inválidos.")
    })
    public ResponseEntity<Page<GoatResponseDTO>> findAllGoatsByFarm(
            @PathVariable("farmId") Long farmId,
            @RequestParam(required = false) GoatBreed breed,
            @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok(
                goatUseCase.findAllGoatsByFarm(farmId, breed, pageable).map(goatMapper::toResponseDTO)
        );
    }

    @GetMapping("/search")
    @Operation(summary = "Busca cabras por nome dentro de uma fazenda")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Busca executada com sucesso."),
            @ApiResponse(responseCode = "400", description = "Parâmetros de busca ou paginação inválidos.")
    })
    public ResponseEntity<Page<GoatResponseDTO>> findGoatsByNameAndFarm(
            @PathVariable("farmId") Long farmId,
            @RequestParam String name,
            @RequestParam(required = false) GoatBreed breed,
            @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok(
                goatUseCase.findGoatsByNameAndFarm(farmId, name, breed, pageable).map(goatMapper::toResponseDTO)
        );
    }

    @GetMapping("/summary")
    @Operation(summary = "Retorna o resumo agregado do rebanho da fazenda")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resumo do rebanho retornado com sucesso.")
    })
    public ResponseEntity<GoatHerdSummaryDTO> getGoatHerdSummary(@PathVariable("farmId") Long farmId) {
        return ResponseEntity.ok(goatMapper.toHerdSummaryDTO(goatUseCase.getGoatHerdSummary(farmId)));
    }
}

