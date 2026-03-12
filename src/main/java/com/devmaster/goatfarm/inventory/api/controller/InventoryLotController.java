package com.devmaster.goatfarm.inventory.api.controller;

import com.devmaster.goatfarm.inventory.api.dto.InventoryLotActivationRequestDTO;
import com.devmaster.goatfarm.inventory.api.dto.InventoryLotCreateRequestDTO;
import com.devmaster.goatfarm.inventory.api.dto.InventoryLotResponseDTO;
import com.devmaster.goatfarm.inventory.api.mapper.InventoryLotApiMapper;
import com.devmaster.goatfarm.inventory.application.ports.in.InventoryLotCommandUseCase;
import com.devmaster.goatfarm.inventory.application.ports.in.InventoryLotQueryUseCase;
import com.devmaster.goatfarm.inventory.business.bo.InventoryLotFilterVO;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/goatfarms/{farmId}/inventory/lots", "/api/goatfarms/{farmId}/inventory/lots"})
@Tag(
        name = "Inventory Lots API",
        description = "Cadastro, listagem e ativação de lotes reais de estoque. Caminho canônico /api/v1; legado /api em descontinuação."
)
public class InventoryLotController {

    private final InventoryLotCommandUseCase commandUseCase;
    private final InventoryLotQueryUseCase queryUseCase;
    private final InventoryLotApiMapper apiMapper;

    public InventoryLotController(
            InventoryLotCommandUseCase commandUseCase,
            InventoryLotQueryUseCase queryUseCase,
            InventoryLotApiMapper apiMapper
    ) {
        this.commandUseCase = commandUseCase;
        this.queryUseCase = queryUseCase;
        this.apiMapper = apiMapper;
    }

    @Operation(
            summary = "Cadastrar lote de estoque",
            description = "Cria um lote real vinculado à fazenda e ao item de estoque informado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Lote criado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Payload inválido."),
            @ApiResponse(responseCode = "403", description = "Acesso negado."),
            @ApiResponse(responseCode = "404", description = "Item de estoque não encontrado."),
            @ApiResponse(responseCode = "409", description = "Já existe lote com o mesmo código para o item.")
    })
    @PreAuthorize("@ownershipService.canManageFarm(#farmId)")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<InventoryLotResponseDTO> createLot(
            @Parameter(description = "Identificador da fazenda.")
            @PathVariable Long farmId,
            @Valid @RequestBody InventoryLotCreateRequestDTO request
    ) {
        var requestVO = apiMapper.toCreateRequestVO(request);
        var responseVO = commandUseCase.createLot(farmId, requestVO);
        return ResponseEntity.status(HttpStatus.CREATED).body(apiMapper.toResponseDTO(responseVO));
    }

    @Operation(
            summary = "Listar lotes de estoque",
            description = "Retorna os lotes da fazenda, com filtro opcional por item e situação ativa."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lotes retornados com sucesso."),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos."),
            @ApiResponse(responseCode = "403", description = "Acesso negado.")
    })
    @PreAuthorize("@ownershipService.canManageFarm(#farmId)")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<InventoryLotResponseDTO>> listLots(
            @Parameter(description = "Identificador da fazenda.")
            @PathVariable Long farmId,

            @Parameter(description = "Filtra por item específico.")
            @RequestParam(required = false) Long itemId,

            @Parameter(description = "Filtra lotes por situação ativa.")
            @RequestParam(required = false) Boolean active,

            @PageableDefault(size = 20, sort = "code", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<InventoryLotResponseDTO> page = queryUseCase.listLots(
                new InventoryLotFilterVO(farmId, itemId, active, pageable)
        ).map(apiMapper::toResponseDTO);

        return ResponseEntity.ok(page);
    }

    @Operation(
            summary = "Ativar ou inativar lote",
            description = "Atualiza a situação ativa do lote informado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Situação do lote atualizada com sucesso."),
            @ApiResponse(responseCode = "400", description = "Payload inválido."),
            @ApiResponse(responseCode = "403", description = "Acesso negado."),
            @ApiResponse(responseCode = "404", description = "Lote não encontrado.")
    })
    @PreAuthorize("@ownershipService.canManageFarm(#farmId)")
    @PatchMapping(path = "/{lotId}/active", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<InventoryLotResponseDTO> updateLotActive(
            @Parameter(description = "Identificador da fazenda.")
            @PathVariable Long farmId,

            @Parameter(description = "Identificador do lote.")
            @PathVariable Long lotId,

            @Valid @RequestBody InventoryLotActivationRequestDTO request
    ) {
        var requestVO = apiMapper.toActivationRequestVO(request);
        var responseVO = commandUseCase.updateLotActive(farmId, lotId, requestVO);
        return ResponseEntity.ok(apiMapper.toResponseDTO(responseVO));
    }
}
