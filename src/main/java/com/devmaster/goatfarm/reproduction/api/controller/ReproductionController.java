package com.devmaster.goatfarm.reproduction.api.controller;

import com.devmaster.goatfarm.application.ports.in.ReproductionCommandUseCase;
import com.devmaster.goatfarm.application.ports.in.ReproductionQueryUseCase;
import com.devmaster.goatfarm.reproduction.api.dto.*;
import com.devmaster.goatfarm.reproduction.business.bo.*;
import com.devmaster.goatfarm.reproduction.mapper.ReproductionMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/goatfarms/{farmId}/goats/{goatId}/reproduction")
public class ReproductionController {

    private final ReproductionCommandUseCase commandUseCase;
    private final ReproductionQueryUseCase queryUseCase;
    private final ReproductionMapper mapper;

    public ReproductionController(ReproductionCommandUseCase commandUseCase, ReproductionQueryUseCase queryUseCase, ReproductionMapper mapper) {
        this.commandUseCase = commandUseCase;
        this.queryUseCase = queryUseCase;
        this.mapper = mapper;
    }

    @PostMapping("/breeding")
    @Operation(summary = "Registrar um evento de cobertura")
    public ResponseEntity<ReproductiveEventResponseDTO> registerBreeding(
            @Parameter(description = "Identificador da fazenda") @PathVariable Long farmId,
            @Parameter(description = "Identificador da cabra") @PathVariable String goatId,
            @Valid @RequestBody BreedingRequestDTO request) {
        BreedingRequestVO vo = mapper.toBreedingRequestVO(request);
        ReproductiveEventResponseVO responseVO = commandUseCase.registerBreeding(farmId, goatId, vo);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toReproductiveEventResponseDTO(responseVO));
    }

    @PatchMapping("/pregnancies/confirm")
    @Operation(summary = "Confirmar gestação e ativar")
    public ResponseEntity<PregnancyResponseDTO> confirmPregnancy(
            @Parameter(description = "Identificador da fazenda") @PathVariable Long farmId,
            @Parameter(description = "Identificador da cabra") @PathVariable String goatId,
            @Valid @RequestBody PregnancyConfirmRequestDTO request) {
        PregnancyConfirmRequestVO vo = mapper.toPregnancyConfirmRequestVO(request);
        PregnancyResponseVO responseVO = commandUseCase.confirmPregnancy(farmId, goatId, vo);
        return ResponseEntity.ok(mapper.toPregnancyResponseDTO(responseVO));
    }

    @GetMapping("/pregnancies/active")
    @Operation(summary = "Buscar gestação ativa da cabra (se existir)")
    public ResponseEntity<PregnancyResponseDTO> getActivePregnancy(
            @Parameter(description = "Identificador da fazenda") @PathVariable Long farmId,
            @Parameter(description = "Identificador da cabra") @PathVariable String goatId) {
        PregnancyResponseVO responseVO = queryUseCase.getActivePregnancy(farmId, goatId);
        return ResponseEntity.ok(mapper.toPregnancyResponseDTO(responseVO));
    }

    @GetMapping("/pregnancies/{pregnancyId}")
    @Operation(summary = "Buscar gestação por identificador")
    public ResponseEntity<PregnancyResponseDTO> getPregnancyById(
            @Parameter(description = "Identificador da fazenda") @PathVariable Long farmId,
            @Parameter(description = "Identificador da cabra") @PathVariable String goatId,
            @Parameter(description = "Identificador da gestação") @PathVariable Long pregnancyId) {
        PregnancyResponseVO responseVO = queryUseCase.getPregnancyById(farmId, goatId, pregnancyId);
        return ResponseEntity.ok(mapper.toPregnancyResponseDTO(responseVO));
    }

    @PatchMapping("/pregnancies/{pregnancyId}/close")
    @Operation(summary = "Encerrar gestação (parto, perda, aborto, etc.)")
    public ResponseEntity<PregnancyResponseDTO> closePregnancy(
            @Parameter(description = "Identificador da fazenda") @PathVariable Long farmId,
            @Parameter(description = "Identificador da cabra") @PathVariable String goatId,
            @Parameter(description = "Identificador da gestação") @PathVariable Long pregnancyId,
            @Valid @RequestBody PregnancyCloseRequestDTO request) {
        PregnancyCloseRequestVO vo = mapper.toPregnancyCloseRequestVO(request);
        PregnancyResponseVO responseVO = commandUseCase.closePregnancy(farmId, goatId, pregnancyId, vo);
        return ResponseEntity.ok(mapper.toPregnancyResponseDTO(responseVO));
    }

    @GetMapping("/events")
    @Operation(summary = "Listar histórico de eventos reprodutivos (paginado)")
    public ResponseEntity<Page<ReproductiveEventResponseDTO>> getReproductiveEvents(
            @Parameter(description = "Identificador da fazenda") @PathVariable Long farmId,
            @Parameter(description = "Identificador da cabra") @PathVariable String goatId,
            @PageableDefault(sort = "eventDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ReproductiveEventResponseVO> pageVO = queryUseCase.getReproductiveEvents(farmId, goatId, pageable);
        Page<ReproductiveEventResponseDTO> pageDTO = pageVO.map(mapper::toReproductiveEventResponseDTO);
        return ResponseEntity.ok(pageDTO);
    }

    @GetMapping("/pregnancies")
    @Operation(summary = "Listar histórico de gestações da cabra (paginado)")
    public ResponseEntity<Page<PregnancyResponseDTO>> getPregnancies(
            @Parameter(description = "Identificador da fazenda") @PathVariable Long farmId,
            @Parameter(description = "Identificador da cabra") @PathVariable String goatId,
            @PageableDefault(sort = "breedingDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PregnancyResponseVO> pageVO = queryUseCase.getPregnancies(farmId, goatId, pageable);
        Page<PregnancyResponseDTO> pageDTO = pageVO.map(mapper::toPregnancyResponseDTO);
        return ResponseEntity.ok(pageDTO);
    }
}
