# Glossario de Dominio
Ultima atualizacao: 2026-02-10
Escopo: termos de negocio usados nos modulos oficiais e contratos de API.
Links relacionados: [Portal](../INDEX.md), [Dominio de negocio](./BUSINESS_DOMAIN.md), [Arquitetura](../01-architecture/ARCHITECTURE.md), [API_CONTRACTS](../03-api/API_CONTRACTS.md)

## Visao geral
Este glossario padroniza termos para reduzir ambiguidade entre backend, testes e documentacao operacional.

## Termos principais
- `GoatFarm`: agregado raiz da fazenda.
- `Goat`: animal individual (cabra/bode).
- `Ownership`: regra de autorizacao vinculada ao `farmId`.
- `Lactation`: ciclo de producao (`ACTIVE` ou `CLOSED`).
- `MilkProduction`: registro diario por data e turno.
- `Pregnancy`: estado de gestacao no modulo de reproducao.
- `HealthEvent`: evento sanitario (agendado, realizado ou cancelado).
- `Shared Kernel`: contrato estavel entre contextos (ex.: `PregnancySnapshot`).

## Regras / Contratos
- Termos de status devem seguir enums da implementacao.
- Contratos de endpoint devem usar nomenclatura consistente com DTOs oficiais.
- Termos de negocio nao devem depender de caminho local ou ambiente especifico.

## Referencias internas
- Detalhamento de entidades e regras: [BUSINESS_DOMAIN.md](./BUSINESS_DOMAIN.md)
- Contratos e erros de API: [API_CONTRACTS.md](../03-api/API_CONTRACTS.md)
- Modulos funcionais: [../02-modules](../02-modules)
