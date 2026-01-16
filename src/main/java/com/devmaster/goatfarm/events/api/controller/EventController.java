package com.devmaster.goatfarm.events.api.controller;

import com.devmaster.goatfarm.application.ports.in.EventManagementUseCase;
import com.devmaster.goatfarm.events.api.dto.EventRequestDTO;
import com.devmaster.goatfarm.events.api.dto.EventResponseDTO;
import com.devmaster.goatfarm.events.business.bo.EventRequestVO;
import com.devmaster.goatfarm.events.business.bo.EventResponseVO;
import com.devmaster.goatfarm.events.enuns.EventType;
import com.devmaster.goatfarm.events.mapper.EventMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/goatfarms/{farmId}/goats/{goatId}/events")
public class EventController {

    private final EventManagementUseCase eventUseCase;
    private final EventMapper eventMapper;

    @Autowired
    public EventController(EventManagementUseCase eventUseCase, EventMapper eventMapper) {
        this.eventUseCase = eventUseCase;
        this.eventMapper = eventMapper;
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or ((hasAuthority('ROLE_OPERATOR') or hasAuthority('ROLE_FARM_OWNER')) and @ownershipService.isFarmOwner(#farmId))")
    @PostMapping
    @Operation(summary = "Cria um novo evento para uma cabra específica em uma fazenda")
    public ResponseEntity<EventResponseDTO> createEvent(
            @PathVariable Long farmId,
            @PathVariable String goatId,
            @Valid @RequestBody EventRequestDTO requestDTO) {
        EventRequestVO requestVO = eventMapper.toRequestVO(requestDTO);
        EventResponseVO responseVO = eventUseCase.createEvent(requestVO, goatId);
        return ResponseEntity.status(HttpStatus.CREATED).body(eventMapper.toResponseDTO(responseVO));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or ((hasAuthority('ROLE_OPERATOR') or hasAuthority('ROLE_FARM_OWNER')) and @ownershipService.isFarmOwner(#farmId))")
    @PutMapping("/{eventId}")
    @Operation(summary = "Atualiza um evento existente de uma cabra em uma fazenda")
    public ResponseEntity<EventResponseDTO> updateEvent(
            @PathVariable Long farmId,
            @PathVariable String goatId,
            @PathVariable Long eventId,
            @Valid @RequestBody EventRequestDTO requestDTO) {
        EventRequestVO requestVO = eventMapper.toRequestVO(requestDTO);
        EventResponseVO responseVO = eventUseCase.updateEvent(eventId, requestVO, goatId);
        return ResponseEntity.ok(eventMapper.toResponseDTO(responseVO));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or ((hasAuthority('ROLE_OPERATOR') or hasAuthority('ROLE_FARM_OWNER')) and @ownershipService.isFarmOwner(#farmId))")
    @GetMapping("/{eventId}")
    @Operation(summary = "Busca um evento pelo ID de uma cabra em uma fazenda")
    public ResponseEntity<EventResponseDTO> findEventById(
            @PathVariable Long farmId,
            @PathVariable String goatId,
            @PathVariable Long eventId) {
        EventResponseVO responseVO = eventUseCase.findEventById(eventId);
        return ResponseEntity.ok(eventMapper.toResponseDTO(responseVO));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or ((hasAuthority('ROLE_OPERATOR') or hasAuthority('ROLE_FARM_OWNER')) and @ownershipService.isFarmOwner(#farmId))")
    @GetMapping
    @Operation(summary = "Lista todos os eventos de uma cabra em uma fazenda")
    public ResponseEntity<Page<EventResponseDTO>> findAllEventsByGoatAndFarm(
            @PathVariable Long farmId,
            @PathVariable String goatId,
            @PageableDefault(size = 12) Pageable pageable) {
        Page<EventResponseVO> responseVOs = eventUseCase.findEventsWithFilters(goatId, null, null, null, pageable);
        Page<EventResponseDTO> responseDTOs = responseVOs.map(eventMapper::toResponseDTO);
        return ResponseEntity.ok(responseDTOs);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or ((hasAuthority('ROLE_OPERATOR') or hasAuthority('ROLE_FARM_OWNER')) and @ownershipService.isFarmOwner(#farmId))")
    @GetMapping("/filter")
    @Operation(summary = "Busca eventos de uma cabra com filtros opcionais em uma fazenda")
    public ResponseEntity<Page<EventResponseDTO>> findEventsByGoatWithFilters(
            @PathVariable Long farmId,
            @PathVariable String goatId,
            @RequestParam(required = false) EventType eventType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 12) Pageable pageable) {
        Page<EventResponseVO> responseVOs = eventUseCase.findEventsWithFilters(goatId, eventType, startDate, endDate, pageable);
        Page<EventResponseDTO> responseDTOs = responseVOs.map(eventMapper::toResponseDTO);
        return ResponseEntity.ok(responseDTOs);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or ((hasAuthority('ROLE_OPERATOR') or hasAuthority('ROLE_FARM_OWNER')) and @ownershipService.isFarmOwner(#farmId))")
    @DeleteMapping("/{eventId}")
    @Operation(summary = "Remove um evento de uma cabra em uma fazenda")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable Long farmId,
            @PathVariable String goatId,
            @PathVariable Long eventId) {
        eventUseCase.deleteEvent(eventId);
        return ResponseEntity.noContent().build();
    }
}
