# Qualidade, supply chain e observabilidade

Última atualização: 2026-09-12
Escopo: gates automatizados ativos, dívida explicitamente observada e proteções planejadas.

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

Os 10 pares atuais de `ApplicationPortPersistenceBoundaryArchUnitTest` são
baseline de migração, não exceções permanentes. A allowlist pode apenas
diminuir em uma mudança arquitetural revisada.

DEV-A11-I3-A isolou o módulo Article (11 -> 10). Address e Phone não foram
parcialmente migrados: permanecem com Farm para a boundary coerente do agregado
`Farm / Address / Phone` prevista em I3-C.

DEV-A11-I2 está arquiteturalmente concluída: os guards de password hashing,
autenticação/JWT e isolamento JPA de Authority permanecem verdes. I2-D/I2-E
não são waves independentes; a implementação `User implements UserDetails` é
limpeza opcional, e atomicidade de password reset é hardening de segurança
separado. A ponte `FarmUserPersistencePort -> User` pertence à auditoria e
limpeza cross-module de I3.

Não estão ativos como guards globais de zero tolerância, pois ainda falhariam
contra dívida existente fora do Authority:

- core para entidades JPA;
- core para `Page`/`Pageable`/`Sort` do Spring Data;
- core para `AuthenticationManager`, `PasswordEncoder` e `JwtDecoder` em
  módulos que ainda não foram migrados. O Authority já possui guards específicos
  para essas APIs.

Após limpar um módulo, adicione seu guard específico; após remover todo o
baseline de uma categoria, substitua a observação temporária pelo guard global.
Não expanda baselines nem enfraqueça testes para acomodar regressões.

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
