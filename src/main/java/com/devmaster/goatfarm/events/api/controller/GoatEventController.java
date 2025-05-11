package com.devmaster.goatfarm.events.api.controller;

import com.devmaster.goatfarm.events.api.dto.EventRequestDTO;
import com.devmaster.goatfarm.events.api.dto.EventResponseDTO;
import com.devmaster.goatfarm.events.business.bo.EventRequestVO;
import com.devmaster.goatfarm.events.business.bo.EventResponseVO;
import com.devmaster.goatfarm.events.converter.EventDTOConverter;
import com.devmaster.goatfarm.events.enuns.EventType;
import com.devmaster.goatfarm.events.facade.EventFacade;

import io.swagger.v3.oas.annotations.Operation;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/goats/{registrationNumber}/events")
public class GoatEventController {

    @Autowired
    private EventFacade eventFacade;

    @Operation(
            summary = "Busca eventos da cabra por registro e filtros opcionais",
            description = "Permite buscar eventos de uma cabra específica com filtros por tipo de evento e intervalo de datas."
    )
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<List<EventResponseDTO>> getGoatEvents(
            @Parameter(description = "Número de registro da cabra", example = "2114517012")
            @PathVariable String registrationNumber,

            @Parameter(description = "Tipo do evento (ex: SAUDE, PARTO, VACINACAO)", example = "SAUDE")
            @RequestParam(required = false) EventType eventType,

            @Parameter(description = "Data inicial para filtro (formato: yyyy-MM-dd)", example = "2025-01-01")
            @RequestParam(required = false) LocalDate startDate,

            @Parameter(description = "Data final para filtro (formato: yyyy-MM-dd)", example = "2025-12-31")
            @RequestParam(required = false) LocalDate endDate
    ) {
        List<EventResponseVO> responseVOs =
                eventFacade.findEventsByGoatWithFilters(registrationNumber, eventType, startDate, endDate);

        return ResponseEntity.ok(responseVOs.stream()
                .map(EventDTOConverter::responseDTO)
                .toList());
    }

    @Operation(
            summary = "Adiciona um novo evento para uma cabra",
            description = "Cria e associa um evento como: COBERTURA, PARTO, MORTE, SAUDE, VACINACAO, TRANSFERENCIA, " +
                    "MUDANCA_PROPRIETARIO, PESAGEM ou OUTRO a uma cabra identificada pelo número de registro."
    )
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<EventResponseDTO> createEvent(
            @Parameter(description = "Número de registro da cabra", example = "2114517012")
            @PathVariable("registrationNumber") String registrationNumber,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados do evento a ser criado")
            @RequestBody @Valid EventRequestDTO requestDTO
    ) {
        EventRequestVO requestVO = EventDTOConverter.toRequestVO(requestDTO);
        EventResponseVO responseVO = eventFacade.createEvent(requestVO, registrationNumber);
        EventResponseDTO responseDTO = EventDTOConverter.responseDTO(responseVO);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @Operation(
            summary = "Atualiza um evento relacionado a uma cabra",
            description = "Permite atualizar ou corrigir os dados de um evento, sem alterar informações do animal vinculado."
    )
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<EventResponseDTO> updateGoatEvent(
            @Parameter(description = "Número de registro da cabra", example = "2114517012")
            @PathVariable("registrationNumber") String registrationNumber,

            @Parameter(description = "ID do evento a ser atualizado", example = "3")
            @PathVariable Long id,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Novos dados para o evento")
            @RequestBody @Valid EventRequestDTO requestDTO
    ) {
        EventRequestVO requestVO = EventDTOConverter.toRequestVO(requestDTO);
        EventResponseVO responseVO = eventFacade.updateEvent(id, requestVO, registrationNumber);
        EventResponseDTO responseDTO = EventDTOConverter.responseDTO(responseVO);

        return ResponseEntity.ok(responseDTO);
    }

    @Operation(
            summary = "Remove um evento existente",
            description = "Exclui permanentemente um evento do sistema com base no seu ID."
    )
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEventById(
            @Parameter(description = "ID do evento a ser removido", example = "3")
            @PathVariable Long id
    ) {
        eventFacade.deleteEventById(id);
        return ResponseEntity.status(HttpStatus.GONE).build();
    }
}
