# DOCS AUDIT BEFORE

Data da auditoria: 2026-02-10
Escopo: docs oficiais em `docs/` (excluidos `docs/_archive` e `docs/_work`)
Links relacionados: [Lista oficial](./DOCS_OFFICIAL_LIST.md), [Portal](../INDEX.md)

## Docs auditados
- [docs/00-overview/BUSINESS_DOMAIN.md](../00-overview/BUSINESS_DOMAIN.md)
- [docs/00-overview/DOMAIN_GLOSSARY.md](../00-overview/DOMAIN_GLOSSARY.md)
- [docs/01-architecture/ADR/ADR-001-atomic-goatfarm-registration-pt-br.md](../01-architecture/ADR/ADR-001-atomic-goatfarm-registration-pt-br.md)
- [docs/01-architecture/ADR/ADR-001-atomic-goatfarm-registration.md](../01-architecture/ADR/ADR-001-atomic-goatfarm-registration.md)
- [docs/01-architecture/ARCHITECTURE.md](../01-architecture/ARCHITECTURE.md)
- [docs/01-architecture/README.md](../01-architecture/README.md)
- [docs/02-modules/ARTICLE_BLOG_MODULE.md](../02-modules/ARTICLE_BLOG_MODULE.md)
- [docs/02-modules/HEALTH_VETERINARY_MODULE.md](../02-modules/HEALTH_VETERINARY_MODULE.md)
- [docs/02-modules/LACTATION_MODULE.md](../02-modules/LACTATION_MODULE.md)
- [docs/02-modules/MILK_PRODUCTION_MODULE.md](../02-modules/MILK_PRODUCTION_MODULE.md)
- [docs/02-modules/REPRODUCTION_MODULE.md](../02-modules/REPRODUCTION_MODULE.md)
- [docs/03-api/API_CONTRACTS.md](../03-api/API_CONTRACTS.md)
- [docs/INDEX.md](../INDEX.md)

## Checklist por doc
| Doc | H1 | Ultima atualizacao | Escopo | Links relacionados | Links quebrados obvios | Duplicacao | Secao como usar/contratos |
|---|---|---|---|---|---|---|---|
| `docs/00-overview/BUSINESS_DOMAIN.md` | Sim | Sim | Sim | Sim | Nao | Parcial com `DOMAIN_GLOSSARY` (esperado) | N/A |
| `docs/00-overview/DOMAIN_GLOSSARY.md` | Sim | Nao | Nao | Nao | Sim (`../BUSINESS_DOMAIN.md`) | Parcial com `BUSINESS_DOMAIN` (esperado) | N/A |
| `docs/01-architecture/ADR/ADR-001-atomic-goatfarm-registration-pt-br.md` | Nao | Nao | Nao | Nao | Nao | Placeholder deprecado apontando para `_archive` | N/A |
| `docs/01-architecture/ADR/ADR-001-atomic-goatfarm-registration.md` | Nao | Nao | Nao | Nao | Nao | Placeholder deprecado apontando para `_archive` | N/A |
| `docs/01-architecture/ARCHITECTURE.md` | Sim | Sim | Sim | Sim | Sim (`file:///...`) | Parcial com `API_CONTRACTS` (esperado) | N/A |
| `docs/01-architecture/README.md` | Nao | Nao | Nao | Nao | Nao | Sobrepoe conteudo de navegacao do portal | N/A |
| `docs/02-modules/ARTICLE_BLOG_MODULE.md` | Sim | Sim | Sim | Sim | Nao | Nao identificado | Sim |
| `docs/02-modules/HEALTH_VETERINARY_MODULE.md` | Sim | Sim | Sim | Sim | Nao | Mistura plano futuro com contrato atual | Parcial |
| `docs/02-modules/LACTATION_MODULE.md` | Sim | Nao | Nao | Nao | Nao | Placeholder sem contrato, sobreposto por historico | Nao |
| `docs/02-modules/MILK_PRODUCTION_MODULE.md` | Sim | Sim | Sim | Sim | Nao | Nao identificado | Sim |
| `docs/02-modules/REPRODUCTION_MODULE.md` | Sim | Sim | Sim | Sim | Nao | Parcial com `API_CONTRACTS` (status/paginacao) | Sim |
| `docs/03-api/API_CONTRACTS.md` | Sim | Sim | Sim | Sim | Nao | Parcial com docs de modulo (esperado) | Sim |
| `docs/INDEX.md` | Sim | Nao | Nao | Nao | Sim (`./04-operations`, `./05-testing`) | Nao identificado | N/A |

## Metricas gerais (BEFORE)
- Quantidade de docs auditados: `13`
- Docs sem cabecalho padrao completo (H1 + Ultima atualizacao + Escopo + Links relacionados): `6`
- Ocorrencias de `file:///` em `docs/`: `1`
- Ocorrencias de `C:\\` em `docs/`: `0`
- Ocorrencias de `.././` em `docs/`: `0`
- Ocorrencias de links absolutos `http/https` em `docs/`: `10`
- Ocorrencias de links absolutos `http/https` apenas nos docs oficiais: `3`

## Resumo objetivo
- Problemas criticos de navegacao: link local `file:///` em arquitetura e links para secoes inexistentes (`04-operations`, `05-testing`) no portal.
- Problemas de padronizacao: 6 docs sem cabecalho minimo; 3 docs com placeholders sem estrutura minima.
- Problemas de consistencia: `HEALTH_VETERINARY_MODULE.md` mistura plano com contrato; `LACTATION_MODULE.md` nao documenta endpoints ativos.

## Evidencias de comandos
- `rg -n "file:///" docs` -> 1 ocorrencia em `docs/01-architecture/ARCHITECTURE.md`.
- `rg -n "C:\\\\" docs` -> sem ocorrencias.
- `rg -n "\.\./\./" docs` -> sem ocorrencias.
- `rg -n "https?://" docs` -> 10 ocorrencias (3 em docs oficiais e 7 em `_archive`).
