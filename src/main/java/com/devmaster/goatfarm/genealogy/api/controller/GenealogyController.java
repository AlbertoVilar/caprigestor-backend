package com.devmaster.goatfarm.genealogy.api.controller;

import com.devmaster.goatfarm.genealogy.api.dto.GenealogyRequestDTO;
import com.devmaster.goatfarm.genealogy.api.dto.GenealogyResponseDTO;
import com.devmaster.goatfarm.application.ports.in.GenealogyManagementUseCase;
import com.devmaster.goatfarm.genealogy.mapper.GenealogyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/goatfarms/{farmId}/goats/{goatId}/genealogies")
public class GenealogyController {

    private final GenealogyManagementUseCase genealogyUseCase;
    private final GenealogyMapper genealogyMapper;

    @Autowired
    public GenealogyController(GenealogyManagementUseCase genealogyUseCase, GenealogyMapper genealogyMapper) {
        this.genealogyUseCase = genealogyUseCase;
        this.genealogyMapper = genealogyMapper;
    }

    @GetMapping
    public ResponseEntity<GenealogyResponseDTO> getGenealogy(@PathVariable Long farmId, @PathVariable String goatId) {
        return ResponseEntity.ok(genealogyMapper.toResponseDTO(genealogyUseCase.findGenealogy(farmId, goatId)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    public ResponseEntity<GenealogyResponseDTO> createGenealogy(@PathVariable Long farmId, @PathVariable String goatId) {
        return new ResponseEntity<>(genealogyMapper.toResponseDTO(genealogyUseCase.createGenealogy(farmId, goatId)), HttpStatus.CREATED);
    }

    @PostMapping("/with-data")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    public ResponseEntity<GenealogyResponseDTO> createGenealogyWithData(@PathVariable Long farmId, @PathVariable String goatId, @RequestBody GenealogyRequestDTO requestDTO) {
        return new ResponseEntity<>(genealogyMapper.toResponseDTO(genealogyUseCase.createGenealogyWithData(farmId, goatId, requestDTO)), HttpStatus.CREATED);
    }
}