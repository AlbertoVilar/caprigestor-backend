# Qualidade, supply chain e observabilidade

Última atualização: 2026-09-13
Escopo: gates automatizados ativos e dívida explicitamente observada.

Links: [Portal](../INDEX.md), [Arquitetura](./ARCHITECTURE.md),
[Status](../00-overview/PROJECT_STATUS.md), [API](../03-api/API_CONTRACTS.md).

## Gates ativos

- `./mvnw.cmd -B clean verify` é o gate local principal de compilação, testes,
  Flyway e cobertura. Execute-o quando o escopo e o ambiente permitirem.
- JaCoCo mantém piso de linhas de `0.7588` (75,88%) no bundle; novos módulos
  não podem reduzi-lo.
- Testes PostgreSQL com Testcontainers executam quando Docker está disponível.
  `disabledWithoutDocker=true` mantém previsibilidade local, mas o CI Linux
  deve executá-los com daemon funcional.
- Os testes ArchUnit existentes protegem boundary de domínio, controller,
  segurança/ownership, Goat, lactação, Milk/Reproduction e o baseline de ports
  de aplicação para entities. A lista e a finalidade de cada guard estão em
  [ARCHITECTURE.md](./ARCHITECTURE.md).
- `GlobalHexagonalBoundaryArchUnitTest` impede o core (`domain`, `application`,
  `business`) de depender diretamente de
  `org.springframework.data.jpa.repository.JpaRepository`. A baseline desse
  guard é zero.
- `GlobalHexagonalBoundaryArchUnitTest` também mantém zero dependências de
  entidades JPA nos pacotes `application` e `business`; conversões ficam nos
  adapters de persistência.
- `GlobalHexagonalBoundaryArchUnitTest` mantém zero dependências de
  `org.springframework.dao` no core `application`/`business`; a tradução de
  conflitos ocorre nos adapters e a representação HTTP permanece nos handlers
  externos.
- `GlobalHexagonalBoundaryArchUnitTest` mantém zero dependências de
  `org.springframework.data.domain` no core global `application`/`business`;
  controllers e adapters são as únicas bordas autorizadas a usar paginação
  Spring.
- `AuthorityPasswordBoundaryArchUnitTest` mantém zero dependências de
  `PasswordEncoder` nos pacotes `authority.application` e `authority.business`;
  o encoder permanece permitido em configuração, adapters e bootstrap.
- `AuthorityAuthenticationBoundaryArchUnitTest` mantém zero dependências de
  `AuthenticationManager`, `Authentication`,
  `UsernamePasswordAuthenticationToken`, `JwtDecoder`, `JwtService` e `Jwt`
  nos pacotes `authority.application` e `authority.business`; esses tipos são
  permitidos somente nos adapters/configuração.
- `AuthorityAccountPersistenceBoundaryArchUnitTest` mantém zero dependências
  dos modelos JPA `User` e `Role` nos pacotes `authority.application` e
  `authority.business`; os modelos de aplicação e o mapper são a fronteira
  oficial.
- `ArticleFarmPaginationBoundaryArchUnitTest` mantém zero dependências de
  `org.springframework.data.domain` no application/business de Article e Farm;
  a conversão permanece restrita à API e aos adapters.
- `HealthBoundaryArchUnitTest` mantém zero dependências de
  `org.springframework.data.domain` no application/business de Health; a
  conversão permanece restrita à API e aos adapters.
- `InventoryBoundaryArchUnitTest` mantém zero dependências de
  `org.springframework.data.domain` no application/business de Inventory; os
  filtros são neutros e a conversão permanece restrita à API e aos adapters.
- `MilkProductionPaginationBoundaryArchUnitTest` mantém zero dependências de
  `org.springframework.data.domain` no caso de uso, port e business de produção
  de leite e Lactation. Reproduction é coberto pelo guard global após F5.
- A superfície HTTP não pode reintroduzir endpoints globais de limpeza ou
  recriação administrativa. Qualquer reset de DEV deve permanecer em tooling
  explícito, fora do fluxo REST, com credenciais externas.
- `codeql.yml` analisa Java em pull requests, pushes protegidos e semanalmente.
- `dependency_review.yml` verifica dependências com Trivy e bloqueia
  vulnerabilidades altas/críticas corrigíveis.
- `supply_chain.yml` publica SBOM CycloneDX e verifica a imagem Docker com
  Trivy, falhando em vulnerabilidades altas/críticas corrigíveis.
- Workflows reutilizáveis são fixados por SHA imutável e usam versões
  compatíveis com Node.js 24 nos runners.

## Dívida observada e gates planejados após remoção

`ApplicationPortPersistenceBoundaryArchUnitTest` mantém uma regra estrutural
de zero dependências de entidades JPA em ports de aplicação. A allowlist
temporária da DEV-A3 foi removida após a conclusão da I3-E2.

DEV-A11-I3-A isolou Article, DEV-A11-I3-B isolou Health e DEV-A11-I3-C isolou
Farm/Address/Phone (11 -> 5), Audit (5 -> 4), Commercial em I3-E1 (4 -> 1) e
Finance em I3-E2 (1 -> 0).
A boundary de I3-C usa modelos tecnológicos
neutros, mantém a transação e preserva os endpoints existentes; o adapter é o
único ponto que monta a graph JPA.

DEV-A11-I2 está arquiteturalmente concluída: os guards de password hashing,
autenticação/JWT e isolamento JPA de Authority permanecem verdes. I2-D/I2-E
não são waves independentes; a implementação `User implements UserDetails` é
limpeza opcional, e atomicidade de password reset é hardening de segurança
separado. `FarmUserPersistencePort` foi removido na I3-C. Finance agora usa
modelos tecnologicamente neutros e não possui dependências JPA no core.

`FarmAddressPhoneCoreBoundaryArchUnitTest` mantém zero dependências de entidades
JPA nos pacotes application/business de Farm, Address e Phone.

O guard global de zero tolerância para `org.springframework.data.domain` no
core `application`/`business` está ativo após DEV-A11-I3-F5 e deve permanecer
verde. Ainda não existe um guard global equivalente para
`AuthenticationManager`, `PasswordEncoder` e `JwtDecoder` em módulos fora do
Authority; esses tipos continuam protegidos pelos guards específicos existentes.

Após limpar um módulo, adicione seu guard específico; após remover todo o
baseline de uma categoria, mantenha o guard global correspondente. Não expanda
baselines nem enfraqueça testes para acomodar regressões.

## Observabilidade

`HttpRequestLoggingFilter` gera ou propaga `correlationId` seguro, devolve-o no
cabeçalho e o inclui no padrão Logback. Logs de negócio podem registrar ids
técnicos e duração, mas não cabeçalhos de autenticação, tokens ou corpos de
requisição.

## Limites operacionais

O scanner de imagem depende do acesso do runner ao registry das imagens-base e
Testcontainers depende de daemon Docker. Falha de infraestrutura do runner é
bloqueio de CI, não motivo para `continue-on-error`.

O banco DEV contém dados descartáveis, mas schema, Flyway, reset e promoção de
ambiente continuam gates explícitos. Nenhum destes é consequência automática de
um refactor ou de um teste arquitetural.
