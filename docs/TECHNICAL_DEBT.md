# Dívidas Técnicas (Technical Debt)

Este documento rastreia débitos técnicos conhecidos, organizados por prioridade e impacto no negócio/arquitetura.

## Legenda de Prioridade
*   **P0 (Crítico)**: Bloqueia evolução ou causa bugs graves. Resolver imediatamente.
*   **P1 (Alto)**: Afeta manutenção ou performance significativamente. Resolver no próximo ciclo.
*   **P2 (Médio)**: Melhoria de código/arquitetura sem impacto direto no usuário. Resolver quando houver tempo.
*   **P3 (Baixo)**: Cosmético ou melhoria menor.

## Backlog

### 1. Acoplamento de HTTP/Infra na Camada de Negócio
*   **Problema**: A classe `ValidationException` e suas subclasses (usadas no Business Layer) acoplam conceitos de API (`HttpStatus`) e infraestrutura (`Instant`).
*   **Impacto**: Violação da Arquitetura Hexagonal. Dificulta testes unitários puros e reutilização do Business em outros contextos (ex: CLI, Jobs).
*   **Solução Sugerida**:
    1.  Criar exceções de domínio puro (ex: `BusinessRuleException` sem dependência de `org.springframework.http`).
    2.  Mover o mapeamento de Status Code para o `GlobalExceptionHandler`.
    3.  Injetar `Clock` ou usar `LocalDate` puro onde `Instant` não for estritamente necessário para auditoria.
*   **Prioridade**: **P2**
*   **Módulo Afetado**: `Core/Shared`, `Business Layer`

### 2. Testes Desabilitados (Diagnostic)
*   **Problema**: Alguns testes de mapeamento de controllers (`GoatControllerMappingsDiagnosticTest`) não rodam no ciclo de CI/CD padrão (`mvn test`).
*   **Impacto**: Risco de regressão silenciosa nas rotas da API se não forem executados manualmente.
*   **Solução Sugerida**:
    1.  Avaliar se devem ser promovidos a testes de integração reais (`@SpringBootTest` com `MockMvc`).
    2.  Ou configurar um profile de build específico para rodar diagnósticos periodicamente.
*   **Prioridade**: **P3**
*   **Módulo Afetado**: `Goat`, `Infrastructure`
