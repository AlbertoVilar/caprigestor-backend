package com.devmaster.goatfarm.health.api.controller;

import com.devmaster.goatfarm.health.api.dto.*;
import com.devmaster.goatfarm.health.api.mapper.HealthEventApiMapper;
import com.devmaster.goatfarm.health.api.mapper.HealthWithdrawalApiMapper;
import com.devmaster.goatfarm.health.application.ports.in.HealthEventCommandUseCase;
import com.devmaster.goatfarm.health.application.ports.in.HealthEventQueryUseCase;
import com.devmaster.goatfarm.health.application.ports.in.HealthWithdrawalQueryUseCase;
import com.devmaster.goatfarm.health.business.bo.HealthEventResponseVO;
import com.devmaster.goatfarm.health.domain.enums.HealthEventStatus;
import com.devmaster.goatfarm.health.domain.enums.HealthEventType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping({"/api/v1/goatfarms/{farmId}/goats/{goatId}/health-events", "/api/goatfarms/{farmId}/goats/{goatId}/health-events"})
@Tag(
        name = "Health Events API",
        description = "Gestão de eventos sanitários por cabra. O caminho canônico é /api/v1; o legado /api segue ativo apenas durante a janela de descontinuação."
)
public class HealthEventController {

    private final HealthEventCommandUseCase commandUseCase;
    private final HealthEventQueryUseCase queryUseCase;
    private final HealthWithdrawalQueryUseCase withdrawalQueryUseCase;
    private final HealthEventApiMapper apiMapper;
    private final HealthWithdrawalApiMapper withdrawalApiMapper;

    public HealthEventController(
            HealthEventCommandUseCase commandUseCase,
            HealthEventQueryUseCase queryUseCase,
            HealthWithdrawalQueryUseCase withdrawalQueryUseCase,
            HealthEventApiMapper apiMapper,
            HealthWithdrawalApiMapper withdrawalApiMapper
    ) {
        this.commandUseCase = commandUseCase;
        this.queryUseCase = queryUseCase;
        this.withdrawalQueryUseCase = withdrawalQueryUseCase;
        this.apiMapper = apiMapper;
        this.withdrawalApiMapper = withdrawalApiMapper;
    }

    @Operation(summary = "Registrar evento de saúde", description = "Cria um evento agendado para a cabra informada.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Evento agendado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Payload inválido ou dados obrigatórios ausentes."),
            @ApiResponse(responseCode = "403", description = "Acesso negado."),
            @ApiResponse(responseCode = "404", description = "Cabra não encontrada para a fazenda informada."),
            @ApiResponse(responseCode = "422", description = "Regra de negócio violada ao agendar o evento.")
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
            @ApiResponse(responseCode = "200", description = "Evento atualizado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Payload inválido ou identificador inválido."),
            @ApiResponse(responseCode = "403", description = "Acesso negado."),
            @ApiResponse(responseCode = "404", description = "Evento não encontrado."),
            @ApiResponse(responseCode = "422", description = "Regra de negócio violada ao atualizar o evento.")
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
            @ApiResponse(responseCode = "200", description = "Evento marcado como realizado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Payload inválido ou identificador inválido."),
            @ApiResponse(responseCode = "403", description = "Acesso negado."),
            @ApiResponse(responseCode = "404", description = "Evento não encontrado."),
            @ApiResponse(responseCode = "422", description = "Regra de negócio violada ao concluir o evento.")
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
            @ApiResponse(responseCode = "200", description = "Evento cancelado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Payload inválido ou identificador inválido."),
            @ApiResponse(responseCode = "403", description = "Acesso negado."),
            @ApiResponse(responseCode = "404", description = "Evento não encontrado."),
            @ApiResponse(responseCode = "422", description = "Regra de negócio violada ao cancelar o evento.")
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

    @Operation(summary = "Reabrir evento", description = "Retorna um evento realizado ou cancelado para o status agendado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evento reaberto com sucesso."),
            @ApiResponse(responseCode = "403", description = "Acesso negado."),
            @ApiResponse(responseCode = "404", description = "Evento não encontrado."),
            @ApiResponse(responseCode = "422", description = "Regra de negócio violada ao reabrir o evento.")
    })
    @PatchMapping(value = "/{eventId}/reopen")
    @PreAuthorize("@ownershipService.canManageFarm(#farmId) and (hasRole('ADMIN') or hasRole('FARM_OWNER'))")
    public ResponseEntity<HealthEventResponseDTO> reopen(
            @PathVariable Long farmId,
            @PathVariable String goatId,
            @PathVariable Long eventId
    ) {
        HealthEventResponseVO reopened = commandUseCase.reopen(farmId, goatId, eventId);
        return ResponseEntity.ok(apiMapper.toDTO(reopened));
    }

    @Operation(summary = "Detalhar evento de saúde", description = "Retorna os dados do evento solicitado da cabra informada.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evento encontrado com sucesso."),
            @ApiResponse(responseCode = "403", description = "Acesso negado."),
            @ApiResponse(responseCode = "404", description = "Evento não encontrado.")
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
            @ApiResponse(responseCode = "200", description = "Página de eventos retornada com sucesso."),
            @ApiResponse(responseCode = "400", description = "Filtros ou paginação inválidos."),
            @ApiResponse(responseCode = "403", description = "Acesso negado.")
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

    @Operation(summary = "Consultar carencia sanitaria ativa", description = "Deriva o status de carencia ativa de leite e carne para a cabra informada.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status de carencia retornado com sucesso."),
            @ApiResponse(responseCode = "403", description = "Acesso negado."),
            @ApiResponse(responseCode = "404", description = "Cabra nao encontrada.")
    })
    @GetMapping("/withdrawal-status")
    @PreAuthorize("@ownershipService.canManageFarm(#farmId)")
    public ResponseEntity<GoatWithdrawalStatusDTO> getWithdrawalStatus(
            @PathVariable Long farmId,
            @PathVariable String goatId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate referenceDate
    ) {
        var status = withdrawalQueryUseCase.getGoatWithdrawalStatus(farmId, goatId, referenceDate);
        return ResponseEntity.ok(withdrawalApiMapper.toDTO(status));
    }
}
