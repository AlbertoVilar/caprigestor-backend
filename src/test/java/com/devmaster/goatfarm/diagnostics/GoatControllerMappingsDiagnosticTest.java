package com.devmaster.goatfarm.diagnostics;

import com.devmaster.goatfarm.goat.api.controller.GoatController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@SpringBootTest
@ActiveProfiles("test")
@Import({com.devmaster.goatfarm.config.exceptions.GlobalExceptionHandler.class})
public class GoatControllerMappingsDiagnosticTest {

    @Autowired
    private RequestMappingHandlerMapping handlerMapping;

    @Autowired
    private GoatController goatController;

    /**
     * Teste diagnóstico para listar todos os mappings registrados.
     * Útil para debug de 404/405 ou problemas de roteamento.
     * Mantenha @Disabled se não quiser poluir o log de testes, 
     * ou use @Tag("diagnostic") para filtrar.
     */
    @Test
    @org.junit.jupiter.api.Tag("diagnostic")
    void listRegisteredMappings() {
        var methods = handlerMapping.getHandlerMethods();
        System.out.println(">>> DIAGNOSTIC: Mappings registrados para GoatController e outros:");
        methods.keySet().stream()
                .filter(info -> info.toString().contains("GoatController") || info.toString().contains("/api/goatfarms"))
                .forEach(info -> System.out.println(info.toString()));
    }
}
