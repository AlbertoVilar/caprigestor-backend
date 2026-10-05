package com.devmaster.goatfarm.authority.business.mapper;

import com.devmaster.goatfarm.authority.business.bo.AuthorityAccount;
import com.devmaster.goatfarm.authority.business.bo.LoginResponseVO;
import com.devmaster.goatfarm.authority.business.bo.UserRequestVO;
import com.devmaster.goatfarm.authority.business.bo.UserResponseVO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/** Maps application-owned Authority models to use-case VOs. */
@Component
public class AuthorityBusinessMapper {

    public AuthorityAccount toAccount(UserRequestVO vo) {
        return new AuthorityAccount(null, vo.getName(), vo.getEmail(), vo.getCpf(), null,
                vo.getRoles() == null ? java.util.Set.of() : java.util.Set.copyOf(vo.getRoles()));
    }

    public UserResponseVO toResponseVO(AuthorityAccount account) {
        return new UserResponseVO(account.id(), account.name(), account.email(), account.cpf(),
                new ArrayList<>(account.roles()));
    }

    public LoginResponseVO toLoginResponseVO(AuthorityAccount account, String accessToken,
                                             String refreshToken, long expiresIn) {
        return LoginResponseVO.builder()
                .user(toResponseVO(account))
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .build();
    }
}
