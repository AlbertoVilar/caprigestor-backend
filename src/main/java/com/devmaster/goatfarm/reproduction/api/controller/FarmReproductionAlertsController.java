package com.devmaster.goatfarm.reproduction.api.controller;

import com.devmaster.goatfarm.reproduction.api.dto.PregnancyDiagnosisAlertItemDTO;
import com.devmaster.goatfarm.reproduction.api.dto.PregnancyDiagnosisAlertResponseDTO;
import com.devmaster.goatfarm.reproduction.api.dto.PregnancyDueAlertItemDTO;
import com.devmaster.goatfarm.reproduction.api.dto.PregnancyDueAlertResponseDTO;
import com.devmaster.goatfarm.reproduction.api.mapper.ReproductionMapper;
import com.devmaster.goatfarm.reproduction.application.ports.in.ReproductionQueryUseCase;
import com.devmaster.goatfarm.reproduction.business.bo.PregnancyDiagnosisAlertVO;
import com.devmaster.goatfarm.reproduction.business.bo.PregnancyDueAlertVO;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/v1/goatfarms/{farmId}/reproduction/alerts")
@PreAuthorize("@ownershipService.canManageFarm(#farmId)")
@Tag(
        name = "Reproduction Alerts API",
        description = "Alertas reprodutivos agregados por fazenda. O caminho canônico é /api/v1; o legado /api segue ativo apenas durante a janela de descontinuação."
)
public class FarmReproductionAlertsController {

    private static final int MAX_ALERT_PAGE_SIZE = 100;

    private final ReproductionQueryUseCase queryUseCase;
    private final ReproductionMapper mapper;

    public FarmReproductionAlertsController(ReproductionQueryUseCase queryUseCase, ReproductionMapper mapper) {
        this.queryUseCase = queryUseCase;
        this.mapper = mapper;
    }

    @GetMapping("/pregnancy-diagnosis")
    @Operation(summary = "Listar alertas agregados de diagnóstico de prenhez pendente por fazenda")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alertas retornados com sucesso."),
            @ApiResponse(responseCode = "400", description = "Parâmetros de paginação ou data de referência inválidos."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para a fazenda informada.")
    })
    public ResponseEntity<PregnancyDiagnosisAlertResponseDTO> getPendingPregnancyDiagnosisAlerts(
            @Parameter(description = "Identificador da fazenda") @PathVariable Long farmId,
            @Parameter(description = "Data de referência (ISO)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate referenceDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = createPageable(page, size);
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

    @GetMapping("/births-due")
    @Operation(summary = "Listar alertas de parto previsto ou atrasado por fazenda")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alertas retornados com sucesso."),
            @ApiResponse(responseCode = "400", description = "Parâmetros de paginação ou data de referência inválidos."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para a fazenda informada.")
    })
    public ResponseEntity<PregnancyDueAlertResponseDTO> getPendingBirthAlerts(
            @Parameter(description = "Identificador da fazenda") @PathVariable Long farmId,
            @Parameter(description = "Data de referência (ISO)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate referenceDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = createPageable(page, size);
        Page<PregnancyDueAlertVO> alertsPage = queryUseCase
                .getPendingBirthAlerts(farmId, referenceDate, pageable);

        List<PregnancyDueAlertItemDTO> alerts = alertsPage.getContent().stream()
                .map(mapper::toPregnancyDueAlertItemDTO)
                .toList();

        return ResponseEntity.ok(PregnancyDueAlertResponseDTO.builder()
                .totalPending(alertsPage.getTotalElements())
                .alerts(alerts)
                .build());
    }

    private Pageable createPageable(int page, int size) {
        if (page < 0) {
            throw new InvalidArgumentException("page", "Página deve ser maior ou igual a zero");
        }
        if (size < 1 || size > MAX_ALERT_PAGE_SIZE) {
            throw new InvalidArgumentException(
                    "size",
                    "Tamanho da página deve estar entre 1 e " + MAX_ALERT_PAGE_SIZE
            );
        }
        return PageRequest.of(page, size);
    }
}
