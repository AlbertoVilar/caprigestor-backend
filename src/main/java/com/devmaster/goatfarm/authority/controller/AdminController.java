package com.devmaster.goatfarm.authority.controller;

import com.devmaster.goatfarm.application.ports.in.AdminMaintenanceUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/maintenance")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final AdminMaintenanceUseCase adminMaintenanceUseCase;

    public AdminController(AdminMaintenanceUseCase adminMaintenanceUseCase) {
        this.adminMaintenanceUseCase = adminMaintenanceUseCase;
    }

    @PostMapping("/clean-admin")
    public ResponseEntity<String> cleanDatabaseAndSetupAdmin(@RequestParam("adminId") Long adminId) {
        logger.info("[AdminController] Iniciando limpeza de banco mantendo adminId={}", adminId);
        adminMaintenanceUseCase.cleanDatabaseAndSetupAdmin(adminId);
        logger.info("[AdminController] Limpeza concluída com sucesso para adminId={}", adminId);
        return ResponseEntity.ok("Limpeza concluída para adminId=" + adminId);
    }

    @PostMapping("/clean-admin-auto")
    public ResponseEntity<String> cleanDatabaseAndSetupAdminAuto() {
        logger.info("[AdminController] Iniciando limpeza automática do banco (admin por email)");
        adminMaintenanceUseCase.cleanDatabaseAndSetupAdminAuto();
        logger.info("[AdminController] Limpeza automática concluída com sucesso");
        return ResponseEntity.ok("Limpeza automática concluída");
    }
}