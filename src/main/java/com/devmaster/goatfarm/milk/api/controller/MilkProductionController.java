package com.devmaster.goatfarm.milk.api.controller;

import com.devmaster.goatfarm.application.ports.in.MilkProductionUseCase;
import com.devmaster.goatfarm.milk.api.dto.MilkProductionRequestDTO;
import com.devmaster.goatfarm.milk.api.dto.MilkProductionResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/farms/{farmId}/goats/{goatId}/milk-productions")
@RequiredArgsConstructor
public class MilkProductionController {

    private final MilkProductionUseCase milkProductionUseCase;

    @PostMapping
    public ResponseEntity<MilkProductionResponseDTO> createMilkProduction(
            @PathVariable Long farmId,
            @PathVariable String goatId,
            @RequestBody MilkProductionRequestDTO request) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @GetMapping
    public ResponseEntity<List<MilkProductionResponseDTO>> getMilkProductions(
            @PathVariable Long farmId,
            @PathVariable String goatId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
