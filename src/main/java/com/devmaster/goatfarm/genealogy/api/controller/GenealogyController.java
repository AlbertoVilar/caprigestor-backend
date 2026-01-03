package com.devmaster.goatfarm.genealogy.api.controller;

import com.devmaster.goatfarm.application.ports.in.GenealogyQueryUseCase;
import com.devmaster.goatfarm.genealogy.business.bo.GenealogyResponseVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/goatfarms/{farmId}/goats/{goatId}/genealogies")
@Tag(name = "Genealogy", description = "Endpoints para consulta de genealogia de caprinos")
public class GenealogyController {

    private final GenealogyQueryUseCase genealogyQueryUseCase;

    public GenealogyController(GenealogyQueryUseCase genealogyQueryUseCase) {
        this.genealogyQueryUseCase = genealogyQueryUseCase;
    }

    @Operation(summary = "Consultar Genealogia", description = "Retorna a árvore genealógica completa de uma cabra (pais, avós e bisavós)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Genealogia recuperada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Cabra não encontrada na fazenda informada")
    })
    @GetMapping
    public ResponseEntity<GenealogyResponseVO> getGenealogy(
            @PathVariable Long farmId,
            @PathVariable String goatId) {
        GenealogyResponseVO response = genealogyQueryUseCase.findGenealogy(farmId, goatId);
        return ResponseEntity.ok(response);
    }
}

