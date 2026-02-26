# Status do Projeto GoatFarm/CapriGestor Backend
Última atualização: 2026-02-26  
Escopo: visão executiva do estado atual para fechamento do MVP.

Links relacionados: [Portal](../INDEX.md), [Roadmap](./ROADMAP.md), [Contratos API](../03-api/API_CONTRACTS.md), [Guia de Versionamento](../03-api/API_VERSIONING_MIGRATION_GUIDE.md), [Prontidão do MVP](./MVP_READY.md)

## 1. Resumo executivo
- Backend do MVP está funcional e com gate verde (`./mvnw -U -T 1C clean test`).
- Padrão de rotas consolidado: `/api/v1` canônico.
- Compatibilidade temporária mantida em `/api` como **deprecated**.
- Prazo sugerido para remoção de `/api` legado: **2026-06-30** (alvo `v2.0.0`), após atualização completa do frontend.

## 2. Módulos do MVP
| Módulo | Status | Endpoints canônicos principais | Evidências de teste |
|---|---|---|---|
| Security / Ownership | CONCLUÍDO | `/api/v1/auth/*` | `AuthControllerIntegrationTest`, `SecurityOwnershipIntegrationTest` |
| Goat / Farm (cadastros base) | CONCLUÍDO | `/api/v1/goatfarms`, `/api/v1/goatfarms/{farmId}/goats` | `GoatFarmControllerTest`, `GoatControllerTest`, `GoatFarmLogoIntegrationTest` |
| Reproduction + alerts | CONCLUÍDO | `/api/v1/goatfarms/{farmId}/goats/{goatId}/reproduction/*`, `/api/v1/goatfarms/{farmId}/reproduction/alerts/pregnancy-diagnosis` | `ReproductionControllerTest`, `ReproductionActivePregnancyIntegrationTest`, `ReproductionFarmPregnancyDiagnosisAlertsIntegrationTest` |
| Lactation + MilkProduction + alerts | CONCLUÍDO | `/api/v1/goatfarms/{farmId}/goats/{goatId}/lactations/*`, `/api/v1/goatfarms/{farmId}/goats/{goatId}/milk-productions/*`, `/api/v1/goatfarms/{farmId}/milk/alerts/dry-off` | `LactationSummaryIntegrationTest`, `MilkProductionCancellationIntegrationTest`, `MilkFarmDryOffAlertsIntegrationTest` |
| Health (eventos, calendário e alertas) | CONCLUÍDO | `/api/v1/goatfarms/{farmId}/goats/{goatId}/health-events/*`, `/api/v1/goatfarms/{farmId}/health-events/calendar`, `/api/v1/goatfarms/{farmId}/health-events/alerts` | `HealthEventControllerTest`, `HealthExceptionHandlingIntegrationTest` |
| Inventory (ledger core) | CONCLUÍDO | `/api/v1/goatfarms/{farmId}/inventory/movements` | `InventoryMovementBusinessTest`, `InventoryMovementPersistenceAdapterIntegrationTest` |

## 3. Critérios atendidos para MVP
- Segurança JWT e ownership por fazenda aplicados nos endpoints privados.
- Módulos principais com documentação em `docs/02-modules/*`.
- Contratos globais de erro/status documentados em [API_CONTRACTS](../03-api/API_CONTRACTS.md).
- Versionamento e migração documentados em [API_VERSIONING_MIGRATION_GUIDE](../03-api/API_VERSIONING_MIGRATION_GUIDE.md).
- Gate automatizado verde no branch de integração.

## 4. Itens fora do escopo do MVP (não bloqueantes)
- Hardening do módulo `events` assíncrono.
- Evolução de observabilidade e redução de warnings de tooling (Mockito/ByteBuddy).
- Fechamento definitivo da janela de compatibilidade `/api` após migração total do frontend.
