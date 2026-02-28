package com.devmaster.goatfarm.health.api.controller;

import com.devmaster.goatfarm.health.api.dto.FarmHealthAlertsResponseDTO;
import com.devmaster.goatfarm.health.api.dto.HealthEventResponseDTO;
import com.devmaster.goatfarm.health.api.mapper.FarmHealthAlertsApiMapper;
import com.devmaster.goatfarm.health.api.mapper.HealthEventApiMapper;
import com.devmaster.goatfarm.health.application.ports.in.FarmHealthAlertsQueryUseCase;
import com.devmaster.goatfarm.health.application.ports.in.HealthEventQueryUseCase;
import com.devmaster.goatfarm.health.business.bo.FarmHealthAlertsResponseVO;
import com.devmaster.goatfarm.health.business.bo.HealthEventResponseVO;
import com.devmaster.goatfarm.health.domain.enums.HealthEventStatus;
import com.devmaster.goatfarm.health.domain.enums.HealthEventType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping({"/api/v1/goatfarms/{farmId}/health-events", "/api/goatfarms/{farmId}/health-events"})
@Tag(
        name = "Farm Health API",
        description = "Consultas agregadas de saúde por fazenda. O caminho canônico é /api/v1; o legado /api segue ativo apenas durante a janela de descontinuação."
)
public class FarmHealthEventController {

    private final HealthEventQueryUseCase queryUseCase;
    private final FarmHealthAlertsQueryUseCase alertsQueryUseCase;
    private final HealthEventApiMapper apiMapper;
    private final FarmHealthAlertsApiMapper alertsMapper;

    public FarmHealthEventController(
            HealthEventQueryUseCase queryUseCase,
            FarmHealthAlertsQueryUseCase alertsQueryUseCase,
            HealthEventApiMapper apiMapper,
            FarmHealthAlertsApiMapper alertsMapper
    ) {
        this.queryUseCase = queryUseCase;
        this.alertsQueryUseCase = alertsQueryUseCase;
        this.apiMapper = apiMapper;
        this.alertsMapper = alertsMapper;
    }

    @Operation(summary = "Calendário sanitário da fazenda", description = "Lista eventos por período, tipo e status de saúde da fazenda.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Calendário retornado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Filtros ou paginação inválidos."),
            @ApiResponse(responseCode = "401", description = "Não autenticado."),
            @ApiResponse(responseCode = "403", description = "Acesso negado.")
    })
    @GetMapping(value = "/calendar", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("@ownershipService.canManageFarm(#farmId)")
    public ResponseEntity<Page<HealthEventResponseDTO>> listCalendar(
            @PathVariable Long farmId,
            @Parameter(description = "Data inicial (ISO)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Data final (ISO)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @Parameter(description = "Filtrar por tipo")
            @RequestParam(required = false) HealthEventType type,
            @Parameter(description = "Filtrar por status")
            @RequestParam(required = false) HealthEventStatus status,
            Pageable pageable
    ) {
        Page<HealthEventResponseVO> pageVO = queryUseCase.listCalendar(farmId, from, to, type, status, pageable);
        return ResponseEntity.ok(pageVO.map(apiMapper::toDTO));
    }

    @Operation(summary = "Alertas do aplicativo", description = "Retorna contadores e top 5 para hoje, próximos dias e atrasados.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alertas calculados com sucesso."),
            @ApiResponse(responseCode = "400", description = "Janela de dias inválida."),
            @ApiResponse(responseCode = "401", description = "Não autenticado."),
            @ApiResponse(responseCode = "403", description = "Acesso negado.")
    })
    @GetMapping(value = "/alerts", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("@ownershipService.canManageFarm(#farmId)")
    public ResponseEntity<FarmHealthAlertsResponseDTO> getAlerts(
            @PathVariable Long farmId,
            @Parameter(description = "Janela de dias para próximos alertas (default 7, máximo 30)")
            @RequestParam(required = false, defaultValue = "7") Integer windowDays
    ) {
        FarmHealthAlertsResponseVO responseVO = alertsQueryUseCase.getAlerts(farmId, windowDays);
        return ResponseEntity.ok(alertsMapper.toDTO(responseVO));
    }
}