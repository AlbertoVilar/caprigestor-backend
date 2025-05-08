package com.devmaster.goatfarm.genealogy.api.controller;

import com.devmaster.goatfarm.genealogy.api.dto.GenealogyResponseDTO;
import com.devmaster.goatfarm.genealogy.business.bo.GenealogyResponseVO;
import com.devmaster.goatfarm.genealogy.converter.GenealogyDTOConverter;
import com.devmaster.goatfarm.genealogy.facade.GenealogyFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://localhost:5500"})
@RestController
@RequestMapping("/genealogies")
public class GenealogyController {

    private final GenealogyFacade genealogyFacade;
    private final GenealogyDTOConverter genealogyDTOConverter; // Adicionando o DTO Converter

    @Autowired
    public GenealogyController(GenealogyFacade genealogyFacade, GenealogyDTOConverter genealogyDTOConverter) {
        this.genealogyFacade = genealogyFacade;
        this.genealogyDTOConverter = genealogyDTOConverter;
    }

    @GetMapping("/{registrationNumber}")
    public ResponseEntity<GenealogyResponseDTO> getGenealogy(@PathVariable String registrationNumber) {
        GenealogyResponseVO responseVO = genealogyFacade.findGenealogy(registrationNumber);
        if (responseVO != null) {
            GenealogyResponseDTO responseDTO = genealogyDTOConverter.toResponseDTO(responseVO);
            return ResponseEntity.ok(responseDTO);
        } else {
          return ResponseEntity.notFound().build();
       }
    }

    @PostMapping("/{registrationNumber}")
    public ResponseEntity<GenealogyResponseDTO> createGenealogy(@PathVariable String registrationNumber) {
        GenealogyResponseVO createdResponseVO = genealogyFacade.createGenealogy(registrationNumber);
        GenealogyResponseDTO createdResponseDTO = genealogyDTOConverter.toResponseDTO(createdResponseVO);
        return new ResponseEntity<>(createdResponseDTO, HttpStatus.CREATED);
    }
}