# Glossario de Dominio
Ultima atualizacao: 2026-02-10
Escopo: termos de negocio usados nos modulos oficiais e contratos de API.
Links relacionados: [Portal](../INDEX.md), [Dominio de negocio](./BUSINESS_DOMAIN.md), [Arquitetura](../01-architecture/ARCHITECTURE.md), [API_CONTRACTS](../03-api/API_CONTRACTS.md)

Atualizado em 2026-09-04 para padronizar os termos de referências genealógicas externas.

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
- `Categoria genealógica`: classificação do animal que define as exigências de
  referências de pai e mãe: `PA` (Pura por Avaliação), `PC` (Pura por Cruza) e
  `PO` (Pura de Origem).
- `Genitor local`: `Goat` já cadastrado no CapriGestor e referenciado por FK;
  pode pertencer a outra fazenda sem que isso altere ownership.
- `Genitor externo`: pai ou mãe representado somente pelo RG, sem criar um
  animal patrimonial no rebanho local.
- `DECLARADO`: origem exibida na genealogia quando um RG externo de genitor foi
  informado para `PA`, mas não foi localizado pela consulta ABCC.
- `ABCC`: origem exibida quando a consulta pública confirmou o RG de um nó
  externo. A informação serve à genealogia e não incorpora automaticamente o
  animal ao rebanho.

## Regras / Contratos
- Termos de status devem seguir enums da implementacao.
- Contratos de endpoint devem usar nomenclatura consistente com DTOs oficiais.
- Termos de negocio nao devem depender de caminho local ou ambiente especifico.

## Referencias internas
- Detalhamento de entidades e regras: [BUSINESS_DOMAIN.md](./BUSINESS_DOMAIN.md)
- Contratos e erros de API: [API_CONTRACTS.md](../03-api/API_CONTRACTS.md)
- Modulos funcionais: [../02-modules](../02-modules)
