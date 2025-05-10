package com.devmaster.goatfarm.events.api.controller;

import com.devmaster.goatfarm.events.api.dto.EventResponseDTO;
import com.devmaster.goatfarm.events.business.bo.EventResponseVO;
import com.devmaster.goatfarm.events.converter.EventDTOConverter;
import com.devmaster.goatfarm.events.dao.EventDao;
import com.devmaster.goatfarm.events.facade.EventFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events")
public class EventController {

    @Autowired
    private EventFacade eventFacade;

    @GetMapping(value = "/{goatNumRegistration}")
    public ResponseEntity<List<EventResponseDTO>> findEventsByGoat(@PathVariable String goatNumRegistration) {

        List<EventResponseVO> responseVOs = eventFacade.findEventByGoat(goatNumRegistration);

        return ResponseEntity.ok( responseVOs.stream().map(EventDTOConverter::responseDTO).toList());
    }




}

