package com.devmaster.goatfarm.health.api.controller;

import com.devmaster.goatfarm.health.api.dto.*;
import com.devmaster.goatfarm.health.api.mapper.HealthEventApiMapper;
import com.devmaster.goatfarm.health.application.ports.in.HealthEventCommandUseCase;
import com.devmaster.goatfarm.health.application.ports.in.HealthEventQueryUseCase;
import com.devmaster.goatfarm.health.business.bo.HealthEventResponseVO;
import com.devmaster.goatfarm.health.domain.enums.HealthEventStatus;
import com.devmaster.goatfarm.health.domain.enums.HealthEventType;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
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

    @PostMapping
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

    @PutMapping("/{eventId}")
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

    @PatchMapping("/{eventId}/done")
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

    @PatchMapping("/{eventId}/cancel")
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
