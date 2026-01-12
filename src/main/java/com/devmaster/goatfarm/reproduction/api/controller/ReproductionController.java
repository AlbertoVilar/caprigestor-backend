package com.devmaster.goatfarm.reproduction.api.controller;

import com.devmaster.goatfarm.application.ports.in.ReproductionCommandUseCase;
import com.devmaster.goatfarm.application.ports.in.ReproductionQueryUseCase;
import com.devmaster.goatfarm.reproduction.api.dto.*;
import com.devmaster.goatfarm.reproduction.business.bo.*;
import com.devmaster.goatfarm.reproduction.mapper.ReproductionMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/goatfarms/{farmId}/goats/{goatId}/reproduction")
@RequiredArgsConstructor
public class ReproductionController {

    private final ReproductionCommandUseCase commandUseCase;
    private final ReproductionQueryUseCase queryUseCase;
    private final ReproductionMapper mapper;

    @PostMapping("/breeding")
    @Operation(summary = "Register a breeding event (coverage)")
    public ResponseEntity<ReproductiveEventResponseDTO> registerBreeding(
            @Parameter(description = "Farm identifier") @PathVariable Long farmId,
            @Parameter(description = "Goat identifier") @PathVariable String goatId,
            @Valid @RequestBody BreedingRequestDTO request) {
        BreedingRequestVO vo = mapper.toBreedingRequestVO(request);
        ReproductiveEventResponseVO responseVO = commandUseCase.registerBreeding(farmId, goatId, vo);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toReproductiveEventResponseDTO(responseVO));
    }

    @PatchMapping("/pregnancies/confirm")
    @Operation(summary = "Confirm pregnancy and create/activate it")
    public ResponseEntity<PregnancyResponseDTO> confirmPregnancy(
            @Parameter(description = "Farm identifier") @PathVariable Long farmId,
            @Parameter(description = "Goat identifier") @PathVariable String goatId,
            @Valid @RequestBody PregnancyConfirmRequestDTO request) {
        PregnancyConfirmRequestVO vo = mapper.toPregnancyConfirmRequestVO(request);
        PregnancyResponseVO responseVO = commandUseCase.confirmPregnancy(farmId, goatId, vo);
        return ResponseEntity.ok(mapper.toPregnancyResponseDTO(responseVO));
    }

    @GetMapping("/pregnancies/active")
    @Operation(summary = "Get active pregnancy (if exists)")
    public ResponseEntity<PregnancyResponseDTO> getActivePregnancy(
            @Parameter(description = "Farm identifier") @PathVariable Long farmId,
            @Parameter(description = "Goat identifier") @PathVariable String goatId) {
        PregnancyResponseVO responseVO = queryUseCase.getActivePregnancy(farmId, goatId);
        return ResponseEntity.ok(mapper.toPregnancyResponseDTO(responseVO));
    }

    @PatchMapping("/pregnancies/{pregnancyId}/close")
    @Operation(summary = "Close pregnancy (e.g., BORN, LOST, ABORTED)")
    public ResponseEntity<PregnancyResponseDTO> closePregnancy(
            @Parameter(description = "Farm identifier") @PathVariable Long farmId,
            @Parameter(description = "Goat identifier") @PathVariable String goatId,
            @Parameter(description = "Pregnancy identifier") @PathVariable Long pregnancyId,
            @Valid @RequestBody PregnancyCloseRequestDTO request) {
        PregnancyCloseRequestVO vo = mapper.toPregnancyCloseRequestVO(request);
        PregnancyResponseVO responseVO = commandUseCase.closePregnancy(farmId, goatId, pregnancyId, vo);
        return ResponseEntity.ok(mapper.toPregnancyResponseDTO(responseVO));
    }

    @GetMapping("/events")
    @Operation(summary = "Get reproductive events history (paginated)")
    public ResponseEntity<Page<ReproductiveEventResponseDTO>> getReproductiveEvents(
            @Parameter(description = "Farm identifier") @PathVariable Long farmId,
            @Parameter(description = "Goat identifier") @PathVariable String goatId,
            @PageableDefault(sort = "eventDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ReproductiveEventResponseVO> pageVO = queryUseCase.getReproductiveEvents(farmId, goatId, pageable);
        Page<ReproductiveEventResponseDTO> pageDTO = pageVO.map(mapper::toReproductiveEventResponseDTO);
        return ResponseEntity.ok(pageDTO);
    }

    @GetMapping("/pregnancies")
    @Operation(summary = "Get pregnancy history (paginated)")
    public ResponseEntity<Page<PregnancyResponseDTO>> getPregnancies(
            @Parameter(description = "Farm identifier") @PathVariable Long farmId,
            @Parameter(description = "Goat identifier") @PathVariable String goatId,
            @PageableDefault(sort = "breedingDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PregnancyResponseVO> pageVO = queryUseCase.getPregnancies(farmId, goatId, pageable);
        Page<PregnancyResponseDTO> pageDTO = pageVO.map(mapper::toPregnancyResponseDTO);
        return ResponseEntity.ok(pageDTO);
    }
}
