package com.devmaster.goatfarm.milk.api.controller;

import com.devmaster.goatfarm.application.ports.in.MilkProductionUseCase;
import com.devmaster.goatfarm.milk.api.dto.MilkProductionRequestDTO;
import com.devmaster.goatfarm.milk.api.dto.MilkProductionResponseDTO;
import com.devmaster.goatfarm.milk.business.bo.MilkProductionRequestVO;
import com.devmaster.goatfarm.milk.business.bo.MilkProductionResponseVO;
import com.devmaster.goatfarm.milk.mapper.MilkProductionMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/goatfarms/{farmId}/goats/{goatId}/milk-productions")
@RequiredArgsConstructor
@Tag(name = "Milk Production API", description = "Milk production management for goats")
public class MilkProductionController {

    private final MilkProductionUseCase milkProductionUseCase;
    private final MilkProductionMapper milkProductionMapper;

    @PostMapping
    @Operation(summary = "Create a daily milk production entry for a goat")
    public ResponseEntity<MilkProductionResponseDTO> createMilkProduction(
            @Parameter(description = "Farm identifier") @PathVariable Long farmId,
            @Parameter(description = "Goat identifier") @PathVariable String goatId,
            @Valid @RequestBody MilkProductionRequestDTO request) {
        MilkProductionRequestVO requestVO = milkProductionMapper.toRequestVO(request);
        MilkProductionResponseVO responseVO = milkProductionUseCase.createMilkProduction(farmId, goatId, requestVO);
        return ResponseEntity.status(HttpStatus.CREATED).body(milkProductionMapper.toResponseDTO(responseVO));
    }

    @Operation(summary = "List milk productions (optional date filter)")
    @GetMapping
    public ResponseEntity<Page<MilkProductionResponseDTO>> getMilkProductions(
            @Parameter(description = "Farm id") @PathVariable Long farmId,
            @Parameter(description = "Goat id") @PathVariable String goatId,

            @Parameter(description = "Start date (inclusive) in ISO format yyyy-MM-dd")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,

            @Parameter(description = "End date (inclusive) in ISO format yyyy-MM-dd")
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

}
