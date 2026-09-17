package com.devmaster.goatfarm.goatownership.api.controller;

import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.config.security.authorization.CanManageFarm;
import com.devmaster.goatfarm.goat.application.routing.GoatRouteIdentifier;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.api.dto.FarmGoatHistoricalDossierBasicResponseDTO;
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

    @GetMapping("/{goatId}")
    @Operation(summary = "Consulta o dossiê histórico básico de uma cabra no livro de registro da fazenda")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dossiê histórico básico retornado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Identificador inválido ou inconsistência na propriedade canônica."),
            @ApiResponse(responseCode = "401", description = "Autenticação obrigatória."),
            @ApiResponse(responseCode = "403", description = "Sem permissão para administrar a fazenda informada."),
            @ApiResponse(responseCode = "404", description = "Animal não encontrado no livro de registro da fazenda.")
    })
    public ResponseEntity<FarmGoatHistoricalDossierBasicResponseDTO> findHistoricalDossierBasic(
            @Parameter(description = "Identificador da fazenda")
            @PathVariable("farmId") Long farmId,
            @Parameter(description = "Identificador estrutural da cabra no formato technical-{id}")
            @PathVariable("goatId") String goatIdToken
    ) {
        GoatId goatId = GoatRouteIdentifier.technicalId(goatIdToken)
                .orElseThrow(() -> new InvalidArgumentException("goatId", "goatId must use format 'technical-{id}' with a positive number"));

        return queryUseCase.findHistoricalDossierBasic(farmId, goatId)
                .map(mapper::toDossierResponse)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Animal não encontrado no livro de registro da fazenda"));
    }
}
