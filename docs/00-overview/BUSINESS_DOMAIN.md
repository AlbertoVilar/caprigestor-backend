# Dominio de Negocio - GoatFarm
Ultima atualizacao: 2026-02-10
Escopo: entidades centrais, regras de negocio e requisitos nao funcionais do backend.
Links relacionados: [Portal](../INDEX.md), [Glossario](./DOMAIN_GLOSSARY.md), [Arquitetura](../01-architecture/ARCHITECTURE.md), [API_CONTRACTS](../03-api/API_CONTRACTS.md)

## Visao geral
O sistema modela operacao de caprinos por fazenda, com foco em ownership, rastreabilidade de eventos e contratos estaveis entre modulos.

## Regras / Contratos
### Entidades principais
- `GoatFarm`:
  - agregado raiz da fazenda.
  - possui identificador de negocio `TOD`.
  - relaciona dados de contato e animais.
- `Goat`:
  - identifica animal por `registrationNumber`.
  - mantem referencia de genealogia (`father`, `mother`).
  - pertence a uma fazenda.
- `User` / `Authority`:
  - controla autenticacao/autorizacao.
  - perfis operacionais: `ROLE_ADMIN`, `ROLE_OPERATOR`, `ROLE_FARM_OWNER`.
- `Lactation`:
  - ciclo produtivo por cabra (`ACTIVE`/`CLOSED`).
  - regra: no maximo uma lactacao ativa por cabra.
- `MilkProduction`:
  - registro diario por data/turno.
  - vinculado a cabra e ciclo de lactacao.
- `Event` (manejo, saude e reproducao):
  - registra fatos operacionais com historico audivel.

### Regras de seguranca e ownership
- Operacoes farm-level devem respeitar o vinculo usuario-fazenda.
- Um usuario nao pode manipular dados de fazenda de terceiros.

### Regras de consistencia
- Unicidade de registro do animal por identificador de negocio.
- Unicidade operacional em pontos sensiveis (ex.: producao por data+turno).
- Fronteiras entre modulos sao protegidas por portas e contratos de shared kernel.

## Fluxos principais
1. Cadastro e manutencao de rebanho:
   fazenda -> cabras -> eventos de ciclo de vida.
2. Ciclo produtivo:
   abertura de lactacao -> producoes diarias -> secagem/encerramento.
3. Ciclo reprodutivo:
   cobertura -> diagnostico -> confirmacao -> encerramento.
4. Operacao sanitaria:
   agendamento -> execucao -> consultas por cabra e por fazenda.

## Requisitos nao funcionais
- Performance:
  - consultas paginadas nas listagens.
  - uso de estrategias para evitar carga excessiva em agregacoes.
- Seguranca:
  - JWT stateless.
  - verificacao de ownership por `farmId`.
- Observabilidade:
  - logs tecnicos em fluxos criticos.
  - suporte a health checks quando habilitado.
- Testes:
  - foco em regra de negocio, contratos de API e gates de arquitetura.

## Referencias internas
- Glossario: [DOMAIN_GLOSSARY.md](./DOMAIN_GLOSSARY.md)
- Arquitetura: [ARCHITECTURE.md](../01-architecture/ARCHITECTURE.md)
- Contratos de API: [API_CONTRACTS.md](../03-api/API_CONTRACTS.md)
- Modulos: [../02-modules](../02-modules)
