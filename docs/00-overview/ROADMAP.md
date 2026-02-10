# Roadmap do Projeto GoatFarm/CapriGestor Backend
Ultima atualizacao: 2026-02-10
Escopo: priorizacao de evolucoes funcionais e tecnicas para escalacao do produto.
Links relacionados: [Portal](../INDEX.md), [Status do Projeto](./PROJECT_STATUS.md), [Arquitetura](../01-architecture/ARCHITECTURE.md), [Contratos API](../03-api/API_CONTRACTS.md), [Contexto para Agentes](./AGENT_CONTEXT.md)

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
- Evolucao de endpoints existentes (sem quebrar contratos atuais).
- Se necessario, endpoint agregador farm-level de alertas transversais: `GET /api/goatfarms/{farmId}/alerts/summary` (proposto).
- Riscos:
- Mudancas em ownership podem impactar autorizacao de rotas existentes.
- Ajustes assicronos em eventos exigem testes de regressao.
- Criterios de pronto (DoD):
- Zero TODOs em fluxos produtivos do `EventConsumer`.
- Testes de arquitetura e suite principal verdes.
- Contratos e docs atualizados sem links quebrados.

### Milestone 2 - Inventory base (estoque)
- Objetivo:
- Criar fundacao de estoque para suportar compras, consumo de racao/medicacao e custos.
- Escopo:
- Catalogo de itens (`feed`, `medicine`, `supply`) por fazenda.
- Movimentacao de entrada/saida e saldo por item/lote.
- Regras de unidade de medida e consistencia de saldo.
- Endpoints esperados (propostos):
- `POST /api/goatfarms/{farmId}/inventory/items`
- `GET /api/goatfarms/{farmId}/inventory/items`
- `POST /api/goatfarms/{farmId}/inventory/movements`
- `GET /api/goatfarms/{farmId}/inventory/stock`
- Riscos:
- Definicao incorreta de unidades pode gerar inconsistencias de saldo.
- Alto volume de movimentacao exige indexacao e filtros desde o inicio.
- Criterios de pronto (DoD):
- Saldo calculado de forma deterministica por item/lote.
- Ownership e autorizacao validados em todas as rotas.
- Testes de regra de negocio cobrindo entradas, saidas e bloqueio de saldo negativo.

### Milestone 3 - Purchases/Sales (compras e vendas)
- Objetivo:
- Conectar operacao comercial ao estoque, mantendo rastreabilidade por fazenda.
- Escopo:
- Compras: pedido, recebimento e impacto em estoque.
- Vendas: pedido/faturamento e baixa de estoque.
- Integracao de status operacional com eventos de dominio.
- Endpoints esperados (propostos):
- `POST /api/goatfarms/{farmId}/purchases/orders`
- `POST /api/goatfarms/{farmId}/purchases/receipts`
- `POST /api/goatfarms/{farmId}/sales/orders`
- `POST /api/goatfarms/{farmId}/sales/invoices`
- Riscos:
- Estados de pedido podem divergir entre modulos se nao houver contrato claro.
- Integracao com estoque requer idempotencia em recebimento/faturamento.
- Criterios de pronto (DoD):
- Fluxo ponta a ponta compra -> estoque e venda -> baixa de estoque validado.
- Contratos de erro/status alinhados com `API_CONTRACTS`.
- Testes de integracao cobrindo cenarios de concorrencia basicos.

### Milestone 4 - Finance consolidator
- Objetivo:
- Disponibilizar consolidacao financeira minima para custos e receitas por fazenda.
- Escopo:
- Lancamentos financeiros basicos (debito/credito) vinculados a origem operacional.
- Visoes consolidadas por periodo e centro de custo.
- Base para analise de margem e custo por area.
- Endpoints esperados (propostos):
- `POST /api/goatfarms/{farmId}/finance/entries`
- `GET /api/goatfarms/{farmId}/finance/entries`
- `GET /api/goatfarms/{farmId}/finance/summary`
- Riscos:
- Modelagem financeira excessiva antes da consolidacao de inventory/comercial.
- Divergencia entre dado operacional e dado financeiro sem reconciliacao minima.
- Criterios de pronto (DoD):
- Regras minimas de contabilizacao documentadas e testadas.
- Fechamento de periodo com agregacoes deterministicas.
- Dashboard farm-level consumivel sem calculos pesados no frontend.

## 3. Dependencias entre marcos
- `Milestone 1` reduz risco para qualquer evolucao posterior.
- `Milestone 2` e prerequisito para `Milestone 3` e `Milestone 4`.
- `Milestone 3` alimenta dados de receita/custo operacional para `Milestone 4`.

## 4. Regras de execucao do roadmap
- Cada marco deve abrir branch `feature/*` a partir de `develop`.
- Cada PR deve atualizar documentacao oficial (`INDEX`, modulo e contratos) junto com codigo.
- Nao criar `.md` no root (exceto `README.md`); artefatos de trabalho ficam em `docs/_work`.
