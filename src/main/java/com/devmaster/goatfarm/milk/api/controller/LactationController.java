package com.devmaster.goatfarm.milk.api.controller;

import com.devmaster.goatfarm.application.ports.in.LactationCommandUseCase;
import com.devmaster.goatfarm.application.ports.in.LactationQueryUseCase;
import com.devmaster.goatfarm.milk.api.dto.LactationRequestDTO;
import com.devmaster.goatfarm.milk.api.dto.LactationDryRequestDTO;
import com.devmaster.goatfarm.milk.api.dto.LactationResponseDTO;
import com.devmaster.goatfarm.milk.business.bo.LactationRequestVO;
import com.devmaster.goatfarm.milk.business.bo.LactationDryRequestVO;
import com.devmaster.goatfarm.milk.business.bo.LactationResponseVO;
import com.devmaster.goatfarm.milk.mapper.LactationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/goatfarms/{farmId}/goats/{goatId}/lactations")
@RequiredArgsConstructor
@Tag(name = "Lactation API", description = "Gestão de lactações")
public class LactationController {

    private final LactationQueryUseCase lactationQueryUseCase;
    private final LactationCommandUseCase lactationCommandUseCase;
    private final LactationMapper lactationMapper;

    @PostMapping
    @Operation(summary = "Iniciar nova lactação")
    public ResponseEntity<LactationResponseDTO> openLactation(
            @Parameter(description = "Identificador da fazenda") @PathVariable Long farmId,
            @Parameter(description = "Identificador da cabra") @PathVariable String goatId,
            @Valid @RequestBody LactationRequestDTO request) {
        LactationRequestVO requestVO = lactationMapper.toRequestVO(request);
        LactationResponseVO responseVO = lactationCommandUseCase.openLactation(farmId, goatId, requestVO);
        return ResponseEntity.status(HttpStatus.CREATED).body(lactationMapper.toResponseDTO(responseVO));
    }

    @GetMapping("/active")
    @Operation(summary = "Buscar lactação ativa")
    public ResponseEntity<LactationResponseDTO> getActiveLactation(
            @Parameter(description = "Identificador da fazenda") @PathVariable Long farmId,
            @Parameter(description = "Identificador da cabra") @PathVariable String goatId) {
        LactationResponseVO responseVO = lactationQueryUseCase.getActiveLactation(farmId, goatId);
        return ResponseEntity.ok(lactationMapper.toResponseDTO(responseVO));
    }

    @PatchMapping("/{lactationId}/dry")
    @Operation(summary = "Encerrar lactação (secagem)")
    public ResponseEntity<LactationResponseDTO> dryLactation(
            @Parameter(description = "Identificador da fazenda") @PathVariable Long farmId,
            @Parameter(description = "Identificador da cabra") @PathVariable String goatId,
            @Parameter(description = "Identificador da lactação") @PathVariable Long lactationId,
            @Valid @RequestBody LactationDryRequestDTO request) {
        LactationDryRequestVO requestVO = lactationMapper.toDryRequestVO(request);
        LactationResponseVO responseVO = lactationCommandUseCase.dryLactation(farmId, goatId, lactationId, requestVO);
        return ResponseEntity.ok(lactationMapper.toResponseDTO(responseVO));
    }

    @GetMapping("/{lactationId}")
    @Operation(summary = "Get lactation by ID")
    public ResponseEntity<LactationResponseDTO> getLactationById(
            @Parameter(description = "Farm identifier") @PathVariable Long farmId,
            @Parameter(description = "Goat identifier") @PathVariable String goatId,
            @Parameter(description = "Lactation identifier") @PathVariable Long lactationId) {
        LactationResponseVO responseVO = lactationQueryUseCase.getLactationById(farmId, goatId, lactationId);
        return ResponseEntity.ok(lactationMapper.toResponseDTO(responseVO));
    }

    @GetMapping
    @Operation(summary = "Listar todas as lactações (histórico)")
    public ResponseEntity<Page<LactationResponseDTO>> getAllLactations(
            @Parameter(description = "Identificador da fazenda") @PathVariable Long farmId,
            @Parameter(description = "Identificador da cabra") @PathVariable String goatId,
            @PageableDefault(sort = "startDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<LactationResponseVO> pageVO = lactationQueryUseCase.getAllLactations(farmId, goatId, pageable);
        // Map Page<VO> to Page<DTO> manually since MapStruct doesn't support Page mapping out of the box easily without wrapper
        Page<LactationResponseDTO> pageDTO = pageVO.map(lactationMapper::toResponseDTO);
        return ResponseEntity.ok(pageDTO);
    }
}
