# ADR-002 - Inventory com ledger, balance materializado e lotes
Última atualização: 2026-02-10
Escopo: decisão arquitetural para o módulo Inventory no backend GoatFarm/CapriGestor.
Links relacionados: [Portal](../../INDEX.md), [Arquitetura](../ARCHITECTURE.md), [Módulo Inventory](../../02-modules/INVENTORY_MODULE.md), [API_CONTRACTS](../../03-api/API_CONTRACTS.md), [TODO MVP](../../_work/INVENTORY_TODO_MVP.md)

## Status
- Proposta aprovada para implementação do MVP.
- Ainda não implementado nesta tarefa.

## Contexto e problema
O backend já possui módulos operacionais relevantes (`health`, `milk`, `reproduction`) e precisa de uma base de estoque farm-level para:
- rastrear consumo de insumos e medicações;
- evitar inconsistência de saldo sob concorrência;
- preparar terreno para módulos futuros (`purchases/sales`, `finance`) sem acoplamento indevido.

Sem um contexto dedicado de Inventory, há risco de:
- lançamentos financeiros sem lastro operacional;
- baixa rastreabilidade de uso de itens críticos;
- regras de saldo duplicadas no front ou em múltiplos módulos.

## Decisão
Adotar `inventory` como bounded context próprio, com MVP já incluindo lotes e validade.

Decisão principal:
- consistência por `ledger + balance`:
- `stock_movement` como trilha imutável (fonte de verdade);
- `stock_balance` como projeção materializada para leitura rápida.

Decisões complementares:
- `OUT` e `ADJUST` decremento não podem gerar saldo negativo;
- idempotência obrigatória para `POST /movements` via `Idempotency-Key`;
- integração entre contextos por `sourceModule` + `sourceRef` (sem FK cruzada);
- concorrência controlada por lock pessimista em saldo (`SELECT ... FOR UPDATE`);
- itens rastreáveis exigem lote (`trackLot=true` => `lotId` em movimentos).

## Alternativas consideradas e trade-offs
### A) Inventory sem lotes/validade no MVP
- Prós:
- menor esforço inicial.
- Contras:
- retrabalho rápido para cenários de saúde;
- perda de rastreabilidade operacional.
- Decisão:
- rejeitada.

### B) Sem `stock_balance` (apenas agregação no ledger)
- Prós:
- menor redundância de armazenamento.
- Contras:
- consultas e alertas caros em escala;
- pior latência para telas farm-level.
- Decisão:
- rejeitada.

### C) Event-driven obrigatório já no MVP
- Prós:
- base assíncrona pronta para ecossistema maior.
- Contras:
- complexidade elevada para primeira entrega;
- aumenta risco de atraso do MVP.
- Decisão:
- adiado para pós-MVP.

### D) FK direta para entidades de outros módulos
- Prós:
- integridade relacional estrita de origem.
- Contras:
- quebra isolamento de contexto;
- acoplamento estrutural entre módulos.
- Decisão:
- rejeitada.

## Consequências e riscos
Consequências positivas:
- base sólida para evolução de compras/vendas e financeiro;
- rastreabilidade auditável de movimentos;
- melhor desempenho em leitura de saldo e alertas.

Riscos técnicos:
- deadlocks ou corrida em baixa simultânea;
- divergência entre ledger e balance se transação não for fechada corretamente;
- conflito de idempotência mal tratado sob retry.

Mitigações planejadas:
- ordem fixa de lock (item consolidado, depois lote);
- transação única para validar, gravar movement e atualizar balance;
- regra explícita de `409` para mesma key com payload divergente;
- testes de concorrência e idempotência no DoD.

Riscos de evolução:
- mudanças futuras de esquema (índices/constraints) exigirão migrações cuidadosas;
- ampliação de categorias/unidades deve preservar compatibilidade com contratos.

## Regras de fronteira (anti-corruption)
- `inventory` não importa camadas internas de `health`, `milk`, `reproduction`.
- única exceção de compartilhamento: `sharedkernel` aprovado.
- integração por referência textual:
- `sourceModule` (enum de contexto);
- `sourceRef` (identificador externo sem FK).

Critério de conformidade arquitetural:
- adicionar e manter `InventoryBoundaryArchUnitTest` no pipeline.

## Impacto no roadmap
`inventory` passa a ser pré-requisito técnico para:
- `purchases/sales`;
- consolidação financeira com lastro operacional;
- alertas operacionais mais consistentes no front.

## Referência de implementação
Backlog executável e ordem de entrega em: [INVENTORY_TODO_MVP.md](../../_work/INVENTORY_TODO_MVP.md)
