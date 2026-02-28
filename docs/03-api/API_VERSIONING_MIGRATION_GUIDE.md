# API_VERSIONING_MIGRATION_GUIDE
Última atualização: 2026-02-28
Escopo: migração de rotas do padrão legado `/api/...` para o padrão canônico `/api/v1/...`.
Links relacionados: [API_CONTRACTS](./API_CONTRACTS.md), [Módulo Goat/Farm](../02-modules/GOAT_FARM_MODULE.md), [Módulo Reproduction](../02-modules/REPRODUCTION_MODULE.md), [Módulo Lactação](../02-modules/LACTATION_MODULE.md), [Módulo Milk Production](../02-modules/MILK_PRODUCTION_MODULE.md), [Módulo Health](../02-modules/HEALTH_VETERINARY_MODULE.md), [Módulo Inventory](../02-modules/INVENTORY_MODULE.md)

## Padrão oficial
- Base URL canônica: `/api/v1`
- Escopo por fazenda: `/api/v1/goatfarms/{farmId}/...`
- Estratégia atual: **compatibilidade temporária** (dual mapping).
  - Canônico: `/api/v1/...`
  - Legado: `/api/...` (**DEPRECATED**)
  - Remoção planejada do legado: **2026-06-30** (versão alvo **v2.0.0**)

## Tabela de migração (OLD -> NEW)
| Domínio | OLD (legado) | NEW (canônico) |
|---|---|---|
| Auth | `/api/auth/*` | `/api/v1/auth/*` |
| Farm | `/api/goatfarms*` | `/api/v1/goatfarms*` |
| Goat | `/api/goatfarms/{farmId}/goats*` | `/api/v1/goatfarms/{farmId}/goats*` |
| Reproduction | `/api/goatfarms/{farmId}/goats/{goatId}/reproduction*` | `/api/v1/goatfarms/{farmId}/goats/{goatId}/reproduction*` |
| Reproduction Alerts | `/api/goatfarms/{farmId}/reproduction/alerts*` | `/api/v1/goatfarms/{farmId}/reproduction/alerts*` |
| Lactation | `/api/goatfarms/{farmId}/goats/{goatId}/lactations*` | `/api/v1/goatfarms/{farmId}/goats/{goatId}/lactations*` |
| Milk Production | `/api/goatfarms/{farmId}/goats/{goatId}/milk-productions*` | `/api/v1/goatfarms/{farmId}/goats/{goatId}/milk-productions*` |
| Milk Alerts | `/api/goatfarms/{farmId}/milk/alerts*` | `/api/v1/goatfarms/{farmId}/milk/alerts*` |
| Health (goat) | `/api/goatfarms/{farmId}/goats/{goatId}/health-events*` | `/api/v1/goatfarms/{farmId}/goats/{goatId}/health-events*` |
| Health (farm) | `/api/goatfarms/{farmId}/health-events*` | `/api/v1/goatfarms/{farmId}/health-events*` |
| Inventory Balances | `/api/goatfarms/{farmId}/inventory/balances*` | `/api/v1/goatfarms/{farmId}/inventory/balances*` |
| Inventory Items | `/api/goatfarms/{farmId}/inventory/items*` | `/api/v1/goatfarms/{farmId}/inventory/items*` |
| Inventory Movements | `/api/goatfarms/{farmId}/inventory/movements*` | `/api/v1/goatfarms/{farmId}/inventory/movements*` |
| Address | `/api/goatfarms/{farmId}/addresses*` | `/api/v1/goatfarms/{farmId}/addresses*` |
| Phone | `/api/goatfarms/{farmId}/phones*` | `/api/v1/goatfarms/{farmId}/phones*` |
| Events | `/api/goatfarms/{farmId}/goats/{goatId}/events*` | `/api/v1/goatfarms/{farmId}/goats/{goatId}/events*` |
| Genealogy | `/api/goatfarms/{farmId}/goats/{goatId}/genealogies*` | `/api/v1/goatfarms/{farmId}/goats/{goatId}/genealogies*` |
| Articles (admin) | `/api/articles*` | `/api/v1/articles*` |
| Users | `/api/users*` | `/api/v1/users*` |
| Admin Maintenance | `/api/admin/maintenance*` | `/api/v1/admin/maintenance*` |

## Detalhes Goat/Farm
- Busca de fazendas por nome:
  - legado: `/api/goatfarms/name`
  - canônico: `/api/v1/goatfarms/name`
- Permissões da fazenda:
  - legado: `/api/goatfarms/{farmId}/permissions`
  - canônico: `/api/v1/goatfarms/{farmId}/permissions`
- Busca de cabras por nome:
  - legado: `/api/goatfarms/{farmId}/goats/search`
  - canônico: `/api/v1/goatfarms/{farmId}/goats/search`
- O legado segue apenas para compatibilidade e não deve receber evolução funcional nova.

## Detalhes Reproduction
- Registro de cobertura:
  - legado: `/api/goatfarms/{farmId}/goats/{goatId}/reproduction/breeding`
  - canônico: `/api/v1/goatfarms/{farmId}/goats/{goatId}/reproduction/breeding`
- Correção de cobertura:
  - legado: `/api/goatfarms/{farmId}/goats/{goatId}/reproduction/breeding/{coverageEventId}/corrections`
  - canônico: `/api/v1/goatfarms/{farmId}/goats/{goatId}/reproduction/breeding/{coverageEventId}/corrections`
- Histórico por cabra:
  - legado: `/api/goatfarms/{farmId}/goats/{goatId}/reproduction/events`
  - canônico: `/api/v1/goatfarms/{farmId}/goats/{goatId}/reproduction/events`
- Alertas por fazenda:
  - legado: `/api/goatfarms/{farmId}/reproduction/alerts/pregnancy-diagnosis`
  - canônico: `/api/v1/goatfarms/{farmId}/reproduction/alerts/pregnancy-diagnosis`
- O legado segue apenas para compatibilidade e não deve receber evolução funcional nova.

## Detalhes Lactation e Milk Production
- Lactação ativa:
  - legado: `/api/goatfarms/{farmId}/goats/{goatId}/lactations/active`
  - canônico: `/api/v1/goatfarms/{farmId}/goats/{goatId}/lactations/active`
- Sumário da lactação ativa:
  - legado: `/api/goatfarms/{farmId}/goats/{goatId}/lactations/active/summary`
  - canônico: `/api/v1/goatfarms/{farmId}/goats/{goatId}/lactations/active/summary`
- Alertas de secagem por fazenda:
  - legado: `/api/goatfarms/{farmId}/milk/alerts/dry-off`
  - canônico: `/api/v1/goatfarms/{farmId}/milk/alerts/dry-off`
- Produção de leite por cabra:
  - legado: `/api/goatfarms/{farmId}/goats/{goatId}/milk-productions`
  - canônico: `/api/v1/goatfarms/{farmId}/goats/{goatId}/milk-productions`
- O legado segue apenas para compatibilidade e não deve receber evolução funcional nova.

## Detalhes Health
- Eventos por cabra:
  - legado: `/api/goatfarms/{farmId}/goats/{goatId}/health-events`
  - canônico: `/api/v1/goatfarms/{farmId}/goats/{goatId}/health-events`
- Calendário da fazenda:
  - legado: `/api/goatfarms/{farmId}/health-events/calendar`
  - canônico: `/api/v1/goatfarms/{farmId}/health-events/calendar`
- Alertas da fazenda:
  - legado: `/api/goatfarms/{farmId}/health-events/alerts`
  - canônico: `/api/v1/goatfarms/{farmId}/health-events/alerts`
- O legado segue apenas para compatibilidade e não deve receber evolução funcional nova.

## Impacto no frontend (checklist)
1. Atualizar `baseURL` para `/api/v1`.
2. Substituir rotas hardcoded de `/api/...` para `/api/v1/...`.
3. Validar fluxos de autenticação em `/api/v1/auth/*`.
4. Validar contratos e códigos HTTP esperados (`400`, `404`, `409`, `422`).
5. Revalidar tratamento de paginação (`page`, `size`, `sort`) após troca de rota.
6. Monitorar logs de chamadas legado (`/api/...`) para remover usos remanescentes antes de 2026-06-30.

## Observações
- `/public/articles` permanece fora de versionamento por ser namespace público.
- Durante a janela de compatibilidade, `/api/v1` deve ser tratado como único caminho de evolução funcional.