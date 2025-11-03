package com.devmaster.goatfarm.genealogy.api.controller;

import com.devmaster.goatfarm.genealogy.api.dto.GenealogyRequestDTO;
import com.devmaster.goatfarm.genealogy.api.dto.GenealogyResponseDTO;
import com.devmaster.goatfarm.genealogy.facade.GenealogyFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/genealogies")
public class GenealogyController {

    private final GenealogyFacade genealogyFacade;

    @Autowired
    public GenealogyController(GenealogyFacade genealogyFacade) {
        this.genealogyFacade = genealogyFacade;
    }

    @GetMapping("/{registrationNumber}")
    public ResponseEntity<GenealogyResponseDTO> getGenealogy(@PathVariable String registrationNumber) {
        return ResponseEntity.ok(genealogyFacade.findGenealogy(registrationNumber));
    }

    @PostMapping("/{registrationNumber}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    public ResponseEntity<GenealogyResponseDTO> createGenealogy(@PathVariable String registrationNumber) {
        return new ResponseEntity<>(genealogyFacade.createGenealogy(registrationNumber), HttpStatus.CREATED);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    public ResponseEntity<GenealogyResponseDTO> createGenealogyWithData(@RequestBody GenealogyRequestDTO requestDTO) {
        return new ResponseEntity<>(genealogyFacade.createGenealogyWithData(requestDTO), HttpStatus.CREATED);
    }
}
