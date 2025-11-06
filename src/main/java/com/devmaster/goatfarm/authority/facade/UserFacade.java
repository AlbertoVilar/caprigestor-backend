package com.devmaster.goatfarm.authority.facade;

import com.devmaster.goatfarm.authority.api.dto.UserRequestDTO;
import com.devmaster.goatfarm.authority.api.dto.UserResponseDTO;
import java.util.List;
import com.devmaster.goatfarm.authority.business.bo.UserResponseVO;
import com.devmaster.goatfarm.authority.business.usersbusiness.UserBusiness;
import com.devmaster.goatfarm.authority.facade.dto.UserFacadeResponseDTO;
import com.devmaster.goatfarm.authority.facade.mapper.UserFacadeMapper;
import com.devmaster.goatfarm.authority.mapper.UserMapper;
import org.springframework.stereotype.Service;

@Service
public class UserFacade {

    private final UserBusiness business;
    private final UserFacadeMapper facadeMapper;
    private final UserMapper userMapper;

    public UserFacade(UserBusiness business, UserFacadeMapper facadeMapper, UserMapper userMapper) {
        this.business = business;
        this.facadeMapper = facadeMapper;
        this.userMapper = userMapper;
    }

    public UserResponseDTO getMe() {
        UserResponseVO responseVO = business.getMe();
        UserFacadeResponseDTO facadeDTO = facadeMapper.toFacadeDTO(responseVO);
        return userMapper.toResponseDTO(facadeDTO);
    }

    public UserResponseDTO saveUser(UserRequestDTO requestDTO) {
        UserResponseVO responseVO = business.saveUser(userMapper.toRequestVO(requestDTO));
        UserFacadeResponseDTO facadeDTO = facadeMapper.toFacadeDTO(responseVO);
        return userMapper.toResponseDTO(facadeDTO);
    }

    public UserResponseDTO updateUser(Long id, UserRequestDTO requestDTO) {
        UserResponseVO responseVO = business.updateUser(id, userMapper.toRequestVO(requestDTO));
        UserFacadeResponseDTO facadeDTO = facadeMapper.toFacadeDTO(responseVO);
        return userMapper.toResponseDTO(facadeDTO);
    }

    public UserResponseDTO findByEmail(String email) {
        UserResponseVO responseVO = business.findByEmail(email);
        UserFacadeResponseDTO facadeDTO = facadeMapper.toFacadeDTO(responseVO);
        return userMapper.toResponseDTO(facadeDTO);
    }

    public UserResponseDTO findById(Long userId) {
        UserResponseVO responseVO = business.findById(userId);
        UserFacadeResponseDTO facadeDTO = facadeMapper.toFacadeDTO(responseVO);
        return userMapper.toResponseDTO(facadeDTO);
    }

    public void updatePassword(Long id, String newPassword) {
        business.updatePassword(id, newPassword);
    }

    public UserResponseDTO updateRoles(Long id, List<String> roles) {
        UserResponseVO responseVO = business.updateRoles(id, roles);
        UserFacadeResponseDTO facadeDTO = facadeMapper.toFacadeDTO(responseVO);
        return userMapper.toResponseDTO(facadeDTO);
    }
}
