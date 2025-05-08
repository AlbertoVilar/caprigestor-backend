package com.devmaster.goatfarm.events.api.controller;

import com.devmaster.goatfarm.events.business.bo.EventResponseVO;
import com.devmaster.goatfarm.events.dao.EventDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/events")
public class EventController {
    @Autowired
    private EventDao eventDao;

    @GetMapping("/{id}")
    public ResponseEntity<EventResponseVO> findEventById(@PathVariable Long id) {

        return ResponseEntity.ok(eventDao.findEventById(id));
    }
}
