package com.devmaster.goatfarm.milk.api.controller;

import com.devmaster.goatfarm.application.ports.in.LactationCommandUseCase;
import com.devmaster.goatfarm.application.ports.in.LactationQueryUseCase;
import com.devmaster.goatfarm.milk.api.dto.LactationResponseDTO;
import com.devmaster.goatfarm.milk.mapper.LactationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/farms/{farmId}/goats/{goatId}/lactations")
@RequiredArgsConstructor
@Tag(name = "Lactation API", description = "Lactation management for goats")
public class LactationController {

    private final LactationQueryUseCase lactationQueryUseCase;
    private final LactationCommandUseCase lactationCommandUseCase;
    private final LactationMapper lactationMapper;

    @GetMapping("/active")
    @Operation(summary = "Get active lactation for a goat")
    public ResponseEntity<LactationResponseDTO> getActiveLactation(
            @Parameter(description = "Farm identifier") @PathVariable Long farmId,
            @Parameter(description = "Goat identifier") @PathVariable String goatId) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @PostMapping("/{lactationId}/dry")
    @Operation(summary = "Mark a lactation as dry")
    public ResponseEntity<Void> dryLactation(
            @Parameter(description = "Farm identifier") @PathVariable Long farmId,
            @Parameter(description = "Goat identifier") @PathVariable String goatId,
            @Parameter(description = "Lactation identifier") @PathVariable Long lactationId) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
