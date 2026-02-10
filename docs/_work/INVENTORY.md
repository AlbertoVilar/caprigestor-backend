# Inventário de Documentação (Fase 1)

## Sumário
Este inventário lista arquivos Markdown existentes, com caminho, título (H1) e observações de diagnóstico.

## Itens
- docs/LACTATION_MODULE.md — H1: "Módulo Lactation" — Tema: Módulo Lactação
- docs/ARCHITECTURE.md — H1: "Arquitetura do Sistema GoatFarm" — Tema: Arquitetura Geral
- docs/history/pr/feature-hex-architecture-refactor.md — H1: "Refactor: Hex Architecture – GoatFarm Aggregate Root" — Tema: Histórico/PR
- docs/history/estrutura-pacotes.md — H1: "Estrutura de Empacotamento e Distribuição de Pacotes (GoatFarm)" — Tema: Histórico/Estrutura
- docs/history/domain_modeling_analysis.md — H1: (TODO verificar no código) — Tema: Histórico/Modelagem de Domínio
- docs/history/arch_analysis.md — H1: (TODO verificar no código) — Tema: Histórico/Análise de Arquitetura
- docs/history/adr/ADR-001-atomic-goatfarm-registration.md — H1: (TODO verificar no código) — Tema: ADR
- docs/history/adr/ADR-001-atomic-goatfarm-registration-pt-br.md — H1: (TODO verificar no código) — Tema: ADR PT-BR
- docs/history/TEST_PLAN.md — H1: (TODO verificar no código) — Tema: Histórico/Plano de Testes
- docs/history/GENEALOGY_MODULE_ANALYSIS.md — H1: (TODO verificar no código) — Tema: Histórico/Genealogia
- docs/history/Estudo_Completo_GoatFarm.md — H1: (TODO verificar no código) — Tema: Histórico/Estudo
- docs/history/ENTIDADES_DIAGRAMA_CLASSES.md — H1: (TODO verificar no código) — Tema: Histórico/Diagrama
- docs/UX_FRONTEND_GUIDE.md — H1: "UX Frontend Guide - GoatFarm/CapriGestor" — Tema: Frontend/UX
- docs/UX_API_EXAMPLES.md — H1: "UX API Examples - GoatFarm/CapriGestor" — Tema: Frontend/Exemplos API
- docs/TRAE_FRONTEND_MILK_REPRO_GUIDE.md — H1: "Guia Frontend (TRAE) - Producao de Leite, Lactacao e Reproducao" — Tema: Frontend/Guia
- docs/REPRODUCTION_MODULE.md — H1: "Módulo de Reprodução (Reproduction)" — Tema: Módulo Reprodução
- docs/PLANO_GESTAO_NEGOCIO_RESUMO.md — H1: (TODO verificar no código) — Tema: Plano de Negócio/Resumo
- docs/PLANO_GESTAO_NEGOCIO.md — H1: (TODO verificar no código) — Tema: Plano de Negócio
- docs/MILK_PRODUCTION_MODULE.md — H1: "Módulo Milk Production" — Tema: Módulo Produção de Leite
- docs/HEALTH_VETERINARY_MODULE.md — H1: "Plano do Módulo C — Saúde/Veterinário" — Tema: Módulo Saúde
- docs/FRONTEND_TODO.md — H1: "Frontend TODO - GoatFarm/CapriGestor" — Tema: Frontend/Tarefas
- docs/BUSINESS_DOMAIN.md — H1: "Domínio de Negócio - GoatFarm" — Tema: Visão de Domínio
- docs/AUDIT_HEALTH_EXCEPTION_HANDLING.md — H1: "Auditoria de Tratamento de Exceções - Módulo de Saúde (Health)" — Tema: Auditoria
- docs/ARTICLE_BLOG_MODULE.md — H1: "Módulo Article/Blog" — Tema: Módulo Artigos/Blog
- README.md — H1: "CapriGestor – Backend" — Tema: Root README
- AUDIT_APPLICATION_CORE_USAGE.md — H1: "AUDIT_APPLICATION_CORE_USAGE.md" — Tema: Auditoria
- HELP.md — H1: "Getting Started" — Tema: Auxílio genérico (Spring Initializr)

## Diagnóstico
- Duplicados/Dispersos: diversos guias de frontend (UX_*, TRAE_*) sobrepostos a docs de backend e devem ser arquivados
- Histórico: pasta `docs/history` contém material antigo; mover para `_archive`
- Módulos: boas bases para fonte oficial (Reprodução, Lactação, Milk, Saúde, Artigos/Blog)
- Arquitetura: documento oficial deve residir em `01-architecture`
- Root: `HELP.md` e `AUDIT_APPLICATION_CORE_USAGE.md` fora de `/docs`

## Proposta de Estrutura Final
- docs/
  - INDEX.md
  - 00-overview/
    - DOMAIN_GLOSSARY.md
    - BUSINESS_DOMAIN.md
  - 01-architecture/
    - ARCHITECTURE.md
    - ADR/
  - 02-modules/
    - REPRODUCTION_MODULE.md
    - LACTATION_MODULE.md
    - MILK_PRODUCTION_MODULE.md
    - HEALTH_VETERINARY_MODULE.md
    - ARTICLE_BLOG_MODULE.md
  - 03-api/
    - API_CONTRACTS.md
  - 04-operations/
  - 05-testing/
  - _archive/YYYY-MM/
