# Roadmap do Projeto GoatFarm/CapriGestor Backend
Última atualização: 2026-08-07
Escopo: próximos ciclos após fechamento técnico do MVP backend.

Links relacionados: [Status do Projeto](./PROJECT_STATUS.md), [Prontidão do MVP](./MVP_READY.md), [Contratos API](../03-api/API_CONTRACTS.md), [Guia de Versionamento](../03-api/API_VERSIONING_MIGRATION_GUIDE.md)

## 1. Estado atual
- MVP backend concluído nos módulos: Security/Ownership, Goat/Farm, Reproduction, Lactation/MilkProduction, Health e Inventory.
- Convenção de rotas padronizada exclusivamente em `/api/v1`.
- Migração do frontend concluída e compatibilidade não versionada removida em 2026-08-07.

## 2. Próximos marcos (pós-MVP)
### Marco 1 - Hardening operacional
- Remover TODOs críticos e lacunas do fluxo assíncrono de eventos.
- Consolidar monitoramento de erros funcionais e técnicos.
- Reduzir warnings recorrentes de tooling em testes (Mockito/ByteBuddy).

### Marco 2 - Evolução de domínio (após estabilização)
- Compras e vendas com integração de estoque.
- Consolidação financeira mínima por fazenda.
- Painéis farm-level com agregação no backend, sem lógica pesada no frontend.

## 3. Critérios de prioridade
- Impacto direto na operação da fazenda.
- Redução de risco de regressão e custo de manutenção.
- Preservação da arquitetura hexagonal e limites entre módulos.
- Contratos de API sempre atualizados junto com o código.

## 4. Regras de execução
- Fluxo obrigatório de Git: `feature/* -> develop -> main` via PR.
- Sem push direto em branches protegidas.
- Gate obrigatório antes de merge: `./mvnw -U -T 1C clean test`.
- Não criar arquivos Markdown na raiz (exceto `README.md`).
