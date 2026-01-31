package com.devmaster.goatfarm.health.api.controller;

import com.devmaster.goatfarm.config.security.OwnershipService;
import com.devmaster.goatfarm.health.api.dto.*;
import com.devmaster.goatfarm.health.api.mapper.HealthEventApiMapper;
import com.devmaster.goatfarm.health.application.ports.in.HealthEventCommandUseCase;
import com.devmaster.goatfarm.health.application.ports.in.HealthEventQueryUseCase;
import com.devmaster.goatfarm.health.domain.enums.HealthEventStatus;
import com.devmaster.goatfarm.health.domain.enums.HealthEventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
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
    private final OwnershipService ownershipService;

    public HealthEventController(
            HealthEventCommandUseCase commandUseCase,
            HealthEventQueryUseCase queryUseCase,
            HealthEventApiMapper apiMapper,
            OwnershipService ownershipService
    ) {
        this.commandUseCase = commandUseCase;
        this.queryUseCase = queryUseCase;
        this.apiMapper = apiMapper;
        this.ownershipService = ownershipService;
    }

    @PostMapping
    @PreAuthorize("@ownershipService.isFarmOwner(#farmId)")
    public ResponseEntity<HealthEventResponseDTO> create(
            @PathVariable Long farmId,
            @PathVariable String goatId,
            @RequestBody HealthEventCreateRequestDTO request
    ) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @PutMapping("/{eventId}")
    @PreAuthorize("@ownershipService.isFarmOwner(#farmId)")
    public ResponseEntity<HealthEventResponseDTO> update(
            @PathVariable Long farmId,
            @PathVariable String goatId,
            @PathVariable Long eventId,
            @RequestBody HealthEventUpdateRequestDTO request
    ) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @PatchMapping("/{eventId}/done")
    @PreAuthorize("@ownershipService.isFarmOwner(#farmId)")
    public ResponseEntity<HealthEventResponseDTO> markAsDone(
            @PathVariable Long farmId,
            @PathVariable String goatId,
            @PathVariable Long eventId,
            @RequestBody HealthEventDoneRequestDTO request
    ) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @PatchMapping("/{eventId}/cancel")
    @PreAuthorize("@ownershipService.isFarmOwner(#farmId)")
    public ResponseEntity<HealthEventResponseDTO> cancel(
            @PathVariable Long farmId,
            @PathVariable String goatId,
            @PathVariable Long eventId,
            @RequestBody HealthEventCancelRequestDTO request
    ) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @GetMapping("/{eventId}")
    @PreAuthorize("@ownershipService.isFarmOwner(#farmId)")
    public ResponseEntity<HealthEventResponseDTO> getById(
            @PathVariable Long farmId,
            @PathVariable String goatId,
            @PathVariable Long eventId
    ) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @GetMapping
    @PreAuthorize("@ownershipService.isFarmOwner(#farmId)")
    public ResponseEntity<Page<HealthEventResponseDTO>> listByGoat(
            @PathVariable Long farmId,
            @PathVariable String goatId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) HealthEventType type,
            @RequestParam(required = false) HealthEventStatus status,
            Pageable pageable
    ) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
