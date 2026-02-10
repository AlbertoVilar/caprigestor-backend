# Auditoria BEFORE - Estado do Projeto
Ultima atualizacao: 2026-02-10
Escopo: diagnostico tecnico antes da criacao dos documentos oficiais de status e roadmap.
Links relacionados: [Portal](../INDEX.md), [Arquitetura](../01-architecture/ARCHITECTURE.md), [API_CONTRACTS](../03-api/API_CONTRACTS.md)

## 1. Contexto da auditoria
- Data da auditoria: 2026-02-10.
- Branch analisada: `feature/docs_project_status_and_roadmap`.
- Fontes analisadas:
- `src/main/java/com/devmaster/goatfarm/*`
- `src/test/java/com/devmaster/goatfarm/*`
- `src/main/resources/db/migration/*`
- `docs/02-modules/*` e `docs/03-api/API_CONTRACTS.md`

## 2. Modulos detectados por pacote
- `address`
- `application`
- `article`
- `authority`
- `config`
- `events`
- `farm`
- `genealogy`
- `goat`
- `health`
- `milk`
- `phone`
- `reproduction`
- `sharedkernel`
- `validation`

## 3. Inventario tecnico por modulo
### address
- Controllers: `AddressController.java` (1)
- Ports in: `AddressManagementUseCase.java` (1)
- Ports out: `AddressPersistencePort.java` (1)
- Business: `AddressBusiness.java`, `AddressBusinessMapper.java`, `AddressRequestVO.java`, `AddressResponseVO.java` (4)
- Persistence: `Address.java`, `AddressPersistenceAdapter.java`, `AddressRepository.java` (3)
- Flyway: `V4__Create_Address_Table.sql`, `V11__Enforce_OneToOne_GoatFarm_Address.sql`
- Testes: `AddressBusinessTest.java`, `AddressControllerTest.java`, `AddressControllerWebTest.java` (3)

### application
- Controllers: nenhum
- Ports in: nenhum
- Ports out: nenhum
- Business: nenhum
- Persistence: nenhum
- Flyway: nao aplicavel
- Testes: nenhum

### article
- Controllers: `ArticleAdminController.java`, `PublicArticleController.java` (2)
- Ports in: `ArticleCommandUseCase.java`, `ArticleQueryUseCase.java` (2)
- Ports out: `ArticlePersistencePort.java` (1)
- Business: `ArticleBusiness.java`, `ArticleBusinessMapper.java`, `ArticleHighlightRequestVO.java`, `ArticlePublicDetailResponseVO.java`, `ArticlePublicListResponseVO.java`, `ArticlePublishRequestVO.java`, `ArticleRequestVO.java`, `ArticleResponseVO.java` (8)
- Persistence: `Article.java`, `ArticlePersistenceAdapter.java`, `ArticleRepository.java` (3)
- Flyway: `V17__create_articles_table.sql`
- Testes: `ArticlePublicAdminIntegrationTest.java` (1)

### authority
- Controllers: `AdminController.java`, `AuthController.java`, `UserController.java` (3)
- Ports in: `AdminMaintenanceUseCase.java`, `AuthManagementUseCase.java`, `UserManagementUseCase.java` (3)
- Ports out: `RolePersistencePort.java`, `UserPersistencePort.java` (2)
- Business: `AdminBusiness.java`, `AdminMaintenanceBusiness.java`, `AdminMaintenanceService.java`, `AuthBusiness.java`, `AuthorityBusinessMapper.java`, `LoginRequestVO.java`, `LoginResponseVO.java`, `RefreshTokenRequestVO.java`, `UserBusiness.java`, `UserRequestVO.java`, `UserResponseVO.java` (11)
- Persistence: `Authority.java`, `AuthorityRepository.java`, `FarmOperator.java`, `FarmOperatorRepository.java`, `Role.java`, `RolePersistenceAdapter.java`, `RoleRepository.java`, `User.java`, `UserPersistenceAdapter.java`, `UserRepository.java` (10)
- Flyway: `V1__Create_Authority_Table.sql`, `V2__Create_Role_Table.sql`, `V3__Create_Users_Table.sql`, `V19__create_farm_operator_table.sql`
- Testes: `AuthControllerIntegrationTest.java`, `UserBusinessTest.java`, `UserControllerTest.java` (3)

### config
- Controllers: nenhum
- Ports in: nenhum
- Ports out: nenhum
- Business: nenhum
- Persistence: nenhum
- Flyway: nao aplicavel
- Testes: `GlobalExceptionHandlerTest.java` (1)

### events
- Controllers: `EventController.java` (1)
- Ports in: `EventManagementUseCase.java` (1)
- Ports out: `EventPersistencePort.java`, `EventPublisher.java` (2)
- Business: `EventBusiness.java`, `EventBusinessMapper.java`, `EventRequestVO.java`, `EventResponseVO.java` (4)
- Persistence: `Event.java`, `EventConsumer.java`, `EventMessage.java`, `EventPersistenceAdapter.java`, `EventRepository.java`, `RabbitMQEventPublisher.java` (6)
- Flyway: `V9__Create_Event_Table.sql`
- Testes: `EventDaoTest.java`, `EventDaoUnitTest.java` (2)

### farm
- Controllers: `GoatFarmController.java` (1)
- Ports in: `GoatFarmManagementUseCase.java` (1)
- Ports out: `GoatFarmPersistencePort.java` (1)
- Business: `FarmBusinessMapper.java`, `FarmPermissionsVO.java`, `GoatFarmBusiness.java`, `GoatFarmFullRequestVO.java`, `GoatFarmFullResponseVO.java`, `GoatFarmRequestVO.java`, `GoatFarmResponseVO.java` (7)
- Persistence: `GoatFarm.java`, `GoatFarmPersistenceAdapter.java`, `GoatFarmRepository.java` (3)
- Flyway: `V5__Create_GoatFarm_Table.sql`, `V11__Enforce_OneToOne_GoatFarm_Address.sql`, `V18__add_logo_url_to_capril.sql`
- Testes: `GoatFarmBusinessTest.java`, `GoatFarmControllerTest.java`, `GoatFarmLogoIntegrationTest.java` (3)

### genealogy
- Controllers: `GenealogyController.java` (1)
- Ports in: `GenealogyQueryUseCase.java` (1)
- Ports out: nenhum
- Business: `GenealogyBusiness.java`, `GenealogyBusinessMapper.java`, `GenealogyResponseVO.java` (3)
- Persistence: nenhum
- Flyway: `V8__Create_Genealogy_Table.sql`, `V12__Drop_Genealogy_Table.sql`
- Testes: `GenealogyBusinessTest.java`, `GenealogyControllerTest.java` (2)

### goat
- Controllers: `GoatController.java` (1)
- Ports in: `GoatManagementUseCase.java` (1)
- Ports out: `GoatGenealogyQueryPort.java`, `GoatPersistencePort.java` (2)
- Business: `GoatBusiness.java`, `GoatBusinessMapper.java`, `GoatRequestVO.java`, `GoatResponseVO.java` (4)
- Persistence: `Goat.java`, `GoatPersistenceAdapter.java`, `GoatRepository.java` (3)
- Flyway: `V7__Create_Goat_Table.sql`, `V10__Update_Enums_To_Portuguese.sql`
- Testes: `GoatBusinessTest.java`, `GoatControllerTest.java` (2)

### health
- Controllers: `FarmHealthEventController.java`, `HealthEventController.java` (2)
- Ports in: `FarmHealthAlertsQueryUseCase.java`, `HealthEventCommandUseCase.java`, `HealthEventQueryUseCase.java` (3)
- Ports out: `HealthEventPersistencePort.java` (1)
- Business: `FarmHealthAlertItemVO.java`, `FarmHealthAlertsBusiness.java`, `FarmHealthAlertsResponseVO.java`, `HealthEventBusiness.java`, `HealthEventBusinessMapper.java`, `HealthEventCancelRequestVO.java`, `HealthEventCreateRequestVO.java`, `HealthEventDoneRequestVO.java`, `HealthEventResponseVO.java`, `HealthEventUpdateRequestVO.java`, `package-info.java` (11)
- Persistence: `HealthEvent.java`, `HealthEventPersistenceAdapter.java`, `HealthEventRepository.java` (3)
- Flyway: `V20__create_health_module_tables.sql`
- Testes: `HealthEventBusinessTest.java`, `HealthEventControllerTest.java`, `HealthExceptionHandlingIntegrationTest.java` (3)

### milk
- Controllers: `FarmMilkAlertsController.java`, `LactationController.java`, `MilkProductionController.java` (3)
- Ports in: `LactationCommandUseCase.java`, `LactationQueryUseCase.java`, `MilkProductionUseCase.java` (3)
- Ports out: `LactationPersistencePort.java`, `MilkProductionPersistencePort.java`, `PregnancySnapshotQueryPort.java` (3)
- Business: `LactationBusiness.java`, `LactationBusinessMapper.java`, `LactationDryOffAlertVO.java`, `LactationDryRequestVO.java`, `LactationRequestVO.java`, `LactationResponseVO.java`, `LactationSummaryResponseVO.java`, `MilkProductionBusiness.java`, `MilkProductionBusinessMapper.java`, `MilkProductionRequestVO.java`, `MilkProductionResponseVO.java`, `MilkProductionUpdateRequestVO.java` (12)
- Persistence: `Lactation.java`, `LactationDryOffAlertProjection.java`, `LactationPersistenceAdapter.java`, `LactationRepository.java`, `MilkProduction.java`, `MilkProductionPersistenceAdapter.java`, `MilkProductionRepository.java`, `PregnancySnapshotQueryAdapter.java` (8)
- Flyway: `V13__create_milk_control_tables.sql`, `V14__fix_goat_id_type_milk_module.sql`, `V21__add_milk_production_cancellation.sql`
- Testes: `LactationBusinessTest.java`, `LactationSummaryIntegrationTest.java`, `MilkFarmDryOffAlertsIntegrationTest.java`, `MilkProductionBusinessTest.java`, `MilkProductionCancellationIntegrationTest.java`, `PregnancySnapshotQueryAdapterTest.java` (6)

### phone
- Controllers: `PhoneController.java` (1)
- Ports in: `PhoneManagementUseCase.java` (1)
- Ports out: `PhonePersistencePort.java` (1)
- Business: `PhoneBusiness.java`, `PhoneBusinessMapper.java`, `PhoneRequestVO.java`, `PhoneResponseVO.java` (4)
- Persistence: `Phone.java`, `PhonePersistenceAdapter.java`, `PhoneRepository.java` (3)
- Flyway: `V6__Create_Phone_Table.sql`
- Testes: `PhoneBusinessTest.java` (1)

### reproduction
- Controllers: `FarmReproductionAlertsController.java`, `ReproductionController.java` (2)
- Ports in: `ReproductionCommandUseCase.java`, `ReproductionQueryUseCase.java` (2)
- Ports out: `PregnancyPersistencePort.java`, `ReproductiveEventPersistencePort.java` (2)
- Business: `BreedingRequestVO.java`, `CoverageCorrectionRequestVO.java`, `DiagnosisRecommendationCheckVO.java`, `DiagnosisRecommendationCoverageVO.java`, `DiagnosisRecommendationResponseVO.java`, `PregnancyCheckRequestVO.java`, `PregnancyCloseRequestVO.java`, `PregnancyConfirmRequestVO.java`, `PregnancyDiagnosisAlertVO.java`, `PregnancyResponseVO.java`, `ReproductionBusiness.java`, `ReproductionBusinessMapper.java`, `ReproductiveEventResponseVO.java` (13)
- Persistence: `Pregnancy.java`, `PregnancyDiagnosisAlertProjection.java`, `PregnancyPersistenceAdapter.java`, `PregnancyRepository.java`, `ReproductiveEvent.java`, `ReproductiveEventPersistenceAdapter.java`, `ReproductiveEventRepository.java` (7)
- Flyway: `V15__create_reproduction_module_tables.sql`, `V16__enforce_single_active_pregnancy.sql`, `V22__add_reproduction_corrections.sql`
- Testes: `ReproductionActivePregnancyIntegrationTest.java`, `ReproductionBreedingLockIntegrationTest.java`, `ReproductionBusinessPendingAlertsTest.java`, `ReproductionBusinessTest.java`, `ReproductionControllerTest.java`, `ReproductionFarmPregnancyDiagnosisAlertsIntegrationTest.java`, `ReproductionRecommendationAndCorrectionIntegrationTest.java` (7)

### sharedkernel
- Controllers: nenhum
- Ports in: nenhum
- Ports out: nenhum
- Business: nenhum
- Persistence: nenhum
- Flyway: nao aplicavel
- Testes: nenhum
- Observacao: `sharedkernel/pregnancy/PregnancySnapshot.java` e usado por `milk` via `PregnancySnapshotQueryPort`.

### validation
- Controllers: nenhum
- Ports in: nenhum
- Ports out: nenhum
- Business: nenhum
- Persistence: nenhum
- Flyway: nao aplicavel
- Testes: nenhum

## 4. Lacunas obvias detectadas por busca
### Busca por "Not implemented yet"
- Comando: `rg -n "Not implemented yet" src/main/java src/test/java`
- Resultado: sem ocorrencias.

### Busca por "UnsupportedOperationException"
- Comando: `rg -n "UnsupportedOperationException" src/main/java src/test/java`
- Resultado: sem ocorrencias.

### Busca por TODO/FIXME
- Comando: `rg -n "TODO|FIXME" src/main/java src/test/java`
- Resultado: 9 ocorrencias em `src/main/java/com/devmaster/goatfarm/events/infrastructure/adapter/in/messaging/EventConsumer.java`:
- linhas `74`, `82`, `91`, `99`, `107`, `115`, `123`, `130`, `137`.
- Sintese: consumidor de eventos de dominio ainda possui placeholders para fluxos operacionais.

### Busca por endpoints/metodos 501
- Comando: `rg -n "NOT_IMPLEMENTED|HttpStatus.NOT_IMPLEMENTED|501" src/main/java src/test/java`
- Resultado: sem ocorrencias.

### Busca complementar por possivel metodo incompleto
- Comando: `rg -n "return null;" src/main/java`
- Resultado: ocorrencias em `article`, `authority`, `goat.enums`, `milk` e `farm mapper`.
- Sintese: pontos de maior atencao em regras de negocio:
- `src/main/java/com/devmaster/goatfarm/article/business/articleservice/ArticleBusiness.java:209`
- `src/main/java/com/devmaster/goatfarm/authority/business/AdminBusiness.java:48`
- `src/main/java/com/devmaster/goatfarm/milk/business/lactationservice/LactationBusiness.java:237`
- `src/main/java/com/devmaster/goatfarm/milk/business/lactationservice/LactationBusiness.java:246`

## 5. Resultado de testes
### Gate de arquitetura
- Comando: `./mvnw.cmd -Dtest=HexagonalArchitectureGuardTest test`
- Resultado: BUILD SUCCESS.
- Testes: 1 executado, 0 falhas, 0 erros, 0 skipped.

### Suite de testes
- Comando: `./mvnw.cmd test`
- Resultado: BUILD SUCCESS.
- Testes: 221 executados, 0 falhas, 0 erros, 0 skipped.
- Observacao: log de runtime mostrou warning de agente dinamico do Mockito no JDK atual (sem quebrar build).

## 6. Status de repositorio e branches
- `git status -sb`
- `## feature/docs_project_status_and_roadmap`
- `?? docs/_work/.module_inventory.csv`

- `git branch`
- `develop`
- `feature/docs_project_status_and_roadmap` (atual)
- `main`

- `git log -n 5 --oneline`
- `9bf6fff docs_add_release_notes_template_and_root_md_gateway`
- `99fb572 Merge pull request #54 from AlbertoVilar/feature/docs_refine_audit_and_polish`
- `e2be93f docs_audit_after_report_and_metrics`
- `8c822d9 docs_polish_overview_api_and_portal`
- `837725d docs_polish_modules_contracts_and_links`

## 7. Sintese BEFORE
- O backend possui base funcional ampla para cadastro, producao, reproducao, saude e conteudo.
- Alertas farm-level ja existem em `reproduction`, `milk` e `health`.
- Principal lacuna tecnica objetiva encontrada: TODOs no consumidor assicrono de `events`.
- A criacao de `PROJECT_STATUS.md`, `ROADMAP.md` e `AGENT_CONTEXT.md` segue como proximo passo para consolidar estado canônico.
