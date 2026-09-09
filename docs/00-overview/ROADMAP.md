# Roadmap do Projeto CapriGestor Backend

Ultima atualizacao: 2026-09-09
Escopo: trabalho futuro ainda nao implementado; nao e um espelho do estado atual.
Links relacionados: [Status](./PROJECT_STATUS.md), [MVP](./MVP_READY.md), [Contratos API](../03-api/API_CONTRACTS.md), [Arquitetura](../01-architecture/ARCHITECTURE.md), [Portal](../INDEX.md)

## Estado de partida

O backend ja possui Authority, Farm, Goat/Genealogy, Reproduction,
Lactation/Milk, Health, Inventory, Commercial, Article e Audit. Esses modulos
nao devem ser listados como trabalho futuro. A V38 de integridade referencial,
os gates de CI e as rotas `/api/v1` fazem parte do estado atual.

## Proximos ciclos

### 1. Homologacao e operacao controlada

- executar smoke de restore, Flyway e fluxos criticos no ambiente HML;
- validar secrets, pares JWT, CORS, SMTP e conexoes por ambiente;
- medir observabilidade, alertas e comportamento de rollback;
- registrar achados funcionais sem alterar contratos fora de uma intervencao propria.

### 2. Governanca tecnica

- manter contratos API sincronizados com controllers e testes;
- ampliar guards ArchUnit para reduzir acoplamento JPA entre contextos;
- automatizar verificacao de links, metadados e rotas documentadas;
- acompanhar o piso de cobertura sem reduzir o ratchet atual de 75,88%.

### 3. Evolucao de produto

- vitrine publica de animais disponiveis, separada de venda concluida;
- integracao comercial/estoque quando houver requisito fechado;
- dashboards farm-scoped com agregacoes no backend;
- evolucao da mensageria somente quando houver necessidade operacional comprovada.

## Criterios para priorizacao

- impacto direto na operacao da fazenda;
- risco de regressao e custo de manutencao;
- preservacao da arquitetura hexagonal e dos limites entre modulos;
- contratos de API atualizados junto com qualquer mudanca funcional;
- evidencias de homologacao antes de promover comportamento para producao.

## Regras de execucao

- fluxo Git: `feature/* -> develop -> main` via PR;
- sem push direto em branches protegidas;
- nenhum item entra neste roadmap depois de implementado: ele deve ser movido
  para o PROJECT_STATUS.
