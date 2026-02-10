package com.devmaster.goatfarm.milk.api.controller;

import com.devmaster.goatfarm.milk.api.dto.LactationDryOffAlertItemDTO;
import com.devmaster.goatfarm.milk.api.dto.LactationDryOffAlertResponseDTO;
import com.devmaster.goatfarm.milk.api.mapper.LactationMapper;
import com.devmaster.goatfarm.milk.application.ports.in.LactationQueryUseCase;
import com.devmaster.goatfarm.milk.business.bo.LactationDryOffAlertVO;
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
@RequestMapping("/api/goatfarms/{farmId}/milk/alerts")
@PreAuthorize("@ownershipService.canManageFarm(#farmId)")
public class FarmMilkAlertsController {

    private final LactationQueryUseCase lactationQueryUseCase;
    private final LactationMapper lactationMapper;

    public FarmMilkAlertsController(LactationQueryUseCase lactationQueryUseCase, LactationMapper lactationMapper) {
        this.lactationQueryUseCase = lactationQueryUseCase;
        this.lactationMapper = lactationMapper;
    }

    @GetMapping("/dry-off")
    @Operation(
            summary = "Listar alertas agregados de secagem recomendada por fazenda",
            description = "Retorna alertas de secagem para lactacoes ativas com prenhez ativa na data de referencia e limiar de secagem atingido."
    )
    public ResponseEntity<LactationDryOffAlertResponseDTO> getDryOffAlerts(
            @Parameter(description = "Identificador da fazenda") @PathVariable Long farmId,
            @Parameter(description = "Data de referencia (ISO)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate referenceDate,
            @Parameter(description = "Indice da pagina (base 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da pagina")
            @RequestParam(defaultValue = "20") int size
    ) {
        LocalDate effectiveReferenceDate = referenceDate != null ? referenceDate : LocalDate.now();
        Pageable pageable = PageRequest.of(page, size);
        Page<LactationDryOffAlertVO> alertsPage = lactationQueryUseCase.getDryOffAlerts(farmId, effectiveReferenceDate, pageable);

        List<LactationDryOffAlertItemDTO> alerts = alertsPage.getContent().stream()
                .map(lactationMapper::toDryOffAlertItemDTO)
                .toList();

        return ResponseEntity.ok(LactationDryOffAlertResponseDTO.builder()
                .totalPending(alertsPage.getTotalElements())
                .alerts(alerts)
                .build());
    }
}
