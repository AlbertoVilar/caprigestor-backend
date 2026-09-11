# Status do Projeto CapriGestor Backend

Ultima atualizacao: 2026-09-10
Escopo: estado funcional humano e versionado do backend no commit integrado de develop.
Links relacionados: [Portal](../INDEX.md), [MVP](./MVP_READY.md), [Roadmap](./ROADMAP.md), [Contratos API](../03-api/API_CONTRACTS.md), [Arquitetura](../01-architecture/ARCHITECTURE.md)

## Resumo executivo

O backend esta funcional e organizado como um monolito modular com arquitetura
hexagonal, PostgreSQL/Flyway, seguranca JWT farm-scoped e CI/CD com gates de
qualidade. O estado tecnico deve ser conferido no codigo, nas migrations, nos
testes, no `pom.xml`, nos workflows e nos manifestos Docker.

Nesta onda, `cabras.id` passou a ser propagado como GoatId técnico nos
consumidores de eventos, genealogia, reprodução, saúde, lactação/leite,
comercial e auditoria. O RG continua como identidade registral/snapshot e as
rotas RG permanecem compatíveis durante a transição. A integridade técnica é
fechada pela migration V41; a troca do `@Id` JPA e das rotas estruturais ainda é
trabalho futuro documentado em [GOAT_IDENTITY_DEPENDENT_MODULES_WAVE](../01-architecture/GOAT_IDENTITY_DEPENDENT_MODULES_WAVE.md).

Baseline desta atualizacao:

- branch: `develop`;
- commit: `651f25d6c9362d497ac25d5c6a492799a6aaa2bd`;
- `origin/main`: `b9aaa94c007e7865ec218816ce99e51b6a7864a5`;
- a arvore de trabalho estava limpa na coleta deste status;
- relatorios de teste existentes: 558 testes, 0 falhas, 0 erros e 1 ignorado.

## Modulos implementados

| Modulo | Estado | Responsabilidade principal |
|---|---|---|
| Authority | Implementado | JWT, usuarios, papeis, refresh session, operadores e reset de senha |
| Farm | Implementado | fazendas, ownership, permissoes, enderecos e telefones |
| Goat / Genealogy | Implementado | animais, genealogia, saida controlada e integracao ABCC |
| Events | Implementado | historico operacional por animal |
| Reproduction | Implementado | coberturas, prenhez, diagnostico, parto, desmame e alertas |
| Lactation / Milk | Implementado | lactacao, producao individual, producao consolidada e alertas |
| Health | Implementado | eventos sanitarios, carencia, calendario e alertas |
| Inventory | Implementado | itens, lotes, ledger, saldo e idempotencia |
| Commercial | Implementado | clientes, vendas e financeiro operacional minimo |
| Article | Implementado | artigos publicos e administracao editorial |
| Audit | Implementado | trilha de auditoria operacional |

## API e seguranca

- As rotas de aplicacao usam exclusivamente `/api/v1`.
- Consultas publicas sao deliberadas e limitadas a fazendas, animais,
  genealogia e consultas ABCC documentadas.
- `@CanManageFarm` permite ADMIN, proprietario da fazenda ou operador vinculado.
- `@FarmOwnerOnly` permite ADMIN ou proprietario da fazenda.
- `@AdminOnly` permite somente ADMIN.
- `GET /api/v1/goatfarms/{farmId}/goats/summary` é uma consulta pública de
  agregados e usa `@PublicEndpoint`; as mutações de animais continuam protegidas.

## Banco, testes e entrega

- Flyway possui migrations V1 a V41; V38 reforca referencias compostas por
  fazenda, V39 introduz GoatId, V40 cria sombras técnicas e V41 exige GoatId
  nos consumidores dependentes. Migrations publicadas não foram editadas.
- Desenvolvimento usa PostgreSQL; testes usam H2 e testes de integracao
  PostgreSQL quando Docker esta disponivel.
- O piso de cobertura efetivo do `pom.xml` e `0.7588` (75,88%).
- CI inclui testes, CodeQL, secret scan, dependency review, SBOM e Trivy.
- Compose local usa PostgreSQL 15; HML/producao devem registrar a versao
  efetiva no runbook do ambiente.

## Limites atuais e proximos passos

- `GoatEntity` ainda possui `num_registro` como `@Id` JPA transitório; não se
  deve trocar RG em produção nem inferir GoatId pelo formato da URL.
- Fallbacks por RG permanecem apenas para compatibilidade de fixtures e rotas
  legadas. A futura API estrutural deverá declarar GoatId explicitamente.
- A base de desenvolvimento continua descartável, mas não foi resetada; o
  reset pré-HML será uma operação separada, após a migração coerente.

Este documento representa onde o produto esta. O trabalho futuro deve ficar no
[ROADMAP](./ROADMAP.md). Nao use este arquivo para registrar hashes efemeros de
cada tarefa, detalhes de implementacao ou contexto exclusivo de um ambiente.
