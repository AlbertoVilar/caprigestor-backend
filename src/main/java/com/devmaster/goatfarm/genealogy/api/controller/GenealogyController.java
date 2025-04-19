package com.devmaster.goatfarm.genealogy.api.controller;


import com.devmaster.goatfarm.genealogy.api.dto.GenealogyResponseDTO;
import com.devmaster.goatfarm.genealogy.business.bo.GenealogyResponseVO;
import com.devmaster.goatfarm.genealogy.converter.GenealogyDTOConverter;
import com.devmaster.goatfarm.genealogy.facade.GenealogyFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://localhost:5500"})
@RestController
@RequestMapping("/genealogies")
public class GenealogyController {

    private final GenealogyFacade genealogyFacade;

    @Autowired
    public GenealogyController(GenealogyFacade genealogyFacade) {
        this.genealogyFacade = genealogyFacade;
    }

    @GetMapping("/{registrationNumber}")
    public ResponseEntity<GenealogyResponseDTO> getGenealogy(@PathVariable String registrationNumber) {

        GenealogyResponseVO response = genealogyFacade.findGenealogy(registrationNumber);

        return ResponseEntity.ok(GenealogyDTOConverter.toResponseDTO(response));
    }
}

