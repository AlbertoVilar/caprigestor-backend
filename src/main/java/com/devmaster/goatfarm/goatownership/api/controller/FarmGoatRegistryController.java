package com.devmaster.goatfarm.goatownership.api.controller;

import com.devmaster.goatfarm.config.security.authorization.CanManageFarm;
import com.devmaster.goatfarm.goatownership.api.dto.FarmGoatRegistryResponseDTO;
import com.devmaster.goatfarm.goatownership.api.mapper.FarmGoatRegistryApiMapper;
import com.devmaster.goatfarm.goatownership.application.ports.in.FarmGoatRegistryQueryUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** HTTP adapter for the farm goat registry read model. */
@RestController
@RequestMapping("/api/v1/goatfarms/{farmId}/goat-registry")
@CanManageFarm
@Tag(name = "Farm Goat Registry API", description = "Livro de registro de cabras da fazenda.")
public class FarmGoatRegistryController {

    private final FarmGoatRegistryQueryUseCase queryUseCase;
    private final FarmGoatRegistryApiMapper mapper;

    public FarmGoatRegistryController(
            FarmGoatRegistryQueryUseCase queryUseCase,
            FarmGoatRegistryApiMapper mapper
    ) {
        this.queryUseCase = queryUseCase;
        this.mapper = mapper;
    }

    @GetMapping
    @Operation(summary = "Consulta o livro de registro de cabras da fazenda")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Livro de registro retornado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Requisição inválida ou inconsistência na propriedade canônica."),
            @ApiResponse(responseCode = "401", description = "Autenticação obrigatória."),
            @ApiResponse(responseCode = "403", description = "Sem permissão para administrar a fazenda informada.")
    })
    public ResponseEntity<List<FarmGoatRegistryResponseDTO>> findForFarm(
            @Parameter(description = "Identificador da fazenda")
            @PathVariable("farmId") Long farmId
    ) {
        return ResponseEntity.ok(mapper.toResponseList(queryUseCase.findForFarm(farmId)));
    }
}
