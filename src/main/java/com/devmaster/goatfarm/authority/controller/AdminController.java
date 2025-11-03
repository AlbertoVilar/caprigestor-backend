package com.devmaster.goatfarm.authority.controller;

import com.devmaster.goatfarm.authority.business.AdminBusiness;
import com.devmaster.goatfarm.authority.business.AdminMaintenanceBusiness;
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

    private final AdminBusiness adminBusiness;
    private final AdminMaintenanceBusiness adminMaintenanceBusiness;

    public AdminController(AdminBusiness adminBusiness, AdminMaintenanceBusiness adminMaintenanceBusiness) {
        this.adminBusiness = adminBusiness;
        this.adminMaintenanceBusiness = adminMaintenanceBusiness;
    }

        @PostMapping("/clean-admin")
    public ResponseEntity<String> cleanDatabaseAndSetupAdmin(@RequestParam("adminId") Long adminId) {
        logger.info("[AdminController] Iniciando limpeza de banco mantendo adminId={}", adminId);
        adminMaintenanceBusiness.cleanDatabaseAndSetupAdmin(adminId);
        logger.info("[AdminController] Limpeza concluÃ­da com sucesso para adminId={}", adminId);
        return ResponseEntity.ok("Limpeza concluÃ­da para adminId=" + adminId);
    }

        @PostMapping("/clean-admin-auto")
    public ResponseEntity<String> cleanDatabaseAndSetupAdminAuto() {
        logger.info("[AdminController] Iniciando limpeza automÃ¡tica do banco (admin por email)");
        adminBusiness.cleanDatabaseAndSetupAdmin();
        logger.info("[AdminController] Limpeza automÃ¡tica concluÃ­da com sucesso");
        return ResponseEntity.ok("Limpeza automÃ¡tica concluÃ­da");
    }
}
