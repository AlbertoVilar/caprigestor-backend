package com.devmaster.goatfarm.milk.api.controller;

import com.devmaster.goatfarm.application.ports.in.MilkProductionUseCase;
import com.devmaster.goatfarm.milk.api.dto.MilkProductionRequestDTO;
import com.devmaster.goatfarm.milk.api.dto.MilkProductionResponseDTO;
import com.devmaster.goatfarm.milk.api.dto.MilkProductionUpdateRequestDTO;
import com.devmaster.goatfarm.milk.business.bo.MilkProductionRequestVO;
import com.devmaster.goatfarm.milk.business.bo.MilkProductionResponseVO;
import com.devmaster.goatfarm.milk.business.bo.MilkProductionUpdateRequestVO;
import com.devmaster.goatfarm.milk.mapper.MilkProductionMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/goatfarms/{farmId}/goats/{goatId}/milk-productions")
@Tag(name = "Milk Production API", description = "Gestão de produção de leite")
public class MilkProductionController {

    private final MilkProductionUseCase milkProductionUseCase;
    private final MilkProductionMapper milkProductionMapper;

    public MilkProductionController(MilkProductionUseCase milkProductionUseCase, MilkProductionMapper milkProductionMapper) {
        this.milkProductionUseCase = milkProductionUseCase;
        this.milkProductionMapper = milkProductionMapper;
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or ((hasAuthority('ROLE_OPERATOR') or hasAuthority('ROLE_FARM_OWNER')) and @ownershipService.isFarmOwner(#farmId))")
    @PostMapping
    @Operation(summary = "Registrar produção de leite diária")
    public ResponseEntity<MilkProductionResponseDTO> createMilkProduction(
            @Parameter(description = "Identificador da fazenda") @PathVariable Long farmId,
            @Parameter(description = "Identificador da cabra") @PathVariable String goatId,
            @Valid @RequestBody MilkProductionRequestDTO request) {
        MilkProductionRequestVO requestVO = milkProductionMapper.toRequestVO(request);
        MilkProductionResponseVO responseVO = milkProductionUseCase.createMilkProduction(farmId, goatId, requestVO);
        return ResponseEntity.status(HttpStatus.CREATED).body(milkProductionMapper.toResponseDTO(responseVO));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or ((hasAuthority('ROLE_OPERATOR') or hasAuthority('ROLE_FARM_OWNER')) and @ownershipService.isFarmOwner(#farmId))")
    @PatchMapping("/{id}")
    @Operation(summary = "Update a milk production entry (partial update)")
    public ResponseEntity<MilkProductionResponseDTO> update(
            @Parameter(description = "Farm identifier") @PathVariable Long farmId,
            @Parameter(description = "Goat identifier") @PathVariable String goatId,
            @Parameter(description = "Milk production identifier") @PathVariable Long id,
            @Valid @RequestBody MilkProductionUpdateRequestDTO request
    ) {
        MilkProductionUpdateRequestVO requestVO = milkProductionMapper.toRequestVO(request);
        MilkProductionResponseVO responseVO = milkProductionUseCase.update(farmId, goatId, id, requestVO);
        return ResponseEntity.ok(milkProductionMapper.toResponseDTO(responseVO));
    }



    @Operation(summary = "Buscar produção de leite por identificador")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or ((hasAuthority('ROLE_OPERATOR') or hasAuthority('ROLE_FARM_OWNER')) and @ownershipService.isFarmOwner(#farmId))")
    @GetMapping("/{id}")
    public ResponseEntity<MilkProductionResponseDTO> findById(
            @Parameter(description = "Identificador da fazenda") @PathVariable Long farmId,
            @Parameter(description = "Identificador da cabra") @PathVariable String goatId,
            @Parameter(description = "Identificador da produção de leite") @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                milkProductionMapper.toResponseDTO(
                        milkProductionUseCase.findById(farmId, goatId, id)
                )
        );
    }

    @Operation(summary = "Listar produções de leite (filtro opcional por data)")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or ((hasAuthority('ROLE_OPERATOR') or hasAuthority('ROLE_FARM_OWNER')) and @ownershipService.isFarmOwner(#farmId))")
    @GetMapping
    public ResponseEntity<Page<MilkProductionResponseDTO>> getMilkProductions(
            @Parameter(description = "Identificador da fazenda") @PathVariable Long farmId,
            @Parameter(description = "Identificador da cabra") @PathVariable String goatId,

            @Parameter(description = "Data inicial (inclusiva) no formato ISO yyyy-MM-dd")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,

            @Parameter(description = "Data final (inclusiva) no formato ISO yyyy-MM-dd")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to,

            @ParameterObject
            @PageableDefault(size = 12)
            @SortDefault.SortDefaults({
                    @SortDefault(sort = "date", direction = Sort.Direction.DESC),
                    @SortDefault(sort = "shift", direction = Sort.Direction.ASC),
                    @SortDefault(sort = "id", direction = Sort.Direction.DESC)
            })
            Pageable pageable
    ) {
        Page<MilkProductionResponseVO> page =
                milkProductionUseCase.getMilkProductions(farmId, goatId, from, to, pageable);

        Page<MilkProductionResponseDTO> dtoPage =
                page.map(milkProductionMapper::toResponseDTO);

        return ResponseEntity.ok(dtoPage);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or ((hasAuthority('ROLE_OPERATOR') or hasAuthority('ROLE_FARM_OWNER')) and @ownershipService.isFarmOwner(#farmId))")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a milk production entry")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Farm identifier") @PathVariable Long farmId,
            @Parameter(description = "Goat identifier") @PathVariable String goatId,
            @Parameter(description = "Milk production identifier") @PathVariable Long id
    ) {
        milkProductionUseCase.delete(farmId, goatId, id);
        return ResponseEntity.noContent().build();
    }
}
