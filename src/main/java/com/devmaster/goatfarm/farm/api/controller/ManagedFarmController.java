package com.devmaster.goatfarm.farm.api.controller;

import com.devmaster.goatfarm.api.pagination.SpringPageMapper;
import com.devmaster.goatfarm.application.pagination.PageResult;
import com.devmaster.goatfarm.farm.api.dto.ManagedFarmSummaryDTO;
import com.devmaster.goatfarm.farm.application.ports.in.ManagedFarmQueryUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Authenticated management selector; distinct from the public farm catalog. */
@RestController
@RequestMapping("/api/v1/goatfarms")
@Tag(name = "Managed Farm API", description = "Descoberta das fazendas administráveis pelo usuário autenticado")
public class ManagedFarmController {
    private final ManagedFarmQueryUseCase managedFarmQuery;

    public ManagedFarmController(ManagedFarmQueryUseCase managedFarmQuery) {
        this.managedFarmQuery = managedFarmQuery;
    }

    @GetMapping("/managed")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_FARM_OWNER', 'ROLE_OPERATOR')")
    @Operation(summary = "Lista as fazendas disponíveis para gestão")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fazendas administráveis retornadas."),
            @ApiResponse(responseCode = "401", description = "Autenticação obrigatória."),
            @ApiResponse(responseCode = "403", description = "Papel sem acesso ao ambiente de gestão.")
    })
    public ResponseEntity<Page<ManagedFarmSummaryDTO>> findManagedFarms(
            @PageableDefault(size = 12, sort = "name", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam(required = false) String query) {
        PageResult<ManagedFarmSummaryDTO> result = managedFarmQuery
                .findManagedFarms(SpringPageMapper.toQuery(pageable), query)
                .map(vo -> new ManagedFarmSummaryDTO(vo.id(), vo.name(), vo.tod(), vo.logoUrl()));
        return ResponseEntity.ok(SpringPageMapper.toSpringPage(result, pageable));
    }
}
