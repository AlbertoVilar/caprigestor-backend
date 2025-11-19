# Refactor: Hex Architecture – GoatFarm Aggregate Root

Branch: `feature/hex-architecture-refactor`

## Contexto
Este PR continua a migração para Arquitetura Hexagonal, colocando `GoatFarm` como Aggregate Root e movendo controllers para depender de portas (UseCases) com mapeamento explícito entre DTOs e VOs. Também consolida o tratamento global de exceções para parsing de JSON.

## Mudanças Principais
- Introdução das portas de entrada (UseCases):
  - `GoatFarmManagementUseCase`
  - `AddressManagementUseCase`
  - `PhoneManagementUseCase`
- Implementação das portas pelas classes de business existentes:
  - `GoatFarmBusiness implements GoatFarmManagementUseCase`
  - `AddressBusiness implements AddressManagementUseCase`
  - `PhoneBusiness implements PhoneManagementUseCase`
- Refatoração dos controllers para depender de UseCases e mappers:
  - `GoatFarmController` agora usa `GoatFarmManagementUseCase` + `GoatFarmMapper`, `UserMapper`, `AddressMapper`, `PhoneMapper`.
  - `AddressController` agora usa `AddressManagementUseCase` + `AddressMapper`.
  - `PhoneController` agora usa `PhoneManagementUseCase` + `PhoneMapper` (removida dependência de `PhoneFacade`).
- Handler Global de exceções atualizado para `HttpMessageNotReadableException` com retorno de `ValidationError` estruturado para `UnrecognizedPropertyException` e `InvalidFormatException`.

## Arquivos alterados/criados (principais)
- `src/main/java/com/devmaster/goatfarm/application/ports/in/GoatFarmManagementUseCase.java` (novo)
- `src/main/java/com/devmaster/goatfarm/application/ports/in/AddressManagementUseCase.java` (novo)
- `src/main/java/com/devmaster/goatfarm/application/ports/in/PhoneManagementUseCase.java` (novo)
- `src/main/java/com/devmaster/goatfarm/farm/business/GoatFarmBusiness.java` (implements UseCase)
- `src/main/java/com/devmaster/goatfarm/address/business/AddressBusiness.java` (implements UseCase)
- `src/main/java/com/devmaster/goatfarm/phone/business/business/PhoneBusiness.java` (implements UseCase)
- `src/main/java/com/devmaster/goatfarm/farm/api/controller/GoatFarmController.java` (refatorado)
- `src/main/java/com/devmaster/goatfarm/address/api/controller/AddressController.java` (refatorado)
- `src/main/java/com/devmaster/goatfarm/phone/api/controller/PhoneController.java` (refatorado)
- `src/main/java/com/devmaster/goatfarm/config/exceptions/GlobalExceptionHandler.java` (ajustado previamente na branch)

## Impacto de API
- Rotas permanecem inalteradas para GoatFarm, Address e Phone.
- Payloads (DTOs) mantidos. Conversões para VO ocorrem internamente no controller via MapStruct.
- `PhoneFacade` deixa de ser usado pelo `PhoneController`. O artefato permanece no projeto por compatibilidade, mas pode ser removido em PRs futuros.

### Segurança e Endpoints Públicos
- Atualização da configuração de segurança para explicitar endpoints públicos:
  - `POST /api/auth/login`, `POST /api/auth/register`, `POST /api/auth/refresh`
  - `POST /api/auth/register-farm` (público)
  - `POST /api/goatfarms/full` (público)
  - GETs públicos já existentes para leitura de fazendas/cabras/genealogias
- Documentação atualizada para refletir essas rotas públicas conforme `SecurityConfig`.

### Ajustes de Mapeamento (DTO/VO)
- Inclusão do campo `version` em `GoatFarmFullResponseVO` para propagação ao `GoatFarmFullResponseDTO`.
- Nota de comportamento: `updatedAt` é nulo na criação; `phones` vem vazio se não informados.

## Testes e Build
- Build executado com sucesso usando `mvn -DskipTests=true -D"spring-boot.repackage.skip"=true package`.
- Ao rodar testes (`mvn verify`), há falhas existentes em `EventDaoTest` por `DataIntegrityViolation` (campo `goat_registration_number` nulo). As falhas não são relacionadas às mudanças de GoatFarm/Address/Phone.

## Considerações de Arquitetura
- Controllers passam a depender de portas (UseCases) — reduz acoplamento a Facades e aproxima do modelo hexagonal (ports/adapters).
- Mappers seguem padrão MapStruct para transformação entre DTO e VO.
- `GoatFarm` segue como Aggregate Root, com `Address` e `Phone` como entidades relacionadas, mantendo verificações de ownership.

## Checklist do PR
- [x] Controllers usam UseCases e mappers coerentes
- [x] Business implementa interfaces de caso de uso
- [x] Build sem erros
- [ ] Remover Facades e artefatos não utilizados (próximo PR)
- [ ] Ajustar testes do módulo de eventos (fora do escopo deste PR)

## Próximos Passos
- Abrir PR para `main` (ou branch alvo) com esta descrição.
- Planejar remoção controlada de `AddressFacade` e `PhoneFacade` onde não são mais utilizados.
- Endereçar falhas no `EventDaoTest` em PR dedicado.

## Mensagem de Commit (referência já enviada)
`fix(exceptions): add global handler for HttpMessageNotReadableException; return structured ValidationError for JSON parse issues (UnrecognizedProperty/InvalidFormat)`