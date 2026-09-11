# Roadmap do Projeto CapriGestor Backend

Ultima atualizacao: 2026-09-10
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

### 4. Debitos tecnicos priorizados — identidade e ciclo de vida do animal

#### TD-ANIMAL-01 — Correcao de cadastro com RG/TOD/TOE divergentes (ID5-A)

- **Prioridade:** alta; risco de integridade de identidade e de referencias
  entre modulos. **Implementado na branch
  `feat/goat-registration-rectification`; aguarda revisão e merge.**
- O `registrationNumber` representa o RG formado por `TOD + TOE` e hoje e a
  chave primaria de `cabras`. O formulario ativo permite editar o `TOE`, mas
  nao recalcula o RG durante a edicao; o backend tambem ignora um novo RG no
  `PUT`. Isso pode persistir um animal com RG diferente da composicao de seus
  identificadores auriculares.
- **Decisao:** o RG nao e alterado pelo `PUT` comum. A ABCC permanece
  opcional: animais sem registro ABCC continuam validos e podem ser cadastrados
  manualmente.
- **Entrega ID5-A:** a geracao/validacao canonica de `RG = TOD + TOE`, o
  bloqueio de mudança de identidade no `PUT` comum e o endpoint administrativo
  `PATCH .../registration` foram implementados. A retificação mantém o mesmo
  GoatId, não reescreve dependências históricas, registra evidência/motivo/ator
  em `goat_registration_history` e na auditoria, e rejeita RG duplicado.
- **Pendente:** frontend para consumir o fluxo, eventual API estrutural futura
  por GoatId e a operação separada de reset pré-HML.
- A consulta ABCC pode ser oferecida como verificacao ou pre-preenchimento,
  mas nao deve ser requisito para o cadastro manual de animais sem registro.

#### TD-ANIMAL-02 — Caso de uso de exclusao completa de animal

- **Prioridade:** alta; risco de perda de historico e violacao de integridade
  referencial.
- A exclusao atual e fisica e pode ser bloqueada quando o animal possui
  referencias em genealogia, eventos, reproducao, lactacao, producao de leite,
  saude, vendas ou auditoria. Algumas FKs fazem `CASCADE`/`SET NULL`, enquanto
  as FKs compostas de integridade entre fazendas podem rejeitar a remocao.
- **Intervencao futura:** especificar uma operacao administrativa com
  pre-visualizacao de dependencias e dois caminhos explicitos:
  1. exclusao fisica somente quando nao houver dependencias;
  2. invalidacao/arquivamento quando houver historico que precise ser
     preservado.
- Se existir requisito legal ou de negocio para apagar absolutamente tudo,
  devera ser desenhado um plano de purga transacional, com autorizacao forte,
  confirmacao explicita, politica para genealogia e historico, tratamento de
  todas as tabelas referenciadas, backup/rollback e testes PostgreSQL. Nenhum
  `CASCADE` amplo deve ser introduzido sem essa decisao.
- O fluxo deve registrar o motivo da exclusao/correcao, ser auditavel quando a
  politica permitir e atualizar contratos da API, frontend, testes e
  documentacao. A autorizacao inicial deve permanecer administrativa
  (`ADMIN`/`FARM_OWNER`).

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
