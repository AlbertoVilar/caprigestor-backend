package com.devmaster.goatfarm.health.api.controller;

import com.devmaster.goatfarm.health.api.dto.*;
import com.devmaster.goatfarm.health.api.mapper.HealthEventApiMapper;
import com.devmaster.goatfarm.health.application.ports.in.HealthEventCommandUseCase;
import com.devmaster.goatfarm.health.application.ports.in.HealthEventQueryUseCase;
import com.devmaster.goatfarm.health.business.bo.HealthEventResponseVO;
import com.devmaster.goatfarm.health.domain.enums.HealthEventStatus;
import com.devmaster.goatfarm.health.domain.enums.HealthEventType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/goatfarms/{farmId}/goats/{goatId}/health-events")
public class HealthEventController {

    private final HealthEventCommandUseCase commandUseCase;
    private final HealthEventQueryUseCase queryUseCase;
    private final HealthEventApiMapper apiMapper;

    public HealthEventController(
            HealthEventCommandUseCase commandUseCase,
            HealthEventQueryUseCase queryUseCase,
            HealthEventApiMapper apiMapper
    ) {
        this.commandUseCase = commandUseCase;
        this.queryUseCase = queryUseCase;
        this.apiMapper = apiMapper;
    }

    @Operation(summary = "Registrar evento de saúde", description = "Cria um evento agendado para a cabra informada.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Evento agendado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("@ownershipService.canManageFarm(#farmId)")
    public ResponseEntity<HealthEventResponseDTO> create(
            @PathVariable Long farmId,
            @PathVariable String goatId,
            @RequestBody @Valid HealthEventCreateRequestDTO request
    ) {
        var vo = apiMapper.toCreateVO(request);
        HealthEventResponseVO created = commandUseCase.create(farmId, goatId, vo);
        return ResponseEntity.status(HttpStatus.CREATED).body(apiMapper.toDTO(created));
    }

    @Operation(summary = "Atualizar evento de saúde", description = "Edita os dados de um evento que ainda está agendado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evento atualizado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @PutMapping(value = "/{eventId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("@ownershipService.canManageFarm(#farmId)")
    public ResponseEntity<HealthEventResponseDTO> update(
            @PathVariable Long farmId,
            @PathVariable String goatId,
            @PathVariable Long eventId,
            @RequestBody @Valid HealthEventUpdateRequestDTO request
    ) {
        var vo = apiMapper.toUpdateVO(request);
        HealthEventResponseVO updated = commandUseCase.update(farmId, goatId, eventId, vo);
        return ResponseEntity.ok(apiMapper.toDTO(updated));
    }

    @Operation(summary = "Marcar como realizado", description = "Registra a conclusão do evento com responsáveis e notas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evento marcado como realizado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @PatchMapping(value = "/{eventId}/done", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("@ownershipService.canManageFarm(#farmId)")
    public ResponseEntity<HealthEventResponseDTO> markAsDone(
            @PathVariable Long farmId,
            @PathVariable String goatId,
            @PathVariable Long eventId,
            @RequestBody @Valid HealthEventDoneRequestDTO request
    ) {
        var vo = apiMapper.toDoneVO(request);
        HealthEventResponseVO done = commandUseCase.markAsDone(farmId, goatId, eventId, vo);
        return ResponseEntity.ok(apiMapper.toDTO(done));
    }

    @Operation(summary = "Cancelar evento", description = "Registra o cancelamento de um evento ainda agendado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evento cancelado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @PatchMapping(value = "/{eventId}/cancel", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("@ownershipService.canManageFarm(#farmId)")
    public ResponseEntity<HealthEventResponseDTO> cancel(
            @PathVariable Long farmId,
            @PathVariable String goatId,
            @PathVariable Long eventId,
            @RequestBody @Valid HealthEventCancelRequestDTO request
    ) {
        var vo = apiMapper.toCancelVO(request);
        HealthEventResponseVO canceled = commandUseCase.cancel(farmId, goatId, eventId, vo);
        return ResponseEntity.ok(apiMapper.toDTO(canceled));
    }

    @Operation(summary = "Detalhar evento de saúde", description = "Retorna os dados do evento solicitado da cabra informada.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evento encontrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @GetMapping("/{eventId}")
    @PreAuthorize("@ownershipService.canManageFarm(#farmId)")
    public ResponseEntity<HealthEventResponseDTO> getById(
            @PathVariable Long farmId,
            @PathVariable String goatId,
            @PathVariable Long eventId
    ) {
        HealthEventResponseVO found = queryUseCase.getById(farmId, goatId, eventId);
        return ResponseEntity.ok(apiMapper.toDTO(found));
    }

    @Operation(summary = "Listar eventos da cabra", description = "Busca eventos sanitários da cabra por período, tipo e status.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de eventos retornada"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @GetMapping
    @PreAuthorize("@ownershipService.canManageFarm(#farmId)")
    public ResponseEntity<Page<HealthEventResponseDTO>> listByGoat(
            @PathVariable Long farmId,
            @PathVariable String goatId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) HealthEventType type,
            @RequestParam(required = false) HealthEventStatus status,
            Pageable pageable
    ) {
        Page<HealthEventResponseVO> page = queryUseCase.listByGoat(farmId, goatId, from, to, type, status, pageable);
        return ResponseEntity.ok(page.map(apiMapper::toDTO));
    }
}
