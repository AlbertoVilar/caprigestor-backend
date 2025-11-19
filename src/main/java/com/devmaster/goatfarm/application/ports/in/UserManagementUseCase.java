package com.devmaster.goatfarm.application.ports.in;

import com.devmaster.goatfarm.authority.business.bo.UserRequestVO;
import com.devmaster.goatfarm.authority.business.bo.UserResponseVO;

/**
 * Porta de entrada (Use Case) para operações de gerenciamento de Usuário.
 */
public interface UserManagementUseCase {

    UserResponseVO saveUser(UserRequestVO vo);

    UserResponseVO updateUser(Long userId, UserRequestVO vo);

    UserResponseVO getMe();

    UserResponseVO findByEmail(String email);

    UserResponseVO findById(Long userId);

    void updatePassword(Long userId, String newPassword);

    UserResponseVO updateRoles(Long userId, java.util.List<String> roles);
}