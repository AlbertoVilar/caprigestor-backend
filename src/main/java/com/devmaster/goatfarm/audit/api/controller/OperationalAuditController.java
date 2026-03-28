package com.devmaster.goatfarm.audit.api.controller;

import com.devmaster.goatfarm.audit.api.dto.OperationalAuditEntryDTO;
import com.devmaster.goatfarm.audit.api.mapper.OperationalAuditApiMapper;
import com.devmaster.goatfarm.audit.application.ports.in.OperationalAuditUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/goatfarms/{farmId}/audit", "/api/goatfarms/{farmId}/audit"})
@Tag(name = "Operational Audit API", description = "Trilha minima de auditoria operacional para operacoes criticas.")
public class OperationalAuditController {

    private final OperationalAuditUseCase operationalAuditUseCase;
    private final OperationalAuditApiMapper operationalAuditApiMapper;

    public OperationalAuditController(
            OperationalAuditUseCase operationalAuditUseCase,
            OperationalAuditApiMapper operationalAuditApiMapper
    ) {
        this.operationalAuditUseCase = operationalAuditUseCase;
        this.operationalAuditApiMapper = operationalAuditApiMapper;
    }

    @PreAuthorize("@ownershipService.canManageFarm(#farmId)")
    @GetMapping("/entries")
    @Operation(summary = "Listar operacoes criticas auditadas por fazenda ou animal")
    public ResponseEntity<List<OperationalAuditEntryDTO>> listEntries(
            @PathVariable Long farmId,
            @RequestParam(required = false) String goatId,
            @RequestParam(defaultValue = "15") int limit
    ) {
        return ResponseEntity.ok(
                operationalAuditUseCase.listEntries(farmId, goatId, limit).stream()
                        .map(operationalAuditApiMapper::toDTO)
                        .toList()
        );
    }
}
