package com.devmaster.goatfarm.inventory.api.controller;

import com.devmaster.goatfarm.inventory.api.dto.InventoryBalanceResponseDTO;
import com.devmaster.goatfarm.inventory.api.mapper.InventoryBalanceApiMapper;
import com.devmaster.goatfarm.inventory.application.ports.in.InventoryBalanceQueryUseCase;
import com.devmaster.goatfarm.inventory.business.bo.InventoryBalanceFilterVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/goatfarms/{farmId}/inventory/balances", "/api/goatfarms/{farmId}/inventory/balances"})
@Tag(
        name = "Inventory Balance API",
        description = "Consulta paginada de saldos por item e lote. Caminho canônico /api/v1; legado /api em descontinuação."
)
public class InventoryBalanceController {

    private final InventoryBalanceQueryUseCase queryUseCase;
    private final InventoryBalanceApiMapper apiMapper;

    public InventoryBalanceController(
            InventoryBalanceQueryUseCase queryUseCase,
            InventoryBalanceApiMapper apiMapper
    ) {
        this.queryUseCase = queryUseCase;
        this.apiMapper = apiMapper;
    }

    @Operation(
            summary = "Listar saldos de estoque",
            description = "Retorna saldos paginados por item e lote, com filtros opcionais."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Saldos retornados com sucesso."),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos."),
            @ApiResponse(responseCode = "403", description = "Acesso negado."),
            @ApiResponse(responseCode = "404", description = "Fazenda não encontrada, quando aplicável.")
    })
    @PreAuthorize("@ownershipService.canManageFarm(#farmId)")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<InventoryBalanceResponseDTO>> listBalances(
            @Parameter(description = "Identificador da fazenda.")
            @PathVariable Long farmId,

            @Parameter(description = "Filtra por item específico.")
            @RequestParam(required = false) Long itemId,

            @Parameter(description = "Filtra por lote específico.")
            @RequestParam(required = false) Long lotId,

            @Parameter(description = "Quando true, retorna apenas itens ativos.", example = "true")
            @RequestParam(defaultValue = "true") boolean activeOnly,

            @PageableDefault(size = 20, sort = "itemId", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<InventoryBalanceResponseDTO> page = queryUseCase.listBalances(
                new InventoryBalanceFilterVO(farmId, itemId, lotId, activeOnly, pageable)
        ).map(apiMapper::toResponseDTO);

        return ResponseEntity.ok(page);
    }
}
