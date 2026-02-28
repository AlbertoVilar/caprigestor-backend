package com.devmaster.goatfarm.inventory.api.controller;

import com.devmaster.goatfarm.inventory.api.dto.InventoryItemCreateRequestDTO;
import com.devmaster.goatfarm.inventory.api.dto.InventoryItemResponseDTO;
import com.devmaster.goatfarm.inventory.api.mapper.InventoryItemApiMapper;
import com.devmaster.goatfarm.inventory.application.ports.in.InventoryItemCommandUseCase;
import com.devmaster.goatfarm.inventory.application.ports.in.InventoryItemQueryUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/goatfarms/{farmId}/inventory/items", "/api/goatfarms/{farmId}/inventory/items"})
@Tag(
        name = "Inventory Items API",
        description = "Cadastro e listagem de itens de estoque da fazenda. Caminho canônico /api/v1; legado /api em descontinuação."
)
public class InventoryItemController {

    private final InventoryItemCommandUseCase commandUseCase;
    private final InventoryItemQueryUseCase queryUseCase;
    private final InventoryItemApiMapper apiMapper;

    public InventoryItemController(
            InventoryItemCommandUseCase commandUseCase,
            InventoryItemQueryUseCase queryUseCase,
            InventoryItemApiMapper apiMapper
    ) {
        this.commandUseCase = commandUseCase;
        this.queryUseCase = queryUseCase;
        this.apiMapper = apiMapper;
    }

    @Operation(
            summary = "Cadastrar item de estoque",
            description = "Cria um novo item de estoque para a fazenda, com verificação de unicidade por nome normalizado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Item criado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Payload inválido."),
            @ApiResponse(responseCode = "403", description = "Acesso negado."),
            @ApiResponse(
                    responseCode = "409",
                    description = "Já existe um item com o mesmo nome normalizado.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "status": 409,
                                              "error": "Conflito de dados",
                                              "errors": [
                                                {
                                                  "fieldName": "name",
                                                  "message": "Já existe um item de estoque com esse nome nesta fazenda."
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
    public ResponseEntity<InventoryItemResponseDTO> createItem(
            @Parameter(description = "Identificador da fazenda.")
            @PathVariable Long farmId,
            @RequestBody InventoryItemCreateRequestDTO request
    ) {
        var requestVO = apiMapper.toCreateRequestVO(request);
        var responseVO = commandUseCase.createItem(farmId, requestVO);
        return ResponseEntity.status(HttpStatus.CREATED).body(apiMapper.toResponseDTO(responseVO));
    }

    @Operation(
            summary = "Listar itens de estoque",
            description = "Retorna a lista paginada de itens de estoque da fazenda."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Itens retornados com sucesso."),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos."),
            @ApiResponse(responseCode = "403", description = "Acesso negado.")
    })
    @PreAuthorize("@ownershipService.canManageFarm(#farmId)")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<InventoryItemResponseDTO>> listItems(
            @Parameter(description = "Identificador da fazenda.")
            @PathVariable Long farmId,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<InventoryItemResponseDTO> page = queryUseCase.listItems(farmId, pageable)
                .map(apiMapper::toResponseDTO);
        return ResponseEntity.ok(page);
    }
}
