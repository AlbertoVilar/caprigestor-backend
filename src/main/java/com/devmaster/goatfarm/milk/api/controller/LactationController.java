package com.devmaster.goatfarm.milk.api.controller;

import com.devmaster.goatfarm.application.ports.in.LactationCommandUseCase;
import com.devmaster.goatfarm.application.ports.in.LactationQueryUseCase;
import com.devmaster.goatfarm.milk.api.dto.LactationRequestDTO;
import com.devmaster.goatfarm.milk.api.dto.LactationResponseDTO;
import com.devmaster.goatfarm.milk.business.bo.LactationRequestVO;
import com.devmaster.goatfarm.milk.business.bo.LactationResponseVO;
import com.devmaster.goatfarm.milk.mapper.LactationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/goatfarms/{farmId}/goats/{goatId}/lactations")
@RequiredArgsConstructor
@Tag(name = "Lactation API", description = "Lactation management for goats")
public class LactationController {

    private final LactationQueryUseCase lactationQueryUseCase;
    private final LactationCommandUseCase lactationCommandUseCase;
    private final LactationMapper lactationMapper;

    @PostMapping
    @Operation(summary = "Open a new lactation for a goat")
    public ResponseEntity<LactationResponseDTO> openLactation(
            @Parameter(description = "Farm identifier") @PathVariable Long farmId,
            @Parameter(description = "Goat identifier") @PathVariable String goatId,
            @Valid @RequestBody LactationRequestDTO request) {
        LactationRequestVO requestVO = lactationMapper.toRequestVO(request);
        LactationResponseVO responseVO = lactationCommandUseCase.openLactation(farmId, goatId, requestVO);
        return ResponseEntity.ok(lactationMapper.toResponseDTO(responseVO));
    }

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
