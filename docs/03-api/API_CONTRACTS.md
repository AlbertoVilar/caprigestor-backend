# Padrões de API e Contratos
Última atualização: 2026-02-10
Escopo: convenções, segurança, paginação e erros da API
Links relacionados: [Portal](../INDEX.md), [Arquitetura](../01-architecture/ARCHITECTURE.md), [Módulos](../02-modules), [Domínio](../00-overview/BUSINESS_DOMAIN.md)

## Base de Rotas
- Base geral: `/api`
- Escopo por fazenda: `/api/goatfarms/{farmId}/...`

## Segurança
- Autenticação: JWT
- Autorização: `@PreAuthorize("@ownershipService.canManageFarm(#farmId)")` em controllers farm-level
- Erros padrão: `ValidationError` JSON via `GlobalExceptionHandler`, `CustomAuthenticationEntryPoint` (401) e `CustomAccessDeniedHandler` (403)

## Paginação
- Parâmetros: `page` (0-based), `size`
- Resposta: `Page<T>` com `totalElements`, `totalPages`, `size`, `number`, `content`

## Convenções de DTO
- Requests e Responses separados por módulo
- Campos e mensagens em PT-BR

## Erros Comuns
- 400: validações de domínio (`InvalidArgumentException`)
- 404: não encontrado (`ResourceNotFoundException`)
- 401: sem token
- 403: sem ownership
- 409: conflito de dados (`DuplicateEntityException`)

## Exemplos
### Exemplo de erro `ValidationError`
```json
{
  "timestamp": "2026-02-01T10:00:00",
  "status": 400,
  "error": "Erro de validação",
  "path": "/api/goatfarms/1/goats/G001/lactations",
  "errors": [
    { "fieldName": "startDate", "message": "Data de início da lactação não pode ser futura." }
  ]
}
```
