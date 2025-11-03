package com.devmaster.goatfarm.farm.api.controller;

import com.devmaster.goatfarm.address.mapper.AddressMapper;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmFullRequestDTO;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmFullResponseDTO;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmRequestDTO;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmResponseDTO;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmUpdateRequestDTO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmFullResponseVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmResponseVO;
import com.devmaster.goatfarm.farm.mapper.GoatFarmMapper;
import com.devmaster.goatfarm.farm.facade.GoatFarmFacade;
import com.devmaster.goatfarm.authority.mapper.UserMapper;
import com.devmaster.goatfarm.phone.mapper.PhoneMapper;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private GoatFarmFacade farmFacade;
    @Autowired
    private GoatFarmMapper farmMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AddressMapper addressMapper;
    @Autowired
    private PhoneMapper phoneMapper;

    @PostMapping("/full")
    public ResponseEntity<GoatFarmFullResponseDTO> createFullGoatFarm(
            @org.springframework.web.bind.annotation.RequestBody GoatFarmFullRequestDTO requestDTO) {

        GoatFarmFullResponseVO responseVO = farmFacade.createFullGoatFarm(
                farmMapper.toRequestVO(requestDTO.getFarm()),
                userMapper.toRequestVO(requestDTO.getUser()),
                addressMapper.toVO(requestDTO.getAddress()),
                requestDTO.getPhones().stream()
                        .map(phoneMapper::toRequestVO)
                        .collect(java.util.stream.Collectors.toList())
        );

        return new ResponseEntity<>(farmMapper.toFullDTO(responseVO), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_OPERATOR')")
    @PostMapping
    public ResponseEntity<GoatFarmResponseDTO> createGoatFarm(
            @org.springframework.web.bind.annotation.RequestBody @Valid GoatFarmRequestDTO requestDTO) {

        try {
            GoatFarmResponseVO responseVO = farmFacade.createGoatFarm(farmMapper.toRequestVO(requestDTO));
            return new ResponseEntity<>(farmMapper.toResponseDTO(responseVO), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            throw new com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException(e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_OPERATOR')")
    @PutMapping("/{id}")
    public ResponseEntity<GoatFarmFullResponseDTO> updateGoatFarm(
            @PathVariable Long id,
            @org.springframework.web.bind.annotation.RequestBody @Valid GoatFarmUpdateRequestDTO requestDTO) {

        GoatFarmFullResponseVO responseVO = farmFacade.updateGoatFarm(
                id,
                farmMapper.toRequestVO(requestDTO.getFarm()),
                userMapper.toRequestVO(requestDTO.getUser()),
                addressMapper.toVO(requestDTO.getAddress()),
                requestDTO.getPhones().stream()
                        .map(phoneMapper::toRequestVO)
                        .collect(java.util.stream.Collectors.toList())
        );

        return ResponseEntity.ok(farmMapper.toFullDTO(responseVO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GoatFarmFullResponseDTO> findGoatFarmById(@PathVariable Long id) {
        GoatFarmFullResponseVO responseVO = farmFacade.findGoatFarmById(id);
        return ResponseEntity.ok(farmMapper.toFullDTO(responseVO));
    }

    @GetMapping("/name")
    public ResponseEntity<Page<GoatFarmFullResponseDTO>> searchGoatFarmByName(
            @RequestParam(value = "name", defaultValue = "") String name,
            @PageableDefault(size = 12, page = 0) Pageable pageable) {

        return ResponseEntity.ok(
                farmFacade.searchGoatFarmByName(name, pageable)
                        .map(farmMapper::toFullDTO)
        );
    }

    @GetMapping
    public ResponseEntity<Page<GoatFarmFullResponseDTO>> findAllGoatFarm(@PageableDefault(size = 12, page = 0) Pageable pageable) {
        return ResponseEntity.ok(
                farmFacade.findAllGoatFarm(pageable)
                        .map(farmMapper::toFullDTO)
        );
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_OPERATOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoatFarm(@PathVariable Long id) {
        farmFacade.deleteGoatFarm(id);
        return ResponseEntity.noContent().build();
    }
}
