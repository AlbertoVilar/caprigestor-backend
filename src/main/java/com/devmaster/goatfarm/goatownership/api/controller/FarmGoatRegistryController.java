package com.devmaster.goatfarm.goatownership.api.controller;

import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.config.security.authorization.CanManageFarm;
import com.devmaster.goatfarm.goat.application.routing.GoatRouteIdentifier;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.api.dto.FarmGoatHistoricalDossierBasicResponseDTO;
import com.devmaster.goatfarm.goatownership.api.dto.FarmGoatHistoricalGenealogyResponseDTO;
import com.devmaster.goatfarm.goatownership.api.dto.FarmGoatHistoricalHealthResponseDTO;
import com.devmaster.goatfarm.goatownership.api.dto.FarmGoatHistoricalMilkLactationResponseDTO;
import com.devmaster.goatfarm.goatownership.api.dto.FarmGoatHistoricalEventsResponseDTO;
import com.devmaster.goatfarm.goatownership.api.dto.FarmGoatHistoricalReproductionResponseDTO;
import com.devmaster.goatfarm.goatownership.api.dto.FarmGoatRegistryResponseDTO;
import com.devmaster.goatfarm.goatownership.api.mapper.FarmGoatHistoricalGenealogyApiMapper;
import com.devmaster.goatfarm.goatownership.api.mapper.FarmGoatHistoricalHealthApiMapper;
import com.devmaster.goatfarm.goatownership.api.mapper.FarmGoatHistoricalMilkLactationApiMapper;
import com.devmaster.goatfarm.goatownership.api.mapper.FarmGoatHistoricalEventsApiMapper;
import com.devmaster.goatfarm.goatownership.api.mapper.FarmGoatHistoricalReproductionApiMapper;
import com.devmaster.goatfarm.goatownership.api.mapper.FarmGoatRegistryApiMapper;
import com.devmaster.goatfarm.goatownership.application.ports.in.FarmGoatHistoricalGenealogyQueryUseCase;
import com.devmaster.goatfarm.goatownership.application.ports.in.FarmGoatHistoricalHealthQueryUseCase;
import com.devmaster.goatfarm.goatownership.application.ports.in.FarmGoatHistoricalMilkLactationQueryUseCase;
import com.devmaster.goatfarm.goatownership.application.ports.in.FarmGoatHistoricalEventsQueryUseCase;
import com.devmaster.goatfarm.goatownership.application.ports.in.FarmGoatHistoricalReproductionQueryUseCase;
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
import org.springframework.web.bind.annotation.RequestParam;
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
    private final FarmGoatHistoricalGenealogyQueryUseCase historicalGenealogyQueryUseCase;
    private final FarmGoatHistoricalGenealogyApiMapper historicalGenealogyMapper;
    private final FarmGoatHistoricalMilkLactationQueryUseCase historicalMilkLactationQueryUseCase;
    private final FarmGoatHistoricalMilkLactationApiMapper historicalMilkLactationMapper;
    private final FarmGoatHistoricalReproductionQueryUseCase historicalReproductionQueryUseCase;
    private final FarmGoatHistoricalReproductionApiMapper historicalReproductionMapper;
    private final FarmGoatHistoricalHealthQueryUseCase historicalHealthQueryUseCase;
    private final FarmGoatHistoricalHealthApiMapper historicalHealthMapper;
    private final FarmGoatHistoricalEventsQueryUseCase historicalEventsQueryUseCase;
    private final FarmGoatHistoricalEventsApiMapper historicalEventsMapper;

    public FarmGoatRegistryController(
            FarmGoatRegistryQueryUseCase queryUseCase,
            FarmGoatRegistryApiMapper mapper,
            FarmGoatHistoricalGenealogyQueryUseCase historicalGenealogyQueryUseCase,
            FarmGoatHistoricalGenealogyApiMapper historicalGenealogyMapper,
            FarmGoatHistoricalMilkLactationQueryUseCase historicalMilkLactationQueryUseCase,
            FarmGoatHistoricalMilkLactationApiMapper historicalMilkLactationMapper,
            FarmGoatHistoricalReproductionQueryUseCase historicalReproductionQueryUseCase,
            FarmGoatHistoricalReproductionApiMapper historicalReproductionMapper,
            FarmGoatHistoricalHealthQueryUseCase historicalHealthQueryUseCase,
            FarmGoatHistoricalHealthApiMapper historicalHealthMapper,
            FarmGoatHistoricalEventsQueryUseCase historicalEventsQueryUseCase,
            FarmGoatHistoricalEventsApiMapper historicalEventsMapper
    ) {
        this.queryUseCase = queryUseCase;
        this.mapper = mapper;
        this.historicalGenealogyQueryUseCase = historicalGenealogyQueryUseCase;
        this.historicalGenealogyMapper = historicalGenealogyMapper;
        this.historicalMilkLactationQueryUseCase = historicalMilkLactationQueryUseCase;
        this.historicalMilkLactationMapper = historicalMilkLactationMapper;
        this.historicalReproductionQueryUseCase = historicalReproductionQueryUseCase;
        this.historicalReproductionMapper = historicalReproductionMapper;
        this.historicalHealthQueryUseCase = historicalHealthQueryUseCase;
        this.historicalHealthMapper = historicalHealthMapper;
        this.historicalEventsQueryUseCase = historicalEventsQueryUseCase;
        this.historicalEventsMapper = historicalEventsMapper;
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

    @GetMapping("/{goatId}/genealogy")
    @Operation(summary = "Consulta a genealogia histórica de uma cabra no livro de registro da fazenda")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Genealogia histórica retornada com sucesso."),
            @ApiResponse(responseCode = "400", description = "Identificador inválido ou inconsistência na propriedade canônica."),
            @ApiResponse(responseCode = "401", description = "Autenticação obrigatória."),
            @ApiResponse(responseCode = "403", description = "Sem permissão para administrar a fazenda informada."),
            @ApiResponse(responseCode = "404", description = "Animal não encontrado no livro de registro da fazenda.")
    })
    public ResponseEntity<FarmGoatHistoricalGenealogyResponseDTO> findHistoricalGenealogy(
            @Parameter(description = "Identificador da fazenda")
            @PathVariable("farmId") Long farmId,
            @Parameter(description = "Identificador estrutural da cabra no formato technical-{id}")
            @PathVariable("goatId") String goatIdToken,
            @Parameter(description = "Se true, enriquece a árvore local com dados públicos da ABCC")
            @RequestParam(name = "complementaryAbcc", defaultValue = "false") boolean complementaryAbcc
    ) {
        GoatId goatId = GoatRouteIdentifier.technicalId(goatIdToken)
                .orElseThrow(() -> new InvalidArgumentException("goatId", "goatId must use format 'technical-{id}' with a positive number"));

        return historicalGenealogyQueryUseCase.findHistoricalGenealogy(farmId, goatId, complementaryAbcc)
                .map(historicalGenealogyMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Animal não encontrado no livro de registro da fazenda"));
    }

    @GetMapping("/{goatId}/milk-lactation")
    @Operation(summary = "Consulta o histórico de lactações e produção de leite de uma cabra no livro de registro da fazenda")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Histórico de lactações e produção de leite retornado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Identificador inválido ou inconsistência na propriedade canônica."),
            @ApiResponse(responseCode = "401", description = "Autenticação obrigatória."),
            @ApiResponse(responseCode = "403", description = "Sem permissão para administrar a fazenda informada."),
            @ApiResponse(responseCode = "404", description = "Animal não encontrado no livro de registro da fazenda.")
    })
    public ResponseEntity<FarmGoatHistoricalMilkLactationResponseDTO> findHistoricalMilkLactation(
            @Parameter(description = "Identificador da fazenda")
            @PathVariable("farmId") Long farmId,
            @Parameter(description = "Identificador estrutural da cabra no formato technical-{id}")
            @PathVariable("goatId") String goatIdToken
    ) {
        GoatId goatId = GoatRouteIdentifier.technicalId(goatIdToken)
                .orElseThrow(() -> new InvalidArgumentException("goatId", "goatId must use format 'technical-{id}' with a positive number"));

        return historicalMilkLactationQueryUseCase.findHistoricalMilkLactation(farmId, goatId)
                .map(historicalMilkLactationMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Animal não encontrado no livro de registro da fazenda"));
    }

    @GetMapping("/{goatId}/reproduction")
    @Operation(summary = "Consulta o histórico reprodutivo de uma cabra no livro de registro da fazenda")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Histórico reprodutivo retornado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Identificador inválido ou inconsistência na propriedade canônica."),
            @ApiResponse(responseCode = "401", description = "Autenticação obrigatória."),
            @ApiResponse(responseCode = "403", description = "Sem permissão para administrar a fazenda informada."),
            @ApiResponse(responseCode = "404", description = "Animal não encontrado no livro de registro da fazenda.")
    })
    public ResponseEntity<FarmGoatHistoricalReproductionResponseDTO> findHistoricalReproduction(
            @Parameter(description = "Identificador da fazenda")
            @PathVariable("farmId") Long farmId,
            @Parameter(description = "Identificador estrutural da cabra no formato technical-{id}")
            @PathVariable("goatId") String goatIdToken
    ) {
        GoatId goatId = GoatRouteIdentifier.technicalId(goatIdToken)
                .orElseThrow(() -> new InvalidArgumentException("goatId", "goatId must use format 'technical-{id}' with a positive number"));

        return historicalReproductionQueryUseCase.findHistoricalReproduction(farmId, goatId)
                .map(historicalReproductionMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Animal não encontrado no livro de registro da fazenda"));
    }

    @GetMapping("/{goatId}/health")
    @Operation(summary = "Consulta o histórico sanitário de uma cabra no livro de registro da fazenda")
    public ResponseEntity<FarmGoatHistoricalHealthResponseDTO> findHistoricalHealth(
            @PathVariable("farmId") Long farmId,
            @PathVariable("goatId") String goatIdToken
    ) {
        GoatId goatId = parseTechnicalGoatId(goatIdToken);
        return historicalHealthQueryUseCase.findHistoricalHealth(farmId, goatId)
                .map(historicalHealthMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Animal não encontrado no livro de registro da fazenda"));
    }

    @GetMapping("/{goatId}/events")
    @Operation(summary = "Consulta o histórico de eventos de uma cabra no livro de registro da fazenda")
    public ResponseEntity<FarmGoatHistoricalEventsResponseDTO> findHistoricalEvents(
            @PathVariable("farmId") Long farmId,
            @PathVariable("goatId") String goatIdToken
    ) {
        GoatId goatId = parseTechnicalGoatId(goatIdToken);
        return historicalEventsQueryUseCase.findHistoricalEvents(farmId, goatId)
                .map(historicalEventsMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Animal não encontrado no livro de registro da fazenda"));
    }

    private GoatId parseTechnicalGoatId(String goatIdToken) {
        return GoatRouteIdentifier.technicalId(goatIdToken)
                .orElseThrow(() -> new InvalidArgumentException("goatId", "goatId must use format 'technical-{id}' with a positive number"));
    }
}
