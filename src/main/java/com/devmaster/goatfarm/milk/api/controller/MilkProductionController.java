package com.devmaster.goatfarm.milk.api.controller;

import com.devmaster.goatfarm.milk.api.dto.MilkProductionRequestDTO;
import com.devmaster.goatfarm.milk.api.dto.MilkProductionResponseDTO;
import com.devmaster.goatfarm.milk.api.dto.MilkProductionUpdateRequestDTO;
import com.devmaster.goatfarm.milk.api.mapper.MilkProductionMapper;
import com.devmaster.goatfarm.milk.application.ports.in.MilkProductionUseCase;
import com.devmaster.goatfarm.milk.business.bo.MilkProductionRequestVO;
import com.devmaster.goatfarm.milk.business.bo.MilkProductionResponseVO;
import com.devmaster.goatfarm.milk.business.bo.MilkProductionUpdateRequestVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping({"/api/v1/goatfarms/{farmId}/goats/{goatId}/milk-productions", "/api/goatfarms/{farmId}/goats/{goatId}/milk-productions"})
@Tag(
        name = "Milk Production API",
        description = "Gestão de produção de leite por cabra. O caminho canônico é /api/v1; o legado /api segue ativo apenas durante a janela de descontinuação."
)
public class MilkProductionController {

    private final MilkProductionUseCase milkProductionUseCase;
    private final MilkProductionMapper milkProductionMapper;

    public MilkProductionController(MilkProductionUseCase milkProductionUseCase, MilkProductionMapper milkProductionMapper) {
        this.milkProductionUseCase = milkProductionUseCase;
        this.milkProductionMapper = milkProductionMapper;
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or ((hasAuthority('ROLE_OPERATOR') or hasAuthority('ROLE_FARM_OWNER')) and @ownershipService.isFarmOwner(#farmId))")
    @PostMapping
    @Operation(summary = "Registrar produção diária de leite")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Produção registrada com sucesso."),
            @ApiResponse(responseCode = "400", description = "Payload inválido ou dados obrigatórios ausentes."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para a fazenda informada."),
            @ApiResponse(responseCode = "404", description = "Cabra não encontrada para a fazenda informada."),
            @ApiResponse(responseCode = "422", description = "Regra de negócio violada ao registrar a produção.")
    })
    public ResponseEntity<MilkProductionResponseDTO> createMilkProduction(
            @Parameter(description = "Identificador da fazenda") @PathVariable Long farmId,
            @Parameter(description = "Identificador da cabra") @PathVariable String goatId,
            @Valid @RequestBody MilkProductionRequestDTO request) {
        MilkProductionRequestVO requestVO = milkProductionMapper.toRequestVO(request);
        MilkProductionResponseVO responseVO = milkProductionUseCase.createMilkProduction(farmId, goatId, requestVO);
        return ResponseEntity.status(HttpStatus.CREATED).body(milkProductionMapper.toResponseDTO(responseVO));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or ((hasAuthority('ROLE_OPERATOR') or hasAuthority('ROLE_FARM_OWNER')) and @ownershipService.isFarmOwner(#farmId))")
    @PatchMapping("/{id}")
    @Operation(summary = "Atualizar parcialmente um registro de produção")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produção atualizada com sucesso."),
            @ApiResponse(responseCode = "400", description = "Payload inválido ou identificador inválido."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para a fazenda informada."),
            @ApiResponse(responseCode = "404", description = "Produção não encontrada."),
            @ApiResponse(responseCode = "422", description = "Regra de negócio violada ao atualizar a produção.")
    })
    public ResponseEntity<MilkProductionResponseDTO> update(
            @Parameter(description = "Identificador da fazenda") @PathVariable Long farmId,
            @Parameter(description = "Identificador da cabra") @PathVariable String goatId,
            @Parameter(description = "Identificador da produção de leite") @PathVariable Long id,
            @Valid @RequestBody MilkProductionUpdateRequestDTO request
    ) {
        MilkProductionUpdateRequestVO requestVO = milkProductionMapper.toRequestVO(request);
        MilkProductionResponseVO responseVO = milkProductionUseCase.update(farmId, goatId, id, requestVO);
        return ResponseEntity.ok(milkProductionMapper.toResponseDTO(responseVO));
    }

    @Operation(summary = "Buscar produção de leite por identificador")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produção retornada com sucesso."),
            @ApiResponse(responseCode = "400", description = "Identificador de produção inválido."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para a fazenda informada."),
            @ApiResponse(responseCode = "404", description = "Produção não encontrada.")
    })
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or ((hasAuthority('ROLE_OPERATOR') or hasAuthority('ROLE_FARM_OWNER')) and @ownershipService.isFarmOwner(#farmId))")
    @GetMapping("/{id}")
    public ResponseEntity<MilkProductionResponseDTO> findById(
            @Parameter(description = "Identificador da fazenda") @PathVariable Long farmId,
            @Parameter(description = "Identificador da cabra") @PathVariable String goatId,
            @Parameter(description = "Identificador da produção de leite") @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                milkProductionMapper.toResponseDTO(
                        milkProductionUseCase.findById(farmId, goatId, id)
                )
        );
    }

    @Operation(summary = "Listar produções de leite (filtro opcional por data)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produções retornadas com sucesso."),
            @ApiResponse(responseCode = "400", description = "Parâmetros de filtro ou paginação inválidos."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para a fazenda informada.")
    })
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or ((hasAuthority('ROLE_OPERATOR') or hasAuthority('ROLE_FARM_OWNER')) and @ownershipService.isFarmOwner(#farmId))")
    @GetMapping
    public ResponseEntity<Page<MilkProductionResponseDTO>> getMilkProductions(
            @Parameter(description = "Identificador da fazenda") @PathVariable Long farmId,
            @Parameter(description = "Identificador da cabra") @PathVariable String goatId,
            @Parameter(description = "Data inicial (inclusiva) no formato ISO yyyy-MM-dd")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,
            @Parameter(description = "Data final (inclusiva) no formato ISO yyyy-MM-dd")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to,
            @Parameter(description = "Incluir registros cancelados")
            @RequestParam(required = false, defaultValue = "false")
            boolean includeCanceled,
            @ParameterObject
            @PageableDefault(size = 12)
            @SortDefault.SortDefaults({
                    @SortDefault(sort = "date", direction = Sort.Direction.DESC),
                    @SortDefault(sort = "shift", direction = Sort.Direction.ASC),
                    @SortDefault(sort = "id", direction = Sort.Direction.DESC)
            })
            Pageable pageable
    ) {
        Page<MilkProductionResponseVO> page =
                milkProductionUseCase.getMilkProductions(farmId, goatId, from, to, pageable, includeCanceled);

        Page<MilkProductionResponseDTO> dtoPage =
                page.map(milkProductionMapper::toResponseDTO);

        return ResponseEntity.ok(dtoPage);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or ((hasAuthority('ROLE_OPERATOR') or hasAuthority('ROLE_FARM_OWNER')) and @ownershipService.isFarmOwner(#farmId))")
    @DeleteMapping("/{id}")
    @Operation(summary = "Cancelar logicamente um registro de produção")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Produção cancelada com sucesso."),
            @ApiResponse(responseCode = "400", description = "Identificador de produção inválido."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para a fazenda informada."),
            @ApiResponse(responseCode = "404", description = "Produção não encontrada."),
            @ApiResponse(responseCode = "422", description = "Regra de negócio violada ao cancelar a produção.")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Identificador da fazenda") @PathVariable Long farmId,
            @Parameter(description = "Identificador da cabra") @PathVariable String goatId,
            @Parameter(description = "Identificador da produção de leite") @PathVariable Long id
    ) {
        milkProductionUseCase.delete(farmId, goatId, id);
        return ResponseEntity.noContent().build();
    }
}