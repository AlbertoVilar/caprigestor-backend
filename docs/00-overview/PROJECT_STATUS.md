# Status do Projeto CapriGestor

Última atualização: 2026-10-07
Escopo: resumo canônico do estado integrado do CapriGestor. Código, migrations,
testes, configuração, workflows e os repositórios são a fonte técnica primária.
Este documento registra o estado integrado atual e não substitui os gates de CI
ou a revisão de promoção.

Links: [Portal](../INDEX.md), [Arquitetura](../01-architecture/ARCHITECTURE.md),
[Quality Gates](../01-architecture/QUALITY_GATES.md),
[Contratos API](../03-api/API_CONTRACTS.md).

## Estado da release candidate

- Backend: o baseline funcional atual de `develop` antes desta atualização
  documental é `6d176f3e0577cb31ba880b2a21712b8645b1c39b`; backend `main` está em
  `7bbdd48cc6d745e1573905b724b1631b16f98e86`. Nesse baseline, `develop`
  está 5 commits à frente, sem commits exclusivos na `main`.
- Frontend `develop`: `f157405b7d8a26b914623adc6a6dd240601ba3ae`; frontend
  `main`: `6ceae1f9bb0dc3bdbaf7600bf10d0a2a2292dcd6`. `develop` está 6 commits
  à frente, sem commits exclusivos na `main`.
- A promoção anterior de `develop` para `main` foi concluída e validada pelas
  PRs backend #341 e frontend #206. A promoção agora em avaliação é incremental
  e contém somente mudanças integradas depois daquela release.
- Os workflows pós-merge mais recentes de `develop` passaram nos SHAs acima.
  Os checks obrigatórios no contexto das próximas PRs para `main`, incluindo as
  revisões de dependências, ainda precisam ser executados e aprovados.
- As PRs coordenadas `develop → main` ainda não foram abertas. A promoção e a
  revisão dos respectivos diffs continuam pendentes. **Este estado não declara
  HML nem produção prontas.**
- A cadeia Flyway integrada vai de V1 a V56. Migrations publicadas não devem ser
  reescritas ou condensadas.

## Arquitetura e módulos

O backend é um monólito modular Java/Spring Boot com PostgreSQL e Flyway. A
direção arquitetural é hexagonal pragmática: API/adapters de entrada chamam
casos de uso e o núcleo dos módulos; persistência, segurança, mensageria e
integrações ficam atrás de boundaries/ports quando já isolados. Ainda há
módulos legados com acoplamentos a reduzir; não se declara isolamento acadêmico
ou conclusão de toda refatoração arquitetural.

Os principais módulos de domínio incluem Authority/Security, Farm, Goat,
Goat Ownership, Genealogy, Reproduction, Lactation/Milk, Health, Events,
Commercial/Finance, Inventory, Article/Blog e Audit.

A identidade estrutural do animal é o `GoatId` técnico. RG/TOD/TOE continuam
identificadores e dados de negócio/registral usados em integrações, consultas e
snapshots; não são a identidade relacional estrutural. As FKs locais críticas
apontam para GoatId.

## Funcionalidades integradas

- **Propriedade e histórico:** períodos de propriedade canônicos, histórico de
  movimentações e transferências internas; fatos históricos mantêm seu contexto
  de fazenda sem redefinir retroativamente a propriedade atual do animal.
- **Comercial:** venda interna integrada ao fluxo de propriedade, conclusão
  orientada pelo pagamento quando aplicável, venda externa e reversão auditável.
- **ABCC e genealogia:** consulta/lookup e pré-visualização são separados da
  confirmação/importação; existe importação em lote com resultado por item.
  Genealogia suporta referências locais e externas, com limites/timeout e
  tratamento explícito de falhas da integração.
- **Eventos e saúde:** Health possui fluxo e persistência próprios, separados do
  módulo genérico Events. Fatos especializados de saúde/reprodução não devem ser
  gravados ou removidos pelo CRUD genérico; registros legados especializados
  permanecem consultáveis, mas sem ações genéricas de edição/exclusão.
  `PESAGEM` e `OUTRO` continuam como tipos genéricos.
- **Frontend:** inclui seletor de fazenda gerenciada, fluxo de importação ABCC e
  cartão de histórico de animais vendidos, compatíveis com os contratos atuais
  do backend.
- **Segurança:** JWT é stateless; a autorização farm-scoped separa políticas
  administrativas de operação. `ADMIN` é global, `FARM_OWNER` administra a
  própria fazenda e `OPERATOR` depende de vínculo persistido para capacidades
  operacionais. Leituras públicas de catálogo/genealogia/consultas ABCC são
  exceções explícitas documentadas; mutações continuam protegidas pelas políticas
  apropriadas.
- **Duração JWT:** fallback global e perfil de testes usam 900 segundos; o
  perfil `dev` usa 86400 segundos para desenvolvimento e QA manual. O perfil de
  produção exige `JWT_DURATION` explícito; a recomendação operacional é 900
  segundos. A duração ampliada de desenvolvimento não deve ser reutilizada em
  HML ou produção.
- **Supply chain:** baseline integrada em `develop`, com SBOM e análise da imagem
  nos workflows. A exceção temporária do Trivy para `CVE-2026-47884` expira em
  `2026-12-31`; ela é uma exceção de política com prazo, não evidência de que a
  vulnerabilidade foi corrigida. Deve ser removida quando houver correção
  suportada ou quando as premissas de não explorabilidade deixarem de valer. Se
  ainda for necessária no vencimento, qualquer renovação exige nova revisão e
  aceitação explícita de risco; não deve ser prorrogada automaticamente.

## Banco, migrations e validação

- O modelo atual usa PostgreSQL e migrations Flyway imutáveis até V56.
- As migrations V39–V43 introduziram e propagaram GoatId estrutural. V45–V56
  estabelecem propriedade/histórico canônicos e evoluem integridade e fluxos
  comerciais.
- A cobertura de integração PostgreSQL/Testcontainers inclui instalação limpa
  no schema atual, upgrades representativos (incluindo V43 até latest) e
  invariantes de ownership. Os checks pós-merge atuais de backend e frontend em
  `develop` concluíram com sucesso; os gates específicos das PRs de promoção
  para `main` ainda não foram executados.
- Os testes backend rodam por `./mvnw -B -U clean verify`; o CI de frontend
  executa lint, typecheck, testes com cobertura, build e Playwright E2E.

## CI, infraestrutura e operação

Nos SHAs atuais de `develop`, os gates pós-merge do backend estão verdes:
Clean test, CodeQL Java analysis, Secret scan, deny_root_markdown, Maven SBOM e
Container image scan. No frontend, os workflows pós-merge observados de
Frontend Quality e Secret Scan estão verdes. As duas branches `main` usam checks
obrigatórios e estritos. Na promoção, o frontend precisa passar novamente por
Lint + Unit + Build, E2E Playwright, Gitleaks e Review dependencies; no backend,
Dependency review e os demais checks obrigatórios também precisam passar no
contexto da PR.

O backend oferece imagem Docker; o compose de produção mantém o backend na rede
interna, usa secrets externos para chaves JWT e deixa a publicação web ao
frontend/proxy. Os arquivos `.env.prod.example`, compose e runbooks são
**templates**: imagens, banco, CORS, hostname, SMTP e secrets precisam de valores
específicos e validação do ambiente. Não devem ser tratados como configuração
pronta para deploy.

## Pendências e limites conhecidos

- Concluir a revisão dos diffs e os checks obrigatórios nas PRs coordenadas
  `develop → main` precede qualquer merge. Os checks devem passar nos heads
  exatos das PRs, com as bases `main` atualizadas.
- A auditoria R1 não confirmou novos defeitos funcionais bloqueantes. Timeline
  unificada somente leitura, eventual código legado não utilizado no frontend,
  limpeza histórica de duplicidades e refatorações sem bug reproduzido ficam
  como investigação/trabalho futuro, não como bloqueadores atuais de `main`.
- O hardening concorrente do fluxo de recuperação de senha permanece uma dívida
  de segurança documentada separadamente; não foi reaberto nem validado por R1.
- A instalação limpa e os arquivos de exemplo não comprovam prontidão de HML ou
  produção. Ambas as declarações dependem de infraestrutura e configuração
  próprias de ambiente.

## Estado do checkout canônico

Na reconciliação concluída em 2026-10-07, o checkout raiz do backend ficou limpo
e sincronizado com `origin/develop@6d176f3e0577cb31ba880b2a21712b8645b1c39b`.
As configurações e documentação locais antigas que aplicavam 24 horas
globalmente foram substituídas pela política por ambiente integrada em
`develop`.

## Continuidade

Consulte [ARCHITECTURE.md](../01-architecture/ARCHITECTURE.md) e
[QUALITY_GATES.md](../01-architecture/QUALITY_GATES.md) para boundaries e gates;
documentos de módulo/API descrevem contratos funcionais. Roadmaps e planos
históricos explicam decisões anteriores, mas não substituem o estado observado
nos repositórios e nos workflows.
