# Roadmap do Projeto GoatFarm/CapriGestor Backend
Ultima atualizacao: 2026-02-10
Escopo: priorizacao de evolucoes funcionais e tecnicas para escalacao do produto.
Links relacionados: [Portal](../INDEX.md), [Status do Projeto](./PROJECT_STATUS.md), [Arquitetura](../01-architecture/ARCHITECTURE.md), [Contratos API](../03-api/API_CONTRACTS.md), [Contexto para Agentes](./AGENT_CONTEXT.md), [Modulo Inventory](../02-modules/INVENTORY_MODULE.md), [ADR Inventory](../01-architecture/ADR/ADR-002-inventory-ledger-balance-and-lots.md)

## 1. Principios de priorizacao
- Valor de negocio primeiro: itens que reduzem retrabalho operacional e melhoram decisao por fazenda.
- Risco tecnico controlado: cada marco deve sair com cobertura minima de testes e sem quebrar gates de arquitetura.
- Dependencias explicitas: nenhum modulo novo deve acoplar diretamente entidades de outro contexto; integracao por portas/shared kernel.
- Escalabilidade progressiva: preferir endpoints farm-level com agregacao no backend e paginacao previsivel.
- Governanca de contratos: qualquer endpoint novo deve ser documentado em `docs/03-api/API_CONTRACTS.md` e no modulo correspondente.

## 2. Proximos marcos
### Milestone 1 - Hardening do nucleo atual (ownership + eventos)
- Objetivo:
- Fechar lacunas do backend atual para estabilizar o nucleo operacional antes de novos dominios.
- Escopo:
- Remover TODOs do `EventConsumer` com regras implementadas e testes.
- Revisar padrao de ownership para consistencia entre `isFarmOwner` e `canManageFarm`.
- Consolidar visao farm-level de alertas existentes (`reproduction`, `milk`, `health`) para consumo de frontend sem logica cara.
- Endpoints esperados:
- evolucao de endpoints existentes (sem quebrar contratos atuais).
- se necessario, endpoint agregador farm-level de alertas transversais: `GET /api/goatfarms/{farmId}/alerts/summary` (proposto).
- Riscos:
- mudancas em ownership podem impactar autorizacao de rotas existentes.
- ajustes assicronos em eventos exigem testes de regressao.
- Criterios de pronto (DoD):
- zero TODOs em fluxos produtivos do `EventConsumer`.
- testes de arquitetura e suite principal verdes.
- contratos e docs atualizados sem links quebrados.

### Milestone 2 - Inventory MVP (lotes + validade)
- Objetivo:
- estabelecer fundacao de estoque para consumo operacional, rastreabilidade e evolucao para compras/vendas e financeiro.
- Escopo:
- `inventory_item`, `inventory_lot`, `stock_movement` (ledger), `stock_balance` (materializado).
- idempotencia por `idempotencyKey` em movimentos.
- regra de dominio: `OUT` nao pode deixar saldo negativo.
- alertas farm-level: `low-stock` e `expiring` (`totalPending` + `alerts`).
- integracao com modulos atuais por `sourceModule` + `sourceRef` (sem FK cruzada).
- Endpoints esperados:
- `POST /api/goatfarms/{farmId}/inventory/items`
- `GET /api/goatfarms/{farmId}/inventory/items`
- `POST /api/goatfarms/{farmId}/inventory/items/{itemId}/lots`
- `POST /api/goatfarms/{farmId}/inventory/movements`
- `GET /api/goatfarms/{farmId}/inventory/movements`
- `GET /api/goatfarms/{farmId}/inventory/stock`
- `GET /api/goatfarms/{farmId}/inventory/alerts/low-stock`
- `GET /api/goatfarms/{farmId}/inventory/alerts/expiring`
- Dependencias:
- concluidos de Milestone 1 (ownership e hardening).
- aprovacao do [ADR-002](../01-architecture/ADR/ADR-002-inventory-ledger-balance-and-lots.md).
- Riscos:
- concorrencia em baixas simultaneas.
- inconsistencias de lote em fluxos de consumo.
- Criterios de pronto (DoD):
- saldo calculado de forma deterministica por item/lote.
- sem saldo negativo em nenhum cenario de comando.
- ownership validado em todos os endpoints.
- testes unitarios, integracao e arquitetura verdes.

### Milestone 3 - Purchases/Sales (compras e vendas)
- Objetivo:
- conectar operacao comercial ao estoque, mantendo rastreabilidade por fazenda.
- Escopo:
- compras: pedido, recebimento e impacto em estoque.
- vendas: pedido/faturamento e baixa de estoque.
- integracao de status operacional com movimentos de estoque.
- Endpoints esperados (propostos):
- `POST /api/goatfarms/{farmId}/purchases/orders`
- `POST /api/goatfarms/{farmId}/purchases/receipts`
- `POST /api/goatfarms/{farmId}/sales/orders`
- `POST /api/goatfarms/{farmId}/sales/invoices`
- Dependencias:
- Milestone 2 concluido (`inventory` como base).
- Riscos:
- estados de pedido podem divergir entre modulos sem contrato claro.
- idempotencia em recebimento/faturamento pode gerar dupla movimentacao.
- Criterios de pronto (DoD):
- fluxo ponta a ponta compra -> estoque e venda -> baixa de estoque validado.
- contratos de erro/status alinhados com `API_CONTRACTS`.
- testes de integracao cobrindo cenarios de concorrencia basicos.

### Milestone 4 - Finance consolidator
- Objetivo:
- disponibilizar consolidacao financeira minima para custos e receitas por fazenda com lastro operacional.
- Escopo:
- lancamentos financeiros basicos (debito/credito) vinculados a origem operacional.
- visoes consolidadas por periodo e centro de custo.
- base para analise de margem e custo por area.
- Endpoints esperados (propostos):
- `POST /api/goatfarms/{farmId}/finance/entries`
- `GET /api/goatfarms/{farmId}/finance/entries`
- `GET /api/goatfarms/{farmId}/finance/summary`
- Dependencias:
- Milestone 2 concluido para custo/consumo de estoque.
- Milestone 3 concluido para entrada de compras/vendas.
- Riscos:
- modelagem financeira excessiva antes da consolidacao operacional.
- divergencia entre dado operacional e financeiro sem reconciliacao minima.
- Criterios de pronto (DoD):
- regras minimas de contabilizacao documentadas e testadas.
- fechamento de periodo com agregacoes deterministicas.
- dashboard farm-level consumivel sem calculos pesados no frontend.

## 3. Dependencias entre marcos
- `Milestone 1` reduz risco para qualquer evolucao posterior.
- `Milestone 2` (`inventory`) e prerequisito direto para `Milestone 3` e `Milestone 4`.
- `Milestone 3` alimenta dados de receita/custo operacional para `Milestone 4`.

## 4. Regras de execucao do roadmap
- Cada marco deve abrir branch `feature/*` a partir de `develop`.
- Cada PR deve atualizar documentacao oficial (`INDEX`, modulo e contratos) junto com codigo.
- Nao criar `.md` no root (exceto `README.md`); artefatos de trabalho ficam em `docs/_work`.
