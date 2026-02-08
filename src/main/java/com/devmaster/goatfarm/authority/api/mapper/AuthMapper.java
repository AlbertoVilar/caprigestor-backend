package com.devmaster.goatfarm.authority.api.mapper;

import com.devmaster.goatfarm.authority.api.dto.LoginRequestDTO;
import com.devmaster.goatfarm.authority.api.dto.LoginResponseDTO;
import com.devmaster.goatfarm.authority.api.dto.RefreshTokenRequestDTO;
import com.devmaster.goatfarm.authority.business.bo.LoginRequestVO;
import com.devmaster.goatfarm.authority.business.bo.LoginResponseVO;
import com.devmaster.goatfarm.authority.business.bo.RefreshTokenRequestVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface AuthMapper {

    LoginRequestVO toRequestVO(LoginRequestDTO dto);

    RefreshTokenRequestVO toRefreshRequestVO(RefreshTokenRequestDTO dto);

    LoginResponseDTO toLoginResponseDTO(LoginResponseVO vo);
}
