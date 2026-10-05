# Status do Projeto CapriGestor

Última atualização: 2026-10-05
Escopo: resumo canônico do estado integrado do CapriGestor. Código, migrations,
testes, configuração, workflows e os repositórios são a fonte técnica primária.
Este documento registra o estado observado na auditoria R1; não substitui os
gates de CI ou a revisão de promoção.

Links: [Portal](../INDEX.md), [Arquitetura](../01-architecture/ARCHITECTURE.md),
[Quality Gates](../01-architecture/QUALITY_GATES.md),
[Contratos API](../03-api/API_CONTRACTS.md).

## Estado da release candidate

- Backend `develop`: `1abf127b10547f2180c76db45a394d0e371d2fd3`.
- Frontend `develop`: `72816f7e8a2299c6820c3e25c86fdaee9a3e4e6c`.
- Backend `main`: `49a3ee26641dfc607ff392533e991ae5d795c8f9`, 220 commits atrás de
  `develop`; frontend `main`: `3c2294a4f5ab48a3aa29cc9c60029ee87c78ebe7`,
  71 commits atrás. A promoção ainda não ocorreu.
- Na auditoria R1 não havia PRs abertas nos dois repositórios.
- Os checks de push dos SHAs atuais de `develop` estavam verdes. As revisões de
  dependências são executadas no contexto de PR e ainda precisam ser observadas
  nas PRs de promoção.
- A validação completa da release candidate e a promoção por PR para `main`
  continuam pendentes. **Este estado não declara HML nem produção prontas.**
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

## Banco, migrations e validação

- O modelo atual usa PostgreSQL e migrations Flyway imutáveis até V56.
- As migrations V39–V43 introduziram e propagaram GoatId estrutural. V45–V56
  estabelecem propriedade/histórico canônicos e evoluem integridade e fluxos
  comerciais.
- A cobertura de integração PostgreSQL/Testcontainers inclui instalação limpa
  no schema atual, upgrades representativos (incluindo V43 até latest) e
  invariantes de ownership. Os workflows de `develop` nos SHAs acima concluíram
  com sucesso no R1; a execução/teste-smoke consolidada de RC ainda é pendente.
- Os testes backend rodam por `./mvnw -B -U clean verify`; o CI de frontend
  executa lint, typecheck, testes com cobertura, build e Playwright E2E.

## CI, infraestrutura e operação

No R1, os gates de push do backend em `develop` estavam verdes: Clean test,
CodeQL, Secret scan, deny_root_markdown, Maven SBOM e Container image scan. No
frontend: Lint + Unit + Build, E2E Playwright e Gitleaks. Dependency review do
backend e Review dependencies do frontend exigem contexto de PR.

O backend oferece imagem Docker; o compose de produção mantém o backend na rede
interna, usa secrets externos para chaves JWT e deixa a publicação web ao
frontend/proxy. Os arquivos `.env.prod.example`, compose e runbooks são
**templates**: imagens, banco, CORS, hostname, SMTP e secrets precisam de valores
específicos e validação do ambiente. Não devem ser tratados como configuração
pronta para deploy.

## Pendências e limites conhecidos

- Executar o gate consolidado de release candidate precede a promoção para
  `main`.
- A promoção deve ocorrer por PRs `develop → main`, com diff de promoção
  revisado e todos os checks obrigatórios, inclusive revisões de dependências,
  concluídos nos heads exatos.
- A auditoria R1 não confirmou novos defeitos funcionais bloqueantes. Timeline
  unificada somente leitura, eventual código legado não utilizado no frontend,
  limpeza histórica de duplicidades e refatorações sem bug reproduzido ficam
  como investigação/trabalho futuro, não como bloqueadores atuais de `main`.
- O hardening concorrente do fluxo de recuperação de senha permanece uma dívida
  de segurança documentada separadamente; não foi reaberto nem validado por R1.
- A instalação limpa e os arquivos de exemplo não comprovam prontidão de HML ou
  produção. Ambas as declarações dependem de infraestrutura e configuração
  próprias de ambiente.

## Nota de escopo local observada no R1

Quatro alterações não commitadas no checkout canônico do backend foram
classificadas como locais e **fora da release candidate**: a alteração de
duração do JWT de acesso de 900 para 86400 segundos e seus arquivos
complementares de teste e documentação (`JwtService.java`,
`application.properties`, `AuthControllerIntegrationTest.java` e
`AUTHORITY_ACCESS_MODULE.md`). Elas não integram os SHAs remotos de `develop`;
não foram incorporadas nem descartadas nesta wave. A duração de 24 horas não é
configuração aprovada para a release.

## Continuidade

Consulte [ARCHITECTURE.md](../01-architecture/ARCHITECTURE.md) e
[QUALITY_GATES.md](../01-architecture/QUALITY_GATES.md) para boundaries e gates;
documentos de módulo/API descrevem contratos funcionais. Roadmaps e planos
históricos explicam decisões anteriores, mas não substituem o estado observado
nos repositórios e nos workflows.
