package com.devmaster.goatfarm.inventory.api.controller;

import com.devmaster.goatfarm.inventory.api.dto.InventoryMovementCreateRequestDTO;
import com.devmaster.goatfarm.inventory.api.dto.InventoryMovementHistoryResponseDTO;
import com.devmaster.goatfarm.inventory.api.dto.InventoryMovementResponseDTO;
import com.devmaster.goatfarm.inventory.api.mapper.InventoryMovementApiMapper;
import com.devmaster.goatfarm.inventory.application.ports.in.InventoryMovementCommandUseCase;
import com.devmaster.goatfarm.inventory.application.ports.in.InventoryMovementQueryUseCase;
import com.devmaster.goatfarm.inventory.business.bo.InventoryMovementFilterVO;
import com.devmaster.goatfarm.inventory.domain.enums.InventoryMovementType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Validated
@RestController
@RequestMapping({"/api/v1/goatfarms/{farmId}/inventory/movements", "/api/goatfarms/{farmId}/inventory/movements"})
@Tag(
        name = "Inventory Movement API",
        description = "Registro e consulta de movimentos de estoque no ledger da fazenda. Caminho canônico /api/v1; legado /api em descontinuação."
)
public class InventoryMovementController {

    private final InventoryMovementCommandUseCase commandUseCase;
    private final InventoryMovementQueryUseCase queryUseCase;
    private final InventoryMovementApiMapper apiMapper;

    public InventoryMovementController(
            InventoryMovementCommandUseCase commandUseCase,
            InventoryMovementQueryUseCase queryUseCase,
            InventoryMovementApiMapper apiMapper
    ) {
        this.commandUseCase = commandUseCase;
        this.queryUseCase = queryUseCase;
        this.apiMapper = apiMapper;
    }

    @Operation(
            summary = "Listar histórico de movimentações",
            description = "Retorna o histórico paginado de movimentos, com filtros opcionais por item, lote, tipo e período."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Histórico retornado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos."),
            @ApiResponse(responseCode = "403", description = "Acesso negado."),
            @ApiResponse(responseCode = "404", description = "Fazenda não encontrada, quando aplicável.")
    })
    @PreAuthorize("@ownershipService.canManageFarm(#farmId)")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<InventoryMovementHistoryResponseDTO>> listMovements(
            @Parameter(description = "Identificador da fazenda.")
            @PathVariable Long farmId,

            @Parameter(description = "Filtra por item específico.")
            @RequestParam(required = false) Long itemId,

            @Parameter(description = "Filtra por lote específico.")
            @RequestParam(required = false) Long lotId,

            @Parameter(description = "Filtra por tipo de movimento.")
            @RequestParam(required = false) InventoryMovementType type,

            @Parameter(description = "Data inicial do período.", example = "2026-02-01")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,

            @Parameter(description = "Data final do período.", example = "2026-02-28")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate,

            @PageableDefault(size = 20)
            @SortDefault.SortDefaults({
                    @SortDefault(sort = "movementDate", direction = Sort.Direction.DESC),
                    @SortDefault(sort = "createdAt", direction = Sort.Direction.DESC)
            })
            Pageable pageable
    ) {
        Page<InventoryMovementHistoryResponseDTO> page = queryUseCase.listMovements(
                new InventoryMovementFilterVO(farmId, itemId, lotId, type, fromDate, toDate, pageable)
        ).map(apiMapper::toHistoryResponseDTO);

        return ResponseEntity.ok(page);
    }

    @Operation(
            summary = "Registrar movimentacao de estoque",
            description = "Cria um movimento do tipo IN, OUT ou ADJUST com idempotencia por header."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Replay idempotente retornado com sucesso."),
            @ApiResponse(responseCode = "201", description = "Movimento criado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Requisicao invalida."),
            @ApiResponse(responseCode = "404", description = "Item de estoque nao encontrado."),
            @ApiResponse(responseCode = "409", description = "Idempotency-Key ja usada com payload diferente."),
            @ApiResponse(
                    responseCode = "422",
                    description = "Regra de negocio violada (ex.: saldo insuficiente).",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "status": 422,
                                              "error": "Regra de negocio violada",
                                              "errors": [
                                                {
                                                  "fieldName": "quantity",
                                                  "message": "Saldo insuficiente para realizar a movimentacao."
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            )
    })
    @PreAuthorize("@ownershipService.canManageFarm(#farmId)")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<InventoryMovementResponseDTO> createMovement(
            @Parameter(description = "Identificador da fazenda.")
            @PathVariable Long farmId,

            @Parameter(description = "Chave de idempotencia obrigatoria para replay seguro.")
            @RequestHeader("Idempotency-Key")
            @NotBlank(message = "Idempotency-Key e obrigatorio.")
            String idempotencyKey,

            @Valid @RequestBody InventoryMovementCreateRequestDTO request
    ) {
        var requestVO = apiMapper.toRequestVO(request);
        var resultVO = commandUseCase.createMovement(farmId, idempotencyKey, requestVO);
        var status = resultVO.replayed() ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(apiMapper.toResponseDTO(resultVO.response()));
    }
}
