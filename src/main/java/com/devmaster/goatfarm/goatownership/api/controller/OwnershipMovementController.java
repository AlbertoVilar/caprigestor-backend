package com.devmaster.goatfarm.goatownership.api.controller;

import com.devmaster.goatfarm.application.pagination.PageResult;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.goatownership.api.dto.OwnershipMovementResponseDTO;
import com.devmaster.goatfarm.goatownership.api.mapper.OwnershipMovementApiMapper;
import com.devmaster.goatfarm.goatownership.application.model.OwnershipMovementDirection;
import com.devmaster.goatfarm.goatownership.application.model.OwnershipMovementKind;
import com.devmaster.goatfarm.goatownership.application.model.OwnershipMovementPageQuery;
import com.devmaster.goatfarm.goatownership.application.ports.in.OwnershipMovementQueryUseCase;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

/** HTTP adapter for the read-only, farm-scoped ownership movement history. */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Ownership Movement API", description = "Histórico administrativo de movimentos de propriedade.")
public class OwnershipMovementController {
    private final OwnershipMovementQueryUseCase queryUseCase;
    private final OwnershipMovementApiMapper mapper;

    public OwnershipMovementController(OwnershipMovementQueryUseCase queryUseCase,
                                       OwnershipMovementApiMapper mapper) {
        this.queryUseCase = queryUseCase;
        this.mapper = mapper;
    }

    @GetMapping("/goatfarms/{farmId}/ownership-movements")
    @Operation(summary = "Lista o histórico de entradas e saídas de propriedade da fazenda")
    public ResponseEntity<Page<OwnershipMovementResponseDTO>> listForFarm(
            @PathVariable Long farmId,
            @RequestParam String direction,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String kind,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var result = queryUseCase.listForFarm(farmId, parseDirection(direction), parseStatus(status),
                parseKind(kind), new OwnershipMovementPageQuery(page, size))
                .map(mapper::toResponse);
        return ResponseEntity.ok(toSpringPage(result));
    }

    private Page<OwnershipMovementResponseDTO> toSpringPage(PageResult<OwnershipMovementResponseDTO> result) {
        return new PageImpl<>(result.content(), PageRequest.of(result.page(), result.size()), result.totalElements());
    }

    private OwnershipMovementDirection parseDirection(String value) {
        try {
            return OwnershipMovementDirection.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException exception) {
            throw new InvalidArgumentException("direction must be INCOMING or OUTGOING");
        }
    }

    private OwnershipTransferStatus parseStatus(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return OwnershipTransferStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new InvalidArgumentException("status is invalid");
        }
    }

    private OwnershipMovementKind parseKind(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return OwnershipMovementKind.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new InvalidArgumentException("kind must be INTERNAL_TRANSFER or INTERNAL_SALE");
        }
    }
}
