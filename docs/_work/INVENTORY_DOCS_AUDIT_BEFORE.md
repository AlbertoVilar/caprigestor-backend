# INVENTORY Docs Audit - BEFORE
Ultima atualizacao: 2026-02-10
Escopo: auditoria inicial da documentacao do modulo Inventory antes de refinamentos finais.
Links relacionados: [Portal](../INDEX.md), [Modulo Inventory](../02-modules/INVENTORY_MODULE.md), [ADR-002](../01-architecture/ADR/ADR-002-inventory-ledger-balance-and-lots.md), [TODO MVP](./INVENTORY_TODO_MVP.md), [API_CONTRACTS](../03-api/API_CONTRACTS.md), [Arquitetura](../01-architecture/ARCHITECTURE.md)

## Arquivos auditados
- `docs/02-modules/INVENTORY_MODULE.md`
- `docs/01-architecture/ADR/ADR-002-inventory-ledger-balance-and-lots.md`
- `docs/_work/INVENTORY_TODO_MVP.md`
- `docs/03-api/API_CONTRACTS.md`
- `docs/01-architecture/ARCHITECTURE.md`
- `docs/INDEX.md`

## Problemas por arquivo
### `docs/02-modules/INVENTORY_MODULE.md`
- Linguagem em varios trechos pode ser interpretada como comportamento ja implementado; falta reforco recorrente de que o conteudo e blueprint planejado.
- Seccao de endpoints de itens nao explicita endpoint dedicado de ativacao/desativacao (apenas `PATCH` generico).
- Existe sobreposicao de conteudo de decisao arquitetural com o ADR (detalhamento de rationale em excesso para documento de modulo).

### `docs/01-architecture/ADR/ADR-002-inventory-ledger-balance-and-lots.md`
- ADR esta tecnicamente bom, mas ainda contem detalhes operacionais que pertencem mais ao modulo/API (pode gerar duplicidade de fonte).
- Falta explicitar status da decisao (proposta/aceita) para orientar leitura de engenharia.
- Falta bloco proprio de regras de fronteira/anti-corruption com separacao visual mais forte.

### `docs/_work/INVENTORY_TODO_MVP.md`
- Backlog esta detalhado, mas mistura pontos de especificacao (ja cobertos no modulo/ADR) com tarefas executaveis.
- Itens MVP e POST-MVP ainda nao estao separados explicitamente.
- Falta checklist final consolidado de validacao para merge (comandos + criterio de aprovacao).

### `docs/03-api/API_CONTRACTS.md`
- Sem divergencias criticas com Inventory no estado atual.
- Servira como referencia para manter consistencia de status code e formato de erro.

### `docs/01-architecture/ARCHITECTURE.md`
- Sem bloqueios para Inventory; gate atual permite extensao por novo teste de fronteira.

### `docs/INDEX.md`
- Links de Inventory ja existem.
- Confirmar se navegacao para artefato de trabalho (`_work`) deve permanecer em "Referencias internas" (nao como doc oficial).

## Checklist de conformidade (restricoes da tarefa)
| Item | Status BEFORE | Observacao |
|---|---|---|
| PT-BR | Parcial | Texto em PT-BR, com termos tecnicos em ingles conforme padrao do repo. |
| Sem inventar comportamento implementado | Parcial | Necessario explicitar melhor "planejado" vs "ja existe". |
| Sem `file:///`, `C:\\`, `/home/` | OK | Nenhuma ocorrencia nos docs oficiais auditados. |
| Header padrao (Titulo, Ultima atualizacao, Escopo, Links relacionados) | Parcial | Estrutura existe, mas padrao textual sera uniformizado. |
| Separacao de papeis (ADR x MODULE x TODO) | Parcial | Existe sobreposicao entre ADR e modulo; TODO com redundancia. |
| Consistencia com `API_CONTRACTS.md` | OK | Status/erros/paginacao alinhados, com ajuste fino pendente. |

## Riscos de interpretacao
- Implementadores podem assumir que endpoints ja existem por uso de linguagem assertiva sem marcador de "planejado".
- Sobreposicao entre ADR e modulo pode causar manutencao dupla e divergencia futura.
- Ausencia de separacao MVP vs POST-MVP no TODO pode ampliar escopo indevidamente na primeira entrega.
- Falta de endpoint de ativacao/desativacao explicitado no modulo pode gerar implementacoes inconsistentes entre controllers.

## Verificacoes objetivas (BEFORE)
- `rg -n "file:///" docs -g "!docs/_work/**" -g "!docs/_archive/**"` -> sem ocorrencias.
- `rg -n "C:\\\\|/home/" docs -g "!docs/_work/**" -g "!docs/_archive/**"` -> sem ocorrencias.
- Policy root markdown: apenas `README.md` no root.
