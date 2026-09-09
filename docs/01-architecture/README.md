# Guia de Arquitetura

Ultima atualizacao: 2026-09-09
Escopo: indice da arquitetura atual, seus gates e decisoes arquiteturais duradouras.
Links relacionados: [Portal](../INDEX.md), [Arquitetura](./ARCHITECTURE.md), [Padrao de modulo](./MODULE_STANDARD_MARKET_GRADE.md), [Quality gates](./QUALITY_GATES.md)

## Ordem de leitura

1. [ARCHITECTURE.md](./ARCHITECTURE.md) - camadas, fronteiras e dependencias.
2. [MODULE_STANDARD_MARKET_GRADE.md](./MODULE_STANDARD_MARKET_GRADE.md) - convencao por modulo.
3. [OBSERVABILITY_LOGGING.md](./OBSERVABILITY_LOGGING.md) - logging e correlation id.
4. [ABCC_INTEGRATION.md](./ABCC_INTEGRATION.md) - fronteira de integracao externa.
5. [QUALITY_GATES.md](./QUALITY_GATES.md) - validacoes locais e CI.
6. [ADR-002](./ADR/ADR-002-inventory-ledger-balance-and-lots.md) e [ADR-003](./ADR/ADR-003-inventory-purchase-cost-breakdown.md) - decisoes vigentes.

## ADR-001

[ADR-001 em ingles](./ADR/ADR-001-atomic-goatfarm-registration.md) e a fonte
canonica da decisao de registro atomico. A pagina em portugues e uma ponte de
idioma, nao uma segunda decisao independente.

## Regra de precedencia

O codigo, migrations, testes, configuracao e CI prevalecem sobre qualquer
descricao humana. Documentos devem explicar o comportamento comprovado e
registrar follow-ups quando a intencao de produto ainda estiver ambigua.
