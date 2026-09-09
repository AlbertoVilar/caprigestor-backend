# Contexto para Agentes (Trae/Codex)
Ultima atualizacao: 2026-09-09
Escopo: regras operacionais e tecnicas para contribuicoes seguras no backend.
Links relacionados: [Portal](../INDEX.md), [Status do Projeto](./PROJECT_STATUS.md), [Roadmap](./ROADMAP.md), [Arquitetura](../01-architecture/ARCHITECTURE.md), [Contratos API](../03-api/API_CONTRACTS.md)

## 1. Non-negotiables do projeto
- Arquitetura hexagonal obrigatoria:
- Camada `business` nao pode importar `api` (gate: `HexagonalArchitectureGuardTest`).
- Modulos nao podem criar acoplamento indevido entre contextos (`MilkReproductionBoundaryArchUnitTest`).
- Sem acoplamento por entidade entre modulos; usar portas (`application/ports`) e shared kernel quando necessario.
- Ownership e seguranca:
- Toda rota farm-level deve respeitar ownership por `farmId`.
- Roles oficiais: `ROLE_ADMIN`, `ROLE_OPERATOR`, `ROLE_FARM_OWNER`.
- Documentacao:
- Nao criar `.md` no root (exceto `README.md`).
- Docs oficiais ficam em `docs/00-overview`, `docs/01-architecture`, `docs/02-modules`, `docs/03-api` e `docs/04-security`.
- Artefatos transitórios não são fonte oficial nem entram no portal; histórico durável fica em `docs/_archive` e auditorias encerradas em `docs/audits`.
- `CAPRIGESTOR_CURRENT_STATE.md`, quando existir, é contexto local ignorado e opcional; confirme seus fatos nas fontes versionadas e no código.
- Commits:
- Usar Conventional Commits em inglês, por exemplo: `docs: consolidate backend documentation governance`.
- Nao fazer push direto em `main`.

## 2. Onde encontrar contratos e contexto
- Portal oficial: [docs/INDEX.md](../INDEX.md)
- Contratos transversais de API: [docs/03-api/API_CONTRACTS.md](../03-api/API_CONTRACTS.md)
- Estado atual do projeto: [docs/00-overview/PROJECT_STATUS.md](./PROJECT_STATUS.md)
- Planejamento de evolucao: [docs/00-overview/ROADMAP.md](./ROADMAP.md)
- Modulos oficiais:
- [docs/02-modules/REPRODUCTION_MODULE.md](../02-modules/REPRODUCTION_MODULE.md)
- [docs/02-modules/LACTATION_MODULE.md](../02-modules/LACTATION_MODULE.md)
- [docs/02-modules/MILK_PRODUCTION_MODULE.md](../02-modules/MILK_PRODUCTION_MODULE.md)
- [docs/02-modules/HEALTH_VETERINARY_MODULE.md](../02-modules/HEALTH_VETERINARY_MODULE.md)
- [docs/02-modules/ARTICLE_BLOG_MODULE.md](../02-modules/ARTICLE_BLOG_MODULE.md)

## 3. Como rodar testes
- Gate de arquitetura:
```bash
./mvnw.cmd -Dtest=HexagonalArchitectureGuardTest test
```
- Gate de fronteira entre modulos:
```bash
./mvnw.cmd -Dtest=MilkReproductionBoundaryArchUnitTest test
```
- Suite completa:
```bash
./mvnw.cmd test
```

## 4. Fluxo de branches e PR
- Fluxo obrigatorio:
- `docs/*` ou `feature/*` -> PR para `develop` -> merge -> PR `develop` -> `main`.
- Nunca push direto para `main`.
- Padrao de trabalho:
```bash
git switch develop
git pull origin develop
git switch -c docs/nome-curto
```
- PRs:
- Descrever contexto tecnico, riscos, testes executados e docs atualizadas.
- Se houver mudanca de contrato, atualizar `API_CONTRACTS.md` e modulo correspondente no mesmo PR.

## 5. Checklist antes de PR
- [ ] `git status -sb` limpo (sem artefatos temporarios).
- [ ] `./mvnw.cmd -Dtest=HexagonalArchitectureGuardTest test` verde.
- [ ] `./mvnw.cmd -B clean verify` verde (ou justificativa explícita para suite parcial/bloqueio ambiental).
- [ ] Sem `.md` novo no root (exceto `README.md`).
- [ ] Links de docs sem protocolo local e sem caminho absoluto de maquina.
- [ ] `docs/INDEX.md` atualizado quando houver novo documento oficial.
- [ ] Commits no padrão Conventional Commits.
