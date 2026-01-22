package com.devmaster.goatfarm.farm.api.controller;

import com.devmaster.goatfarm.farm.api.dto.GoatFarmFullRequestDTO;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmFullResponseDTO;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmUpdateRequestDTO;
import com.devmaster.goatfarm.farm.api.dto.FarmPermissionsDTO;
import com.devmaster.goatfarm.application.ports.in.GoatFarmManagementUseCase;
import com.devmaster.goatfarm.farm.mapper.GoatFarmMapper;
import com.devmaster.goatfarm.authority.mapper.UserMapper;
import com.devmaster.goatfarm.address.mapper.AddressMapper;
import com.devmaster.goatfarm.phone.mapper.PhoneMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/goatfarms")
public class GoatFarmController {

    private final GoatFarmManagementUseCase farmUseCase;
    private final GoatFarmMapper farmMapper;
    private final UserMapper userMapper;
    private final AddressMapper addressMapper;
    private final PhoneMapper phoneMapper;

    public GoatFarmController(GoatFarmManagementUseCase farmUseCase,
                              GoatFarmMapper farmMapper,
                              UserMapper userMapper,
                              AddressMapper addressMapper,
                              PhoneMapper phoneMapper) {
        this.farmUseCase = farmUseCase;
        this.farmMapper = farmMapper;
        this.userMapper = userMapper;
        this.addressMapper = addressMapper;
        this.phoneMapper = phoneMapper;
    }

    @PostMapping
    public ResponseEntity<GoatFarmFullResponseDTO> createGoatFarm(@RequestBody @Valid GoatFarmFullRequestDTO requestDTO) {
        var responseVO = farmUseCase.createGoatFarm(farmMapper.toFullRequestVO(requestDTO));
        return new ResponseEntity<>(farmMapper.toFullDTO(responseVO), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_OPERATOR')")
    @PutMapping("/{id}")
    public ResponseEntity<GoatFarmFullResponseDTO> updateGoatFarm(@PathVariable Long id, @RequestBody @Valid GoatFarmUpdateRequestDTO requestDTO) {
        var responseVO = farmUseCase.updateGoatFarm(
                id,
                farmMapper.toRequestVO(requestDTO.getFarm()),
                userMapper.toRequestVO(requestDTO.getUser()),
                addressMapper.toVO(requestDTO.getAddress()),
                requestDTO.getPhones().stream().map(phoneMapper::toRequestVO).collect(java.util.stream.Collectors.toList())
        );
        return ResponseEntity.ok(farmMapper.toFullDTO(responseVO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GoatFarmFullResponseDTO> findGoatFarmById(@PathVariable Long id) {
        return ResponseEntity.ok(farmMapper.toFullDTO(farmUseCase.findGoatFarmById(id)));
    }

    @GetMapping("/name")
    public ResponseEntity<Page<GoatFarmFullResponseDTO>> searchGoatFarmByName(
            @RequestParam(value = "name", defaultValue = "") String name,
            @PageableDefault(size = 12, page = 0) Pageable pageable) {
        return ResponseEntity.ok(farmUseCase.searchGoatFarmByName(name, pageable).map(farmMapper::toFullDTO));
    }

    @GetMapping
    public ResponseEntity<Page<GoatFarmFullResponseDTO>> findAllGoatFarm(@PageableDefault(size = 12, page = 0) Pageable pageable) {
        return ResponseEntity.ok(farmUseCase.findAllGoatFarm(pageable).map(farmMapper::toFullDTO));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_OPERATOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoatFarm(@PathVariable Long id) {
        farmUseCase.deleteGoatFarm(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_OPERATOR')")
    @GetMapping("/{farmId}/permissions")
    public ResponseEntity<FarmPermissionsDTO> getFarmPermissions(@PathVariable Long farmId) {
        var vo = farmUseCase.getFarmPermissions(farmId);
        FarmPermissionsDTO dto = new FarmPermissionsDTO();
        dto.setCanCreateGoat(vo.isCanCreateGoat());
        return ResponseEntity.ok(dto);
    }
}