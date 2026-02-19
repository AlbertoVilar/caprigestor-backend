# API_CONTRACTS
Ultima atualizacao: 2026-02-19
Escopo: padroes transversais de rotas, autenticacao, paginacao, idempotencia e erros da API.
Links relacionados: [Portal](../INDEX.md), [Arquitetura](../01-architecture/ARCHITECTURE.md), [Modulo Reproduction](../02-modules/REPRODUCTION_MODULE.md), [Modulo Milk Production](../02-modules/MILK_PRODUCTION_MODULE.md), [Modulo Health](../02-modules/HEALTH_VETERINARY_MODULE.md), [Modulo Inventory](../02-modules/INVENTORY_MODULE.md)

## Visao geral
Este documento define contratos comuns para todos os controllers oficiais do backend.

## Regras / Contratos
### Base de rotas
- Base geral: `/api/v1`
- Escopo por fazenda: `/api/v1/goatfarms/{farmId}/...`
- Rotas publicas sem autenticacao (quando aplicavel) usam namespace separado, exemplo: `/public/articles`.

### Seguranca
- Autenticacao: JWT.
- Autorizacao: ownership por `farmId` e/ou roles (`ROLE_ADMIN`, `ROLE_OPERATOR`, `ROLE_FARM_OWNER`).
- Respostas de seguranca:
  - `401` via `CustomAuthenticationEntryPoint`
  - `403` via `CustomAccessDeniedHandler` ou `AccessDeniedException`

### Paginacao
- Parametros padrao: `page` (base 0), `size`, `sort`.
- Resposta padrao de pagina contem `content`, `number`, `size`, `totalElements`, `totalPages`.

### Convencoes de payload
- DTOs de request e response separados por modulo.
- Datas em formato ISO (`yyyy-MM-dd` ou `yyyy-MM-dd'T'HH:mm:ss`).
- Mensagens de validacao em PT-BR.

### Idempotencia de comandos
Para endpoints que exigem idempotencia (ex.: `POST /api/v1/goatfarms/{farmId}/inventory/movements`):
- Header obrigatorio: `Idempotency-Key`.
- Primeira execucao valida: `201 Created`.
- Mesma key + payload equivalente: replay (`200` com resposta persistida).
- Mesma key + payload diferente: `409 Conflict`.
- Ausencia de key: `400 Bad Request`.

## Erros/Status
### Estrutura de erro padrao
Erros seguem estrutura `ValidationError`:

```json
{
  "timestamp": "2026-02-18T10:00:00Z",
  "status": 422,
  "error": "Regra de negocio violada",
  "path": "/api/v1/goatfarms/1/inventory/movements",
  "errors": [
    {
      "fieldName": "quantity",
      "message": "Saldo insuficiente para realizar a movimentacao"
    }
  ]
}
```

### Mapeamento principal de status
| Status | Origem tipica |
|---|---|
| `400 Bad Request` | `InvalidArgumentException`, `IllegalArgumentException`, JSON invalido |
| `401 Unauthorized` | falha de autenticacao/token |
| `403 Forbidden` | falha de ownership/perfil |
| `404 Not Found` | `ResourceNotFoundException` |
| `405 Method Not Allowed` | metodo HTTP nao suportado |
| `409 Conflict` | `DuplicateEntityException`, `DataIntegrityViolationException` |
| `415 Unsupported Media Type` | content type nao suportado |
| `422 Unprocessable Entity` | `BusinessRuleException`, validacao de bean |
| `500 Internal Server Error` | erro nao tratado |

## Referencias internas
- Handler global: [src/main/java/com/devmaster/goatfarm/config/exceptions/GlobalExceptionHandler.java](../../src/main/java/com/devmaster/goatfarm/config/exceptions/GlobalExceptionHandler.java)
- Entry point 401: [src/main/java/com/devmaster/goatfarm/config/security/CustomAuthenticationEntryPoint.java](../../src/main/java/com/devmaster/goatfarm/config/security/CustomAuthenticationEntryPoint.java)
- Handler 403: [src/main/java/com/devmaster/goatfarm/config/security/CustomAccessDeniedHandler.java](../../src/main/java/com/devmaster/goatfarm/config/security/CustomAccessDeniedHandler.java)
- Modulos oficiais: [../02-modules](../02-modules)
