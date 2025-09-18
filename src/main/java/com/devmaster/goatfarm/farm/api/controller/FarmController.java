package com.devmaster.goatfarm.farm.api.controller;

import com.devmaster.goatfarm.farm.api.dto.GoatFarmResponseDTO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmResponseVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmFullResponseVO;
import com.devmaster.goatfarm.farm.converters.GoatFarmDTOConverter;
import com.devmaster.goatfarm.farm.facade.GoatFarmFacade;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/farms")
@CrossOrigin(origins = {"http://localhost:5500", "http://localhost:5173", "http://localhost:8080"})
public class FarmController {

    @Autowired
    private GoatFarmFacade farmFacade;

    @Operation(summary = "Listar todas as fazendas", description = "Endpoint público para listar fazendas")
    @GetMapping
    public ResponseEntity<List<GoatFarmResponseDTO>> getAllFarms(
            @PageableDefault(size = 20) Pageable pageable) {
        
        try {
            Page<GoatFarmFullResponseVO> farmsPage = farmFacade.findAllGoatFarm(pageable);
            
            List<GoatFarmResponseDTO> farms = farmsPage.getContent()
                .stream()
                .map(farm -> {
                    GoatFarmResponseDTO dto = new GoatFarmResponseDTO();
                    dto.setId(farm.getId());
                    dto.setName(farm.getName());
                    dto.setTod(farm.getTod());
                    return dto;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(farms);
            
        } catch (Exception e) {
            // Em caso de erro, retorna lista vazia
            return ResponseEntity.ok(List.of());
        }
    }

    @Operation(summary = "Obter fazenda por ID", description = "Endpoint público para obter detalhes de uma fazenda")
    @GetMapping("/{id}")
    public ResponseEntity<GoatFarmResponseDTO> getFarmById(@PathVariable Long id) {
        
        try {
            GoatFarmFullResponseVO farm = farmFacade.findGoatFarmById(id);
            
            GoatFarmResponseDTO dto = new GoatFarmResponseDTO();
            dto.setId(farm.getId());
            dto.setName(farm.getName());
            dto.setTod(farm.getTod());
            
            return ResponseEntity.ok(dto);
            
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}