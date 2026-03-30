package com.devmaster.goatfarm.milk.api.controller;

import com.devmaster.goatfarm.milk.api.dto.*;
import com.devmaster.goatfarm.milk.api.mapper.FarmMilkProductionMapper;
import com.devmaster.goatfarm.milk.application.ports.in.FarmMilkProductionUseCase;
import com.devmaster.goatfarm.milk.business.bo.FarmMilkProductionAnnualSummaryVO;
import com.devmaster.goatfarm.milk.business.bo.FarmMilkProductionDailySummaryVO;
import com.devmaster.goatfarm.milk.business.bo.FarmMilkProductionMonthlySummaryVO;
import com.devmaster.goatfarm.milk.business.bo.FarmMilkProductionUpsertRequestVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/goatfarms/{farmId}/milk-consolidated-productions")
@Tag(
        name = "Farm Milk Production API",
        description = "Registro consolidado diário de produção de leite da fazenda."
)
public class FarmMilkProductionController {

    private final FarmMilkProductionUseCase useCase;
    private final FarmMilkProductionMapper mapper;

    public FarmMilkProductionController(FarmMilkProductionUseCase useCase, FarmMilkProductionMapper mapper) {
        this.useCase = useCase;
        this.mapper = mapper;
    }

    @PreAuthorize("@ownershipService.canManageFarm(#farmId)")
    @PutMapping("/{productionDate}")
    @Operation(summary = "Criar ou atualizar o consolidado diário de leite da fazenda")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registro diario consolidado salvo com sucesso."),
            @ApiResponse(responseCode = "400", description = "Payload invalido ou data invalida."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para a fazenda informada."),
            @ApiResponse(responseCode = "404", description = "Fazenda nao encontrada."),
            @ApiResponse(responseCode = "422", description = "Regra de negocio violada ao salvar o consolidado.")
    })
    public ResponseEntity<FarmMilkProductionDailySummaryDTO> upsertDailyProduction(
            @Parameter(description = "Identificador da fazenda") @PathVariable Long farmId,
            @Parameter(description = "Data do registro consolidado no formato yyyy-MM-dd")
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate productionDate,
            @Valid @RequestBody FarmMilkProductionUpsertRequestDTO request
    ) {
        FarmMilkProductionUpsertRequestVO requestVO = mapper.toRequestVO(request);
        FarmMilkProductionDailySummaryVO response = useCase.upsertDailyProduction(farmId, productionDate, requestVO);
        return ResponseEntity.ok(mapper.toDailySummaryDTO(response));
    }

    @PreAuthorize("@ownershipService.canManageFarm(#farmId)")
    @GetMapping("/daily")
    @Operation(summary = "Consultar a visão diária consolidada da fazenda")
    public ResponseEntity<FarmMilkProductionDailySummaryDTO> getDailySummary(
            @PathVariable Long farmId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        FarmMilkProductionDailySummaryVO response = useCase.getDailySummary(farmId, date);
        return ResponseEntity.ok(mapper.toDailySummaryDTO(response));
    }

    @PreAuthorize("@ownershipService.canManageFarm(#farmId)")
    @GetMapping("/monthly")
    @Operation(summary = "Consultar a visão mensal consolidada da fazenda")
    public ResponseEntity<FarmMilkProductionMonthlySummaryDTO> getMonthlySummary(
            @PathVariable Long farmId,
            @RequestParam int year,
            @RequestParam int month
    ) {
        FarmMilkProductionMonthlySummaryVO response = useCase.getMonthlySummary(farmId, year, month);
        return ResponseEntity.ok(mapper.toMonthlySummaryDTO(response));
    }

    @PreAuthorize("@ownershipService.canManageFarm(#farmId)")
    @GetMapping("/annual")
    @Operation(summary = "Consultar a visão anual consolidada da fazenda")
    public ResponseEntity<FarmMilkProductionAnnualSummaryDTO> getAnnualSummary(
            @PathVariable Long farmId,
            @RequestParam int year
    ) {
        FarmMilkProductionAnnualSummaryVO response = useCase.getAnnualSummary(farmId, year);
        return ResponseEntity.ok(mapper.toAnnualSummaryDTO(response));
    }
}
