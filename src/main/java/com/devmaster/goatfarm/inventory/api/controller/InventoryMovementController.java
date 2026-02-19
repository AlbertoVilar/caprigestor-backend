package com.devmaster.goatfarm.inventory.api.controller;

import com.devmaster.goatfarm.inventory.api.dto.InventoryMovementCreateRequestDTO;
import com.devmaster.goatfarm.inventory.api.dto.InventoryMovementResponseDTO;
import com.devmaster.goatfarm.inventory.api.mapper.InventoryMovementApiMapper;
import com.devmaster.goatfarm.inventory.application.ports.in.InventoryMovementCommandUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/goatfarms/{farmId}/inventory/movements")
@Tag(
        name = "Inventory Movement API",
        description = "Registro de movimentos de estoque no ledger da fazenda."
)
public class InventoryMovementController {

    private final InventoryMovementCommandUseCase commandUseCase;
    private final InventoryMovementApiMapper apiMapper;

    public InventoryMovementController(
            InventoryMovementCommandUseCase commandUseCase,
            InventoryMovementApiMapper apiMapper
    ) {
        this.commandUseCase = commandUseCase;
        this.apiMapper = apiMapper;
    }

    @Operation(
            summary = "Registrar movimentacao de estoque",
            description = "Cria um movimento do tipo IN, OUT ou ADJUST com idempotencia por header."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Movimento registrado com sucesso."),
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
        var responseVO = commandUseCase.createMovement(farmId, idempotencyKey, requestVO);
        return ResponseEntity.status(HttpStatus.CREATED).body(apiMapper.toResponseDTO(responseVO));
    }
}
