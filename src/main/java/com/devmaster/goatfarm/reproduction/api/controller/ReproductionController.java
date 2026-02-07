package com.devmaster.goatfarm.reproduction.api.controller;

import com.devmaster.goatfarm.reproduction.application.ports.in.ReproductionCommandUseCase;
import com.devmaster.goatfarm.reproduction.application.ports.in.ReproductionQueryUseCase;
import com.devmaster.goatfarm.reproduction.api.dto.*;
import com.devmaster.goatfarm.reproduction.business.bo.*;
import com.devmaster.goatfarm.reproduction.api.mapper.ReproductionMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/goatfarms/{farmId}/goats/{goatId}/reproduction")
@PreAuthorize("hasAuthority('ROLE_ADMIN') or ((hasAuthority('ROLE_OPERATOR') or hasAuthority('ROLE_FARM_OWNER')) and @ownershipService.isFarmOwner(#farmId))")
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

    @PostMapping("/breeding/{coverageEventId}/corrections")
    @Operation(summary = "Registrar correção de cobertura")
    public ResponseEntity<ReproductiveEventResponseDTO> correctCoverage(
            @Parameter(description = "Identificador da fazenda") @PathVariable Long farmId,
            @Parameter(description = "Identificador da cabra") @PathVariable String goatId,
            @Parameter(description = "Identificador do evento de cobertura") @PathVariable Long coverageEventId,
            @Valid @RequestBody CoverageCorrectionRequestDTO request) {
        CoverageCorrectionRequestVO vo = mapper.toCoverageCorrectionRequestVO(request);
        ReproductiveEventResponseVO responseVO = commandUseCase.correctCoverage(farmId, goatId, coverageEventId, vo);
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

    @PostMapping("/pregnancies/checks")
    @Operation(summary = "Registrar diagnóstico negativo de prenhez",
            description = "Registra NEGATIVE e, se houver gestação ativa, encerra como falso positivo.")
    public ResponseEntity<ReproductiveEventResponseDTO> registerPregnancyCheck(
            @Parameter(description = "Identificador da fazenda") @PathVariable Long farmId,
            @Parameter(description = "Identificador da cabra") @PathVariable String goatId,
            @Valid @RequestBody PregnancyCheckRequestDTO request) {
        PregnancyCheckRequestVO vo = mapper.toPregnancyCheckRequestVO(request);
        ReproductiveEventResponseVO responseVO = commandUseCase.registerPregnancyCheck(farmId, goatId, vo);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toReproductiveEventResponseDTO(responseVO));
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

    @GetMapping("/pregnancies/diagnosis-recommendation")
    @Operation(summary = "Obter recomendação para diagnóstico de prenhez")
    public ResponseEntity<DiagnosisRecommendationResponseDTO> getDiagnosisRecommendation(
            @Parameter(description = "Identificador da fazenda") @PathVariable Long farmId,
            @Parameter(description = "Identificador da cabra") @PathVariable String goatId,
            @Parameter(description = "Data de referência (ISO)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate referenceDate) {
        DiagnosisRecommendationResponseVO responseVO = queryUseCase.getDiagnosisRecommendation(farmId, goatId, referenceDate);
        return ResponseEntity.ok(mapper.toDiagnosisRecommendationResponseDTO(responseVO));
    }
}
