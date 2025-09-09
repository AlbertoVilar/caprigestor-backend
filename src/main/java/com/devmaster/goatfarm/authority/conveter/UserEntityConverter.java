package com.devmaster.goatfarm.authority.conveter;

import com.devmaster.goatfarm.authority.business.bo.UserRequestVO;
import com.devmaster.goatfarm.authority.business.bo.UserResponseVO;
import com.devmaster.goatfarm.authority.model.entity.Role;
import com.devmaster.goatfarm.authority.model.entity.User;


import java.util.ArrayList;
import java.util.List;

public class UserEntityConverter {

    public static UserResponseVO toVO(User user) {
        List<String> roles = new ArrayList<>();
        for (Role role : user.getRoles()) {
            roles.add(role.getAuthority());
        }

        return new UserResponseVO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                roles
        );
    }

    public static User fromVO(UserRequestVO vo) {
        User user = new User();
        user.setName(vo.getName());
        user.setEmail(vo.getEmail());
        user.setCpf(vo.getCpf());
        user.setPassword(vo.getPassword());
        return user;
    }

}
