package com.devmaster.goatfarm.infrastructure.adapters.in.web;

import com.devmaster.goatfarm.application.ports.in.EventManagementUseCase;
import com.devmaster.goatfarm.events.api.dto.EventRequestDTO;
import com.devmaster.goatfarm.events.api.dto.EventResponseDTO;
import com.devmaster.goatfarm.events.business.bo.EventRequestVO;
import com.devmaster.goatfarm.events.business.bo.EventResponseVO;
import com.devmaster.goatfarm.events.enuns.EventType;
import com.devmaster.goatfarm.events.mapper.EventMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
@RestController
@RequestMapping("/api/hex/goats/{registrationNumber}/events")
@Tag(name = "Eventos de Cabras (Hex)", description = "API hexagonal para gerenciamento de eventos de cabras (rota isolada)")
public class EventWebAdapter {

    private final EventManagementUseCase eventManagementUseCase;
    private final EventMapper eventMapper;

    public EventWebAdapter(EventManagementUseCase eventManagementUseCase, EventMapper eventMapper) {
        this.eventManagementUseCase = eventManagementUseCase;
        this.eventMapper = eventMapper;
    }

    @PostMapping
    @Operation(summary = "Criar evento", description = "Cria um novo evento para uma cabra")
    public ResponseEntity<EventResponseDTO> createEvent(
            @Parameter(description = "Número de registro da cabra") @PathVariable String registrationNumber,
            @Valid @RequestBody EventRequestDTO requestDTO) {
        
        EventRequestVO requestVO = eventMapper.toRequestVO(requestDTO);
        EventResponseVO responseVO = eventManagementUseCase.createEvent(requestVO, registrationNumber);
        EventResponseDTO responseDTO = eventMapper.toResponseDTO(responseVO);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @PutMapping("/{eventId}")
    @Operation(summary = "Atualizar evento", description = "Atualiza um evento existente")
    public ResponseEntity<EventResponseDTO> updateEvent(
            @Parameter(description = "Número de registro da cabra") @PathVariable String registrationNumber,
            @Parameter(description = "ID do evento") @PathVariable Long eventId,
            @Valid @RequestBody EventRequestDTO requestDTO) {
        
        EventRequestVO requestVO = eventMapper.toRequestVO(requestDTO);
        EventResponseVO responseVO = eventManagementUseCase.updateEvent(eventId, requestVO, registrationNumber);
        EventResponseDTO responseDTO = eventMapper.toResponseDTO(responseVO);
        
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping
    @Operation(summary = "Listar eventos", description = "Lista todos os eventos de uma cabra")
    public ResponseEntity<List<EventResponseDTO>> getEventsByGoat(
            @Parameter(description = "Número de registro da cabra") @PathVariable String registrationNumber) {
        
        List<EventResponseVO> responseVOs = eventManagementUseCase.findEventsByGoat(registrationNumber);
        List<EventResponseDTO> responseDTOs = eventMapper.toResponseDTOList(responseVOs);
        
        return ResponseEntity.ok(responseDTOs);
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar eventos com filtros", description = "Busca eventos com filtros e paginação")
    public ResponseEntity<Page<EventResponseDTO>> searchEventsWithFilters(
            @Parameter(description = "Número de registro da cabra") @PathVariable String registrationNumber,
            @Parameter(description = "Tipo do evento") @RequestParam(required = false) EventType eventType,
            @Parameter(description = "Data inicial") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Data final") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Pageable pageable) {
        
        Page<EventResponseVO> responseVOs = eventManagementUseCase.findEventsWithFilters(
                registrationNumber, eventType, startDate, endDate, pageable);
        Page<EventResponseDTO> responseDTOs = responseVOs.map(eventMapper::toResponseDTO);
        
        return ResponseEntity.ok(responseDTOs);
    }

    @DeleteMapping("/{eventId}")
    @Operation(summary = "Deletar evento", description = "Remove um evento")
    public ResponseEntity<Void> deleteEvent(
            @Parameter(description = "Número de registro da cabra") @PathVariable String registrationNumber,
            @Parameter(description = "ID do evento") @PathVariable Long eventId) {
        
        eventManagementUseCase.deleteEvent(eventId);
        
        return ResponseEntity.noContent().build();
    }
}
