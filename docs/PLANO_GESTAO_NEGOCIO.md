# Plano de Fase 2 — Gestão do Negócio da Fazenda

## Visão Geral

- Objetivo: ampliar o sistema para gerir o negócio da fazenda (compras, vendas, estoque, financeiro, produção de leite e despesas veterinárias), mantendo a arquitetura atual (controller → facade → business → mapper/DAO) e evoluindo de forma incremental.
- Abordagem: módulos autocontidos, integração mínima entre domínios por eventos de negócio e serviços, com persistência relacional e transações nos fluxos críticos.

## Escopo

- Compras de insumos (ração, medicamentos, materiais); cadastro de fornecedores e produtos/insumos.
- Vendas (leite e outros produtos), cadastro de clientes, pedidos e faturamento.
- Estoque e movimentações (entradas, saídas, ajustes, lotes e validade, rastreabilidade).
- Alimentação: planos alimentares e consumo por lote/animal.
- Veterinário: atendimentos, protocolos, receitas, custos e controle de medicamentos.
- Produção de leite: registro diário, qualidade básica, destino e venda.
- Financeiro: plano de contas, centros de custo, lançamentos (receitas/despesas), contas a pagar/receber e conciliação simples.
- Relatórios e indicadores (KPIs) para apoio à gestão.

## Módulos e Domínios

- Fornecedores e Compras
  - Entidades: Fornecedor, Produto/Insumo, PedidoCompra, ItemCompra, Recebimento, FaturaCompra.
  - Fluxos: criar pedido, aprovar/ordenar, receber (gera entrada de estoque), lançar pagamento.

- Clientes e Vendas
  - Entidades: Cliente, PedidoVenda, ItemVenda, SaídaEstoque, FaturaVenda, Entrega.
  - Fluxos: criar pedido, faturar, separar/expedir (baixa de estoque), receber pagamento.

- Estoque
  - Entidades: Produto, Lote, MovimentacaoEstoque (entrada/saída/ajuste), Local/Armazém.
  - Regras: movimentações atômicas, validação de saldo, controle de validade/lote.

- Alimentação
  - Entidades: PlanoAlimentar, Ingrediente/Composto, ConsumoDiario (por lote/animal), CustoAlimentar.
  - Fluxos: planejar dieta, registrar consumo, impactar estoque e custo.

- Veterinário
  - Entidades: Atendimento, Protocolo, Prescricao, UsoMedicamento (baixa estoque), CustoVeterinario.
  - Fluxos: registrar atendimento, prescrever medicamento, controlar uso/estoque, custos.

- Produção de Leite
  - Entidades: ProducaoLeite (diária), Tanque/LoteLeite, Qualidade (atributos básicos), SaidaLeite.
  - Fluxos: registrar produção, consolidar por período, destinar para venda/uso, gerar saída/receita.

- Financeiro
  - Entidades: PlanoContas, CentroCusto, LancamentoFinanceiro, ContaPagar, ContaReceber, Conciliacao.
  - Fluxos: lançar despesas/receitas por centro de custo, gerar contas a pagar/receber a partir de compras/vendas, liquidar e conciliar.

## Modelagem de Dados (esboço)

### Entidades e Associações (completas)

Observação: todas as entidades de negócio devem referenciar `farm_id` para suporte multi-fazenda (escopo por fazenda) e se encaixar nos módulos já existentes (`authority`, `farm`, `goat`). Onde aplicável, usamos chaves técnicas (`id`) e códigos/opcionais legíveis (ex.: `sku`, `codigo_lote`).

#### Núcleo de Pessoas e Cadastros

- Fornecedor(id, farm_id, nome, tipo: PF|PJ, documento: cpf|cnpj, contato, endereço_id)
  - Associação: 1..1 Endereço; 1..N PedidoCompra; 1..N ContaPagar

- Cliente(id, farm_id, nome/razao, tipo: PF|PJ, documento: cpf|cnpj, contato, endereço_id)
  - Associação: 1..1 Endereço; 1..N PedidoVenda; 1..N ContaReceber

- Endereço(id, logradouro, numero, complemento, bairro, cidade, estado, cep)
  - Associação: usado por Fornecedor/Cliente; reuso de modelo existente em `farm/address`

#### Catálogo e Unidades

- Produto(id, farm_id, nome, categoria: INSUMO|MEDICAMENTO|ALIMENTO|LEITE|OUTRO, unidade_id, sku, ativo)
  - Associação: N..1 Unidade; 1..N Lote; 1..N MovimentacaoEstoque; 1..N ItemCompra/ItemVenda; 0..N Regras de Alimentação

- Unidade(id, sigla: KG|L|UN|SACO|CX, descricao)
  - Associação: 1..N Produto

#### Estoque

- LocalEstoque(id, farm_id, nome, tipo: ARMAZEM|TANQUE|SALA|FREEZER, pai_id?)
  - Associação: 1..N Lote (posicionado), 1..N MovimentacaoEstoque

- Lote(id, farm_id, produto_id, codigo, validade?, saldo, local_estoque_id)
  - Associação: N..1 Produto; N..1 LocalEstoque; 1..N MovimentacaoEstoque

- MovimentacaoEstoque(id, farm_id, produto_id, lote_id?, local_origem_id?, local_destino_id?, tipo: ENTRADA|SAIDA|AJUSTE|TRANSFERENCIA, quantidade, origem_referencia, data)
  - Associação: N..1 Produto; 0..1 Lote; 0..1 Local origem/destino; origem_referencia (polimórfica: id de PedidoCompra/Recebimento, PedidoVenda, UsoMedicamento, ConsumoAlimentar, SaidaLeite etc.)

- SaldoEstoque(id, farm_id, produto_id, lote_id?, local_estoque_id, quantidade)
  - Observação: tabela materializada/derivada para consultas; atualizada por gatilhos transacionais no `InventoryService`

#### Compras

- PedidoCompra(id, farm_id, fornecedor_id, data, status: RASCUNHO|APROVADO|RECEBIDO|CANCELADO, observacao)
  - Associação: N..1 Fornecedor; 1..N ItemCompra; 0..N Recebimento; 0..1 FaturaCompra; 0..N ContaPagar

- ItemCompra(id, pedido_compra_id, produto_id, quantidade, preco_unitario)
  - Associação: N..1 PedidoCompra; N..1 Produto

- Recebimento(id, pedido_compra_id, data, observacao)
  - Associação: N..1 PedidoCompra; 1..N RecebimentoItem

- RecebimentoItem(id, recebimento_id, produto_id, quantidade, lote_codigo?, validade?, local_estoque_id)
  - Associação: N..1 Recebimento; N..1 Produto; N..1 LocalEstoque; cria/atualiza Lote + MovimentacaoEstoque(ENTRADA)

- FaturaCompra(id, pedido_compra_id, numero, emissao, valor_total)
  - Associação: N..1 PedidoCompra; 1..N ContaPagar

#### Vendas

- PedidoVenda(id, farm_id, cliente_id, data, status: RASCUNHO|FATURADO|EXPEDIDO|CANCELADO)
  - Associação: N..1 Cliente; 1..N ItemVenda; 0..1 FaturaVenda; 0..N ContaReceber; 0..N Entrega

- ItemVenda(id, pedido_venda_id, produto_id, quantidade, preco_unitario, lote_id?)
  - Associação: N..1 PedidoVenda; N..1 Produto; 0..1 Lote; gera MovimentacaoEstoque(SAIDA)

- FaturaVenda(id, pedido_venda_id, numero, emissao, valor_total)
  - Associação: N..1 PedidoVenda; 1..N ContaReceber

- Entrega(id, pedido_venda_id, data, metodo: RETIRADA|ENTREGA, status)
  - Associação: N..1 PedidoVenda; baixa logística e rastreio

#### Alimentação

- PlanoAlimentar(id, farm_id, nome, escopo: FAZENDA|LOTE|ANIMAL, destino_id?)
  - Associação: 1..N PlanoAlimentarItem; escopo define se `destino_id` referencia Lote ou Animal

- PlanoAlimentarItem(id, plano_id, produto_id, quantidade_por_dia, unidade_id)
  - Associação: N..1 PlanoAlimentar; N..1 Produto; N..1 Unidade

- ConsumoAlimentar(id, farm_id, data, escopo: LOTE|ANIMAL, destino_id, produto_id, quantidade)
  - Associação: N..1 Produto; reduz estoque via MovimentacaoEstoque(SAIDA); gera CustoAlimentar

- CustoAlimentar(id, farm_id, data, destino: LOTE|ANIMAL, valor, origem_consumo_id)
  - Associação: N..1 ConsumoAlimentar; 0..N LancamentoFinanceiro

#### Veterinário

- AtendimentoVet(id, farm_id, data, destino: LOTE|ANIMAL, destino_id, protocolo, observacao)
  - Associação: 1..N Prescricao; 0..N UsoMedicamento; gera CustoVeterinario

- Prescricao(id, atendimento_id, descricao)
  - Associação: N..1 AtendimentoVet; 1..N PrescricaoItem

- PrescricaoItem(id, prescricao_id, produto_id, dose, unidade_id)
  - Associação: N..1 Prescricao; N..1 Produto; N..1 Unidade

- UsoMedicamento(id, atendimento_id, produto_id, quantidade, lote_id?)
  - Associação: N..1 AtendimentoVet; N..1 Produto; 0..1 Lote; reduz estoque via MovimentacaoEstoque(SAIDA); gera CustoVeterinario

- CustoVeterinario(id, farm_id, atendimento_id, valor)
  - Associação: N..1 AtendimentoVet; 0..N LancamentoFinanceiro

#### Produção de Leite

- ProducaoLeite(id, farm_id, data, quantidade_total, qualidade_basica)
  - Associação: 0..N AmostraQualidade; 0..N TanqueLeite; 0..N SaidaLeite; pode gerar Receita

- AmostraQualidade(id, producao_id, atributo: ACIDEZ|GORDURA|DENSIDADE|TEMP, valor)
  - Associação: N..1 ProducaoLeite

- TanqueLeite(id, farm_id, codigo, capacidade, local_estoque_id)
  - Associação: 1..N Lote (de produto LEITE) posicionados; integra com Estoque em `LocalEstoque`

- SaidaLeite(id, farm_id, producao_id?, produto_id, quantidade, destino: VENDA|CONSUMO_INTERNO)
  - Associação: N..1 Produto (LEITE); 0..1 ProducaoLeite; reduz estoque via MovimentacaoEstoque(SAIDA); gera ContaReceber se VENDA

#### Financeiro

- PlanoContas(id, farm_id, codigo, nome, tipo: RECEITA|DESPESA)
  - Associação: 1..N LancamentoFinanceiro

- CentroCusto(id, farm_id, nome)
  - Associação: 1..N LancamentoFinanceiro

- LancamentoFinanceiro(id, farm_id, plano_contas_id, centro_custo_id, data, valor, natureza: CREDITO|DEBITO, origem_referencia)
  - Associação: N..1 PlanoContas; N..1 CentroCusto; origem de módulos (Compra/Venda/Alimentação/Vet/Leite)

- ContaPagar(id, farm_id, fornecedor_id, pedido_compra_id?, vencimento, valor, status: ABERTA|PAGA|CANCELADA)
  - Associação: N..1 Fornecedor; 0..1 PedidoCompra; 1..N PagamentoPagar

- PagamentoPagar(id, conta_pagar_id, data, valor, metodo)
  - Associação: N..1 ContaPagar; gera LancamentoFinanceiro(DÉBITO)

- ContaReceber(id, farm_id, cliente_id, pedido_venda_id?, vencimento, valor, status: ABERTA|RECEBIDA|CANCELADA)
  - Associação: N..1 Cliente; 0..1 PedidoVenda; 1..N RecebimentoReceber

- RecebimentoReceber(id, conta_receber_id, data, valor, metodo)
  - Associação: N..1 ContaReceber; gera LancamentoFinanceiro(CRÉDITO)

#### Autoridade e Fazendas (integração existente)

- Farm(id, nome, tod, endereço_id)
  - Associação: 1..1 Endereço; entidades desta fase referenciam `farm_id` para escopo/tenancy

- User(id, name, email, cpf, roles)
  - Associação: permissões por módulo/ação já existentes; vincular operações críticas à auditoria

### Limites de Agregados e Regras de Propriedade

- Inventory como dono de `MovimentacaoEstoque`, `SaldoEstoque` e `Lote`: outros módulos solicitam movimentações via `InventoryService` (não manipulam DAOs diretamente).
- Purchasing é dono de `PedidoCompra`, `ItemCompra`, `Recebimento` e cria entradas via serviço de estoque.
- Sales é dono de `PedidoVenda`, `ItemVenda`, `FaturaVenda` e dispara saídas via estoque.
- Feeding/Vet geram baixas de estoque e custos; não manipulam diretamente objetos de estoque ou financeiro.
- Finance é dono de `LancamentoFinanceiro`, `PlanoContas`, `CentroCusto`, `AP/AR` e recebe eventos/solicitações dos demais.

### Cardinalidades e Invariantes (exemplos críticos)

- `PedidoCompra` deve ter ≥1 `ItemCompra`; transição para `RECEBIDO` exige pelo menos um `Recebimento` com quantidade válida.
- `MovimentacaoEstoque` não pode resultar em saldo negativo por `produto+lote+local`.
- `ItemVenda` associado a `lote_id` opcional, mas se especificado, valida saldo do lote.
- `ConsumoAlimentar` e `UsoMedicamento` sempre reduzem estoque com validação de unidade e produto.
- `ContaPagar/Receber` somatório de `Pagamento/Recebimento` não pode exceder valor do título; status consistente.

### Encaixe com Módulos Existentes (GoatFarm)

- Reuso de `authority` (roles/permissions) para controlar operações: aprovar pedido, receber, faturar, liquidar, ajustar estoque.
- `farm` já define a fazenda e endereço; novas entidades referenciam `farm_id` e, quando aplicável, endereços existentes.
- Domínio de animais/caprinos existente se integra com `feeding` e `vet` via destino `LOTE|ANIMAL` (IDs já presentes no módulo `goat`).
- Padrão de camadas atual preservado: `controller → facade → business → mapper/DAO` por módulo; serviços entre módulos via interfaces (ex.: `InventoryService`, `FinanceService`).


## Fluxos de Negócio (alto nível)

- Compra
  - Criar pedido → aprovar → receber (entrada estoque/lotes) → gerar contas a pagar → liquidar.

- Venda
  - Criar pedido → faturar → separar (saída estoque) → gerar contas a receber → receber.

- Leite
  - Registrar produção diária → destinar (venda ou uso interno) → baixar estoque ou registrar receita.

- Veterinário
  - Registrar atendimento → prescrever uso → baixar medicamento em estoque → lançar custo.

- Alimentação
  - Planejar dieta → registrar consumo → baixar insumos → lançar custo alimentar por centro de custo.

- Financeiro
  - Lançamentos manuais e automáticos (vinculados a compras/vendas) → pagar/receber → conciliar.

## Relatórios e KPIs

- Custos por litro de leite (alimentação, veterinário, insumos, mão-de-obra simplificada).
- Margem por produto/venda.
- Giro de estoque e validade crítica.
- Produção diária/mensal e qualidade básica.
- Despesas por centro de custo e categoria.

## Permissões e Segurança

- Perfis: Operador, Veterinário, Estoquista, Financeiro, Administrador.
- Escopo por módulo e operação (CRUD, aprovar, receber, faturar, liquidar).
- Logs de auditoria de operações críticas (movimentação de estoque, financeiro).

## Integrações (posteriores)

- Emissão de documentos fiscais (futuro, não nesta fase inicial).
- Exportação CSV/Excel de relatórios.
- Webhooks/outbox para integrações externas.

## Roadmap (marcos)

1. Fundamentos de cadastro: Fornecedores, Clientes, Produtos/Unidades.
2. Estoque mínimo: entradas/saídas, lotes e validade.
3. Compras: pedido → recebimento → contas a pagar.
4. Vendas: pedido → faturamento → contas a receber.
5. Produção de leite: registro e venda.
6. Veterinário: atendimentos e uso de medicamentos.
7. Alimentação: planos e consumo.
8. Financeiro: plano de contas, centros de custo e lançamentos.
9. Relatórios/KPIs e permissões detalhadas.

## APIs e Páginas (rascunho)

> **Nota**: O padrão de rotas do projeto segue a estrutura hierárquica `/api/goatfarms/{farmId}/...`. As rotas abaixo são sugestões e devem ser adaptadas para esse padrão na implementação.

- Compras: `/api/goatfarms/{farmId}/suppliers`, `/api/goatfarms/{farmId}/products`, `/api/goatfarms/{farmId}/purchase-orders`
- Vendas: `/api/goatfarms/{farmId}/customers`, `/api/goatfarms/{farmId}/sales-orders`, `/api/goatfarms/{farmId}/invoices`
- Estoque: `/api/goatfarms/{farmId}/inventory/movements`, `/api/goatfarms/{farmId}/inventory/lots`
- Leite: `/api/goatfarms/{farmId}/milk/production`, `/api/goatfarms/{farmId}/milk/sales`
- Veterinário: `/api/goatfarms/{farmId}/vet/appointments`, `/api/goatfarms/{farmId}/vet/med-usage`
- Alimentação: `/api/goatfarms/{farmId}/feeding/plans`, `/api/goatfarms/{farmId}/feeding/consumption`
- Financeiro: `/api/goatfarms/{farmId}/finance/chart-of-accounts`, `/api/goatfarms/{farmId}/finance/entries`

## Riscos e Considerações

- Complexidade financeira: iniciar com fluxo simples (sem conciliações bancárias avançadas).
- Integração fiscal: tratar como fase separada.
- Performance de estoque: indexar movimentos e usar transações com cuidado.
- Usabilidade: processos guiados (wizard) para compra/venda/recebimento/expedição.

## Critérios de Aceite (por módulo)

- Compras/Vendas: pedidos com itens, atualização de estoque e geração de títulos.
- Estoque: saldo consistente por produto/lote, histórico auditável.
- Leite: registro diário e vendas refletindo em receitas.
- Veterinário/Alimentação: baixa de estoque e custos associados.
- Financeiro: lançamentos por plano de contas e centro de custo, apuração básica.

---

Observação: documento de trabalho (rascunho). Ajustaremos detalhes conforme feedback.