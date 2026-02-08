package com.devmaster.goatfarm.reproduction.api.controller;

import com.devmaster.goatfarm.reproduction.api.dto.PregnancyDiagnosisAlertItemDTO;
import com.devmaster.goatfarm.reproduction.api.dto.PregnancyDiagnosisAlertResponseDTO;
import com.devmaster.goatfarm.reproduction.api.mapper.ReproductionMapper;
import com.devmaster.goatfarm.reproduction.application.ports.in.ReproductionQueryUseCase;
import com.devmaster.goatfarm.reproduction.business.bo.PregnancyDiagnosisAlertVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/goatfarms/{farmId}/reproduction/alerts")
@PreAuthorize("hasAuthority('ROLE_ADMIN') or ((hasAuthority('ROLE_OPERATOR') or hasAuthority('ROLE_FARM_OWNER')) and @ownershipService.isFarmOwner(#farmId))")
public class FarmReproductionAlertsController {

    private final ReproductionQueryUseCase queryUseCase;
    private final ReproductionMapper mapper;

    public FarmReproductionAlertsController(ReproductionQueryUseCase queryUseCase, ReproductionMapper mapper) {
        this.queryUseCase = queryUseCase;
        this.mapper = mapper;
    }

    @GetMapping("/pregnancy-diagnosis")
    @Operation(summary = "Listar alertas agregados de diagnostico de prenhez pendente por fazenda")
    public ResponseEntity<PregnancyDiagnosisAlertResponseDTO> getPendingPregnancyDiagnosisAlerts(
            @Parameter(description = "Identificador da fazenda") @PathVariable Long farmId,
            @Parameter(description = "Data de referencia (ISO)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate referenceDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PregnancyDiagnosisAlertVO> alertsPage = queryUseCase
                .getPendingPregnancyDiagnosisAlerts(farmId, referenceDate, pageable);

        List<PregnancyDiagnosisAlertItemDTO> alerts = alertsPage.getContent().stream()
                .map(mapper::toPregnancyDiagnosisAlertItemDTO)
                .toList();

        return ResponseEntity.ok(PregnancyDiagnosisAlertResponseDTO.builder()
                .totalPending(alertsPage.getTotalElements())
                .alerts(alerts)
                .build());
    }
}
