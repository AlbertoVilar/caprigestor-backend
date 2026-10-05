package com.devmaster.goatfarm.goatownership.api.controller;

import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.api.dto.OwnershipHistoryResponseDTO;
import com.devmaster.goatfarm.goatownership.api.mapper.OwnershipHistoryApiMapper;
import com.devmaster.goatfarm.goatownership.application.ports.in.GoatOwnershipHistoryQueryUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Authenticated HTTP adapter for the canonical goat ownership history. */
@RestController
@RequestMapping("/api/v1/goats")
@Tag(name = "Goat Ownership History API", description = "Histórico canônico de propriedade do animal.")
public class GoatOwnershipHistoryController {
    private final GoatOwnershipHistoryQueryUseCase queryUseCase;
    private final OwnershipHistoryApiMapper mapper;

    public GoatOwnershipHistoryController(GoatOwnershipHistoryQueryUseCase queryUseCase,
                                          OwnershipHistoryApiMapper mapper) {
        this.queryUseCase = queryUseCase;
        this.mapper = mapper;
    }

    @GetMapping("/{goatId}/ownership-history")
    @Operation(summary = "Consulta o histórico de propriedade de um animal")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Histórico retornado com sucesso."),
            @ApiResponse(responseCode = "400", description = "GoatId estrutural inválido."),
            @ApiResponse(responseCode = "401", description = "Autenticação obrigatória."),
            @ApiResponse(responseCode = "403", description = "Sem autorização para consultar o histórico."),
            @ApiResponse(responseCode = "404", description = "Animal não encontrado."),
            @ApiResponse(responseCode = "422", description = "Inconsistência na propriedade canônica.")
    })
    public ResponseEntity<OwnershipHistoryResponseDTO> findHistory(@PathVariable String goatId) {
        GoatId structuralGoatId = parseStructuralGoatId(goatId);
        return ResponseEntity.ok(mapper.toResponse(queryUseCase.findOwnershipHistory(structuralGoatId)));
    }

    private GoatId parseStructuralGoatId(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidArgumentException("goatId", "GoatId must be a positive number");
        }
        try {
            long parsed = Long.parseLong(value);
            if (parsed <= 0) {
                throw new InvalidArgumentException("goatId", "GoatId must be a positive number");
            }
            return new GoatId(parsed);
        } catch (NumberFormatException exception) {
            throw new InvalidArgumentException("goatId", "GoatId must be a positive number");
        }
    }
}
