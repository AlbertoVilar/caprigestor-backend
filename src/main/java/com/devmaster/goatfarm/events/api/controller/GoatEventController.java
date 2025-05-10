package com.devmaster.goatfarm.events.api.controller;

import com.devmaster.goatfarm.events.api.dto.EventRequestDTO;
import com.devmaster.goatfarm.events.api.dto.EventResponseDTO;
import com.devmaster.goatfarm.events.business.bo.EventRequestVO;
import com.devmaster.goatfarm.events.business.bo.EventResponseVO;
import com.devmaster.goatfarm.events.converter.EventDTOConverter;
import com.devmaster.goatfarm.events.enuns.EventType;
import com.devmaster.goatfarm.events.facade.EventFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/goats/{registrationNumber}/events")
public class GoatEventController {

    @Autowired
    private EventFacade eventFacade;

    @GetMapping
    public ResponseEntity<List<EventResponseDTO>> getGoatEvents(
            @PathVariable String registrationNumber,
            @RequestParam(required = false) EventType eventType, // Filtro opcional
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate
    ) {
        // Usando o método ajustado no facade
        List<EventResponseVO> responseVOs =
                eventFacade.findEventsByGoatWithFilters(registrationNumber, eventType, startDate, endDate);

        return ResponseEntity.ok(responseVOs.stream()
                .map(EventDTOConverter::responseDTO)
                .toList());
    }

    @PostMapping
    public ResponseEntity<EventResponseDTO> createEvent(
            @RequestBody EventRequestDTO requestDTO,
            @PathVariable("registrationNumber") String registrationNumber
    ) {
        EventRequestVO requestVO = EventDTOConverter.toRequestVO(requestDTO);
        EventResponseVO responseVO = eventFacade.createEvent(requestVO, registrationNumber);
        EventResponseDTO responseDTO = EventDTOConverter.responseDTO(responseVO);

        return ResponseEntity
                .status(HttpStatus.CREATED) // Semântica correta para POST
                .body(responseDTO);
    }


}
