> **NOTA HISTÓRICA**: Este arquivo é mantido para fins de registro histórico. Seu conteúdo pode não refletir o estado atual do código ou da arquitetura.
> Última verificação: 2026-01-11

# Estrutura de Empacotamento e Distribuição de Pacotes (GoatFarm)

Este documento descreve, em formato de visão de pastas do VSCode, como está a estrutura de empacotamento do sistema e a distribuição de todos os pacotes e classes Java. Inclui também um panorama de build/empacotamento (Maven/Spring Boot).

## Visão Geral de Build/Empacotamento
- Projeto Maven (`pom.xml`) com Spring Boot.
- Binário construído como `jar` executável: `target/GoatFarm-<versão>.jar`.
- Perfis de execução e configs em `src/main/resources`.
- Docker Compose disponível em `docs/docker/docker-compose.yml` (serviços de apoio).
- Documentação técnica em `DOCUMENTACAO_BACKEND.md` e `README.md`.

## Layout do Projeto
- `src/main/java` código de produção.
- `src/test/java` testes unitários e de integração.
- Pacotes Java sob `com.devmaster.goatfarm`.

## Pacotes e Classes (src/main/java)

**com.devmaster.goatfarm**
- `GoatFarmApplication.java`

**com.devmaster.goatfarm.address.business**
- `AddressBusiness.java`

**com.devmaster.goatfarm.address.business.bo**
- `AddressVO.java`
- `AddressResponseVO.java`

**com.devmaster.goatfarm.address.mapper**
- `AddressMapper.java`

**com.devmaster.goatfarm.authority.api.controller**
- `AuthController.java`
- `UserController.java`

**com.devmaster.goatfarm.authority.api.dto**
- `LoginRequestDTO.java`
- `LoginResponseDTO.java`
- `RefreshTokenRequestDTO.java`
- `RegisterRequestDTO.java`
- `UserRequestDTO.java`
- `UserResponseDTO.java`
- `UserUpdateRequestDTO.java`
- `UserPasswordUpdateDTO.java`
- `UserRolesUpdateDTO.java`

**com.devmaster.goatfarm.authority.api.projection**
- `UserDetailsProjection.java`

**com.devmaster.goatfarm.authority.controller**
- `AdminController.java`

**com.devmaster.goatfarm.authority.mapper**
- `AuthMapper.java`
- `RoleMapper.java`
- `UserMapper.java`

**com.devmaster.goatfarm.authority.model.entity**
- `Authority.java`
- `Role.java`
- `User.java`

**com.devmaster.goatfarm.authority.model.repository**
- `AuthorityRepository.java`
- `RoleRepository.java`
- `UserRepository.java`

**com.devmaster.goatfarm.config**
- `WebConfig.java`
- `CorsConfig.java`
- `JacksonConfig.java`
- `AdminUserInitializer.java`

**com.devmaster.goatfarm.config.exceptions**
- `DuplicateEntityException.java`
- `ResourceNotFoundException.java`

**com.devmaster.goatfarm.config.exceptions.custom**
- `CustomError.java`
- `DatabaseException.java`
- `FieldMessage.java`
- `ForbiddenException.java`
- `InvalidArgumentException.java`
- `ResourceNotFoundException.java`
- `UnauthorizedException.java`
- `ValidationError.java`
- `ValidationException.java`

**com.devmaster.goatfarm.config.security**
- `CustomUserDetailsService.java`
- `JwtDebugFilter.java`
- `JwtService.java`
- `OwnershipService.java`
- `PasswordConfig.java`
- `SecurityConfig.java`

**com.devmaster.goatfarm.events.api.controller**
- `EventController.java`

**com.devmaster.goatfarm.events.api.dto**
- `EventRequestDTO.java`
- `EventResponseDTO.java`

**com.devmaster.goatfarm.events.business.bo**
- `EventRequestVO.java`
- `EventResponseVO.java`

**com.devmaster.goatfarm.events.enuns**
- `EventType.java`

**com.devmaster.goatfarm.events.mapper**
- `EventMapper.java`

**com.devmaster.goatfarm.events.model.entity**
- `Event.java`

**com.devmaster.goatfarm.events.model.repository**
- `EventRepository.java`

**com.devmaster.goatfarm.farm.api.controller**
- `GoatFarmController.java`

**com.devmaster.goatfarm.farm.api.dto**
- `FarmPermissionsDTO.java`
- `GoatFarmFullRequestDTO.java`
- `GoatFarmRequestDTO.java`
- `GoatFarmResponseDTO.java`
- `GoatFarmUpdateFarmDTO.java`
- `GoatFarmUpdateRequestDTO.java`

**com.devmaster.goatfarm.farm.business.bo**
- `FarmPermissionsVO.java`
- `GoatFarmFullRequestVO.java`
- `GoatFarmFullResponseVO.java`
- `GoatFarmRequestVO.java`
- `GoatFarmResponseVO.java`
- `GoatFarmUpdateRequestVO.java`

**com.devmaster.goatfarm.farm.business.farmbusiness**
- `GoatFarmBusiness.java`

**com.devmaster.goatfarm.farm.mapper**
- `GoatFarmMapper.java`

**com.devmaster.goatfarm.farm.model.entity**
- `GoatFarm.java`

**com.devmaster.goatfarm.farm.model.repository**
- `GoatFarmRepository.java`

**com.devmaster.goatfarm.genealogy.mapper**
- `GenealogyMapper.java`

**com.devmaster.goatfarm.goat.api.controller**
- `GoatController.java`

**com.devmaster.goatfarm.goat.api.dto**
- `GoatRequestDTO.java`
- `GoatResponseDTO.java`

**com.devmaster.goatfarm.goat.business.bo**
- `GoatRequestVO.java`
- `GoatResponseVO.java`

**com.devmaster.goatfarm.goat.business.goatbusiness**
- `GoatBusiness.java`

**com.devmaster.goatfarm.goat.enums**
- `Category.java`
- `Gender.java`
- `GoatBreed.java`
- `GoatStatus.java`

**com.devmaster.goatfarm.goat.mapper**
- `GoatMapper.java`

**com.devmaster.goatfarm.goat.model.entity**
- `Goat.java`

**com.devmaster.goatfarm.goat.model.repository**
- `GoatRepository.java`

**com.devmaster.goatfarm.infrastructure.config**
- `HexagonalConfiguration.java`

**com.devmaster.goatfarm.phone.api.controller**
- `PhoneController.java`

**com.devmaster.goatfarm.phone.api.dto**
- `PhoneRequestDTO.java`
- `PhoneResponseDTO.java`

**com.devmaster.goatfarm.phone.business.bo**
- `PhoneRequestVO.java`
- `PhoneResponseVO.java`

**com.devmaster.goatfarm.phone.business.business**
- `PhoneBusiness.java`

**com.devmaster.goatfarm.phone.mapper**
- `PhoneMapper.java`

**com.devmaster.goatfarm.phone.model.entity**
- `Phone.java`

**com.devmaster.goatfarm.phone.model.repository**
- `PhoneRepository.java`

---

## Pacotes e Classes (src/test/java)

**com.devmaster.goatfarm**
- `GoatFarmApplicationTests.java`

**com.devmaster.goatfarm.authority.api.controller**
- `AuthControllerIntegrationTest.java`
- `UserControllerTest.java`

**com.devmaster.goatfarm.authority.business.usersbusiness**
- `UserBusinessTest.java`

**com.devmaster.goatfarm.config.exceptions**
- `GlobalExceptionHandlerTest.java`

**com.devmaster.goatfarm.goat.api.controller**
- `GoatControllerTest.java`

**com.devmaster.goatfarm.goat.business.goatbusiness**
- `GoatBusinessTest.java`

**com.devmaster.goatfarm.tests**
- `EventDaoTest.java`

---

## Observações
- A nomenclatura segue o padrão por módulo (authority, farm, goat, events, address, phone), com DTO/VO/Entity/Repository/Mapper/Controller por pacote.
- Camada de configuração e segurança em `config/*`.
- A configuração hexagonal encontra-se em `infrastructure/config/HexagonalConfiguration.java`.

## Como ver no VSCode
- No explorador, a estrutura acima corresponde às pastas sob `src/main/java/com/devmaster/goatfarm/` e `src/test/java/com/devmaster/goatfarm/`.
- Cada diretório representa um pacote Java; os arquivos listados são as classes visíveis dentro de cada pacote.

## Descarte do Documento
Este arquivo é temporário e será ignorado pelo Git conforme abaixo.

---

## Alinhamento da Arquitetura: Qual é a estrutura real?

- Conclusão: estamos usando a Opção B (Hexagonal) com módulos de domínio. Ou seja, o núcleo de regras de negócio fica centralizado em `application/core/business` e os módulos (`events`, `goat`, `farm`, `address`, `phone`) organizam API, DTOs/VOs, entidades, repositórios e mapeadores.

### Camada de Aplicação (Hexagonal)
- `application/core/business`
  - Contém os serviços de negócio (casos de uso concretos), como:
    - `EventBusiness`
    - `GoatBusiness`
    - `GoatFarmBusiness`
- `application/ports/in`
  - Define contratos dirigidos pelo domínio para entrada (use cases), por exemplo:
    - `EventManagementUseCase`
- `application/ports/out`
  - Define contratos para saída/acesso a tecnologias (persistência, gateways), por exemplo:
    - `EventPersistencePort`
    - `GoatPersistencePort`
  - Esses ports são implementados por adapters de infraestrutura (persistência, etc.).

### Módulos de Domínio (por feature)
- `events/*`
  - `api/controller`: `EventController`
  - `api/dto`: `EventRequestDTO`, `EventResponseDTO`
  - `business/bo`: `EventRequestVO`, `EventResponseVO` (VOs; não há classes de serviço aqui)
  - `enuns`: `EventType`
  - `mapper`: `EventMapper`
  - `model/entity`: `Event`
  - `model/repository`: `EventRepository`
- `goat/*`
  - `api/controller`: `GoatController`
  - `api/dto`: `GoatRequestDTO`, `GoatResponseDTO`
  - `business/bo`: `GoatRequestVO`, `GoatResponseVO`
  - `enums`: `Category`, `Gender`, `GoatBreed`, `GoatStatus`
  - `mapper`: `GoatMapper`
  - `model/entity`: `Goat`
  - `model/repository`: `GoatRepository`
- `farm/*`
  - `api/controller`: `GoatFarmController`
  - `api/dto`: `GoatFarmFullRequestDTO`, `GoatFarmRequestDTO`, `GoatFarmResponseDTO`, `GoatFarmUpdateRequestDTO`, `FarmPermissionsDTO`
  - `business/bo`: `GoatFarmFullRequestVO`, `GoatFarmFullResponseVO`, `GoatFarmRequestVO`, `GoatFarmResponseVO`, `GoatFarmUpdateRequestVO`, `FarmPermissionsVO`
  - `business/farmbusiness`: `GoatFarmBusiness`
  - `mapper`: `GoatFarmMapper`
  - `model/entity`: `GoatFarm`
  - `model/repository`: `GoatFarmRepository`
- `address/*`
  - `business`: `AddressBusiness`
  - `business/bo`: `AddressVO`, `AddressResponseVO`
  - `mapper`: `AddressMapper`
- `phone/*`
  - `api/controller`: `PhoneController`
  - `api/dto`: `PhoneRequestDTO`, `PhoneResponseDTO`
  - `business/business`: `PhoneBusiness`
  - `business/bo`: `PhoneRequestVO`, `PhoneResponseVO`
  - `mapper`: `PhoneMapper`
  - `model/entity`: `Phone`
  - `model/repository`: `PhoneRepository`
- `authority/*`
  - `api/controller`: `AuthController`, `UserController`
  - `api/dto`: `LoginRequestDTO`, `LoginResponseDTO`, `RegisterRequestDTO`, `RefreshTokenRequestDTO`, `UserRequestDTO`, `UserResponseDTO`, `UserUpdateRequestDTO`, `UserPasswordUpdateDTO`, `UserRolesUpdateDTO`
  - `api/projection`: `UserDetailsProjection`
  - `controller`: `AdminController`
  - `mapper`: `AuthMapper`, `RoleMapper`, `UserMapper`
  - `model/entity`: `Authority`, `Role`, `User`
  - `model/repository`: `AuthorityRepository`, `RoleRepository`, `UserRepository`

### Configuração e Segurança
- `config/*`: `WebConfig`, `CorsConfig`, `JacksonConfig`, `AdminUserInitializer`
- `config/security/*`: `SecurityConfig`, `JwtService`, `JwtDebugFilter`, `CustomUserDetailsService`, `OwnershipService`, `PasswordConfig`
- `config/exceptions/*` e `config/exceptions/custom/*`: exceções e handler global
- `infrastructure/config`: `HexagonalConfiguration`

### Fluxo típico (exemplo: criação de evento)
1. `events/api/controller/EventController` recebe o `EventRequestDTO`.
2. O controller chama o contrato `application/ports/in/EventManagementUseCase`.
3. A implementação concreta `application/core/business/EventBusiness`:
   - Usa `EventMapper` para converter DTO/VO/Entity.
   - Valida/obtém a cabra via `GoatPersistencePort`.
   - Persiste o evento via `EventPersistencePort`.
4. O resultado volta como `EventResponseVO` → `EventResponseDTO` para a API.

### Por que o diretório `events/business/bo` não tem Business classes?
- Os serviços de negócio (casos de uso) foram centralizados na camada de aplicação (`application/core/business`), seguindo o estilo Hexagonal. Os pacotes `*/business/bo` guardam VOs (objetos de valor) usados nas conversões e no transporte interno entre camadas.

### Resposta objetiva à dúvida
- A estrutura em uso é a Opção B (Hexagonal), com módulos de domínio organizando controllers/DTOs/VOs/entidades/repos e o núcleo de regras em `application/*` via ports/adapters.
## Eventos e Mensageria

Pacotes adicionados para suportar processamento assíncrono via RabbitMQ:

```
com.devmaster.goatfarm.events.messaging
├── config        # Beans do RabbitMQ (RabbitTemplate, confirms/returns, listener)
├── consumer      # EventConsumer com @RabbitListener
├── dto           # EventMessage (payload do evento)
└── publisher     # RabbitMQEventPublisher (implementa a porta EventPublisher)
```

Observações:
- Publisher com confirmações e retornos habilitados para diagnóstico de roteamento.
- Listener realiza ACK após o processamento bem-sucedido.
- Logs detalhados disponíveis no perfil `dev` em `logs/dev.log`.