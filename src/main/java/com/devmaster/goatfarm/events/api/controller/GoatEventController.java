package com.devmaster.goatfarm.events.api.controller;

import com.devmaster.goatfarm.application.ports.in.EventManagementUseCase;
import com.devmaster.goatfarm.events.api.dto.EventRequestDTO;
import com.devmaster.goatfarm.events.api.dto.EventResponseDTO;
import com.devmaster.goatfarm.events.business.bo.EventRequestVO;
import com.devmaster.goatfarm.events.business.bo.EventResponseVO;
import com.devmaster.goatfarm.events.mapper.EventMapper;
import com.devmaster.goatfarm.events.enuns.EventType;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/goats/{registrationNumber}/events")
@Tag(name = "Eventos de Cabras", description = "Gerenciamento de eventos relacionados Ã s cabras")
public class GoatEventController {

    @Autowired
    private EventManagementUseCase eventManagementUseCase;

    @Autowired
    private EventMapper eventMapper;

    @Operation(
            summary = "Busca eventos da cabra por registro e filtros opcionais",
            description = "Permite buscar eventos de uma cabra especÃ­fica com filtros por tipo de evento e intervalo de datas."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Eventos encontrados com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - usuÃ¡rio nÃ£o Ã© proprietÃ¡rio da fazenda"),
            @ApiResponse(responseCode = "404", description = "Cabra nÃ£o encontrada")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<Page<EventResponseDTO>> getGoatEvents(
            @Parameter(description = "NÃºmero de registro da cabra", example = "2114517012")
            @PathVariable String registrationNumber,

            @Parameter(description = "Tipo do evento", example = "SAUDE")
            @RequestParam(required = false) String eventType,

            @Parameter(description = "Data inicial", example = "2025-01-01")
            @RequestParam(required = false) LocalDate startDate,

            @Parameter(description = "Data final", example = "2025-12-31")
            @RequestParam(required = false) LocalDate endDate,

            @Parameter(hidden = true) Pageable pageable
    ) {
        EventType enumEventType = null;
        if (eventType != null && !eventType.trim().isEmpty() && !"null".equalsIgnoreCase(eventType)) {
            enumEventType = EventType.valueOf(eventType.trim().toUpperCase());
        }

        Page<EventResponseVO> responseVOPage = eventManagementUseCase.findEventsWithFilters(
                registrationNumber, enumEventType, startDate, endDate, pageable);

        return ResponseEntity.ok(responseVOPage.map(responseVO -> 
            eventMapper.toResponseDTO(responseVO)
        ));
    }


    @Operation(
            summary = "Adiciona um novo evento para uma cabra",
            description = "Cria e associa um evento como: COBERTURA, PARTO, MORTE, SAUDE, VACINACAO, TRANSFERENCIA, " +
                    "MUDANCA_PROPRIETARIO, PESAGEM ou OUTRO a uma cabra identificada pelo nÃºmero de registro."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Evento criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados invÃ¡lidos fornecidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - usuÃ¡rio nÃ£o Ã© proprietÃ¡rio da fazenda"),
            @ApiResponse(responseCode = "404", description = "Cabra nÃ£o encontrada")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<EventResponseDTO> createEvent(
            @Parameter(description = "NÃºmero de registro da cabra", example = "2114517012")
            @PathVariable("registrationNumber") String registrationNumber,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados do evento a ser criado")
            @RequestBody @Valid EventRequestDTO requestDTO
    ) {
        EventRequestVO requestVO = eventMapper.toRequestVO(requestDTO);
        EventResponseVO responseVO = eventManagementUseCase.createEvent(requestVO, registrationNumber);
        EventResponseDTO responseDTO = eventMapper.toResponseDTO(responseVO);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @Operation(
            summary = "Atualiza um evento relacionado a uma cabra",
            description = "Permite atualizar ou corrigir os dados de um evento, sem alterar informaÃ§Ãµes do animal vinculado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Evento atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados invÃ¡lidos fornecidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - usuÃ¡rio nÃ£o Ã© proprietÃ¡rio da fazenda"),
            @ApiResponse(responseCode = "404", description = "Evento ou cabra nÃ£o encontrada")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<EventResponseDTO> updateGoatEvent(
            @Parameter(description = "NÃºmero de registro da cabra", example = "2114517012")
            @PathVariable("registrationNumber") String registrationNumber,

            @Parameter(description = "ID do evento a ser atualizado", example = "3")
            @PathVariable String id,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Novos dados para o evento")
            @RequestBody @Valid EventRequestDTO requestDTO
    ) {
                if ("null".equals(id) || id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID do evento nÃ£o pode ser null ou vazio");
        }
        
        Long eventId;
        try {
            eventId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ID do evento deve ser um nÃºmero vÃ¡lido: " + id);
        }
        EventRequestVO requestVO = eventMapper.toRequestVO(requestDTO);
        EventResponseVO responseVO = eventManagementUseCase.updateEvent(eventId, requestVO, registrationNumber);
        EventResponseDTO responseDTO = eventMapper.toResponseDTO(responseVO);

        return ResponseEntity.ok(responseDTO);
    }

    @Operation(
            summary = "Remove um evento existente",
            description = "Exclui permanentemente um evento do sistema com base no seu ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Evento removido com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - usuÃ¡rio nÃ£o Ã© proprietÃ¡rio da fazenda"),
            @ApiResponse(responseCode = "404", description = "Evento nÃ£o encontrado")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<Void> deleteEventById(
            @Parameter(description = "ID do evento a ser removido", example = "3")
            @PathVariable String id
    ) {
                if ("null".equals(id) || id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID do evento nÃ£o pode ser null ou vazio");
        }
        
        Long eventId;
        try {
            eventId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ID do evento deve ser um nÃºmero vÃ¡lido: " + id);
        }
        
        eventManagementUseCase.deleteEvent(eventId);
        return ResponseEntity.noContent().build();
    }
}

