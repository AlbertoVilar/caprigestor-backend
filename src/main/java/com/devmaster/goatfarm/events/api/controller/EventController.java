package com.devmaster.goatfarm.events.api.controller;

import com.devmaster.goatfarm.events.application.ports.in.EventManagementUseCase;
import com.devmaster.goatfarm.events.application.ports.out.EventPage;
import com.devmaster.goatfarm.events.application.ports.out.EventPageQuery;
import com.devmaster.goatfarm.config.security.authorization.CanManageFarm;
import com.devmaster.goatfarm.config.security.authorization.FarmOwnerOnly;
import com.devmaster.goatfarm.events.api.dto.EventRequestDTO;
import com.devmaster.goatfarm.events.api.dto.EventResponseDTO;
import com.devmaster.goatfarm.events.business.bo.EventRequestVO;
import com.devmaster.goatfarm.events.business.bo.EventResponseVO;
import com.devmaster.goatfarm.events.enums.EventType;
import com.devmaster.goatfarm.events.api.mapper.EventMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/goatfarms/{farmId}/goats/{goatId}/events")
public class EventController {

    private final EventManagementUseCase eventUseCase;
    private final EventMapper eventMapper;

    public EventController(EventManagementUseCase eventUseCase, EventMapper eventMapper) {
        this.eventUseCase = eventUseCase;
        this.eventMapper = eventMapper;
    }

    @CanManageFarm
    @PostMapping
    @Operation(summary = "Cria um novo evento para uma cabra específica em uma fazenda")
    public ResponseEntity<EventResponseDTO> createEvent(
            @PathVariable Long farmId,
            @PathVariable String goatId,
            @Valid @RequestBody EventRequestDTO requestDTO) {
        EventRequestVO requestVO = eventMapper.toRequestVO(requestDTO);
        EventResponseVO responseVO = eventUseCase.createEvent(farmId, goatId, requestVO);
        return ResponseEntity.status(HttpStatus.CREATED).body(eventMapper.toResponseDTO(responseVO));
    }

    @FarmOwnerOnly
    @PutMapping("/{eventId}")
    @Operation(summary = "Atualiza um evento existente de uma cabra em uma fazenda")
    public ResponseEntity<EventResponseDTO> updateEvent(
            @PathVariable Long farmId,
            @PathVariable String goatId,
            @PathVariable Long eventId,
            @Valid @RequestBody EventRequestDTO requestDTO) {
        EventRequestVO requestVO = eventMapper.toRequestVO(requestDTO);
        EventResponseVO responseVO = eventUseCase.updateEvent(farmId, goatId, eventId, requestVO);
        return ResponseEntity.ok(eventMapper.toResponseDTO(responseVO));
    }

    @CanManageFarm
    @GetMapping("/{eventId}")
    @Operation(summary = "Busca um evento pelo ID de uma cabra em uma fazenda")
    public ResponseEntity<EventResponseDTO> findEventById(
            @PathVariable Long farmId,
            @PathVariable String goatId,
            @PathVariable Long eventId) {
        EventResponseVO responseVO = eventUseCase.findEventById(farmId, goatId, eventId);
        return ResponseEntity.ok(eventMapper.toResponseDTO(responseVO));
    }

    @CanManageFarm
    @GetMapping
    @Operation(summary = "Lista todos os eventos de uma cabra em uma fazenda")
    public ResponseEntity<Page<EventResponseDTO>> findAllEventsByGoatAndFarm(
            @PathVariable Long farmId,
            @PathVariable String goatId,
            @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok(toResponsePage(eventUseCase.findEventsWithFilters(
                farmId, goatId, null, null, null, toPageQuery(pageable))));
    }

    @CanManageFarm
    @GetMapping("/filter")
    @Operation(summary = "Busca eventos de uma cabra com filtros opcionais em uma fazenda")
    public ResponseEntity<Page<EventResponseDTO>> findEventsByGoatWithFilters(
            @PathVariable Long farmId,
            @PathVariable String goatId,
            @RequestParam(required = false) EventType eventType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok(toResponsePage(eventUseCase.findEventsWithFilters(
                farmId, goatId, eventType, startDate, endDate, toPageQuery(pageable))));
    }

    @FarmOwnerOnly
    @DeleteMapping("/{eventId}")
    @Operation(summary = "Remove um evento de uma cabra em uma fazenda")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable Long farmId,
            @PathVariable String goatId,
            @PathVariable Long eventId) {
        eventUseCase.deleteEvent(farmId, goatId, eventId);
        return ResponseEntity.noContent().build();
    }

    private EventPageQuery toPageQuery(Pageable pageable) {
        String sort = pageable.getSort().stream().findFirst()
                .map(order -> order.getProperty() + "," + order.getDirection().name())
                .orElse("");
        return new EventPageQuery(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    private Page<EventResponseDTO> toResponsePage(EventPage<EventResponseVO> page) {
        return new PageImpl<>(page.content().stream().map(eventMapper::toResponseDTO).toList(),
                PageRequest.of(page.page(), page.size()), page.totalElements());
    }
}
