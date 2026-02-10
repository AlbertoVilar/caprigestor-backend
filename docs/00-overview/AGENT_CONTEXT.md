# Contexto para Agentes (Trae/Codex)
Ultima atualizacao: 2026-02-10
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
- Docs oficiais ficam em `docs/00-overview`, `docs/01-architecture`, `docs/02-modules`, `docs/03-api` (e `04/05` quando existirem).
- Artefatos de trabalho ficam em `docs/_work`.
- Commits:
- Mensagens sem espaco (usar underscore), por exemplo: `docs_add_project_status_and_roadmap`.
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
- `feature/*` -> PR para `develop` -> merge -> PR `develop` -> `main`.
- Nunca push direto para `main`.
- Padrao de trabalho:
```bash
git switch develop
git pull origin develop
git switch -c feature/nome_curto_sem_espaco
```
- PRs:
- Descrever contexto tecnico, riscos, testes executados e docs atualizadas.
- Se houver mudanca de contrato, atualizar `API_CONTRACTS.md` e modulo correspondente no mesmo PR.

## 5. Checklist antes de PR
- [ ] `git status -sb` limpo (sem artefatos temporarios).
- [ ] `./mvnw.cmd -Dtest=HexagonalArchitectureGuardTest test` verde.
- [ ] `./mvnw.cmd test` verde (ou justificativa explicita para suite parcial).
- [ ] Sem `.md` novo no root (exceto `README.md`).
- [ ] Links de docs sem protocolo local e sem caminho absoluto de maquina.
- [ ] `docs/INDEX.md` atualizado quando houver novo documento oficial.
- [ ] Commits com mensagem sem espaco.
