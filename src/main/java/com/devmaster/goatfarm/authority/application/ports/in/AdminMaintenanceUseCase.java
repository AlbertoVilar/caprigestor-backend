package com.devmaster.goatfarm.authority.application.ports.in;

/**
 * Porta de entrada para manutenção administrativa (limpeza e configuração de admin).
 */
public interface AdminMaintenanceUseCase {

    /**
     * Limpa dados de outros usuários mantendo o admin informado.
     */
    void cleanDatabaseAndSetupAdmin(Long adminId);

    /**
     * Limpa dados e configura automaticamente o admin padrão por email.
     */
    void cleanDatabaseAndSetupAdminAuto();
}