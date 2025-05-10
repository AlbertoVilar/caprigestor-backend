package com.devmaster.goatfarm.events.api.controller;

import com.devmaster.goatfarm.events.api.dto.EventRequestDTO;
import com.devmaster.goatfarm.events.api.dto.EventResponseDTO;
import com.devmaster.goatfarm.events.business.bo.EventRequestVO;
import com.devmaster.goatfarm.events.business.bo.EventResponseVO;
import com.devmaster.goatfarm.events.converter.EventDTOConverter;
import com.devmaster.goatfarm.events.enuns.EventType;
import com.devmaster.goatfarm.events.facade.EventFacade;
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

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<List<EventResponseDTO>> getGoatEvents(
            @PathVariable String registrationNumber,
            @RequestParam(required = false) EventType eventType,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate
    ) {
        List<EventResponseVO> responseVOs =
                eventFacade.findEventsByGoatWithFilters(registrationNumber, eventType, startDate, endDate);

        return ResponseEntity.ok(responseVOs.stream()
                .map(EventDTOConverter::responseDTO)
                .toList());
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<EventResponseDTO> createEvent(
            @RequestBody @Valid EventRequestDTO requestDTO,
            @PathVariable("registrationNumber") String registrationNumber
    ) {
        EventRequestVO requestVO = EventDTOConverter.toRequestVO(requestDTO);
        EventResponseVO responseVO = eventFacade.createEvent(requestVO, registrationNumber);
        EventResponseDTO responseDTO = EventDTOConverter.responseDTO(responseVO);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<EventResponseDTO> updateGoatEvent(
            @PathVariable("registrationNumber") String registrationNumber,
            @PathVariable Long id,
            @RequestBody @Valid EventRequestDTO requestDTO
    ) {
        EventRequestVO requestVO = EventDTOConverter.toRequestVO(requestDTO);
        EventResponseVO responseVO = eventFacade.updateEvent(id, requestVO, registrationNumber);
        EventResponseDTO responseDTO = EventDTOConverter.responseDTO(responseVO);

        return ResponseEntity.ok(responseDTO);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEventById(@PathVariable Long id) {
        eventFacade.deleteEventById(id);
        return ResponseEntity.status(HttpStatus.GONE).build();
    }
}
