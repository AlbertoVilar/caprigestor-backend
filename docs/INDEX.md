# Portal de Documentação CapriGestor Backend

Última atualização: 2026-09-12
Escopo: porta de navegação para a documentação oficial versionada do backend.

Links: [README](../README.md), [Status atual](./00-overview/PROJECT_STATUS.md),
[Arquitetura](./01-architecture/ARCHITECTURE.md).

## Como usar este portal

Código, migrations, testes, configuração e CI são a fonte técnica primária.
[PROJECT_STATUS.md](./00-overview/PROJECT_STATUS.md) é a única fonte humana
versionada de estado atual. Planos, roadmaps, auditorias e documentos locais
podem explicar contexto, mas não substituem o repositório atual.

## Trilha para novo desenvolvimento

1. [README do backend](../README.md)
2. [Status do projeto](./00-overview/PROJECT_STATUS.md)
3. [Domínio de negócio](./00-overview/BUSINESS_DOMAIN.md)
4. [Arquitetura](./01-architecture/ARCHITECTURE.md)
5. [Quality Gates](./01-architecture/QUALITY_GATES.md)
6. [Módulo em que vai trabalhar](./02-modules)
7. [Contratos de API](./03-api/API_CONTRACTS.md)
8. [ADRs relevantes](./01-architecture/ADR)

## Estado e contexto

- [PROJECT_STATUS.md](./00-overview/PROJECT_STATUS.md) — estado atual e waves.
- [AGENT_CONTEXT.md](./00-overview/AGENT_CONTEXT.md) — contexto operacional.
- [MVP_READY.md](./00-overview/MVP_READY.md) — critérios do MVP.
- [ROADMAP.md](./00-overview/ROADMAP.md) — planejamento futuro, não estado atual.
- [ONBOARDING_GUIDE_PT-BR.md](./00-overview/ONBOARDING_GUIDE_PT-BR.md)
- [ONBOARDING_GUIDE_EN.md](./00-overview/ONBOARDING_GUIDE_EN.md)
- [USER_MANUAL_PT-BR.md](./00-overview/USER_MANUAL_PT-BR.md)
- [USER_MANUAL_EN.md](./00-overview/USER_MANUAL_EN.md)
- [HOMOLOGATION_OPERATION_RUNBOOK.md](./00-overview/HOMOLOGATION_OPERATION_RUNBOOK.md)
- [PILOT_FREEZE_PLAYBOOK.md](./00-overview/PILOT_FREEZE_PLAYBOOK.md)

## Domínio

- [BUSINESS_DOMAIN.md](./00-overview/BUSINESS_DOMAIN.md)
- [DOMAIN_GLOSSARY.md](./00-overview/DOMAIN_GLOSSARY.md)

## Arquitetura e procedures

- [ARCHITECTURE.md](./01-architecture/ARCHITECTURE.md)
- [QUALITY_GATES.md](./01-architecture/QUALITY_GATES.md)
- [README.md](./01-architecture/README.md) — mapa histórico de arquitetura.
- [ABCC_INTEGRATION.md](./01-architecture/ABCC_INTEGRATION.md)
- [MODULE_STANDARD_MARKET_GRADE.md](./01-architecture/MODULE_STANDARD_MARKET_GRADE.md)
- [OBSERVABILITY_LOGGING.md](./01-architecture/OBSERVABILITY_LOGGING.md)
- [Procedimento de arquitetura](../.agents/skills/caprigestor-architecture/SKILL.md)
- [Procedimento de refatoração segura](../.agents/skills/caprigestor-safe-refactor/SKILL.md)
- [Procedimento de auditoria](../.agents/skills/caprigestor-audit/SKILL.md)
- [Procedimento de documentação](../.agents/skills/caprigestor-documentation/SKILL.md)
- [ADR-001 — Atomic GoatFarm registration](./01-architecture/ADR/ADR-001-atomic-goatfarm-registration.md)
- [ADR-001 — Registro atômico (PT)](./01-architecture/ADR/ADR-001-atomic-goatfarm-registration-pt-br.md)
- [ADR-002 — Inventory ledger](./01-architecture/ADR/ADR-002-inventory-ledger-balance-and-lots.md)
- [ADR-003 — Purchase cost](./01-architecture/ADR/ADR-003-inventory-purchase-cost-breakdown.md)
- [ADR-004 — Goat identity model](./01-architecture/ADR/ADR-004-goat-identity-model.md)
- [GoatId — onda de consumidores dependentes (historical)](./01-architecture/GOAT_IDENTITY_DEPENDENT_MODULES_WAVE.md)

## Módulos

- [GOAT_FARM_MODULE.md](./02-modules/GOAT_FARM_MODULE.md)
- [AUTHORITY_ACCESS_MODULE.md](./02-modules/AUTHORITY_ACCESS_MODULE.md)
- [REPRODUCTION_MODULE.md](./02-modules/REPRODUCTION_MODULE.md)
- [LACTATION_MODULE.md](./02-modules/LACTATION_MODULE.md)
- [MILK_PRODUCTION_MODULE.md](./02-modules/MILK_PRODUCTION_MODULE.md)
- [HEALTH_VETERINARY_MODULE.md](./02-modules/HEALTH_VETERINARY_MODULE.md)
- [INVENTORY_MODULE.md](./02-modules/INVENTORY_MODULE.md)
- [COMMERCIAL_MODULE.md](./02-modules/COMMERCIAL_MODULE.md)
- [ARTICLE_BLOG_MODULE.md](./02-modules/ARTICLE_BLOG_MODULE.md)

## API e segurança

- [API_CONTRACTS.md](./03-api/API_CONTRACTS.md)
- [API_VERSIONING_MIGRATION_GUIDE.md](./03-api/API_VERSIONING_MIGRATION_GUIDE.md)
- [JWT_KEY_ROTATION_RUNBOOK.md](./04-security/JWT_KEY_ROTATION_RUNBOOK.md)
- [SECURITY_INCIDENT_RESPONSE.md](./04-security/SECURITY_INCIDENT_RESPONSE.md)
- [W0_SECURITY_EVIDENCE.md](./04-security/W0_SECURITY_EVIDENCE.md) — evidência
  histórica; não é estado corrente.

## Operação

- [HOMOLOGATION_OPERATION_RUNBOOK.md](./00-overview/HOMOLOGATION_OPERATION_RUNBOOK.md)
- [PRODUCTION_BASE_SANITIZATION_RUNBOOK.md](./00-overview/PRODUCTION_BASE_SANITIZATION_RUNBOOK.md)
- [PRODUCTION_DATABASE_PROMOTION_CHECKLIST.md](./00-overview/PRODUCTION_DATABASE_PROMOTION_CHECKLIST.md)
- [PRODUCTION_DOCKER_DEPLOY_RUNBOOK.md](./00-overview/PRODUCTION_DOCKER_DEPLOY_RUNBOOK.md)
- [CROSS_FARM_INTEGRITY_PRECHECK.md](./00-overview/CROSS_FARM_INTEGRITY_PRECHECK.md)

## Histórico e manutenção

Use [docs/_archive](./_archive) e [docs/audits](./audits) para rastreabilidade,
não para orientar implementação atual. Preserve ADRs, planos e auditorias;
classifique-os quando necessário em vez de reescrever decisões históricas.

- Não crie Markdown na raiz, exceto `README.md`.
- Cada documento ativo tem uma responsabilidade única.
- Atualize o documento ativo correspondente quando fatos relevantes mudarem.
- Atualize este portal somente quando a navegação oficial mudar.
