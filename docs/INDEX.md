# Portal de Documentacao CapriGestor Backend

Ultima atualizacao: 2026-09-10
Escopo: porta unica de navegacao para a documentacao oficial versionada do backend.
Links relacionados: [README](../README.md), [Status atual](./00-overview/PROJECT_STATUS.md), [Arquitetura](./01-architecture/ARCHITECTURE.md)

## Como usar este portal

O codigo, as migrations, os testes, a configuracao e o CI sao a fonte tecnica
primaria. Este portal aponta para a documentacao humana que explica o produto,
o dominio, os contratos e a operacao. O arquivo local
`00-overview/CAPRIGESTOR_CURRENT_STATE.md`, quando existir, e apenas contexto
auxiliar e nao e necessario para um clone limpo.

## Trilha recomendada para um novo desenvolvedor

1. [README do backend](../README.md)
2. [Status do projeto](./00-overview/PROJECT_STATUS.md)
3. [Dominio de negocio](./00-overview/BUSINESS_DOMAIN.md)
4. [Arquitetura](./01-architecture/ARCHITECTURE.md)
5. [Acesso e autorizacao](./02-modules/AUTHORITY_ACCESS_MODULE.md)
6. [Modulo em que vai trabalhar](./02-modules)
7. [Contratos de API](./03-api/API_CONTRACTS.md)
8. [ADRs relevantes](./01-architecture/ADR)

## Getting started e estado funcional

- [PROJECT_STATUS.md](./00-overview/PROJECT_STATUS.md) - onde estamos agora.
- [MVP_READY.md](./00-overview/MVP_READY.md) - criterios de prontidao do MVP.
- [ROADMAP.md](./00-overview/ROADMAP.md) - somente trabalho futuro.
- [AGENT_CONTEXT.md](./00-overview/AGENT_CONTEXT.md) - contexto operacional versionado.
- [ONBOARDING_GUIDE_PT-BR.md](./00-overview/ONBOARDING_GUIDE_PT-BR.md)
- [ONBOARDING_GUIDE_EN.md](./00-overview/ONBOARDING_GUIDE_EN.md)
- [USER_MANUAL_PT-BR.md](./00-overview/USER_MANUAL_PT-BR.md)
- [USER_MANUAL_EN.md](./00-overview/USER_MANUAL_EN.md)
- [HOMOLOGATION_OPERATION_RUNBOOK.md](./00-overview/HOMOLOGATION_OPERATION_RUNBOOK.md)
- [PILOT_FREEZE_PLAYBOOK.md](./00-overview/PILOT_FREEZE_PLAYBOOK.md)

## Domain

- [BUSINESS_DOMAIN.md](./00-overview/BUSINESS_DOMAIN.md)
- [DOMAIN_GLOSSARY.md](./00-overview/DOMAIN_GLOSSARY.md)

## Architecture

- [ARCHITECTURE.md](./01-architecture/ARCHITECTURE.md)
- [README.md](./01-architecture/README.md) - mapa da arquitetura e seus gates.
- [ABCC_INTEGRATION.md](./01-architecture/ABCC_INTEGRATION.md)
- [MODULE_STANDARD_MARKET_GRADE.md](./01-architecture/MODULE_STANDARD_MARKET_GRADE.md)
- [OBSERVABILITY_LOGGING.md](./01-architecture/OBSERVABILITY_LOGGING.md)
- [QUALITY_GATES.md](./01-architecture/QUALITY_GATES.md)
- [ADR-001 - Atomic GoatFarm registration](./01-architecture/ADR/ADR-001-atomic-goatfarm-registration.md)
- [ADR-001 - Registro atomico (PT)](./01-architecture/ADR/ADR-001-atomic-goatfarm-registration-pt-br.md)
- [ADR-002 - Inventory ledger](./01-architecture/ADR/ADR-002-inventory-ledger-balance-and-lots.md)
- [ADR-003 - Purchase cost](./01-architecture/ADR/ADR-003-inventory-purchase-cost-breakdown.md)
- [ADR-004 - Goat identity model](./01-architecture/ADR/ADR-004-goat-identity-model.md)
- [GoatId — onda de consumidores dependentes](./01-architecture/GOAT_IDENTITY_DEPENDENT_MODULES_WAVE.md)

## Modules

- [GOAT_FARM_MODULE.md](./02-modules/GOAT_FARM_MODULE.md)
- [AUTHORITY_ACCESS_MODULE.md](./02-modules/AUTHORITY_ACCESS_MODULE.md)
- [REPRODUCTION_MODULE.md](./02-modules/REPRODUCTION_MODULE.md)
- [LACTATION_MODULE.md](./02-modules/LACTATION_MODULE.md)
- [MILK_PRODUCTION_MODULE.md](./02-modules/MILK_PRODUCTION_MODULE.md)
- [HEALTH_VETERINARY_MODULE.md](./02-modules/HEALTH_VETERINARY_MODULE.md)
- [INVENTORY_MODULE.md](./02-modules/INVENTORY_MODULE.md)
- [COMMERCIAL_MODULE.md](./02-modules/COMMERCIAL_MODULE.md)
- [ARTICLE_BLOG_MODULE.md](./02-modules/ARTICLE_BLOG_MODULE.md)

## API

- [API_CONTRACTS.md](./03-api/API_CONTRACTS.md)
- [API_VERSIONING_MIGRATION_GUIDE.md](./03-api/API_VERSIONING_MIGRATION_GUIDE.md)

## Security

- [AUTHORITY_ACCESS_MODULE.md](./02-modules/AUTHORITY_ACCESS_MODULE.md)
- [JWT_KEY_ROTATION_RUNBOOK.md](./04-security/JWT_KEY_ROTATION_RUNBOOK.md)
- [SECURITY_INCIDENT_RESPONSE.md](./04-security/SECURITY_INCIDENT_RESPONSE.md)
- [W0_SECURITY_EVIDENCE.md](./04-security/W0_SECURITY_EVIDENCE.md) - evidencia historica imutavel; nao e estado corrente.

## Operations

- [HOMOLOGATION_OPERATION_RUNBOOK.md](./00-overview/HOMOLOGATION_OPERATION_RUNBOOK.md)
- [PRODUCTION_BASE_SANITIZATION_RUNBOOK.md](./00-overview/PRODUCTION_BASE_SANITIZATION_RUNBOOK.md)
- [PRODUCTION_DATABASE_PROMOTION_CHECKLIST.md](./00-overview/PRODUCTION_DATABASE_PROMOTION_CHECKLIST.md)
- [PRODUCTION_DOCKER_DEPLOY_RUNBOOK.md](./00-overview/PRODUCTION_DOCKER_DEPLOY_RUNBOOK.md)
- [CROSS_FARM_INTEGRITY_PRECHECK.md](./00-overview/CROSS_FARM_INTEGRITY_PRECHECK.md)

## Referencia historica

Historico nao deve orientar implementacao atual. Use [docs/_archive](./_archive)
somente para investigar decisoes anteriores. Auditorias encerradas ficam em
[docs/audits](./audits) e nao representam o estado corrente.

## Regras de manutencao documental

- Nao criar Markdown na raiz, exceto `README.md`.
- Cada documento ativo deve ter uma responsabilidade unica.
- Atualize o documento oficial correspondente junto com uma mudanca relevante.
- Nao copie o estado local ignorado para o frontend ou para outra pasta.
- Artefatos de trabalho nao entram neste portal.
