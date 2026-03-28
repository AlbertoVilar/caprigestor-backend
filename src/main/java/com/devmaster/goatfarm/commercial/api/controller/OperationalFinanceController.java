package com.devmaster.goatfarm.commercial.api.controller;

import com.devmaster.goatfarm.commercial.api.dto.MonthlyOperationalSummaryDTO;
import com.devmaster.goatfarm.commercial.api.dto.OperationalExpenseRequestDTO;
import com.devmaster.goatfarm.commercial.api.dto.OperationalExpenseResponseDTO;
import com.devmaster.goatfarm.commercial.api.mapper.OperationalFinanceApiMapper;
import com.devmaster.goatfarm.commercial.application.ports.in.OperationalFinanceUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/goatfarms/{farmId}/commercial", "/api/goatfarms/{farmId}/commercial"})
@Tag(name = "Operational Finance API", description = "Financeiro operacional minimo por fazenda.")
public class OperationalFinanceController {

    private final OperationalFinanceUseCase operationalFinanceUseCase;
    private final OperationalFinanceApiMapper apiMapper;

    public OperationalFinanceController(
            OperationalFinanceUseCase operationalFinanceUseCase,
            OperationalFinanceApiMapper apiMapper
    ) {
        this.operationalFinanceUseCase = operationalFinanceUseCase;
        this.apiMapper = apiMapper;
    }

    @PreAuthorize("@ownershipService.canManageFarm(#farmId)")
    @PostMapping("/operational-expenses")
    @Operation(summary = "Registrar despesa operacional da fazenda")
    public ResponseEntity<OperationalExpenseResponseDTO> createOperationalExpense(
            @PathVariable Long farmId,
            @Valid @RequestBody OperationalExpenseRequestDTO requestDTO
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(apiMapper.toDTO(operationalFinanceUseCase.createOperationalExpense(farmId, apiMapper.toVO(requestDTO))));
    }

    @PreAuthorize("@ownershipService.canManageFarm(#farmId)")
    @GetMapping("/operational-expenses")
    @Operation(summary = "Listar despesas operacionais da fazenda")
    public ResponseEntity<List<OperationalExpenseResponseDTO>> listOperationalExpenses(@PathVariable Long farmId) {
        return ResponseEntity.ok(
                operationalFinanceUseCase.listOperationalExpenses(farmId).stream().map(apiMapper::toDTO).toList()
        );
    }

    @PreAuthorize("@ownershipService.canManageFarm(#farmId)")
    @GetMapping("/monthly-summary")
    @Operation(summary = "Resumo mensal simples da fazenda")
    public ResponseEntity<MonthlyOperationalSummaryDTO> getMonthlySummary(
            @PathVariable Long farmId,
            @RequestParam int year,
            @RequestParam int month
    ) {
        return ResponseEntity.ok(apiMapper.toDTO(operationalFinanceUseCase.getMonthlySummary(farmId, year, month)));
    }
}
