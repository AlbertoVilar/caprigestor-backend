# Auditoria de Tratamento de Exceções - Módulo de Saúde (Health)

**Data:** 2026-02-01
**Responsável:** Trae AI
**Branch:** feature/audit-health-exception-handling

## 1. Objetivo
Auditar e padronizar o tratamento de exceções no módulo de Saúde (Health), garantindo que todos os erros (4xx, 5xx) retornem respostas JSON consistentes no formato `ValidationError`, em conformidade com o padrão do projeto e mensagens em PT-BR.

## 2. Cenários Auditados e Resultados

A auditoria foi realizada através da criação de testes de integração (`HealthExceptionHandlingIntegrationTest`) cobrindo os seguintes cenários:

| Cenário | Status HTTP Esperado | Status Anterior | Status Atual (Pós-Fix) | Observação |
|---------|----------------------|-----------------|------------------------|------------|
| POST sem corpo (body missing) | 400 Bad Request | 400 | 400 | Já tratado corretamente pelo `GlobalExceptionHandler` (`HttpMessageNotReadableException`). |
| POST com JSON inválido (syntax error) | 400 Bad Request | 400 | 400 | Já tratado corretamente pelo `GlobalExceptionHandler`. |
| POST com Enum inválido | 400 Bad Request | 400 | 400 | Já tratado corretamente. |
| POST com ID inexistente (GoatNotFound) | 404 Not Found | 404 | 404 | Já tratado corretamente (`ResourceNotFoundException`). |
| POST sem autenticação (Token ausente) | 401 Unauthorized | 401 (Empty/Default) | 401 (JSON Padronizado) | **Corrigido.** Adicionado `CustomAuthenticationEntryPoint`. |
| POST sem permissão (Role insuficiente) | 403 Forbidden | 403 (Empty/Default) | 403 (JSON Padronizado) | **Corrigido.** Adicionado `CustomAccessDeniedHandler`. |

## 3. Correções Implementadas

### 3.1. Spring Security (401/403)
O tratamento padrão do Spring Security retornava corpos vazios ou páginas HTML de erro padrão para falhas de autenticação e autorização ocorridas *antes* da execução do Controller (filtros).

**Arquivos Criados:**
- `src/main/java/com/devmaster/goatfarm/config/security/CustomAuthenticationEntryPoint.java`: Intercepta erros 401 no filtro de segurança e retorna JSON `ValidationError`.
- `src/main/java/com/devmaster/goatfarm/config/security/CustomAccessDeniedHandler.java`: Intercepta erros 403 no filtro de segurança e retorna JSON `ValidationError`.

**Alterações em `SecurityConfig.java`:**
- Injeção dos novos handlers.
- Configuração `.exceptionHandling()` para usar os novos entry points.

### 3.2. GlobalExceptionHandler
O `GlobalExceptionHandler` existente já cobria corretamente as exceções lançadas pela aplicação (camada de negócio e controller), incluindo:
- `MethodArgumentNotValidException` (Validação de DTO)
- `ResourceNotFoundException`
- `DuplicateEntityException`
- `AccessDeniedException` (Para verificações `@PreAuthorize`)

Nenhuma alteração foi necessária no `GlobalExceptionHandler` para este escopo, pois ele já estava aderente.

## 4. Verificação

Os testes de integração em `HealthExceptionHandlingIntegrationTest` foram executados com sucesso, validando que todos os endpoints agora retornam a estrutura JSON padrão:

```json
{
  "timestamp": "2026-02-01T...",
  "status": 4xx,
  "error": "Descrição do Erro",
  "path": "/api/...",
  "errors": [
    {
      "fieldName": "campo",
      "message": "Detalhe do erro"
    }
  ]
}
```

## 5. Próximos Passos
- Realizar merge da branch `feature/audit-health-exception-handling` para `develop`.
- Monitorar logs para garantir que não haja regressões em outros módulos (a alteração no SecurityConfig é global).
