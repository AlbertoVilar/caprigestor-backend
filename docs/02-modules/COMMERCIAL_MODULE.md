# Modulo Commercial (Comercial e Financeiro Operacional Minimo)
Ultima atualizacao: 2026-03-28
Escopo: estado tecnico e funcional do modulo `commercial` apos a consolidacao da camada comercial minima e da etapa 1 do financeiro operacional da fazenda.
Links relacionados: [Portal](../INDEX.md), [Arquitetura](../01-architecture/ARCHITECTURE.md), [API Contracts](../03-api/API_CONTRACTS.md), [Inventory](./INVENTORY_MODULE.md)

## Status do documento
- Natureza: especificacao funcional e tecnica.
- Estado atual do modulo: clientes, vendas de animal, vendas de leite, recebiveis minimos, despesas operacionais e resumo mensal implementados.
- Objetivo: manter contratos, limites de escopo e regras de consistencia sincronizados com o codigo.

## Visao geral
O modulo `commercial` concentra a camada comercial e o financeiro operacional minimo por fazenda.

O escopo atual cobre:
- cadastro basico de clientes e compradores;
- venda de animal com coerencia com a saida controlada do rebanho;
- venda de leite com total calculado no backend;
- recebiveis minimos com estado `OPEN` ou `PAID`;
- despesas operacionais da fazenda;
- resumo mensal simples com receitas, saidas e saldo operacional.

Fora de escopo nesta etapa:
- ERP;
- contabilidade formal;
- centro de custo avancado;
- fluxo de caixa completo;
- contas a pagar complexas;
- DRE ou analytics avancado.

## Contratos REST
Base canonica: `/api/v1/goatfarms/{farmId}/commercial`

### Clientes
| Metodo | URL | Finalidade |
|---|---|---|
| `POST` | `/customers` | cadastrar cliente/comprador |
| `GET` | `/customers` | listar clientes da fazenda |

### Vendas
| Metodo | URL | Finalidade |
|---|---|---|
| `POST` | `/animal-sales` | registrar venda de animal com saida coerente |
| `GET` | `/animal-sales` | listar vendas de animal |
| `PATCH` | `/animal-sales/{saleId}/payment` | registrar pagamento da venda de animal |
| `POST` | `/milk-sales` | registrar venda de leite |
| `GET` | `/milk-sales` | listar vendas de leite |
| `PATCH` | `/milk-sales/{saleId}/payment` | registrar pagamento da venda de leite |

### Recebiveis
| Metodo | URL | Finalidade |
|---|---|---|
| `GET` | `/receivables` | listar recebiveis minimos derivados das vendas |

### Financeiro operacional
| Metodo | URL | Finalidade |
|---|---|---|
| `POST` | `/operational-expenses` | registrar despesa operacional |
| `GET` | `/operational-expenses` | listar despesas operacionais |
| `GET` | `/monthly-summary?year=2026&month=3` | resumo mensal simples da fazenda |

## Despesas operacionais
Estrutura minima da despesa:
- `farmId`
- `category`
- `description`
- `amount`
- `expenseDate`
- `notes` opcional

Categorias suportadas:
- `ENERGY`
- `WATER`
- `FREIGHT`
- `MAINTENANCE`
- `VETERINARY`
- `FUEL`
- `LABOR`
- `FEES`
- `OTHER`

Regras:
- `amount` deve ser maior que zero;
- `description` e `expenseDate` sao obrigatorios;
- a despesa pertence sempre a uma fazenda;
- nao existe aprovacao, plano de contas ou centro de custo nesta etapa.

## Resumo mensal simples
O resumo mensal responde:
- quanto entrou no mes;
- quanto saiu no mes;
- qual foi o saldo operacional do periodo.

Fontes de receita consideradas:
- vendas de animal efetivamente recebidas no mes;
- vendas de leite efetivamente recebidas no mes.

Fontes de saida consideradas:
- despesas operacionais pela `expenseDate`;
- compras de estoque com custo registrado pela `purchaseDate`.

Campos principais expostos:
- `totalRevenue`
- `totalExpenses`
- `balance`
- `animalSalesRevenue`
- `milkSalesRevenue`
- `operationalExpensesTotal`
- `inventoryPurchaseCostsTotal`

## Regras e consistencia
- todo endpoint do modulo e farm-level;
- o ownership continua em `@ownershipService.canManageFarm(#farmId)`;
- a venda de animal nao duplica a logica do ciclo do rebanho;
- recebiveis continuam minimos e derivados das vendas;
- o resumo mensal usa dados reais persistidos, sem agregador paralelo ou BI.

## Limites conscientes
Esta etapa nao implementa:
- valuation sofisticado de estoque;
- custo medio ou FIFO;
- caixa consolidado;
- contas a pagar complexas;
- contabilidade, impostos ou fiscal;
- dashboard gerencial grande.

## Validacao pratica desta etapa
Validacao real executada no capril de testes `farmId=17` em `2026-03-28`:
- compra de estoque com custo total `62.50`;
- despesa operacional `80.00`;
- venda de leite recebida `165.00`;
- delta mensal confirmado:
  - receita `+165.00`
  - saidas `+142.50`
  - saldo `+22.50`
