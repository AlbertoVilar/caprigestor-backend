package com.devmaster.goatfarm.goat.api.controller;

import com.devmaster.goatfarm.goat.api.dto.GoatResponseDTO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.mapper.GoatMapper;
import com.devmaster.goatfarm.goat.facade.GoatFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller dedicado exclusivamente para endpoints de busca/consulta de cabras.
 * Separado do GoatController principal para manter organização e clareza.
 * Endpoints públicos para consulta sem necessidade de autenticação.
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Goat Search API", description = "Endpoints públicos para busca e consulta de cabras")
@CrossOrigin(origins = {"http://localhost:5500", "http://localhost:5173", "http://localhost:8080"})
public class GoatSearchController {

    private final GoatFacade goatFacade;
    private final GoatMapper goatMapper;

    @Autowired
    public GoatSearchController(GoatFacade goatFacade, GoatMapper goatMapper) {
        this.goatFacade = goatFacade;
        this.goatMapper = goatMapper;
    }

    /**
     * Busca paginada de todas as cabras cadastradas.
     * Endpoint público para consulta geral.
     */
    @Operation(summary = "Busca paginada de todas as cabras",
            description = "Endpoint público que retorna uma lista paginada de todas as cabras cadastradas no sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso.")
    })
    @GetMapping("/goats")
    public ResponseEntity<Page<GoatResponseDTO>> findAllGoats(
            @Parameter(hidden = true)
            @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok(goatFacade.findAllGoats(pageable)
                .map(goatMapper::toResponseDTO));
    }

    /**
     * Busca paginada por cabras usando parte do nome.
     * Endpoint público para consulta por nome.
     */
    @Operation(summary = "Busca paginada por cabras usando parte do nome",
            description = "Endpoint público que retorna cabras que contêm o nome especificado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso."),
            @ApiResponse(responseCode = "404", description = "Nenhuma cabra encontrada com o nome especificado.")
    })
    @GetMapping("/goats/name")
    public ResponseEntity<Page<GoatResponseDTO>> searchGoatByName(
            @Parameter(description = "Nome ou parte do nome da cabra", example = "NAIDE")
            @RequestParam(defaultValue = "") String name,
            @Parameter(hidden = true)
            @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok(goatFacade.searchGoatByName(name, pageable)
                .map(goatMapper::toResponseDTO));
    }

    /**
     * Busca uma cabra pelo seu número de registro exato.
     * Endpoint público para consulta específica.
     */
    @Operation(summary = "Busca uma cabra pelo número de registro",
            description = "Endpoint público que retorna os detalhes de uma cabra específica usando seu número de registro.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cabra encontrada com sucesso."),
            @ApiResponse(responseCode = "404", description = "Cabra não encontrada.")
    })
    @GetMapping("/goats/registration/{registrationNumber}")
    public ResponseEntity<GoatResponseDTO> findByRegistrationNumber(
            @Parameter(description = "Número de registro da cabra", example = "2114517012", required = true)
            @PathVariable String registrationNumber) {
        GoatResponseVO goatVO = goatFacade.findGoatByRegistrationNumber(registrationNumber);
        GoatResponseDTO responseDTO = goatMapper.toResponseDTO(goatVO);
        return ResponseEntity.ok(responseDTO);
    }
}