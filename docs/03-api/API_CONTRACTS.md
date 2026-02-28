# API_CONTRACTS
Última atualização: 2026-02-28
Escopo: padrões transversais de rotas, autenticação, paginação, idempotência e erros da API.
Links relacionados: [Portal](../INDEX.md), [Arquitetura](../01-architecture/ARCHITECTURE.md), [Módulo Reproduction](../02-modules/REPRODUCTION_MODULE.md), [Módulo Milk Production](../02-modules/MILK_PRODUCTION_MODULE.md), [Módulo Health](../02-modules/HEALTH_VETERINARY_MODULE.md), [Módulo Inventory](../02-modules/INVENTORY_MODULE.md), [Guia de Migração de Versionamento](./API_VERSIONING_MIGRATION_GUIDE.md)

## Visão geral
Este documento define contratos comuns para todos os controllers oficiais do backend.

## Regras / Contratos
### Base de rotas
- Base geral: `/api/v1`
- Escopo por fazenda: `/api/v1/goatfarms/{farmId}/...`
- Rotas públicas sem autenticação (quando aplicável) usam namespace separado, exemplo: `/public/articles`.

### Versionamento e compatibilidade
- Rotas canônicas: sempre em `/api/v1/...`.
- Compatibilidade temporária: rotas legadas em `/api/...` permanecem ativas por 1 ciclo como **DEPRECATED**.
- Remoção planejada das rotas legadas: **2026-06-30** (versão alvo **v2.0.0**).
- Novos endpoints não devem ser publicados fora de `/api/v1`.

### Segurança
- Autenticação: JWT.
- Autorização: ownership por `farmId` e/ou roles (`ROLE_ADMIN`, `ROLE_OPERATOR`, `ROLE_FARM_OWNER`).
- Respostas de segurança:
  - `401` via `CustomAuthenticationEntryPoint`
  - `403` via `CustomAccessDeniedHandler` ou `AccessDeniedException`

### Paginação
- Parâmetros padrão: `page` (base 0), `size`, `sort`.
- Resposta padrão de página contém `content` e metadados em `page.number`, `page.size`, `page.totalElements`, `page.totalPages`.

### Convenções de payload
- DTOs de request e response separados por modulo.
- Datas em formato ISO (`yyyy-MM-dd` ou `yyyy-MM-dd'T'HH:mm:ss`).
- Mensagens de validação em PT-BR.

### Idempotência de comandos
Para endpoints que exigem idempotência (ex.: `POST /api/v1/goatfarms/{farmId}/inventory/movements`):
- Header obrigatório: `Idempotency-Key`.
- Primeira execução válida: `201 Created`.
- Mesma key + payload equivalente: replay (`200` com resposta persistida).
- Mesma key + payload diferente: `409 Conflict`.
- Ausência de key: `400 Bad Request`.

### Inventory (itens de estoque)
Para `POST /api/v1/goatfarms/{farmId}/inventory/items`:
- Resposta de criação: `201 Created`.
- Nome duplicado na mesma fazenda: `409 Conflict`.
- Listagem paginada: `GET /api/v1/goatfarms/{farmId}/inventory/items`.

## Erros/Status
### Estrutura de erro padrão
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
