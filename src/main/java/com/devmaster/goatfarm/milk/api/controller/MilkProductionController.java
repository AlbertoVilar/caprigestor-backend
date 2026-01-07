package com.devmaster.goatfarm.milk.api.controller;

import com.devmaster.goatfarm.application.ports.in.MilkProductionUseCase;
import com.devmaster.goatfarm.milk.api.dto.MilkProductionRequestDTO;
import com.devmaster.goatfarm.milk.api.dto.MilkProductionResponseDTO;
import com.devmaster.goatfarm.milk.mapper.MilkProductionMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/farms/{farmId}/goats/{goatId}/milk-productions")
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
            @RequestBody MilkProductionRequestDTO request) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @GetMapping
    @Operation(summary = "List milk productions for a goat with optional date filters")
    public ResponseEntity<List<MilkProductionResponseDTO>> getMilkProductions(
            @Parameter(description = "Farm identifier") @PathVariable Long farmId,
            @Parameter(description = "Goat identifier") @PathVariable String goatId,
            @Parameter(description = "Start date (inclusive)") @RequestParam(required = false) LocalDate from,
            @Parameter(description = "End date (inclusive)") @RequestParam(required = false) LocalDate to) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
