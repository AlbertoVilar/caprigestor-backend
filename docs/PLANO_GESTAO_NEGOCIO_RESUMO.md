# Plano Fase 2 — Resumo Executivo

## O que vamos fazer
- Expandir o sistema para gerir o negócio da fazenda: compras, vendas, estoque, alimentação, veterinário, produção de leite e financeiro.
- Manter a arquitetura atual (controller → facade → business → mapper/DAO) e evoluir por módulos.

## Por que
- Unificar dados operacionais com dados financeiros para ter visão real de custo e margem.
- Reduzir retrabalho em planilhas e melhorar rastreabilidade (estoque, lotes, validade, uso de medicamentos, consumo de ração).

## Módulos (visão rápida)
- Compras: fornecedores, produtos/insumos, pedido → recebimento → contas a pagar.
- Vendas: clientes, pedidos → faturamento → saída de estoque → contas a receber.
- Estoque: movimentos (entrada/saída/ajuste), lotes, validade, locais.
- Alimentação: planos alimentares, consumo por lote/animal, custo alimentar.
- Veterinário: atendimentos, protocolos, uso de medicamentos (baixa estoque), custos.
- Leite: produção diária, qualidade básica, destino/venda.
- Financeiro: plano de contas, centros de custo, lançamentos, títulos (a pagar/receber), conciliação simples.

## Dados essenciais (esboço)
- Produto (tipo, unidade, SKU), Fornecedor/Cliente.
- PedidoCompra/ItemCompra, Recebimento, Lote, MovimentacaoEstoque.
- PedidoVenda/ItemVenda, Fatura/Entrega.
- ProducaoLeite, SaidaLeite.
- AtendimentoVet, UsoMedicamento.
- PlanoContas, CentroCusto, LancamentoFinanceiro, ContaPagar/Receber.

## Primeiros 90 dias (marcos)
1. Cadastros base: fornecedores, clientes, produtos/unidades.
2. Estoque mínimo: entradas/saídas, lotes e validade.
3. Compras: pedido → recebimento → contas a pagar.
4. Vendas: pedido → faturamento → contas a receber.
5. Leite: registro diário e venda básica.
6. Relatórios/KPIs iniciais (custo por litro, margem por produto, giro de estoque).

## Critérios de aceite
- Estoque consistente por produto/lote, histórico auditável de movimentos.
- Compras/Vendas atualizam estoque e geram títulos corretamente.
- Produção de leite registrada por dia com destino (venda/uso interno).
- Relatórios refletem dados consolidados e confiáveis.

## Permissões (rascunho)
- Perfis: Operador, Veterinário, Estoquista, Financeiro, Admin.
- Escopo por ação crítica (aprovar, receber, faturar, liquidar, ajustar estoque).

## Riscos e como mitigar
- Complexidade financeira: começar simples, evoluir com feedback.
- Performance em estoque: índices e transações cuidadosas; preferir agregações por períodos.
- Integração fiscal: tratar como fase separada (não bloquear entregas operacionais).

## Próximos passos
- Validar escopo e prioridades.
- Detalhar épicos e histórias por módulo com critérios de aceite.
- Planejar sprints com entregas incrementais (backlog inicial).

---
Documento de apoio para leitura rápida. Veja também `docs/PLANO_GESTAO_NEGOCIO.md` para detalhes.