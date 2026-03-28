package com.devmaster.goatfarm.milk.api.controller;

import com.devmaster.goatfarm.milk.api.dto.LactationDryRequestDTO;
import com.devmaster.goatfarm.milk.api.dto.LactationRequestDTO;
import com.devmaster.goatfarm.milk.api.dto.LactationResponseDTO;
import com.devmaster.goatfarm.milk.api.dto.LactationSummaryResponseDTO;
import com.devmaster.goatfarm.milk.api.mapper.LactationMapper;
import com.devmaster.goatfarm.milk.application.ports.in.LactationCommandUseCase;
import com.devmaster.goatfarm.milk.application.ports.in.LactationQueryUseCase;
import com.devmaster.goatfarm.milk.business.bo.LactationDryRequestVO;
import com.devmaster.goatfarm.milk.business.bo.LactationRequestVO;
import com.devmaster.goatfarm.milk.business.bo.LactationResponseVO;
import com.devmaster.goatfarm.milk.business.bo.LactationSummaryResponseVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/goatfarms/{farmId}/goats/{goatId}/lactations", "/api/goatfarms/{farmId}/goats/{goatId}/lactations"})
@Tag(
        name = "Lactation API",
        description = "GestÃ£o de lactaÃ§Ãµes por cabra. O caminho canÃ´nico Ã© /api/v1; o legado /api segue ativo apenas durante a janela de descontinuaÃ§Ã£o."
)
public class LactationController {

    private final LactationQueryUseCase lactationQueryUseCase;
    private final LactationCommandUseCase lactationCommandUseCase;
    private final LactationMapper lactationMapper;

    public LactationController(LactationQueryUseCase lactationQueryUseCase, LactationCommandUseCase lactationCommandUseCase, LactationMapper lactationMapper) {
        this.lactationQueryUseCase = lactationQueryUseCase;
        this.lactationCommandUseCase = lactationCommandUseCase;
        this.lactationMapper = lactationMapper;
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or ((hasAuthority('ROLE_OPERATOR') or hasAuthority('ROLE_FARM_OWNER')) and @ownershipService.isFarmOwner(#farmId))")
    @PostMapping
    @Operation(summary = "Abrir uma nova lactaÃ§Ã£o para uma cabra")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "LactaÃ§Ã£o aberta com sucesso."),
            @ApiResponse(responseCode = "400", description = "Payload invÃ¡lido ou dados obrigatÃ³rios ausentes."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para a fazenda informada."),
            @ApiResponse(responseCode = "404", description = "Cabra nÃ£o encontrada para a fazenda informada."),
            @ApiResponse(responseCode = "422", description = "Regra de negÃ³cio violada ao abrir a lactaÃ§Ã£o.")
    })
    public ResponseEntity<LactationResponseDTO> openLactation(
            @Parameter(description = "Identificador da fazenda") @PathVariable Long farmId,
            @Parameter(description = "Identificador da cabra") @PathVariable String goatId,
            @Valid @RequestBody LactationRequestDTO request) {
        LactationRequestVO requestVO = lactationMapper.toRequestVO(request);
        LactationResponseVO responseVO = lactationCommandUseCase.openLactation(farmId, goatId, requestVO);
        return ResponseEntity.status(HttpStatus.CREATED).body(lactationMapper.toResponseDTO(responseVO));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or ((hasAuthority('ROLE_OPERATOR') or hasAuthority('ROLE_FARM_OWNER')) and @ownershipService.isFarmOwner(#farmId))")
    @GetMapping("/active")
    @Operation(summary = "Buscar lactaÃ§Ã£o ativa de uma cabra")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "LactaÃ§Ã£o ativa retornada com sucesso."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para a fazenda informada."),
            @ApiResponse(responseCode = "404", description = "LactaÃ§Ã£o ativa nÃ£o encontrada.")
    })
    public ResponseEntity<LactationResponseDTO> getActiveLactation(
            @Parameter(description = "Identificador da fazenda") @PathVariable Long farmId,
            @Parameter(description = "Identificador da cabra") @PathVariable String goatId) {
        LactationResponseVO responseVO = lactationQueryUseCase.getActiveLactation(farmId, goatId);
        return ResponseEntity.ok(lactationMapper.toResponseDTO(responseVO));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or ((hasAuthority('ROLE_OPERATOR') or hasAuthority('ROLE_FARM_OWNER')) and @ownershipService.isFarmOwner(#farmId))")
    @GetMapping("/active/summary")
    @Operation(summary = "Buscar sumÃ¡rio da lactaÃ§Ã£o ativa da cabra")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "SumÃ¡rio da lactaÃ§Ã£o ativa retornado com sucesso."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para a fazenda informada."),
            @ApiResponse(responseCode = "404", description = "LactaÃ§Ã£o ativa nÃ£o encontrada.")
    })
    public ResponseEntity<LactationSummaryResponseDTO> getActiveLactationSummary(
            @Parameter(description = "Identificador da fazenda") @PathVariable Long farmId,
            @Parameter(description = "Identificador da cabra") @PathVariable String goatId) {
        LactationSummaryResponseVO responseVO = lactationQueryUseCase.getActiveLactationSummary(farmId, goatId);
        return ResponseEntity.ok(lactationMapper.toSummaryResponseDTO(responseVO));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or ((hasAuthority('ROLE_OPERATOR') or hasAuthority('ROLE_FARM_OWNER')) and @ownershipService.isFarmOwner(#farmId))")
    @PatchMapping("/{lactationId}/dry")
    @Operation(summary = "Marcar uma lactaÃ§Ã£o como seca")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "LactaÃ§Ã£o encerrada como seca com sucesso."),
            @ApiResponse(responseCode = "400", description = "Payload invÃ¡lido ou identificador de lactaÃ§Ã£o invÃ¡lido."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para a fazenda informada."),
            @ApiResponse(responseCode = "404", description = "LactaÃ§Ã£o nÃ£o encontrada."),
            @ApiResponse(responseCode = "422", description = "Regra de negÃ³cio violada ao secar a lactaÃ§Ã£o.")
    })
    public ResponseEntity<LactationResponseDTO> dryLactation(
            @Parameter(description = "Identificador da fazenda") @PathVariable Long farmId,
            @Parameter(description = "Identificador da cabra") @PathVariable String goatId,
            @Parameter(description = "Identificador da lactaÃ§Ã£o") @PathVariable Long lactationId,
            @Valid @RequestBody LactationDryRequestDTO request) {
        LactationDryRequestVO requestVO = lactationMapper.toDryRequestVO(request);
        LactationResponseVO responseVO = lactationCommandUseCase.dryLactation(farmId, goatId, lactationId, requestVO);
        return ResponseEntity.ok(lactationMapper.toResponseDTO(responseVO));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or ((hasAuthority('ROLE_OPERATOR') or hasAuthority('ROLE_FARM_OWNER')) and @ownershipService.isFarmOwner(#farmId))")
    @PatchMapping("/{lactationId}/resume")
    @Operation(summary = "Retomar uma lactacao previamente secada")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lactacao retomada com sucesso."),
            @ApiResponse(responseCode = "400", description = "Identificador de lactacao invalido."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para a fazenda informada."),
            @ApiResponse(responseCode = "404", description = "Lactacao nao encontrada."),
            @ApiResponse(responseCode = "422", description = "Regra de negocio violada ao retomar a lactacao.")
    })
    public ResponseEntity<LactationResponseDTO> resumeLactation(
            @Parameter(description = "Identificador da fazenda") @PathVariable Long farmId,
            @Parameter(description = "Identificador da cabra") @PathVariable String goatId,
            @Parameter(description = "Identificador da lactacao") @PathVariable Long lactationId) {
        LactationResponseVO responseVO = lactationCommandUseCase.resumeLactation(farmId, goatId, lactationId);
        return ResponseEntity.ok(lactationMapper.toResponseDTO(responseVO));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or ((hasAuthority('ROLE_OPERATOR') or hasAuthority('ROLE_FARM_OWNER')) and @ownershipService.isFarmOwner(#farmId))")
    @GetMapping("/{lactationId}")
    @Operation(summary = "Buscar lactaÃ§Ã£o por identificador")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "LactaÃ§Ã£o retornada com sucesso."),
            @ApiResponse(responseCode = "400", description = "Identificador de lactaÃ§Ã£o invÃ¡lido."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para a fazenda informada."),
            @ApiResponse(responseCode = "404", description = "LactaÃ§Ã£o nÃ£o encontrada.")
    })
    public ResponseEntity<LactationResponseDTO> getLactationById(
            @Parameter(description = "Identificador da fazenda") @PathVariable Long farmId,
            @Parameter(description = "Identificador da cabra") @PathVariable String goatId,
            @Parameter(description = "Identificador da lactaÃ§Ã£o") @PathVariable Long lactationId) {
        LactationResponseVO responseVO = lactationQueryUseCase.getLactationById(farmId, goatId, lactationId);
        return ResponseEntity.ok(lactationMapper.toResponseDTO(responseVO));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or ((hasAuthority('ROLE_OPERATOR') or hasAuthority('ROLE_FARM_OWNER')) and @ownershipService.isFarmOwner(#farmId))")
    @GetMapping("/{lactationId}/summary")
    @Operation(summary = "Buscar sumÃ¡rio da lactaÃ§Ã£o por identificador")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "SumÃ¡rio da lactaÃ§Ã£o retornado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Identificador de lactaÃ§Ã£o invÃ¡lido."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para a fazenda informada."),
            @ApiResponse(responseCode = "404", description = "LactaÃ§Ã£o nÃ£o encontrada.")
    })
    public ResponseEntity<LactationSummaryResponseDTO> getLactationSummary(
            @Parameter(description = "Identificador da fazenda") @PathVariable Long farmId,
            @Parameter(description = "Identificador da cabra") @PathVariable String goatId,
            @Parameter(description = "Identificador da lactaÃ§Ã£o") @PathVariable Long lactationId) {
        LactationSummaryResponseVO responseVO = lactationQueryUseCase.getLactationSummary(farmId, goatId, lactationId);
        return ResponseEntity.ok(lactationMapper.toSummaryResponseDTO(responseVO));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or ((hasAuthority('ROLE_OPERATOR') or hasAuthority('ROLE_FARM_OWNER')) and @ownershipService.isFarmOwner(#farmId))")
    @GetMapping
    @Operation(summary = "Listar histÃ³rico de lactaÃ§Ãµes de uma cabra")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "HistÃ³rico de lactaÃ§Ãµes retornado com sucesso."),
            @ApiResponse(responseCode = "400", description = "ParÃ¢metros de paginaÃ§Ã£o invÃ¡lidos."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para a fazenda informada.")
    })
    public ResponseEntity<Page<LactationResponseDTO>> getAllLactations(
            @Parameter(description = "Identificador da fazenda") @PathVariable Long farmId,
            @Parameter(description = "Identificador da cabra") @PathVariable String goatId,
            @PageableDefault(sort = "startDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<LactationResponseVO> pageVO = lactationQueryUseCase.getAllLactations(farmId, goatId, pageable);
        Page<LactationResponseDTO> pageDTO = pageVO.map(lactationMapper::toResponseDTO);
        return ResponseEntity.ok(pageDTO);
    }
}