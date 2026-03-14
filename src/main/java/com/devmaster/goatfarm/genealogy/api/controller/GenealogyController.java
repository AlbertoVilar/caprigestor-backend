package com.devmaster.goatfarm.genealogy.api.controller;

import com.devmaster.goatfarm.genealogy.application.ports.in.GenealogyQueryUseCase;
import com.devmaster.goatfarm.genealogy.application.ports.in.GenealogyComplementaryQueryUseCase;
import com.devmaster.goatfarm.genealogy.business.bo.GenealogyComplementaryResponseVO;
import com.devmaster.goatfarm.genealogy.business.bo.GenealogyResponseVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/goatfarms/{farmId}/goats/{goatId}/genealogies", "/api/goatfarms/{farmId}/goats/{goatId}/genealogies"})
@Tag(name = "Genealogy", description = "Endpoints para consulta de genealogia de caprinos. Caminho canÃ´nico /api/v1; legado /api em descontinuaÃ§Ã£o.")
public class GenealogyController {

    private final GenealogyQueryUseCase genealogyQueryUseCase;
    private final GenealogyComplementaryQueryUseCase genealogyComplementaryQueryUseCase;

    public GenealogyController(
            GenealogyQueryUseCase genealogyQueryUseCase,
            GenealogyComplementaryQueryUseCase genealogyComplementaryQueryUseCase
    ) {
        this.genealogyQueryUseCase = genealogyQueryUseCase;
        this.genealogyComplementaryQueryUseCase = genealogyComplementaryQueryUseCase;
    }

    @Operation(summary = "Consultar Genealogia", description = "Retorna a Ã¡rvore genealÃ³gica completa de uma cabra (pais, avÃ³s e bisavÃ³s)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Genealogia recuperada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Cabra nÃ£o encontrada na fazenda informada")
    })
    @GetMapping(params = "!complementaryAbcc")
    public ResponseEntity<GenealogyResponseVO> getGenealogy(
            @PathVariable Long farmId,
            @PathVariable String goatId) {
        GenealogyResponseVO response = genealogyQueryUseCase.findGenealogy(farmId, goatId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Consultar Genealogia Complementar ABCC",
            description = "Retorna a Ã¡rvore genealÃ³gica local com complemento externo da ABCC em modo somente leitura"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Genealogia complementar recuperada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Cabra nÃ£o encontrada na fazenda informada")
    })
    @GetMapping(params = "complementaryAbcc=true")
    public ResponseEntity<GenealogyComplementaryResponseVO> getComplementaryGenealogy(
            @PathVariable Long farmId,
            @PathVariable String goatId) {
        GenealogyComplementaryResponseVO response =
                genealogyComplementaryQueryUseCase.findComplementaryGenealogy(farmId, goatId);
        return ResponseEntity.ok(response);
    }
}

