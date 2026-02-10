# ADR-002 - Inventory com ledger, saldo materializado e lotes
Ultima atualizacao: 2026-02-10
Escopo: decisao arquitetural para o modulo de estoque farm-level com rastreabilidade, idempotencia, concorrencia segura e validade por lote.
Links relacionados: [Portal](../../INDEX.md), [Arquitetura](../ARCHITECTURE.md), [Modulo Inventory](../../02-modules/INVENTORY_MODULE.md), [API_CONTRACTS](../../03-api/API_CONTRACTS.md), [TODO MVP](../../_work/INVENTORY_TODO_MVP.md)

## Contexto
O backend possui modulos operacionais maduros (`health`, `milk`, `reproduction`) com padrao farm-level, ownership e alertas agregados. A proxima evolucao precisa fornecer base consistente para:
- consumo de insumos sanitarios;
- rastreabilidade de lotes/validade;
- integracao com compras, vendas e financeiro sem acoplamento indevido.

Riscos identificados sem um modulo de `inventory`:
- financeiro sem lastro operacional;
- perda de rastreabilidade de uso de medicamentos e vacinas;
- inconsistencias de saldo sob concorrencia.

### Escopo do MVP (fechado)
Inclui:
- itens, lotes, movimentos, saldo materializado e alertas `low-stock`/`expiring`.

Nao inclui:
- `purchases/sales`, `finance`, `feeding` e motor de eventos como dependencia de consistencia.

## Decisao
Adotar `inventory` como bounded context dedicado, com MVP ja incluindo lotes/validade para itens rastreaveis.

Elementos da decisao:
- Modelo de consistencia:
- ledger imutavel em `stock_movement`;
- saldo materializado em `stock_balance` em dois niveis (item consolidado e lote).
- Regra de dominio obrigatoria:
- movimentos `OUT` nao podem deixar saldo negativo;
- `ADJUST` com decremento nao pode deixar saldo negativo;
- `trackLot=true` exige `lotId` em qualquer movimento.
- Quantidade e sentido:
- `quantity` sempre positiva;
- sentido definido por `movementType` (`IN`, `OUT`, `ADJUST`) + `adjustDirection` quando `ADJUST`.
- Idempotencia de API:
- `Idempotency-Key` obrigatoria no header de `POST /movements`;
- mesma chave + mesmo payload logico retorna replay idempotente (`200`);
- mesma chave + payload diferente retorna `409`.
- Integracao entre contextos:
- sem FK cruzada para modulos externos;
- referencia por `sourceModule` + `sourceRef`.
- Concorrencia:
- row-lock pessimista (`SELECT ... FOR UPDATE`) em `stock_balance`;
- ordem de lock fixa: saldo item, depois saldo lote.
- Seguranca:
- endpoints farm-level com `@PreAuthorize("@ownershipService.canManageFarm(#farmId)")`.
- Alertas:
- `low-stock` e `expiring` com formato `totalPending` + `alerts[]` e ordenacao estavel por severidade.
- Guardrail arquitetural:
- `inventory..` nao importa `health..`, `milk..`, `reproduction..` (exceto `sharedkernel..`).

## Alternativas consideradas
### A) Fazer Finance antes de Inventory
- Vantagem:
- entrega tela de lancamento mais rapida.
- Desvantagem:
- sem base de saldo/movimento, vira lancamento manual e fraco para auditoria.
- Decisao:
- rejeitada.

### B) Inventory sem lotes/validade no MVP
- Vantagem:
- menor esforco inicial.
- Desvantagem:
- retrabalho rapido no contexto de `health` (medicamentos/vacinas);
- reduz rastreabilidade regulatoria e operacional.
- Decisao:
- rejeitada.

### C) Integracao via FK direta para tabelas de outros modulos
- Vantagem:
- integridade referencial estrita no banco para origem do movimento.
- Desvantagem:
- cria acoplamento forte entre contextos e quebra principio hexagonal do repositorio;
- eleva custo de evolucao e migracao.
- Decisao:
- rejeitada.

### D) Saldo calculado apenas por agregacao de movimentos (sem tabela de balance)
- Vantagem:
- sem redundancia de armazenamento.
- Desvantagem:
- custo elevado de leitura para consultas e alertas frequentes;
- piora desempenho em escala.
- Decisao:
- rejeitada para MVP.

## Consequencias
### Positivas
- base tecnica solida para `purchases/sales` e `finance`;
- rastreabilidade completa de consumo por modulo de origem;
- leitura performatica de saldo e alertas farm-level;
- aderencia ao padrao arquitetural atual do repositorio.

### Custos e trade-offs
- maior complexidade inicial (ledger + balance + lotes + idempotencia + lock de concorrencia);
- necessidade de testes de concorrencia para garantir integridade de saldo;
- necessidade de normalizacao de `sourceRef` por modulo.

### Guardrails obrigatorios
- manter gates de arquitetura existentes verdes;
- adicionar `InventoryBoundaryArchUnitTest` com regra explicita de nao acoplamento;
- documentar qualquer integracao nova via `sourceModule` + `sourceRef` no modulo e no `API_CONTRACTS`.

### Impacto esperado em roadmap
- Inventory passa a ser pre-requisito explicito para:
- `purchases/sales`;
- `finance` com custo confiavel;
- evolucoes de consumo em `health` e `reproduction`.
