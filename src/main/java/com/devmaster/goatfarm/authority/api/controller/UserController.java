package com.devmaster.goatfarm.authority.api.controller;

import com.devmaster.goatfarm.authority.api.dto.UserRequestDTO;
import com.devmaster.goatfarm.authority.api.dto.UserResponseDTO;
import com.devmaster.goatfarm.authority.business.bo.UserRequestVO;
import com.devmaster.goatfarm.authority.business.bo.UserResponseVO;
import com.devmaster.goatfarm.authority.conveter.UserDTOConverter;
import com.devmaster.goatfarm.authority.facade.UserFacade;
import com.devmaster.goatfarm.authority.dao.UserDAO;
import com.devmaster.goatfarm.authority.model.entity.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserFacade userFacade;
    private final UserDAO userDAO;

    public UserController(UserFacade userFacade, UserDAO userDAO) {
        this.userFacade = userFacade;
        this.userDAO = userDAO;
    }


    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getMe() {
        UserResponseVO vo = userFacade.getMe();
        return ResponseEntity.ok(UserDTOConverter.toDTO(vo));
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody @Valid UserRequestDTO dto) {
        // Validações granulares
        Map<String, String> validationErrors = new HashMap<>();
        
        // Validar se password e confirmPassword são iguais
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            validationErrors.put("confirmPassword", "As senhas não coincidem");
        }
        
        // Validar formato do CPF mais rigorosamente
        if (dto.getCpf() != null && !dto.getCpf().matches("^\\d{11}$")) {
            validationErrors.put("cpf", "CPF deve conter exatamente 11 dígitos numéricos");
        }
        
        // Validar se roles são válidas
        if (dto.getRoles() != null) {
            for (String role : dto.getRoles()) {
                if (!role.equals("ROLE_ADMIN") && !role.equals("ROLE_OPERATOR")) {
                    validationErrors.put("roles", "Role inválida: " + role + ". Roles válidas: ROLE_ADMIN, ROLE_OPERATOR");
                    break;
                }
            }
        }
        
        if (!validationErrors.isEmpty()) {
            throw new com.devmaster.goatfarm.config.exceptions.custom.ValidationException(
                "Dados inválidos", validationErrors);
        }
        
        UserRequestVO requestVO = UserDTOConverter.toVO(dto);
        UserResponseVO responseVO = userFacade.saveUser(requestVO);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserDTOConverter.toDTO(responseVO));
    }

    // Endpoint temporário para debug - verificar roles do usuário
    @GetMapping("/debug/{email}")
    public ResponseEntity<Map<String, Object>> debugUserRoles(@PathVariable String email) {
        try {
            User user = userDAO.findUserByUsername(email);
            Map<String, Object> debugInfo = new HashMap<>();
            debugInfo.put("email", user.getEmail());
            debugInfo.put("name", user.getName());
            debugInfo.put("rolesCount", user.getRoles().size());
            debugInfo.put("roles", user.getRoles().stream()
                .map(role -> role.getAuthority())
                .collect(Collectors.toList()));
            // Authorities removidas - usando apenas roles
            return ResponseEntity.ok(debugInfo);
        } catch (Exception e) {
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("error", e.getMessage());
            errorInfo.put("email", email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorInfo);
        }
    }
}
