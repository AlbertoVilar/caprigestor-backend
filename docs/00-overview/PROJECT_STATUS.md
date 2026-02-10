# Status do Projeto GoatFarm/CapriGestor Backend
Ultima atualizacao: 2026-02-10
Escopo: visao executiva + mapa tecnico para continuidade.
Links relacionados: [Portal](../INDEX.md), [Arquitetura](../01-architecture/ARCHITECTURE.md), [Contratos API](../03-api/API_CONTRACTS.md), [Roadmap](./ROADMAP.md), [Contexto para Agentes](./AGENT_CONTEXT.md)

## 1. Resumo Executivo
- O que esta pronto:
- Dominios centrais (`farm`, `goat`, `address`, `phone`) com API, regras de negocio e testes.
- Fluxos produtivos/reprodutivos/sanitarios com alertas farm-level em `milk`, `reproduction` e `health`.
- Seguranca JWT e ownership por fazenda aplicados nos principais controllers.
- O que esta em risco ou deficit:
- `events` possui TODOs no consumidor assicrono (`EventConsumer`) para tratamento de tipos de evento.
- Gestao de operador por fazenda existe no modelo (`FarmOperator`) mas sem contrato de API oficial dedicado.
- `genealogy` segue com escopo somente de consulta (`GET`), sem comandos explicitos.
- Proximas 3 prioridades:
- Fechar backlog P0 de `events` (consumo assicrono sem placeholders).
- Consolidar contrato oficial de operacao por fazenda (ownership + perfil operador).
- Planejar inicio do eixo de gestao de negocio (`inventory`, `purchases/sales`, `finance`) em marcos incrementais.

## 2. Mapa de Modulos (Status)
| Modulo | Status | Principais endpoints | Regras de dominio chave | Performance | Testes | Deficits |
|---|---|---|---|---|---|---|
| `farm` | ✅ Concluido | `POST /api/goatfarms`<br>`PUT /api/goatfarms/{id}`<br>`GET /api/goatfarms/{id}`<br>`GET /api/goatfarms`<br>`GET /api/goatfarms/{farmId}/permissions` | Ownership por fazenda.<br>Cadastro completo da fazenda e dados associados.<br>Operacoes de alteracao protegidas por role/perfil. | Listagem por fazendas com pagina/ordenacao quando aplicavel. | `GoatFarmBusinessTest`<br>`GoatFarmControllerTest`<br>`GoatFarmLogoIntegrationTest` | Formalizar contrato detalhado do endpoint de permissoes no portal de API. |
| `goat` | ✅ Concluido | `POST /api/goatfarms/{farmId}/goats`<br>`PUT /api/goatfarms/{farmId}/goats/{goatId}`<br>`GET /api/goatfarms/{farmId}/goats/{goatId}`<br>`GET /api/goatfarms/{farmId}/goats`<br>`GET /api/goatfarms/{farmId}/goats/search` | Cabra pertence a uma fazenda.<br>Regras de role + ownership para alteracoes.<br>Consulta e busca por escopo de fazenda. | Filtros de busca e listagem evitam carregamento amplo em cliente. | `GoatBusinessTest`<br>`GoatControllerTest` | Revisar cobertura para cenarios extremos de busca/paginacao. |
| `address` | ✅ Concluido | `POST /api/goatfarms/{farmId}/addresses`<br>`PUT /api/goatfarms/{farmId}/addresses/{addressId}`<br>`GET /api/goatfarms/{farmId}/addresses/{addressId}`<br>`DELETE /api/goatfarms/{farmId}/addresses/{addressId}` | Endereco vinculado ao escopo da fazenda.<br>CRUD controlado por contexto do `farmId`. | Operacoes pontuais por ID. | `AddressBusinessTest`<br>`AddressControllerTest`<br>`AddressControllerWebTest` | Sem deficit critico identificado na auditoria atual. |
| `phone` | ✅ Concluido | `POST /api/goatfarms/{farmId}/phones`<br>`GET /api/goatfarms/{farmId}/phones/{phoneId}`<br>`GET /api/goatfarms/{farmId}/phones`<br>`PUT /api/goatfarms/{farmId}/phones/{phoneId}`<br>`DELETE /api/goatfarms/{farmId}/phones/{phoneId}` | Telefone segue ownership da fazenda.<br>CRUD completo por escopo. | Listagem simples por fazenda. | `PhoneBusinessTest` | Evoluir cobertura de teste de controller para cenarios de erro/autorizacao. |
| `authority/user/auth` | ⚠️ Parcial | `POST /api/auth/login`<br>`POST /api/auth/register`<br>`POST /api/auth/refresh`<br>`GET /api/auth/me`<br>`POST /api/auth/register-farm`<br>`POST /api/users`<br>`PATCH /api/users/{id}/roles` | JWT como contrato base de autenticacao.<br>Roles `ADMIN`, `OPERATOR`, `FARM_OWNER`.<br>Controle de usuario/roles ativo. | Fluxos autenticados e stateless por token. | `AuthControllerIntegrationTest`<br>`UserBusinessTest`<br>`UserControllerTest` | Estruturas de `FarmOperator` existem, mas falta contrato oficial dedicado para operacao por fazenda. |
| `milk` (lactation + milk production) | ✅ Concluido | `POST /api/goatfarms/{farmId}/goats/{goatId}/lactations`<br>`GET /api/goatfarms/{farmId}/goats/{goatId}/lactations/active/summary`<br>`PATCH /api/goatfarms/{farmId}/goats/{goatId}/lactations/{lactationId}/dry`<br>`POST /api/goatfarms/{farmId}/goats/{goatId}/milk-productions`<br>`GET /api/goatfarms/{farmId}/goats/{goatId}/milk-productions`<br>`GET /api/goatfarms/{farmId}/milk/alerts/dry-off` | Lactacao ativa unica por cabra.<br>Producao depende de lactacao ativa.<br>Alertas farm-level de secagem com ownership. | Alertas farm-level agregados com pagina.<br>Listagens por cabra com filtros e ordenacao. | `LactationBusinessTest`<br>`LactationSummaryIntegrationTest`<br>`MilkProductionBusinessTest`<br>`MilkProductionCancellationIntegrationTest`<br>`MilkFarmDryOffAlertsIntegrationTest` | Expandir observabilidade para consultas de sumario em ambientes de alta carga. |
| `reproduction` | ✅ Concluido | `POST /api/goatfarms/{farmId}/goats/{goatId}/reproduction/breeding`<br>`PATCH /api/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies/confirm`<br>`POST /api/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies/checks`<br>`PATCH /api/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies/{pregnancyId}/close`<br>`GET /api/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies/diagnosis-recommendation`<br>`GET /api/goatfarms/{farmId}/reproduction/alerts/pregnancy-diagnosis` | Fluxo de cobertura, confirmacao e encerramento de prenhez.<br>Uma gestacao ativa por cabra (enforced em DB e negocio).<br>Ownership em todas as rotas. | Alertas de diagnostico pendente agregados por fazenda.<br>Historicos paginados por cabra. | `ReproductionBusinessTest`<br>`ReproductionControllerTest`<br>`ReproductionBusinessPendingAlertsTest`<br>`ReproductionActivePregnancyIntegrationTest`<br>`ReproductionFarmPregnancyDiagnosisAlertsIntegrationTest` | Revisar UX/API para ajustes de correcoes com trilha de auditoria expandida. |
| `health` | ✅ Concluido | `POST /api/goatfarms/{farmId}/goats/{goatId}/health-events`<br>`PATCH /api/goatfarms/{farmId}/goats/{goatId}/health-events/{eventId}/done`<br>`PATCH /api/goatfarms/{farmId}/goats/{goatId}/health-events/{eventId}/cancel`<br>`PATCH /api/goatfarms/{farmId}/goats/{goatId}/health-events/{eventId}/reopen`<br>`GET /api/goatfarms/{farmId}/health-events/calendar`<br>`GET /api/goatfarms/{farmId}/health-events/alerts` | Agenda sanitaria por cabra e por fazenda.<br>Ownership padronizado por `@ownershipService.canManageFarm`.<br>Reabertura com restricao de perfil. | Alertas farm-level (`windowDays`) e calendario paginado por periodo. | `HealthEventBusinessTest`<br>`HealthEventControllerTest`<br>`HealthExceptionHandlingIntegrationTest` | Sem gap funcional critico na agenda farm-level; manter evolucao de cobertura para filtros de calendario. |
| `article` | ✅ Concluido | `GET /public/articles`<br>`GET /public/articles/highlights`<br>`GET /public/articles/{slug}`<br>`POST /api/articles`<br>`PATCH /api/articles/{id}/publish`<br>`PATCH /api/articles/{id}/highlight` | Canal publico separado de canal admin.<br>Mutacoes de artigos restritas a `ROLE_ADMIN`.<br>Fluxo draft -> publish/hightlight. | Listagens publicas e administrativas paginadas. | `ArticlePublicAdminIntegrationTest` | Aumentar cobertura unitaria em regras internas de publicacao/destaque. |
| `events` | ⚠️ Parcial | `POST /api/goatfarms/{farmId}/goats/{goatId}/events`<br>`PUT /api/goatfarms/{farmId}/goats/{goatId}/events/{eventId}`<br>`GET /api/goatfarms/{farmId}/goats/{goatId}/events`<br>`GET /api/goatfarms/{farmId}/goats/{goatId}/events/filter`<br>`DELETE /api/goatfarms/{farmId}/goats/{goatId}/events/{eventId}` | Registro historico de eventos por cabra.<br>Ownership por fazenda nas operacoes. | Consulta por filtro e listagem paginada por escopo de cabra/fazenda. | `EventDaoTest`<br>`EventDaoUnitTest` | `EventConsumer` possui TODOs para tipos de evento, impactando completude assicrona. |
| `genealogy` | ⚠️ Parcial | `GET /api/goatfarms/{farmId}/goats/{goatId}/genealogies` | Consulta de genealogia por cabra/fazenda.<br>Sem comandos dedicados de escrita no modulo atual. | Consulta direta por escopo especifico. | `GenealogyBusinessTest`<br>`GenealogyControllerTest` | Definir se o escopo permanece read-only ou se havera comandos proprios no roadmap. |
| `config/security` | ✅ Concluido | Endpoints de seguranca em `auth` + handlers globais (`401`, `403`, validacao e erros). | Contrato global de erro e validacao em PT-BR.<br>JWT + ownership service no backend. | Regras cross-cutting aplicadas sem acoplamento com dominio de negocio. | `HexagonalArchitectureGuardTest`<br>`MilkReproductionBoundaryArchUnitTest`<br>`GlobalExceptionHandlerTest`<br>`SecurityOwnershipIntegrationTest` | Preparar mitigacao para warning futuro do Mockito no JDK (agente dinamico). |

## 3. Cross-cutting (Padroes Globais)
- Seguranca e ownership:
- Padrao atual usa combinacoes de `@ownershipService.canManageFarm(...)` e `@ownershipService.isFarmOwner(...)` conforme modulo.
- Roles principais: `ROLE_ADMIN`, `ROLE_OPERATOR`, `ROLE_FARM_OWNER`.
- Gates de arquitetura:
- `HexagonalArchitectureGuardTest` impede import de camada `api` dentro de `business`.
- `MilkReproductionBoundaryArchUnitTest` bloqueia dependencia de `milk` em `reproduction.api`, `reproduction.business` e `reproduction.persistence.entity`.
- Shared kernel:
- `sharedkernel/pregnancy/PregnancySnapshot` integra leitura entre `reproduction` e `milk` via porta (`PregnancySnapshotQueryPort`), sem acoplamento de entidades.
- Erros e API:
- Contrato transversal de status e payload de erro em [API_CONTRACTS](../03-api/API_CONTRACTS.md).

## 4. Divida Tecnica Prioritaria (Backlog tecnico)
- P0:
- Concluir TODOs do `EventConsumer` e cobrir por testes de integracao de mensageria.
- Revisar convergencia de expressoes de ownership para reduzir variacao entre modulos.
- P1:
- Formalizar contrato oficial para operacao por fazenda (gestao de operador), alinhando `authority` com regras de ownership.
- Expandir cobertura automatizada para cenarios de erro limite em modulos com maior volume (`milk`, `health`, `events`).
- P2:
- Preparar configuracao de testes para remover dependencia de agente dinamico do Mockito em futuros JDKs.
- Definir, no roadmap funcional, se `genealogy` permanece somente consulta ou evolui para comandos dedicados.
