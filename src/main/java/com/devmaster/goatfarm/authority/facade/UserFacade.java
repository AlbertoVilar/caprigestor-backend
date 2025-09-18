package com.devmaster.goatfarm.authority.facade;

import com.devmaster.goatfarm.authority.business.bo.UserRequestVO;
import com.devmaster.goatfarm.authority.business.bo.UserResponseVO;
import com.devmaster.goatfarm.authority.business.usersbusiness.UserBusiness;
import com.devmaster.goatfarm.authority.facade.dto.UserFacadeResponseDTO;
import com.devmaster.goatfarm.authority.facade.mapper.UserFacadeMapper;
import com.devmaster.goatfarm.authority.model.entity.User;
import org.springframework.stereotype.Service;

@Service
public class UserFacade {

    private final UserBusiness business;
    private final UserFacadeMapper facadeMapper;

    public UserFacade(UserBusiness business, UserFacadeMapper facadeMapper) {
        this.business = business;
        this.facadeMapper = facadeMapper;
    }

    // 🔍 Usado para buscar diretamente a entidade User pelo username
    public User findByUsername(String username) {
        return business.findByUsername(username);
    }

    public UserFacadeResponseDTO getMe() {
        UserResponseVO responseVO = business.getMe();
        return facadeMapper.toFacadeDTO(responseVO);
    }

    public UserFacadeResponseDTO saveUser(UserRequestVO requestVO) {
        UserResponseVO responseVO = business.saveUser(requestVO);
        return facadeMapper.toFacadeDTO(responseVO);
    }

    public UserFacadeResponseDTO findByEmail(String email) {
        UserResponseVO responseVO = business.findByEmail(email);
        return facadeMapper.toFacadeDTO(responseVO);
    }

    public UserFacadeResponseDTO findById(Long userId) {
        UserResponseVO responseVO = business.findById(userId);
        return facadeMapper.toFacadeDTO(responseVO);
    }
}
