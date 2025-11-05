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
@RequestMapping("/api/goatfarms/{farmId}/goats/{goatId}/genealogies")
public class GenealogyController {

    private final GenealogyFacade genealogyFacade;

    @Autowired
    public GenealogyController(GenealogyFacade genealogyFacade) {
        this.genealogyFacade = genealogyFacade;
    }

    @GetMapping
    public ResponseEntity<GenealogyResponseDTO> getGenealogy(@PathVariable Long farmId, @PathVariable String goatId) {
        return ResponseEntity.ok(genealogyFacade.findGenealogy(farmId, goatId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    public ResponseEntity<GenealogyResponseDTO> createGenealogy(@PathVariable Long farmId, @PathVariable String goatId) {
        return new ResponseEntity<>(genealogyFacade.createGenealogy(farmId, goatId), HttpStatus.CREATED);
    }

    @PostMapping("/with-data")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    public ResponseEntity<GenealogyResponseDTO> createGenealogyWithData(@PathVariable Long farmId, @PathVariable String goatId, @RequestBody GenealogyRequestDTO requestDTO) {
        return new ResponseEntity<>(genealogyFacade.createGenealogyWithData(farmId, goatId, requestDTO), HttpStatus.CREATED);
    }
}