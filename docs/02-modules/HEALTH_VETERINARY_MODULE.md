# Plano do Módulo C — Saúde/Veterinário
Última atualização: 2026-02-10
Escopo: eventos sanitários, calendário e ações (realizar/cancelar)
Links relacionados: [Portal](../INDEX.md), [Arquitetura](../01-architecture/ARCHITECTURE.md), [Padrões de API](../03-api/API_CONTRACTS.md), [Domínio](../00-overview/BUSINESS_DOMAIN.md)

Este documento detalha o planejamento para o módulo de gestão sanitária e veterinária do sistema GoatFarm/CapriGestor.

## 1. Objetivo do Módulo

Centralizar eventos sanitários e tratamentos do rebanho, oferecendo:
- Registro de vacinas, vermifugações, medicações, procedimentos e doenças/ocorrências.
- Calendário sanitário (agendado + concluído).
- Controle por cabra individual e/ou lote (opcional na fase 1).
- Histórico consultável e relatórios simples (ex.: “próximos 30 dias”, “atrasados”).

---

## 2. Requisitos do Produto

### 2.1 Funcionais

- **Registrar evento de saúde para:**
  - Uma cabra (`goatId`).
  - Várias cabras (lista de `goatIds`).
  - *(Opcional Fase 2)* Lote/grupo (`groupId`).

- **Tipos de Evento:**
  - `VACINA`
  - `VERMIFUGAÇÃO`
  - `MEDICAÇÃO/TRATAMENTO`
  - `PROCEDIMENTO`
  - `DOENÇA/OCORRÊNCIA`

- **Status do Evento:**
  - `AGENDADO`
  - `REALIZADO`
  - `CANCELADO`
  - `ATRASADO` (Estado derivado: `scheduledDate < hoje` e `status=AGENDADO`).

- **Detalhamento:**
  - Para eventos com medicação/vacina: registrar produto, dose, via, lote, carência (leite/carne).

- **Listagens:**
  - Por cabra (timeline).
  - Por período (calendário).
  - Próximos eventos (ex.: próximos 7 ou 30 dias).
  - Atrasados.

- **Ações:**
  - **Editar evento:** ajustes de data, observações, dose.
  - **Concluir evento:** marcar como `REALIZADO` (exige `performedAt`, responsável, observações).
  - **Cancelar evento:** exige motivo.

- **Relatórios Simples (Fase 1):**
  - Próximos eventos por cabra.
  - Eventos realizados no mês.

### 2.2 Não Funcionais

- **Ownership:** Validação por `farmId` em todas as rotas `/api/goatfarms/{farmId}/...`.
- **Localização:** Mensagens de erro em PT-BR e payload padrão de erro.
- **Arquitetura:** Hexagonal (Controller não acessa Repository diretamente).
- **Testes:** Mínimo de 1 happy path + 1 regra crítica + 1 ownership deny.
- **Banco de Dados:** Migrações Flyway com constraints úteis.

---

## 3. Variáveis e Domínio

### 3.1 Entidades Principais

#### A) HealthEvent (Núcleo)
Representa “algo sanitário” (agendado ou realizado).

**Campos:**
- `id`: Long
- `farmId`: Long
- `goatId`: String (Fase 1 focada em individual; múltiplos vira endpoint em lote que cria vários eventos)
- `type`: `HealthEventType` (enum)
- `status`: `HealthEventStatus` (enum)
- `title`: String (ex.: “Vacina clostridial”, “Vermifugação trimestral”)
- `description`: String?
- `scheduledDate`: LocalDate (obrigatório para `AGENDADO`)
- `performedAt`: LocalDateTime? (obrigatório se `REALIZADO`)
- `responsible`: String? (nome de quem aplicou/realizou)
- `notes`: String?

**Campos "Clínica/Medicação" (Opcional por tipo):**
- `productName`: String?
- `activeIngredient`: String?
- `dose`: BigDecimal?
- `doseUnit`: `DoseUnit`?
- `route`: `AdministrationRoute`?
- `batchNumber`: String?
- `withdrawalMilkDays`: Integer?
- `withdrawalMeatDays`: Integer?

**Auditoria:**
- `createdAt`, `updatedAt`

#### B) HealthProtocol (Fase 2 - Opcional)
Modelo de “calendário padrão” (ex.: calendário de vacina/vermífugo) que gera eventos automaticamente.

### 3.2 Enums

- **HealthEventType:** `VACINA`, `VERMIFUGAÇÃO`, `MEDICAÇÃO`, `PROCEDIMENTO`, `DOENÇA`
- **HealthEventStatus:** `AGENDADO`, `REALIZADO`, `CANCELADO`
- **DoseUnit:** `ML`, `MG`, `UI`, `G`, `TABLET`, etc.
- **AdministrationRoute:** `IM`, `SC`, `IV`, `VO`, `TOPICA`, etc.

---

## 4. Regras de Negócio (Fase 1)

### RB1 — Ownership Obrigatório
Qualquer operação com `farmId` exige `OwnershipService.isFarmOwner(farmId)` (ou admin quando aplicável).

### RB2 — Coerência de Status e Datas
- **Se `status = AGENDADO`:**
  - `scheduledDate` é obrigatório.
  - `performedAt` deve ser nulo.
- **Se `status = REALIZADO`:**
  - `performedAt` obrigatório.
  - `scheduledDate` pode existir (para rastrear atraso/planejamento).
- **Se `status = CANCELADO`:**
  - Exigir `notes` com motivo (mínimo X caracteres).

### RB3 — “ATRASADO” é Derivado
Não persistir status `ATRASADO`. Um evento é considerado atrasado se:
- `status = AGENDADO` E `scheduledDate < hoje`.
- A API deve devolver um campo calculado: `isOverdue: boolean`.

### RB4 — Validações por Tipo
- **Para `VACINA` e `MEDICAÇÃO`:**
  - `productName` obrigatório.
  - `dose` e `doseUnit` obrigatórios.
  - `route` recomendado (pode ser obrigatório conforme regra de negócio).
- **Para `DOENÇA`:**
  - `description` obrigatório (mínimo).
- **Para `VERMIFUGAÇÃO`:**
  - `productName` obrigatório.

### RB5 — Integridade
- `goatId` deve existir e pertencer ao `farmId`.
- **Nota:** Não bloquear eventos por sexo (machos também recebem cuidados).

---

## 5. Métodos e Casos de Uso (Ports In)

### Port In: HealthEventCommandUseCase
- `createEvent(farmId, goatId, HealthEventCreateRequestVO) -> HealthEventResponseVO`
- `updateEvent(farmId, goatId, eventId, HealthEventUpdateRequestVO) -> HealthEventResponseVO`
- `markAsDone(farmId, goatId, eventId, HealthEventDoneRequestVO) -> HealthEventResponseVO`
- `cancelEvent(farmId, goatId, eventId, HealthEventCancelRequestVO) -> HealthEventResponseVO`
- `createBatchEvents(farmId, List<goatId>, HealthEventCreateRequestVO) -> List<HealthEventResponseVO>` (Opcional Fase 1)

### Port In: HealthEventQueryUseCase
- `getById(farmId, goatId, eventId)`
- `listByGoat(farmId, goatId, from, to, type, status, pageable)`
- `listCalendar(farmId, from, to, type, status, pageable)` (Todos os eventos do capril no período)
- `listOverdue(farmId, referenceDate=hoje, pageable)`
- `listUpcoming(farmId, days=30, pageable)`

### Port Out: HealthEventPersistencePort
- `save(event)`
- `findByIdAndFarmIdAndGoatId(...)`
- `findByFarmIdAndGoatId(filters...)`
- `findByFarmIdAndPeriod(filters...)`
- `existsGoatInFarm(farmId, goatId)` (ou reutilizar `GoatPersistencePort`)

---

## 6. API (Controllers) — Sugestão de Rotas

Base: `/api/goatfarms/{farmId}`

### Por Cabra
- `POST /goats/{goatId}/health-events`
- `PUT /goats/{goatId}/health-events/{eventId}`
- `PATCH /goats/{goatId}/health-events/{eventId}/done`
- `PATCH /goats/{goatId}/health-events/{eventId}/cancel`
- `GET /goats/{goatId}/health-events`
- `GET /goats/{goatId}/health-events/{eventId}`

### Visão do Capril (Calendário)
- `GET /health-events/calendar?from=...&to=...`
- `GET /health-events/overdue`
- `GET /health-events/upcoming?days=30`

**Notas:**
- Incluir `isOverdue` calculado na resposta.
- Erros em PT-BR com payload padrão.

---

## 7. Persistência (Flyway + JPA)

### Migration Sugerida
Arquivo: `VXX__create_health_events.sql`

### Constraints e Índices
- **FK Lógica:** `(farmId, goatId)` validada na camada de negócio (ou FK real se possível).
- **Índices:**
  - `(farm_id, goat_id, scheduled_date)`
  - `(farm_id, status, scheduled_date)`
  - `(farm_id, type, scheduled_date)`

---

## 8. Testes Essenciais (Fase 1)

### Integração (Mínimo Necessário)
- **Happy Path:** Criar evento `AGENDADO` e listar por cabra.
- **Regra Crítica:** Tentar marcar como `done` sem informar `performedAt` (deve falhar).
- **Ownership:** Validar que endpoint nega acesso sem posse (403) e sem token (401).

### Unitário (Enxuto)
- **Validator de Coerência:** Testar combinações de `status` vs `datas`.
- **Validator por Tipo:** Testar exigência de `dose`/`produto` para vacina/medicação.

---

## 9. Plano Passo a Passo de Implementação

### Passo 1 — Domínio e Contratos
- Criar Enums.
- Criar VOs (Create, Update, Done, Cancel, Response).
- Criar Validators de regra (mensagens PT-BR).

### Passo 2 — Ports + Business
- Definir interfaces `HealthEventCommandUseCase` e `HealthEventQueryUseCase`.
- Implementar `HealthEventBusiness` com as regras RB1–RB5.

### Passo 3 — Persistence
- Criar migration Flyway.
- Criar Entity JPA + Repository Spring Data.
- Implementar Adapter `HealthEventPersistenceAdapter`.

### Passo 4 — Controller
- Criar `HealthEventController` (rotas por cabra + calendário capril).
- Aplicar `@PreAuthorize` e validação de ownership.

### Passo 5 — Testes
- Implementar 3 testes de integração essenciais.
- Implementar testes unitários de validação.

### Passo 6 — Swagger PT-BR
- Adicionar `@Schema(description/example)` nos DTOs.
- Documentar filtros (from, to, type, status).

### Passo 7 — Frontend (Futuro)
- Timeline de saúde.
- Calendário sanitário.
- Formulários de criação/edição.

---

## 10. Entregáveis da Fase 1

- CRUD funcional (com fluxos de Done/Cancel).
- Endpoints de Calendário (`listCalendar`, `overdue`, `upcoming`).
- Testes essenciais verdes.
- Swagger e README atualizados com endpoints e exemplos.
