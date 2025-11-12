package com.devmaster.goatfarm.farm.facade;

import com.devmaster.goatfarm.address.mapper.AddressMapper;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmFullRequestDTO;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmFullResponseDTO;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmRequestDTO;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmResponseDTO;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmUpdateRequestDTO;
import com.devmaster.goatfarm.farm.api.dto.FarmPermissionsDTO;
import com.devmaster.goatfarm.farm.business.farmbusiness.GoatFarmBusiness;
import com.devmaster.goatfarm.farm.mapper.GoatFarmMapper;
import com.devmaster.goatfarm.authority.mapper.UserMapper;
import com.devmaster.goatfarm.phone.mapper.PhoneMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class GoatFarmFacade {

    @Autowired
    private GoatFarmBusiness farmBusiness;
    @Autowired
    private GoatFarmMapper farmMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AddressMapper addressMapper;
    @Autowired
    private PhoneMapper phoneMapper;

    public GoatFarmFullResponseDTO createFullGoatFarm(GoatFarmFullRequestDTO requestDTO) {
        return farmMapper.toFullDTO(farmBusiness.createFullGoatFarm(
                farmMapper.toRequestVO(requestDTO.getFarm()),
                userMapper.toRequestVO(requestDTO.getUser()),
                addressMapper.toVO(requestDTO.getAddress()),
                requestDTO.getPhones().stream()
                        .map(phoneMapper::toRequestVO)
                        .collect(java.util.stream.Collectors.toList())
        ));
    }

    public GoatFarmResponseDTO createGoatFarm(GoatFarmRequestDTO requestDTO) {
        return farmMapper.toResponseDTO(farmBusiness.createGoatFarm(farmMapper.toRequestVO(requestDTO)));
    }

    public GoatFarmFullResponseDTO updateGoatFarm(Long id, GoatFarmUpdateRequestDTO requestDTO) {
        return farmMapper.toFullDTO(farmBusiness.updateGoatFarm(
                id,
                farmMapper.toRequestVO(requestDTO.getFarm()),
                userMapper.toRequestVO(requestDTO.getUser()),
                addressMapper.toVO(requestDTO.getAddress()),
                requestDTO.getPhones().stream()
                        .map(phoneMapper::toRequestVO)
                        .collect(java.util.stream.Collectors.toList())
        ));
    }

    public GoatFarmFullResponseDTO findGoatFarmById(Long id) {
        return farmMapper.toFullDTO(farmBusiness.findGoatFarmById(id));
    }

    public Page<GoatFarmFullResponseDTO> searchGoatFarmByName(String name, Pageable pageable) {
        return farmBusiness.searchGoatFarmByName(name, pageable).map(farmMapper::toFullDTO);
    }

    public Page<GoatFarmFullResponseDTO> findAllGoatFarm(Pageable pageable) {
        return farmBusiness.findAllGoatFarm(pageable).map(farmMapper::toFullDTO);
    }

    public void deleteGoatFarm(Long id) {
        farmBusiness.deleteGoatFarm(id);
    }

    public FarmPermissionsDTO getFarmPermissions(Long farmId) {
        var vo = farmBusiness.getFarmPermissions(farmId);
        FarmPermissionsDTO dto = new FarmPermissionsDTO();
        dto.setCanCreateGoat(vo.isCanCreateGoat());
        return dto;
    }
}
