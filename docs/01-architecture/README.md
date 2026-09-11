# Guia de Arquitetura

Ultima atualizacao: 2026-09-10
Escopo: indice da arquitetura atual, seus gates e decisoes arquiteturais duradouras.
Links relacionados: [Portal](../INDEX.md), [Arquitetura](./ARCHITECTURE.md), [Padrao de modulo](./MODULE_STANDARD_MARKET_GRADE.md), [Quality gates](./QUALITY_GATES.md), [Goat ID4-B2](./GOAT_HEXAGONAL_CORE_ID4_B2.md), [Onda de consumidores GoatId](./GOAT_IDENTITY_DEPENDENT_MODULES_WAVE.md)

## Ordem de leitura

1. [ARCHITECTURE.md](./ARCHITECTURE.md) - camadas, fronteiras e dependencias.
2. [MODULE_STANDARD_MARKET_GRADE.md](./MODULE_STANDARD_MARKET_GRADE.md) - convencao por modulo.
3. [OBSERVABILITY_LOGGING.md](./OBSERVABILITY_LOGGING.md) - logging e correlation id.
4. [ABCC_INTEGRATION.md](./ABCC_INTEGRATION.md) - fronteira de integracao externa.
5. [QUALITY_GATES.md](./QUALITY_GATES.md) - validacoes locais e CI.
6. [ADR-002](./ADR/ADR-002-inventory-ledger-balance-and-lots.md), [ADR-003](./ADR/ADR-003-inventory-purchase-cost-breakdown.md) e [ADR-004](./ADR/ADR-004-goat-identity-model.md) - decisoes e propostas vigentes.
7. [GOAT_HEXAGONAL_CORE_ID4_B2](./GOAT_HEXAGONAL_CORE_ID4_B2.md) - primeira fronteira hexagonal implementada para o agregado Goat.
8. [GOAT_IDENTITY_DEPENDENT_MODULES_WAVE](./GOAT_IDENTITY_DEPENDENT_MODULES_WAVE.md) - propagacao tecnica e integridade dos consumidores dependentes.

## ADR-001

[ADR-001 em ingles](./ADR/ADR-001-atomic-goatfarm-registration.md) e a fonte
canonica da decisao de registro atomico. A pagina em portugues e uma ponte de
idioma, nao uma segunda decisao independente.

## Regra de precedencia

O codigo, migrations, testes, configuracao e CI prevalecem sobre qualquer
descricao humana. Documentos devem explicar o comportamento comprovado e
registrar follow-ups quando a intencao de produto ainda estiver ambigua.
