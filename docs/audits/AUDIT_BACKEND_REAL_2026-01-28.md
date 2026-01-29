# AUDIT_BACKEND_REAL_2026-01-28

## Resumo executivo (10 pontos)
- Baseline de testes via `mvn -q verify` passou; total 136 testes, 0 falhas, 0 erros, 0 skipped (evidencia em `target/surefire-reports`).
- `mvn -q clean test` e `mvn -q test` expiraram por timeout (~4 min cada) antes de concluir; testes confirmados pelo `verify`.
- Arquitetura Hexagonal preservada: controllers chamam Port In (UseCase) e nao acessam repositories diretamente.
- Regras de dominio estao concentradas em Business/Validators (ex.: `GoatGenderValidator`, `LactationBusiness`, `ReproductionBusiness`).
- Ownership e roles sao aplicados por `@PreAuthorize` e/ou `OwnershipService` (provas em `SecurityOwnershipIntegrationTest`).
- Endpoints publicos estao restritos a `/public/**` e GETs especificos em `/api/goatfarms` e `/api/goatfarms/*/goats/**` conforme `SecurityConfig`.
- Contrato de erros centralizado em `GlobalExceptionHandler` com payload padrao; mensagens PT-BR na maioria dos casos.
- Modulo Articles possui migration com indices e unicidade de slug; testes cobrem public/admin e duplicidade de slug.
- LogoUrl do capril: migration `V18__add_logo_url_to_capril.sql` e teste de integracao `GoatFarmLogoIntegrationTest` cobrem persistencia e retorno.
- Achados P0/P1: nenhum comprovado com reproducao/teste nesta auditoria.

## Etapa 0 - Provas (baseline)
### Comandos executados
- `mvn -q clean test` -> timeout (~4 min) antes de concluir.
- `mvn -q test` -> timeout (~4 min) antes de concluir.
- `mvn -q verify` -> SUCESSO (exit 0).

### Totais de testes (a partir de `target/surefire-reports` gerado no `verify`)
- tests: 136
- failures: 0
- errors: 0
- skipped: 0

### Warnings observados no `mvn -q verify`
- Logback: sem `logback-test.xml` e warnings de `SizeAndTimeBasedRollingPolicy`/`SizeBasedTriggeringPolicy`.
- Mockito: aviso de self-attach do agente de mock inline.

## Etapa 1 - Inventario completo
### Controllers e endpoints (metodo + path + auth/roles)
**AddressController** (`/api/goatfarms/{farmId}/addresses`)
- POST `` -> auth (ROLE_ADMIN/ROLE_OPERATOR/ROLE_FARM_OWNER), ownership via business
- PUT `/{addressId}` -> auth, ownership via business
- GET `/{addressId}` -> auth, ownership via business
- DELETE `/{addressId}` -> auth, ownership via business

**ArticleAdminController** (`/api/articles`) - ADMIN only
- POST ``
- PUT `/{id}`
- GET ``
- GET `/{id}`
- PATCH `/{id}/publish`
- PATCH `/{id}/highlight`
- DELETE `/{id}`

**PublicArticleController** (`/public/articles`) - public
- GET ``
- GET `/highlights`
- GET `/{slug}`

**AuthController** (`/api/auth`)
- POST `/login` (public)
- POST `/register` (public)
- POST `/refresh` (public)
- POST `/register-farm` (public)
- GET `/me` (auth)

**UserController** (`/api/users`)
- POST `` (ROLE_ADMIN/ROLE_OPERATOR)
- PUT `/{id}` (ROLE_ADMIN/ROLE_OPERATOR)
- GET `/me` (auth)
- GET `/{id}` (ROLE_ADMIN/ROLE_OPERATOR)
- PATCH `/{id}/password` (ROLE_ADMIN or self)
- PATCH `/{id}/roles` (ROLE_ADMIN)
- GET `/debug/{email}` (ROLE_ADMIN/ROLE_OPERATOR)

**AdminController** (`/api/admin/maintenance`) - ROLE_ADMIN
- POST `/clean-admin`
- POST `/clean-admin-auto`

**EventController** (`/api/goatfarms/{farmId}/goats/{goatId}/events`)
- POST `` -> auth + ownership (@PreAuthorize + OwnershipService)
- PUT `/{eventId}` -> auth + ownership
- GET `/{eventId}` -> auth + ownership
- GET `` -> auth + ownership
- GET `/filter` -> auth + ownership
- DELETE `/{eventId}` -> auth + ownership

**GoatFarmController** (`/api/goatfarms`)
- POST `` -> auth (ROLE_ADMIN/ROLE_OPERATOR/ROLE_FARM_OWNER)
- PUT `/{id}` -> auth (roles) + ownership no business
- GET `/{id}` -> public (permitAll)
- GET `/name` -> public (permitAll)
- GET `` -> public (permitAll)
- DELETE `/{id}` -> auth (roles)
- GET `/{farmId}/permissions` -> auth (roles)

**GenealogyController** (`/api/goatfarms/{farmId}/goats/{goatId}/genealogies`)
- GET `` -> public (permitAll)

**GoatController** (`/api/goatfarms/{farmId}/goats`)
- POST `` -> auth + ownership
- PUT `/{goatId}` -> auth + ownership
- DELETE `/{goatId}` -> auth + ownership
- GET `/{goatId}` -> public (permitAll)
- GET `` -> public (permitAll)
- GET `/search` -> public (permitAll)

**LactationController** (`/api/goatfarms/{farmId}/goats/{goatId}/lactations`)
- POST `` -> auth + ownership
- GET `/active` -> auth + ownership
- GET `/active/summary` -> auth + ownership
- PATCH `/{lactationId}/dry` -> auth + ownership
- GET `/{lactationId}` -> auth + ownership
- GET `/{lactationId}/summary` -> auth + ownership
- GET `` -> auth + ownership

**MilkProductionController** (`/api/goatfarms/{farmId}/goats/{goatId}/milk-productions`)
- POST `` -> auth + ownership
- PATCH `/{id}` -> auth + ownership
- GET `/{id}` -> auth + ownership
- GET `` -> auth + ownership
- DELETE `/{id}` -> auth + ownership

**PhoneController** (`/api/goatfarms/{farmId}/phones`)
- POST `` -> auth, ownership via business
- GET `/{phoneId}` -> auth, ownership via business
- GET `` -> auth, ownership via business
- PUT `/{phoneId}` -> auth, ownership via business
- DELETE `/{phoneId}` -> auth, ownership via business

**ReproductionController** (`/api/goatfarms/{farmId}/goats/{goatId}/reproduction`)
- POST `/breeding` -> auth + ownership
- PATCH `/pregnancies/confirm` -> auth + ownership
- GET `/pregnancies/active` -> auth + ownership
- GET `/pregnancies/{pregnancyId}` -> auth + ownership
- PATCH `/pregnancies/{pregnancyId}/close` -> auth + ownership
- GET `/events` -> auth + ownership
- GET `/pregnancies` -> auth + ownership

### Portas (UseCases) - Port In
- `application/ports/in`: AddressManagementUseCase, AdminMaintenanceUseCase, ArticleCommandUseCase, ArticleQueryUseCase, AuthManagementUseCase, EventManagementUseCase, GenealogyQueryUseCase, GoatFarmManagementUseCase, GoatManagementUseCase, LactationCommandUseCase, LactationQueryUseCase, MilkProductionUseCase, PhoneManagementUseCase, ReproductionCommandUseCase, ReproductionQueryUseCase, UserManagementUseCase.

### Portas (Persistence/External) - Port Out
- `application/ports/out`: AddressPersistencePort, ArticlePersistencePort, EventPersistencePort, EventPublisher, GoatFarmPersistencePort, GoatGenealogyQueryPort, GoatPersistencePort, LactationPersistencePort, MilkProductionPersistencePort, PhonePersistencePort, PregnancyPersistencePort, ReproductiveEventPersistencePort, RolePersistencePort, UserPersistencePort.

### Mapa por modulo (Controllers / Business / Ports / Adapters / Entities / DTOs / Mappers)
(Referencias por arquivo - ver detalhamento no inventario gerado)
- address: `AddressController`, `AddressBusiness`, `AddressPersistencePort`, `AddressPersistenceAdapter`, `Address` entity, `AddressRequestDTO/ResponseDTO`, `AddressMapper`.
- article: `ArticleAdminController`, `PublicArticleController`, `ArticleBusiness`, `ArticlePersistencePort`, `ArticlePersistenceAdapter`, `Article` entity, DTOs + VOs, `ArticleMapper`.
- authority: `AuthController`, `UserController`, `AdminController`, `AuthBusiness/UserBusiness/AdminMaintenanceBusiness`, `UserPersistencePort/RolePersistencePort`, adapters, `User/Role/Authority` entities, DTOs + Mappers.
- events: `EventController`, `EventBusiness`, `EventPersistencePort` + `EventPublisher`, `EventPersistenceAdapter`, `Event` entity, DTOs + Mapper.
- farm: `GoatFarmController`, `GoatFarmBusiness`, `GoatFarmPersistencePort`, `GoatFarmPersistenceAdapter`, `GoatFarm` entity, DTOs + Mappers.
- genealogy: `GenealogyController`, `GenealogyBusiness`, `GoatGenealogyQueryPort`, `GenealogyMapper`.
- goat: `GoatController`, `GoatBusiness`, `GoatPersistencePort`, `GoatPersistenceAdapter`, `Goat` entity, DTOs + Mapper.
- milk: `LactationController`, `MilkProductionController`, `LactationBusiness`, `MilkProductionBusiness`, ports out, adapters, `Lactation/MilkProduction` entities, DTOs + Mappers.
- phone: `PhoneController`, `PhoneBusiness`, `PhonePersistencePort`, `PhonePersistenceAdapter`, `Phone` entity, DTOs + Mapper.
- reproduction: `ReproductionController`, `ReproductionBusiness`, `PregnancyPersistencePort/ReproductiveEventPersistencePort`, adapters, `Pregnancy/ReproductiveEvent` entities, DTOs + Mapper.

## Etapa 2 - Auditoria de arquitetura (Hexagonal/DDD)
### Evidencias
- Controllers chamam apenas Port In (UseCase); nenhum `*Repository` importado em controllers (ver `rg` em `*Controller*.java`).
- Business concentra regras de dominio e valida regras antes de persistir (ex.: `GoatGenderValidator`, `LactationBusiness`, `ReproductionBusiness`, `GoatFarmBusiness`, `PhoneBusiness`).
- Entidades JPA nao vazam para API: controllers retornam DTOs mapeados por mappers.
- Repositories usados apenas em adapters e camada infra (ex.: `infrastructure/adapters/out/persistence/*`).

## Etapa 3 - Auditoria de seguranca e ownership
### Evidencias
- `SecurityConfig` define public endpoints (`/public/**`, `/api/auth/*`, GETs publicos de farm/goat/genealogy).
- Endpoints com farmId usam `@PreAuthorize` com `@ownershipService.isFarmOwner(#farmId)` ou verificacao via business (Address/Phone/Farm).
- Testes: `SecurityOwnershipIntegrationTest` cobre 401 sem token, 403 sem ownership, 200 com owner/admin.

## Etapa 4 - Contrato de erros / validacoes / status
### Padrao atual
- `GlobalExceptionHandler` retorna payload com: `timestamp`, `status`, `error`, `path`, `errors`.
- Status codes usados:
  - 400: `DatabaseException`, `InvalidArgumentException`, `HttpMessageNotReadableException`.
  - 401: `UnauthorizedException`.
  - 403: `AccessDeniedException`.
  - 404: `ResourceNotFoundException`.
  - 409: `DuplicateEntityException`, `DataIntegrityViolationException`.
  - 422: `ValidationException`, `MethodArgumentNotValidException`.

### Observacao (nao P0/P1)
- Mensagens de `DataIntegrityViolationException` incluem texto em ingles (ver P2).

## Etapa 5 - Auditoria por modulo (regras, endpoints, validacoes, seguranca, testes)
### Farm/Capril (inclui logoUrl)
- Regras: nome/tod unicos, ownership no update/delete, phones obrigatorios no update, logoUrl opcional com validacao de http/https e tamanho.
- Persistencia: `GoatFarm` com `logo_url` em `V18__add_logo_url_to_capril.sql`.
- Testes: `GoatFarmLogoIntegrationTest` (logoUrl create/list/detail), `SecurityOwnershipIntegrationTest` (update com ownership), `GoatFarmBusinessTest`.

### Goat
- Regras: registro unico, ownership no create/update/delete; busca publica.
- Testes: `GoatControllerTest`, `GoatBusinessTest`.

### Lactation + Summary
- Regras: apenas femeas (`GoatGenderValidator`), 1 lactacao ativa, datas validas, summary por dias distintos.
- Testes: `LactationSummaryIntegrationTest` (metrics, sem producoes, macho 422, 404), `LactationBusinessTest`.

### MilkProduction
- Regras: apenas femeas, data nao futura, nao duplicar data+turno, exige lactacao ativa.
- Testes: `MilkProductionBusinessTest` cobre duplicidade, data futura, lactacao ativa, update/delete.

### Reproduction
- Regras: apenas femeas, datas validas, confirmacao requer cobertura previa, uma gestacao ativa, fechamento com motivo.
- Testes: `ReproductionBusinessTest`, `ReproductionControllerTest`.

### Articles/Blog
- Regras: slug unico, public mostra apenas published, highlights previsiveis, admin-only para CRUD.
- Persistencia: `V17__create_articles_table.sql` com indices e constraint de slug.
- Testes: `ArticlePublicAdminIntegrationTest` (public/admin/slug dup/highlights).

### Address/Phone
- Regras: ownership, validacoes de CEP/UF/telefone, pelo menos 1 telefone.
- Testes: `PhoneBusinessTest` (nao permite deletar ultimo), `SecurityOwnershipIntegrationTest` (phone delete 422), `AddressBusinessTest`.

### Events
- Regras: ownership via goat/farm, eventos vinculados a cabra.
- Testes: `EventDaoTest`, `EventDaoUnitTest`.

### Genealogy
- Regras: consulta publica; depende de `GoatGenealogyQueryPort`.
- Testes: `GenealogyControllerTest`, `GenealogyBusinessTest`.

### Security/Auth
- Regras: JWT, roles, ownership, endpoints publicos restritos.
- Testes: `AuthControllerIntegrationTest`, `SecurityOwnershipIntegrationTest`, `GlobalExceptionHandlerTest`.

## Etapa 6 - Persistencia / migracoes / performance
- Migrations relevantes: `V17__create_articles_table.sql` (indices e unique slug), `V18__add_logo_url_to_capril.sql`.
- Unicidade de gestacao ativa via indice parcial `ux_pregnancy_single_active_per_goat` (V16).
- Nenhum N+1 evidente observado nos casos principais; sugestoes de indices adicionais nao aplicadas.

## Etapa 7 - Integracao entre modulos
- MilkProduction -> Lactation: regra de lactacao ativa aplicada em `MilkProductionBusiness` e testada.
- Reproduction -> Goat/Farm: ownership + female-only aplicados e testados.
- Ownership -> endpoints com farmId: `@PreAuthorize` + `OwnershipService` com testes de 401/403/200.
- Public endpoints -> Security: cobertura em `SecurityOwnershipIntegrationTest`.

## Tabela: Regra/Invariante -> Teste existente? -> Acao
| Regra/Invariante | Teste existente | Acao |
| --- | --- | --- |
| LogoUrl opcional, retorna em list/detail | `GoatFarmLogoIntegrationTest` | OK |
| Ownership em endpoints privados (401/403/200) | `SecurityOwnershipIntegrationTest` | OK |
| Apenas femeas em lactacao/summary | `LactationSummaryIntegrationTest` | OK |
| Lactacao ativa unica | `LactationBusinessTest` | OK |
| MilkProduction requer lactacao ativa | `MilkProductionBusinessTest` | OK |
| MilkProduction nao duplica data+turno | `MilkProductionBusinessTest` | OK |
| Reproduction exige cobertura previa e valida datas | `ReproductionBusinessTest` | OK |
| Articles publicos somente published | `ArticlePublicAdminIntegrationTest` | OK |
| Slug unico (Articles) | `ArticlePublicAdminIntegrationTest` | OK |
| Phone nao pode deletar ultimo | `PhoneBusinessTest` + `SecurityOwnershipIntegrationTest` | OK |
| Validacao de CEP/UF | `AddressBusinessTest` | OK |
| Public goat/goatfarm endpoints acessiveis | `SecurityOwnershipIntegrationTest` | OK |

## Achados
### P0
- Nenhum achado P0 comprovado por teste/reproducao.

### P1
- Nenhum achado P1 comprovado por teste/reproducao.

### P2
1) Mensagens em ingles no handler de `DataIntegrityViolationException`.
   - Arquivo: `src/main/java/com/devmaster/goatfarm/config/exceptions/GlobalExceptionHandler.java:114` e `:116`.
   - Sintoma: `err.addError("status", "Duplicate active pregnancy for goat")` e `err.addError("integrity", "Database constraint violation")`.
   - Impacto: mensagem foge do padrao PT-BR.
   - Recomendacao: traduzir para PT-BR e, se possivel, adicionar teste de integridade de gestacao ativa.

2) Warnings recorrentes no `mvn -q verify` (logback e Mockito inline agent).
   - Arquivos/Config: logback config e configuracao de testes.
   - Impacto: ruido no build e possivel confusao de diagnostico.
   - Recomendacao: adicionar `logback-test.xml` e revisar politica de rolling; considerar recomendacao do Mockito para agent.

## Checklist DoD pos-auditoria
- [x] `mvn -q verify` verde (136 testes).
- [x] Relatorio completo em `docs/audits/AUDIT_BACKEND_REAL_2026-01-28.md`.
- [x] Inventario de controllers/endpoints e mapa de modulos.
- [x] Auditoria de seguranca/ownership com evidencias.
- [x] Auditoria de regras por modulo e cobertura de testes.
- [ ] (Opcional) Tratar warnings logback/Mockito.
- [ ] (Opcional) Padronizar mensagens PT-BR no handler de integridade.

## Arquivos alterados
- Apenas este relatorio.

## Commits
- Nenhum commit criado nesta auditoria.
