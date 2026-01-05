package com.devmaster.goatfarm.milk.api.controller;

import com.devmaster.goatfarm.application.ports.in.LactationCommandUseCase;
import com.devmaster.goatfarm.application.ports.in.LactationQueryUseCase;
import com.devmaster.goatfarm.milk.api.dto.LactationResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/farms/{farmId}/goats/{goatId}/lactations")
@RequiredArgsConstructor
public class LactationController {

    private final LactationQueryUseCase lactationQueryUseCase;
    private final LactationCommandUseCase lactationCommandUseCase;

    @GetMapping("/active")
    public ResponseEntity<LactationResponseDTO> getActiveLactation(
            @PathVariable Long farmId,
            @PathVariable String goatId) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @PostMapping("/{lactationId}/dry")
    public ResponseEntity<Void> dryLactation(
            @PathVariable Long farmId,
            @PathVariable String goatId,
            @PathVariable Long lactationId) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
