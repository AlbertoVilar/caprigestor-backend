# MVP_READY
Última atualização: 2026-02-26
Escopo: prontidão do backend para uso em MVP com API versionada e suíte mínima de validação.

Links relacionados: [Status do Projeto](./PROJECT_STATUS.md), [Roadmap](./ROADMAP.md), [Contratos API](../03-api/API_CONTRACTS.md), [Guia de Migração de Versionamento](../03-api/API_VERSIONING_MIGRATION_GUIDE.md)

## O que é o MVP
O MVP do backend CapriGestor é o conjunto mínimo estável para operação diária da fazenda com segurança, rastreabilidade e alertas farm-level, sem depender de ajustes manuais fora da API.

## Módulos incluídos
- Security/Ownership (JWT + autorização por fazenda).
- Goat/Farm (cadastros base).
- Reproduction (fluxo reprodutivo + alertas de diagnóstico).
- Lactation/MilkProduction (produção, resumo e alertas de secagem).
- Health (eventos sanitários, calendário e alertas).
- Inventory (ledger de movimentações com idempotência).

## Como rodar
1. Pré-requisitos:
- Java 21+.
- Maven Wrapper do projeto (`mvnw` / `mvnw.cmd`).

2. Subir backend local:
```bash
./mvnw clean spring-boot:run
```

3. Executar suite completa:
```bash
./mvnw -U -T 1C clean test
```

4. Banco/migrações:
- Flyway habilitado no ciclo da aplicação e nos testes de integração.
- Novas migrações devem seguir padrão `Vxx__descricao.sql`.

## Smoke tests
Suite mínima de fumaça para `/api/v1`:
```bash
./mvnw -Dtest=ApiV1SmokeTest test
```

## Checklist do Frontend
- Usar `baseURL` canônica em `/api/v1`.
- Atualizar rotas conforme `docs/03-api/API_VERSIONING_MIGRATION_GUIDE.md`.
- Validar tratamento de erros HTTP `400`, `404`, `409` e `422`.
- Garantir envio de `Idempotency-Key` nos comandos do Inventory.
- Validar paginação padrão (`page`, `size`, `sort`) nos endpoints listáveis.

## Compatibilidade e depreciação
- Caminho oficial: `/api/v1/...`.
- Caminho legado: `/api/...` em compatibilidade temporária, marcado como **deprecated**.
- Remoção planejada do legado: **2026-06-30** (versão alvo `v2.0.0`), após o frontend operar 100% em `/api/v1`.
