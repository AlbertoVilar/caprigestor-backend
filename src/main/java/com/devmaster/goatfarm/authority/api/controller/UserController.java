package com.devmaster.goatfarm.authority.api.controller;

import com.devmaster.goatfarm.authority.api.dto.UserRequestDTO;
import com.devmaster.goatfarm.authority.api.dto.UserResponseDTO;
import com.devmaster.goatfarm.authority.business.bo.UserRequestVO;
import com.devmaster.goatfarm.authority.business.bo.UserResponseVO;
import com.devmaster.goatfarm.authority.mapper.UserMapper;
import com.devmaster.goatfarm.authority.facade.UserFacade;
import com.devmaster.goatfarm.authority.facade.dto.UserFacadeResponseDTO;
import com.devmaster.goatfarm.authority.dao.UserDAO;
import com.devmaster.goatfarm.authority.business.usersbusiness.UserBusiness;
import com.devmaster.goatfarm.authority.model.entity.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserFacade userFacade;
    private final UserDAO userDAO;
    private final UserBusiness userBusiness;
    private final UserMapper userMapper;

    public UserController(UserFacade userFacade, UserDAO userDAO, UserBusiness userBusiness, UserMapper userMapper) {
        this.userFacade = userFacade;
        this.userDAO = userDAO;
        this.userBusiness = userBusiness;
        this.userMapper = userMapper;
    }


    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getMe() {
        UserFacadeResponseDTO facadeDTO = userFacade.getMe();
        UserResponseVO vo = new UserResponseVO(facadeDTO.getId(), facadeDTO.getName(), facadeDTO.getEmail(), facadeDTO.getCpf(), facadeDTO.getRoles());
        return ResponseEntity.ok(userMapper.toResponseDTO(vo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        logger.info("Iniciando busca por usuário com ID: {}", id);
        try {
            logger.info("Chamando userFacade.findById({})", id);
            UserFacadeResponseDTO facadeDTO = userFacade.findById(id);
            logger.info("UserFacadeResponseDTO obtido: {}", facadeDTO != null ? facadeDTO.getName() : "null");
            
            logger.info("Convertendo para DTO...");
            UserResponseVO vo = new UserResponseVO(facadeDTO.getId(), facadeDTO.getName(), facadeDTO.getEmail(), facadeDTO.getCpf(), facadeDTO.getRoles());
            UserResponseDTO userDTO = userMapper.toResponseDTO(vo);
            logger.info("DTO convertido com sucesso: {}", userDTO != null ? userDTO.getName() : "null");
            
            return ResponseEntity.ok(userDTO);
        } catch (Exception e) {
            logger.error("ERRO COMPLETO ao buscar usuário com ID {}: {}", id, e.getMessage());
            logger.error("Stack trace completo:", e);
            throw e;
        }
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody @Valid UserRequestDTO dto) {
        try {
            UserRequestVO requestVO = userMapper.toRequestVO(dto);
            UserFacadeResponseDTO facadeDTO = userFacade.saveUser(requestVO);
            UserResponseVO responseVO = new UserResponseVO(facadeDTO.getId(), facadeDTO.getName(), facadeDTO.getEmail(), facadeDTO.getCpf(), facadeDTO.getRoles());
            return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toResponseDTO(responseVO));
        } catch (IllegalArgumentException e) {
            Map<String, String> validationErrors = new HashMap<>();
            validationErrors.put("validation", e.getMessage());
            throw new com.devmaster.goatfarm.config.exceptions.custom.ValidationException(
                "Dados inválidos", validationErrors);
        } catch (Exception e) {
            throw e;
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Long id, @RequestBody @Valid UserRequestDTO dto) {
        logger.info("Iniciando atualização do usuário com ID: {}", id);
        try {
            UserRequestVO requestVO = userMapper.toRequestVO(dto);
            UserResponseVO responseVO = userBusiness.updateUser(id, requestVO);
            logger.info("Usuário atualizado com sucesso: {}", responseVO.getName());
            return ResponseEntity.ok(userMapper.toResponseDTO(responseVO));
        } catch (IllegalArgumentException e) {
            Map<String, String> validationErrors = new HashMap<>();
            validationErrors.put("validation", e.getMessage());
            throw new com.devmaster.goatfarm.config.exceptions.custom.ValidationException(
                "Dados inválidos", validationErrors);
        } catch (Exception e) {
            logger.error("ERRO ao atualizar usuário com ID {}: {}", id, e.getMessage());
            logger.error("Stack trace completo:", e);
            throw e;
        }
    }

    // Temporary endpoint for debug - check user roles
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
