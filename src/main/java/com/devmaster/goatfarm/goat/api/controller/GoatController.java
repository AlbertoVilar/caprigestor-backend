package com.devmaster.goatfarm.goat.api.controller;

import com.devmaster.goatfarm.goat.api.dto.GoatRequestDTO;
import com.devmaster.goatfarm.goat.api.dto.GoatResponseDTO;
import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.mapper.GoatMapper;
import com.devmaster.goatfarm.goat.facade.GoatFacade;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping({"/api", "/api/goatfarms"})
@Tag(name = "Goat API", description = "Gerenciamento de cabras na fazenda")
public class GoatController {

    private final GoatFacade goatFacade;
    private final GoatMapper goatMapper;

    @Autowired
    public GoatController(GoatFacade goatFacade, GoatMapper goatMapper) {
        this.goatFacade = goatFacade;
        this.goatMapper = goatMapper;
    }

    /**
     * Cadastra uma nova cabra no sistema.
     */
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_OPERATOR')")
    @PostMapping("/goats")
    @Operation(summary = "Cadastra uma nova cabra no sistema",
            description = "Cria um novo registro de cabra, associando-a a um usuÃ¡rio e fazenda existentes.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cabra cadastrada com sucesso."),
            @ApiResponse(responseCode = "400", description = "Dados invÃ¡lidos ou nÃºmero de registro jÃ¡ existe."),
            @ApiResponse(responseCode = "403", description = "Acesso negado."),
            @ApiResponse(responseCode = "404", description = "UsuÃ¡rio ou fazenda nÃ£o encontrados.")
    })
    public ResponseEntity<GoatResponseDTO> createGoat(
            @Parameter(description = "Dados da cabra para cadastro", required = true)
            @Valid @RequestBody GoatRequestDTO goatRequestDTO) {

        GoatRequestVO requestVO = goatMapper.toRequestVO(goatRequestDTO);
        Long userId = goatRequestDTO.getUserId() != null ? goatRequestDTO.getUserId() : 0L;
        Long farmId = goatRequestDTO.getFarmId() != null ? goatRequestDTO.getFarmId() : 0L;
        GoatResponseVO vo = goatFacade.createGoat(requestVO, userId, farmId);
        return ResponseEntity.status(HttpStatus.CREATED).body(goatMapper.toResponseDTO(vo));
    }

    /**
     * Atualiza os dados de uma cabra existente.
     */
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_OPERATOR')")
    @PutMapping("/goats/{registrationNumber}")
    @Operation(summary = "Atualiza os dados de uma cabra existente",
            description = "Modifica os dados de uma cabra especÃ­fica pelo seu nÃºmero de registro.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cabra atualizada com sucesso."),
            @ApiResponse(responseCode = "400", description = "Dados invÃ¡lidos."),
            @ApiResponse(responseCode = "403", description = "Acesso negado."),
            @ApiResponse(responseCode = "404", description = "Cabra nÃ£o encontrada.")
    })
    public ResponseEntity<GoatResponseDTO> updateGoat(
            @Parameter(description = "NÃºmero de registro da cabra", example = "2114517012", required = true)
            @PathVariable String registrationNumber,
            @Parameter(description = "Novos dados da cabra", required = true)
            @Valid @RequestBody GoatRequestDTO goatRequestDTO) {

        GoatRequestVO requestVO = goatMapper.toRequestVO(goatRequestDTO);
        GoatResponseVO vo = goatFacade.updateGoat(registrationNumber, requestVO);
        return ResponseEntity.ok(goatMapper.toResponseDTO(vo));
    }

    /**
     * Remove uma cabra pelo nÃºmero de registro.
     */
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_OPERATOR')")
    @DeleteMapping("/goats/{registrationNumber}")
    @Operation(summary = "Remove uma cabra do sistema",
            description = "Exclui uma cabra do sistema pelo seu nÃºmero de registro.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cabra removida com sucesso."),
            @ApiResponse(responseCode = "403", description = "Acesso negado."),
            @ApiResponse(responseCode = "404", description = "Cabra nÃ£o encontrada."),
            @ApiResponse(responseCode = "409", description = "Conflito: Cabra referenciada por outro animal.")
    })
    public ResponseEntity<Void> deleteGoat(
            @Parameter(description = "NÃºmero de registro da cabra a ser removida", example = "2114517012", required = true)
            @PathVariable String registrationNumber) {

        goatFacade.deleteGoat(registrationNumber);
        return ResponseEntity.noContent().build();
    }

            
    /**
     * Busca paginada de todas as cabras.
     */
    @GetMapping("/goats")
    @Operation(summary = "Busca paginada de todas as cabras")
    public ResponseEntity<Map<String, Object>> findAllGoats(@PageableDefault(size = 12) Pageable pageable) {
        Page<GoatResponseVO> page = goatFacade.findAllGoats(pageable);
        Map<String, Object> body = new HashMap<>();
        body.put("content", page.map(goatMapper::toResponseDTO).getContent());
        body.put("totalElements", page.getTotalElements());
        return ResponseEntity.ok(body);
    }

    /**
     * Busca paginada por cabras usando parte do nome.
     */
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_OPERATOR')")
    @GetMapping("/goats/name")
    @Operation(summary = "Busca paginada por cabras usando parte do nome")
    public ResponseEntity<Map<String, Object>> searchGoatByName(
            @Parameter(description = "Nome ou parte do nome da cabra", example = "NAIDE")
            @RequestParam(defaultValue = "") String name,
            @PageableDefault(size = 12) Pageable pageable) {
        Page<GoatResponseVO> page = goatFacade.searchGoatByName(name, pageable);
        Map<String, Object> body = new HashMap<>();
        body.put("content", page.map(goatMapper::toResponseDTO).getContent());
        body.put("totalElements", page.getTotalElements());
        return ResponseEntity.ok(body);
    }

    /**
     * Lista cabras de uma fazenda com filtros opcionais.
     */
    @GetMapping({"/{farmId}/goats", "/goats/farm/{farmId}"})
    @Operation(summary = "Busca cabras por fazenda e filtros opcionais")
    public ResponseEntity<Map<String, Object>> findGoatsByFarmId(
            @Parameter(description = "ID do capril", example = "1", required = true)
            @PathVariable Long farmId,
            @Parameter(description = "NÃºmero de registro da cabra")
            @RequestParam(value = "registrationNumber", required = false) String registrationNumber,
            @Parameter(description = "Nome ou parte do nome da cabra")
            @RequestParam(value = "name", required = false) String name,
            @PageableDefault(size = 12) Pageable pageable) {
        Page<GoatResponseVO> page;
        if (name != null && !name.isBlank()) {
            page = goatFacade.findGoatsByNameAndFarmId(farmId, name, pageable);
        } else {
            page = goatFacade.findGoatsByFarmIdAndRegistrationNumber(farmId, registrationNumber, pageable);
        }
        Map<String, Object> body = new HashMap<>();
        body.put("content", page.map(goatMapper::toResponseDTO).getContent());
        body.put("totalElements", page.getTotalElements());
        return ResponseEntity.ok(body);
    }

    /**
     * Busca uma cabra pelo seu nÃºmero de registro.
     */
    @GetMapping({"/goats/registration/{registrationNumber}", "/goats/{registrationNumber}"})
    @Operation(summary = "Busca uma cabra pelo nÃºmero de registro")
    public ResponseEntity<GoatResponseDTO> findByRegistrationNumber(@PathVariable String registrationNumber) {
        GoatResponseVO goatVO = goatFacade.findGoatByRegistrationNumber(registrationNumber);
        return ResponseEntity.ok(goatMapper.toResponseDTO(goatVO));
    }
}
