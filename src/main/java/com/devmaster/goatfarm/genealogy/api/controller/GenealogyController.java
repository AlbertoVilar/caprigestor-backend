package com.devmaster.goatfarm.genealogy.api.controller;

import com.devmaster.goatfarm.genealogy.api.dto.GenealogyRequestDTO;
import com.devmaster.goatfarm.genealogy.api.dto.GenealogyResponseDTO;
import com.devmaster.goatfarm.application.ports.in.GenealogyManagementUseCase;
import com.devmaster.goatfarm.application.ports.in.LegacyGenealogyUseCase;
import com.devmaster.goatfarm.genealogy.mapper.GenealogyMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/goatfarms/{farmId}/goats/{goatId}/genealogies")
@Tag(name = "Genealogia", description = "Gerenciamento da árvore genealógica (Projeção sob demanda)")
public class GenealogyController {

    private final GenealogyManagementUseCase genealogyUseCase;
    private final LegacyGenealogyUseCase legacyGenealogyUseCase;
    private final GenealogyMapper genealogyMapper;

    @Autowired
    public GenealogyController(GenealogyManagementUseCase genealogyUseCase, LegacyGenealogyUseCase legacyGenealogyUseCase, GenealogyMapper genealogyMapper) {
        this.genealogyUseCase = genealogyUseCase;
        this.legacyGenealogyUseCase = legacyGenealogyUseCase;
        this.genealogyMapper = genealogyMapper;
    }

    @GetMapping
    @Operation(summary = "Obter genealogia", description = "Retorna a árvore genealógica projetada a partir dos dados cadastrais da cabra e seus ancestrais. Não utiliza persistência dedicada.")
    public ResponseEntity<GenealogyResponseDTO> getGenealogy(@PathVariable Long farmId, @PathVariable String goatId) {
        return ResponseEntity.ok(genealogyMapper.toResponseDTO(genealogyUseCase.findGenealogy(farmId, goatId)));
    }

    @Deprecated
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    @Operation(summary = "Criar genealogia (DEPRECATED)", description = "Este endpoint foi depreciado. A genealogia agora é gerada automaticamente. Mantido apenas para compatibilidade retroativa; retorna a projeção atual.", deprecated = true)
    public ResponseEntity<GenealogyResponseDTO> createGenealogy(@PathVariable Long farmId, @PathVariable String goatId) {
        return new ResponseEntity<>(genealogyMapper.toResponseDTO(legacyGenealogyUseCase.createGenealogy(farmId, goatId)), HttpStatus.CREATED);
    }

    @Deprecated
    @PostMapping("/with-data")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    @Operation(summary = "Criar genealogia com dados (DEPRECATED)", description = "Este endpoint foi depreciado. Para atualizar ancestrais, utilize a atualização da entidade Cabra. Este endpoint retorna uma simulação da projeção com os dados enviados, mas NÃO persiste alterações.", deprecated = true)
    public ResponseEntity<GenealogyResponseDTO> createGenealogyWithData(@PathVariable Long farmId, @PathVariable String goatId, @RequestBody GenealogyRequestDTO requestDTO) {
        return new ResponseEntity<>(genealogyMapper.toResponseDTO(legacyGenealogyUseCase.createGenealogyWithData(farmId, goatId, requestDTO)), HttpStatus.CREATED);
    }
}