# Estudo Completo do Sistema GoatFarm

Este documento é uma compilação de todo o código-fonte, configurações e documentação do sistema GoatFarm, gerado após uma série de refatorações arquiteturais. O objetivo é servir como um material de estudo completo e uma referência do estado final e arquiteturalmente correto do projeto.

## 1. Documentação Geral (README.md)

# CapriGestor — Backend

> Status: Em desenvolvimento (MVP) até 02/10/2025.

### Contato

- Nome: José Alberto Vilar Pereira
- E-mail: [albertovilar1@gmail.com](mailto:albertovilar1@gmail.com)
- LinkedIn: [linkedin.com/in/alberto-vilar-316725ab](https://www.linkedin.com/in/alberto-vilar-316725ab)
- GitHub: [github.com/albertovilar](https://github.com/albertovilar)

## 1. Descrição

CapriGestor é um sistema backend para gerenciamento de caprinos (cabras) que suporta cadastro, acompanhamento de eventos e genealogia, além de recursos de fazenda e autoridades/usuários. O backend é desenvolvido em Spring Boot 3, segue princípios de arquitetura hexagonal (ports & adapters) e expõe APIs REST seguras, documentadas via Swagger.

## 2. Tecnologias Utilizadas

- Java 21
- Spring Boot 3
- JWT
- OAuth2
- PostgreSQL
- Flyway (migrações de banco)

## 3. Organização dos pacotes

Resumo por módulo (camadas seguindo hexagonal: `domain`, `application`, `infrastructure`):

- `goat`: regras de negócio, cadastro, atributos, conversores e acesso a dados de caprinos.
- `events`: eventos relacionados aos caprinos (nascimentos, coberturas, pesagens, etc.).
- `genealogy`: relacionamento e linhagem entre caprinos (ascendência/descendência).
- `farm`: entidades e serviços de fazendas/estábulos/locais associados.
- `authority`: autenticação, autorização, usuários e papéis.
- `shared`: utilitários, DTOs comuns, exceções e infra compartilhada.

Observação: os pacotes seguem o padrão de separação por domínio, mantendo baixo acoplamento e alta coesão, com conversores e facades onde aplicável.

## 4. Perfis de execução

- `dev`: desenvolvimento local com configurações e dados de exemplo, logs mais verbosos.
- `test`: execução de testes, banco em memória/containers e configurações de teste.
- `prod`: produção, variáveis externas, segurança reforçada e tuning de performance.

Ative via propriedade `spring.profiles.active`.

Exemplos:

```bash
# Windows (PowerShell)
./mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev

# Test
./mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=test

# Linux/Mac
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
./mvnw spring-boot:run -Dspring-boot.run.profiles=test
```

## 5. Banco de dados

- Migrações: `src/main/resources/db/migration` (controladas pelo Flyway).
- Seed inicial: `import.sql` (desabilitado por padrão; habilite `spring.sql.init.mode=always` se necessário).
- Perfis: `test` usa H2 em memória com `MODE=PostgreSQL`, `ddl-auto=validate` e `Flyway` habilitado; `dev` usa PostgreSQL com `ddl-auto=validate` e `Flyway` habilitado.
- Banco padrão (dev): PostgreSQL. Configure credenciais e URL no `application-dev.properties`.

As migrações versionadas (ex.: `V9__Create_Event_Table.sql`) garantem a evolução consistente do schema.

## 6. Como rodar o projeto

Você pode rodar na IDE ou via Docker Compose.

- IDE (IntelliJ/Eclipse):
  - Java 21 instalado.
  - Importar o projeto Maven.
  - Selecionar o perfil desejado (`dev`, `test`, `prod`).
  - Executar a aplicação (classe principal Spring Boot).

- Maven CLI:
  ```bash
  # Dev
  ./mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
  ```

- Docker Compose:
  - Arquivo: `docker/docker-compose.yml`.
  - Sobe serviços (ex.: PostgreSQL) e integra com a aplicação.
  - Comandos:
    ```bash
    # Windows (PowerShell)
    docker compose up -d
    
    # Para parar
    docker compose down
    ```

Após subir, a API estará acessível em `http://localhost:8080` (ajuste conforme perfil/porta).

## 7. Segurança com JWT + OAuth2

- Autenticação via OAuth2/JWT.
- Autorização baseada em papéis:
  - `ROLE_ADMIN`
  - `ROLE_OPERATOR`
- Endpoints protegidos exigem cabeçalho `Authorization: Bearer <token>`.
- Políticas de acesso definidas nas configurações de segurança do Spring.

## 8. Swagger

- UI: `http://localhost:8080/swagger-ui/index.html`
- Permite explorar e testar endpoints REST com schemas e exemplos.

## 9. Link cruzado com o repositório do frontend

Frontend associado: `https://github.com/albertovilar/caprigestor-frontend`

## 10. Status do projeto

MVP em desenvolvimento, já funcional.

---

## 📸 Prints ou GIFs

Espaço reservado para screenshots, GIFs de uso e observações futuras sobre UX e integração.

## 2. Documentação da API (DOCUMENTACAO_BACKEND.md)

# Documentação Técnica do Backend - Sistema GoatFarm

## 1. Visão Geral da Arquitetura

### Arquitetura em Camadas

O sistema GoatFarm utiliza uma arquitetura em camadas bem definida, seguindo os princípios da Clean Architecture. Para facilitar o entendimento, utilizamos a **"Analogia do Restaurante"**:

- **Controller (O Garçom)**: Porta de entrada da API, recebe as requisições HTTP e direciona para o Facade
- **Facade (O Maître d')**: Ponto de contato principal, gerencia segurança e orquestra chamadas
- **Business (O Chef)**: Contém a lógica de negócio, validações e orquestração de operações
- **DAO (O Ajudante de Cozinha)**: Acesso direto aos dados, operações CRUD específicas
- **Repository (A Dispensa)**: Interface com o banco de dados, abstração da persistência

### Tecnologias Principais

- **Spring Boot 3.x**: Framework principal
- **Spring Security 6.x**: Autenticação e autorização com JWT
- **Spring Data JPA**: Persistência de dados
- **Hibernate**: ORM para mapeamento objeto-relacional
- **MapStruct**: Mapeamento automático entre DTOs, VOs e Entities
- **Flyway**: Controle de versão do banco de dados
- **H2 Database**: Banco em memória para testes
- **PostgreSQL**: Banco de dados para produção
- **Maven**: Gerenciamento de dependências

## 2. Modelagem de Dados (Entidades JPA)

### User (Usuário)
**Propósito**: Representa os usuários do sistema (proprietários de fazendas, operadores, administradores).

**Campos Principais**:
- `id`: Identificador único
- `name`: Nome completo
- `email`: Email único para login
- `cpf`: CPF único
- `password`: Senha criptografada (BCrypt)
- `createdAt`, `updatedAt`: Timestamps de auditoria

**Relacionamentos**:
- `@ManyToMany` com `Role`: Um usuário pode ter múltiplas roles (ADMIN, OPERATOR)
- `@OneToMany` com `GoatFarm`: Um usuário pode possuir múltiplas fazendas

### Role (Papel/Função)
**Propósito**: Define os papéis e permissões no sistema.

**Campos Principais**:
- `id`: Identificador único
- `authority`: Nome da role (ROLE_ADMIN, ROLE_OPERATOR)
- `description`: Descrição da role

**Relacionamentos**:
- `@ManyToMany` com `User`: Uma role pode ser atribuída a múltiplos usuários
- `@ManyToMany` com `Authority`: Uma role pode ter múltiplas authorities

### GoatFarm (Fazenda de Caprinos)
**Propósito**: Representa as fazendas/capris onde os animais são criados.

**Campos Principais**:
- `id`: Identificador único
- `name`: Nome da fazenda
- `tod`: Tatuagem Orelha Direita (identificação única)
- `createdAt`, `updatedAt`: Timestamps de auditoria

**Relacionamentos**:
- `@ManyToOne` com `User`: Uma fazenda pertence a um usuário
- `@ManyToOne` com `Address`: Uma fazenda tem um endereço
- `@OneToMany` com `Phone`: Uma fazenda pode ter múltiplos telefones
- `@OneToMany` com `Goat`: Uma fazenda pode ter múltiplos animais

**Observação**: Utiliza `@JsonManagedReference` e `@JsonBackReference` para evitar loops de serialização JSON.

### Address (Endereço)
**Propósito**: Armazena informações de localização das fazendas.

**Campos Principais**:
- `id`: Identificador único
- `street`: Rua/logradouro
- `number`: Número
- `complement`: Complemento
- `neighborhood`: Bairro
- `city`: Cidade
- `state`: Estado
- `zipCode`: CEP
- `createdAt`, `updatedAt`: Timestamps de auditoria

### Phone (Telefone)
**Propósito**: Armazena telefones de contato das fazendas.

**Campos Principais**:
- `id`: Identificador único
- `ddd`: Código de área
- `number`: Número do telefone

**Relacionamentos**:
- `@ManyToOne` com `GoatFarm`: Um telefone pertence a uma fazenda

### Goat (Caprino)
**Propósito**: Representa os animais (cabras/bodes) do sistema.

**Campos Principais**:
- `registrationNumber`: Número de registro único (PK)
- `name`: Nome do animal
- `gender`: Sexo (MALE/FEMALE)
- `breed`: Raça
- `color`: Cor
- `birthDate`: Data de nascimento
- `status`: Status (ATIVO, INACTIVE, SOLD, DECEASED)
- `category`: Categoria (PO, PA, PC)
- `tod`, `toe`: Tatuagens das orelhas
- `createdAt`, `updatedAt`: Timestamps de auditoria

**Relacionamentos**:
- `@ManyToOne` com `GoatFarm`: Um animal pertence a uma fazenda
- `@ManyToOne` com `User`: Um animal tem um responsável
- `@ManyToOne` com `Goat` (pai): Referência ao pai
- `@ManyToOne` com `Goat` (mãe): Referência à mãe
- `@OneToMany` com `Genealogy`: Um animal pode ter múltiplos registros genealógicos

### Genealogy (Genealogia)
**Propósito**: Registra informações genealógicas e linhagem dos animais.

**Campos Principais**:
- `id`: Identificador único
- `generation`: Geração na linhagem
- `relationship`: Tipo de relacionamento (pai, mãe, avô, etc.)
- `createdAt`, `updatedAt`: Timestamps de auditoria

**Relacionamentos**:
- `@ManyToOne` com `Goat`: Uma genealogia pertence a um animal

### Event (Evento)
**Propósito**: Registra eventos relacionados aos animais (nascimento, vacinação, etc.).

**Campos Principais**:
- `id`: Identificador único
- `goatRegistrationNumber`: Referência ao animal
- `eventType`: Tipo do evento
- `eventDate`: Data do evento
- `description`: Descrição detalhada
- `createdAt`, `updatedAt`: Timestamps de auditoria

## 3. Camada de Mapeamento (MapStruct Mappers)

### Função do MapStruct
O MapStruct é utilizado para converter automaticamente entre diferentes representações de dados:
- **DTOs**: Objetos de transferência de dados da API
- **VOs**: Objetos de valor da camada de negócio
- **Entities**: Entidades JPA

### UserMapper
```java
@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponseDTO toResponseDTO(UserResponseVO vo);
    UserRequestVO toRequestVO(UserRequestDTO dto);
    
    @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToStringList")
    UserResponseVO toResponseVO(User user);
    
    @Named("rolesToStringList")
    default List<String> rolesToStringList(Set<Role> roles) {
        return roles.stream().map(Role::getAuthority).collect(Collectors.toList());
    }
}
```

**Fluxos de Conversão**:
- **Request**: `UserRequestDTO` → `UserRequestVO` → `User`
- **Response**: `User` → `UserResponseVO` → `UserResponseDTO`

### GoatFarmMapper
Similar ao UserMapper, gerencia conversões para fazendas:
- Converte relacionamentos complexos (User, Address, List<Phone>)
- Trata mapeamentos de IDs para objetos completos

### PhoneMapper, AddressMapper, GoatMapper
Cada entidade possui seu mapper específico seguindo o mesmo padrão:
- Métodos para conversão de requisição e resposta
- Mapeamento de listas
- Tratamento de relacionamentos

### EventMapper
Responsável pelos mapeamentos entre `Event` (Entity), `EventResponseVO` (Business) e `EventResponseDTO` (API).

- Conversões principais:
  - `Event` → `EventResponseVO` mapeia:
    - `id` → `eventId`
    - `goat.registrationNumber` → `goatId`
    - `goat.name` → `goatName`
  - `EventResponseVO` → `EventResponseDTO` mapeia:
    - `eventId` → `id`

- Métodos expostos:
  - `toRequestVO(EventRequestDTO)`
  - `toEntity(EventRequestVO)`
  - `toResponseVO(Event)`
  - `toResponseDTO(EventResponseVO)`
  - `toResponseVOList(List<Event>)`
  - `toResponseDTOList(List<EventResponseVO>)`
  - `updateEvent(@MappingTarget Event, EventRequestVO)`

Campos retornados pelo endpoint de eventos (`EventResponseDTO`): `id`, `goatId`, `goatName`, `eventType`, `date`, `description`, `location`, `veterinarian`, `outcome`.

## 4. Contrato da API (Endpoints)

### Para a Equipe Frontend:

Este documento detalha as alterações nos endpoints da API após a refatoração arquitetural e o aninhamento de recursos sob o agregado `GoatFarm`. Por favor, revise cuidadosamente para atualizar suas chamadas de API.

**Princípio Geral:** Recursos que pertencem a uma fazenda (`GoatFarm`) agora são acessados através de URLs aninhadas, usando o `farmId` como contexto. Recursos que pertencem a uma cabra (`Goat`) são aninhados sob `GoatFarm` e `Goat`.

---

#### **1. Módulo `Authority`**

**1.1. `AuthController`**
*   **Base URL:** `/api/auth`
*   **Endpoints:**
    *   `POST /api/auth/login`
        *   **Descrição:** Autentica um usuário e retorna tokens JWT.
        *   **Request Body:** `LoginRequestDTO` (email, password)
        *   **Response:** `LoginResponseDTO` (accessToken, refreshToken, tokenType, expiresIn, user)
    *   `POST /api/auth/register`
        *   **Descrição:** Registra um novo usuário com a role padrão `ROLE_OPERATOR`.
        *   **Request Body:** `RegisterRequestDTO` (name, email, cpf, password, confirmPassword)
        *   **Response:** `UserResponseDTO`
    *   `POST /api/auth/refresh`
        *   **Descrição:** Renova o token de acesso usando um refresh token.
        *   **Request Body:** `RefreshTokenRequestDTO` (refreshToken)
        *   **Response:** `LoginResponseDTO` (newAccessToken, newRefreshToken, tokenType, expiresIn)
    *   `GET /api/auth/me`
        *   **Descrição:** Retorna os dados do usuário atualmente autenticado.
        *   **Response:** `UserResponseDTO`
    *   `POST /api/auth/register-farm`
        *   **Descrição:** Registra uma nova fazenda junto com seu usuário proprietário.
        *   **Request Body:** `GoatFarmFullRequestDTO` (contém dados da fazenda, usuário, endereço e telefones)
        *   **Response:** `GoatFarmFullResponseDTO`

**1.2. `UserController`**
*   **Base URL:** `/api/users`
*   **Endpoints:**
    *   `GET /api/users/me`
        *   **Descrição:** Retorna os dados do usuário atualmente autenticado.
        *   **Response:** `UserResponseDTO`
    *   `GET /api/users/{id}`
        *   **Descrição:** Busca um usuário pelo ID.
        *   **Path Variable:** `id` (Long)
        *   **Response:** `UserResponseDTO`
    *   `POST /api/users`
        *   **Descrição:** Cria um novo usuário.
        *   **Request Body:** `UserRequestDTO`
        *   **Response:** `UserResponseDTO`
    *   `PUT /api/users/{id}`
        *   **Descrição:** Atualiza um usuário existente.
        *   **Path Variable:** `id` (Long)
        *   **Request Body:** `UserRequestDTO`
        *   **Response:** `UserResponseDTO`
    *   `GET /api/users/debug/{email}`
        *   **Descrição:** Endpoint temporário para debug de roles de usuário.
        *   **Path Variable:** `email` (String)
        *   **Response:** `Map<String, Object>` (informações de debug)

**1.3. `AdminController`**
*   **Base URL:** `/api/admin/maintenance`
*   **Endpoints:**
    *   `POST /api/admin/maintenance/clean-admin`
        *   **Descrição:** Limpa o banco de dados, mantendo apenas o usuário administrador especificado.
        *   **Query Parameter:** `adminId` (Long)
        *   **Response:** `String` (mensagem de sucesso)
    *   `POST /api/admin/maintenance/clean-admin-auto`
        *   **Descrição:** Limpeza automática do banco de dados, configurando um admin padrão.
        *   **Response:** `String` (mensagem de sucesso)

---

#### **2. Módulo `Farm`**

**2.1. `GoatFarmController`**
*   **Base URL:** `/api/goatfarms`
*   **Endpoints:**
    *   `POST /api/goatfarms/full`
        *   **Descrição:** Cria uma fazenda completa (fazenda, usuário, endereço, telefones).
        *   **Request Body:** `GoatFarmFullRequestDTO`
        *   **Response:** `GoatFarmFullResponseDTO`
    *   `POST /api/goatfarms`
        *   **Descrição:** Cria uma nova fazenda.
        *   **Request Body:** `GoatFarmRequestDTO`
        *   **Response:** `GoatFarmResponseDTO`
    *   `PUT /api/goatfarms/{id}`
        *   **Descrição:** Atualiza uma fazenda existente.
        *   **Path Variable:** `id` (Long)
        *   **Request Body:** `GoatFarmUpdateRequestDTO`
        *   **Response:** `GoatFarmFullResponseDTO`
    *   `GET /api/goatfarms/{id}`
        *   **Descrição:** Busca uma fazenda pelo ID.
        *   **Path Variable:** `id` (Long)
        *   **Response:** `GoatFarmFullResponseDTO`
    *   `GET /api/goatfarms/name`
        *   **Descrição:** Busca fazendas por nome (paginado).
        *   **Query Parameter:** `name` (String, opcional)
        *   **Response:** `Page<GoatFarmFullResponseDTO>`
    *   `GET /api/goatfarms`
        *   **Descrição:** Lista todas as fazendas (paginado).
        *   **Response:** `Page<GoatFarmFullResponseDTO>`
    *   `DELETE /api/goatfarms/{id}`
        *   **Descrição:** Remove uma fazenda pelo ID.
        *   **Path Variable:** `id` (Long)
        *   **Response:** `204 No Content`

---

#### **3. Módulo `Address`**

**3.1. `AddressController`**
*   **Base URL:** `/api/goatfarms/{farmId}/addresses`
*   **Endpoints:**
    *   `POST /api/goatfarms/{farmId}/addresses`
        *   **Descrição:** Cria um novo endereço para uma fazenda específica.
        *   **Path Variable:** `farmId` (Long)
        *   **Request Body:** `AddressRequestDTO`
        *   **Response:** `AddressResponseDTO`
    *   `PUT /api/goatfarms/{farmId}/addresses/{addressId}`
        *   **Descrição:** Atualiza um endereço existente de uma fazenda específica.
        *   **Path Variables:** `farmId` (Long), `addressId` (Long)
        *   **Request Body:** `AddressRequestDTO`
        *   **Response:** `AddressResponseDTO`
    *   `GET /api/goatfarms/{farmId}/addresses/{addressId}`
        *   **Descrição:** Busca um endereço pelo ID dentro de uma fazenda específica.
        *   **Path Variables:** `farmId` (Long), `addressId` (Long)
        *   **Response:** `AddressResponseDTO`
    *   `DELETE /api/goatfarms/{farmId}/addresses/{addressId}`
        *   **Descrição:** Remove um endereço pelo ID de uma fazenda específica.
        *   **Path Variables:** `farmId` (Long), `addressId` (Long)
        *   **Response:** `String` (mensagem de sucesso)
    *   `GET /api/goatfarms/{farmId}/addresses/all` (Note: `farmId` na URL não é usado para filtrar, lista todos os endereços do sistema. Considerar refatorar para `/api/addresses/all` se a intenção é global, ou filtrar por `farmId` se a intenção é específica da fazenda.)
        *   **Descrição:** Lista todos os endereços registrados no sistema.
        *   **Response:** `List<AddressResponseDTO>`

---

#### **4. Módulo `Phone`**

**4.1. `PhoneController`**
*   **Base URL:** `/api/goatfarms/{farmId}/phones`
*   **Endpoints:**
    *   `POST /api/goatfarms/{farmId}/phones`
        *   **Descrição:** Cadastra um novo telefone para uma fazenda específica.
        *   **Path Variable:** `farmId` (Long)
        *   **Request Body:** `PhoneRequestDTO`
        *   **Response:** `PhoneResponseDTO`
    *   `GET /api/goatfarms/{farmId}/phones/{phoneId}`
        *   **Descrição:** Busca um telefone pelo ID dentro de uma fazenda específica.
        *   **Path Variables:** `farmId` (Long), `phoneId` (Long)
        *   **Response:** `PhoneResponseDTO`
    *   `GET /api/goatfarms/{farmId}/phones`
        *   **Descrição:** Lista todos os telefones de uma fazenda específica.
        *   **Path Variable:** `farmId` (Long)
        *   **Response:** `List<PhoneResponseDTO>`
    *   `PUT /api/goatfarms/{farmId}/phones/{phoneId}`
        *   **Descrição:** Atualiza um telefone existente em uma fazenda específica.
        *   **Path Variables:** `farmId` (Long), `phoneId` (Long)
        *   **Request Body:** `PhoneRequestDTO`
        *   **Response:** `PhoneResponseDTO`
    *   `DELETE /api/goatfarms/{farmId}/phones/{phoneId}`
        *   **Descrição:** Remove um telefone existente de uma fazenda específica.
        *   **Path Variables:** `farmId` (Long), `phoneId` (Long)
        *   **Response:** `204 No Content`

---

#### **5. Módulo `Goat`**

**5.1. `GoatController`**
*   **Base URL:** `/api/goatfarms/{farmId}/goats`
*   **Endpoints:**
    *   `POST /api/goatfarms/{farmId}/goats`
        *   **Descrição:** Cadastra uma nova cabra em uma fazenda específica.
        *   **Path Variable:** `farmId` (Long)
        *   **Request Body:** `GoatRequestDTO`
        *   **Response:** `GoatResponseDTO`
    *   `PUT /api/goatfarms/{farmId}/goats/{goatId}`
        *   **Descrição:** Atualiza os dados de uma cabra existente em uma fazenda específica.
        *   **Path Variables:** `farmId` (Long), `goatId` (String)
        *   **Request Body:** `GoatRequestDTO`
        *   **Response:** `GoatResponseDTO`
    *   `DELETE /api/goatfarms/{farmId}/goats/{goatId}`
        *   **Descrição:** Remove uma cabra de uma fazenda específica.
        *   **Path Variables:** `farmId` (Long), `goatId` (String)
        *   **Response:** `204 No Content`
    *   `GET /api/goatfarms/{farmId}/goats/{goatId}`
        *   **Descrição:** Busca uma cabra pelo ID dentro de uma fazenda específica.
        *   **Path Variables:** `farmId` (Long), `goatId` (String)
        *   **Response:** `GoatResponseDTO`
    *   `GET /api/goatfarms/{farmId}/goats`
        *   **Descrição:** Lista todas as cabras de uma fazenda específica (paginado).
        *   **Path Variable:** `farmId` (Long)
        *   **Response:** `Page<GoatResponseDTO>`
    *   `GET /api/goatfarms/{farmId}/goats/search`
        *   **Descrição:** Busca cabras por nome dentro de uma fazenda específica (paginado).
        *   **Path Variable:** `farmId` (Long)
        *   **Query Parameter:** `name` (String)
        *   **Response:** `Page<GoatResponseDTO>`

---

#### **6. Módulo `Event`**

**6.1. `EventController`**
*   **Base URL:** `/api/goatfarms/{farmId}/goats/{goatId}/events`
*   **Endpoints:**
    *   `POST /api/goatfarms/{farmId}/goats/{goatId}/events`
        *   **Descrição:** Cria um novo evento para uma cabra específica em uma fazenda.
        *   **Path Variables:** `farmId` (Long), `goatId` (String)
        *   **Request Body:** `EventRequestDTO`
        *   **Response:** `EventResponseDTO`
    *   `PUT /api/goatfarms/{farmId}/goats/{goatId}/events/{eventId}`
        *   **Descrição:** Atualiza um evento existente de uma cabra em uma fazenda.
        *   **Path Variables:** `farmId` (Long), `goatId` (String), `eventId` (Long)
        *   **Request Body:** `EventRequestDTO`
        *   **Response:** `EventResponseDTO`
    *   `GET /api/goatfarms/{farmId}/goats/{goatId}/events/{eventId}`
        *   **Descrição:** Busca um evento pelo ID de uma cabra em uma fazenda.
        *   **Path Variables:** `farmId` (Long), `goatId` (String), `eventId` (Long)
        *   **Response:** `EventResponseDTO`
    *   `GET /api/goatfarms/{farmId}/goats/{goatId}/events`
        *   **Descrição:** Lista todos os eventos de uma cabra em uma fazenda (paginado).
        *   **Path Variables:** `farmId` (Long), `goatId` (String)
        *   **Response:** `Page<EventResponseDTO>`
    *   `GET /api/goatfarms/{farmId}/goats/{goatId}/events/filter`
        *   **Descrição:** Busca eventos de uma cabra com filtros opcionais em uma fazenda.
        *   **Path Variables:** `farmId` (Long), `goatId` (String)
        *   **Query Parameters:** `eventType` (EventType, opcional), `startDate` (LocalDate, opcional), `endDate` (LocalDate, opcional)
        *   **Response:** `Page<EventResponseDTO>`
    *   `DELETE /api/goatfarms/{farmId}/goats/{goatId}/events/{eventId}`
        *   **Descrição:** Remove um evento de uma cabra em uma fazenda.
        *   **Path Variables:** `farmId` (Long), `goatId` (String), `eventId` (Long)
        *   **Response:** `204 No Content`

---

#### **7. Módulo `Genealogy`**

**7.1. `GenealogyController`**
*   **Base URL:** `/api/goatfarms/{farmId}/goats/{goatId}/genealogies`
*   **Endpoints:**
    *   `GET /api/goatfarms/{farmId}/goats/{goatId}/genealogies`
        *   **Descrição:** Busca a genealogia de uma cabra específica em uma fazenda.
        *   **Path Variables:** `farmId` (Long), `goatId` (String)
        *   **Response:** `GenealogyResponseDTO`
    *   `POST /api/goatfarms/{farmId}/goats/{goatId}/genealogies`
        *   **Descrição:** Cria a genealogia para uma cabra específica em uma fazenda (gerada automaticamente a partir dos dados da cabra).
        *   **Path Variables:** `farmId` (Long), `goatId` (String)
        *   **Response:** `GenealogyResponseDTO`
    *   `POST /api/goatfarms/{farmId}/goats/{goatId}/genealogies/with-data`
        *   **Descrição:** Cria a genealogia para uma cabra específica em uma fazenda, fornecendo dados detalhados.
        *   **Path Variables:** `farmId` (Long), `goatId` (String)
        *   **Request Body:** `GenealogyRequestDTO`
        *   **Response:** `GenealogyResponseDTO`

## 5. Segurança

### Fluxo de Autenticação JWT

1. **Login**: Cliente envia credenciais para `POST /api/auth/login`
2. **Validação**: Spring Security valida email/senha
3. **Geração de Tokens**: `JwtService` gera `accessToken` e `refreshToken`
4. **Resposta**: Retorna tokens e dados do usuário
5. **Uso**: Cliente inclui `Authorization: Bearer <accessToken>` nas requisições

### Configuração de Segurança (SecurityConfig)

O sistema utiliza **múltiplos filtros de segurança** com diferentes ordens:

#### Filtro 1 (Ordem 1) - Endpoints Públicos
```java
@Bean
@Order(1)
public SecurityFilterChain publicSecurityFilterChain(HttpSecurity http) {
    return http
        .securityMatcher("/api/auth/login", "/api/auth/register", "/api/auth/refresh", "/api/auth/register-farm", "/h2-console/**", "/swagger-ui/**", "/v3/api-docs/**")
        .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
        .csrf(csrf -> csrf.disable())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .build();
}
```

#### Filtro 2 (Ordem 2) - Endpoints da API com JWT
```java
@Bean
@Order(2)
public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) {
    return http
        .securityMatcher("/api/**")
        .authorizeHttpRequests(authorize -> authorize
            // Leitura pública
            .requestMatchers(HttpMethod.GET, "/api/goats/**", "/api/genealogies/**", "/api/farms/**", "/api/goatfarms/**").permitAll()
            // Operações administrativas
            .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
            .requestMatchers("/api/users/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_OPERATOR")
            // Operações de modificação
            .requestMatchers(HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE).hasAnyAuthority("ROLE_ADMIN", "ROLE_OPERATOR")
            .anyRequest().authenticated()
        )
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
        .addFilterBefore(jwtDebugFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
}
```

### Roles e Permissões

#### ROLE_ADMIN
- Acesso total ao sistema
- Gerenciamento de usuários
- Gerenciamento de todas as fazendas
- Todas as operações CRUD

#### ROLE_OPERATOR
- Gerenciamento da própria fazenda
- CRUD de animais
- CRUD de genealogias
- Visualização de dados públicos

### Geração e Validação de JWT

#### JwtService
```java
@Service
public class JwtService {
    public String generateToken(User user) {
        String scope = user.getRoles().stream()
            .map(Role::getAuthority)
            .collect(Collectors.joining(" "));
            
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer("goatfarm-api")
            .issuedAt(now)
            .expiresAt(now.plus(24, ChronoUnit.HOURS))
            .subject(user.getEmail())
            .claim("scope", scope)
            .claim("userId", user.getId())
            .claim("name", user.getName())
            .claim("email", user.getEmail())
            .build();
            
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
```

### Chaves RSA
O sistema utiliza chaves RSA para assinar e validar tokens JWT:
- **Chave Privada**: Para assinar tokens (`app.key`)
- **Chave Pública**: Para validar tokens (`app.pub`)

## 6. Tratamento de Erros

### GlobalExceptionHandler

O sistema possui um `@ControllerAdvice` que captura e trata exceções globalmente:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleResourceNotFound(ResourceNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.badRequest().body(errors);
    }
    
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ProblemDetail> handleBadCredentials(BadCredentialsException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problemDetail);
    }
}
```

### Tipos de Exceções Tratadas

- **ResourceNotFoundException**: Retorna HTTP 404 quando recurso não é encontrado
- **MethodArgumentNotValidException**: Retorna HTTP 400 com detalhes de validação
- **BadCredentialsException**: Retorna HTTP 401 para credenciais inválidas
- **DataIntegrityViolationException**: Retorna HTTP 409 para violações de integridade
- **UnauthorizedException**: Retorna HTTP 403 para acesso negado

### Formato de Resposta de Erro

```json
{
  "type": "about:blank",
  "title": "Not Found",
  "status": 404,
  "detail": "Fazenda com ID 999 não encontrada",
  "instance": "/api/goatfarms/999"
}
```

## 7. Persistência e Migrações

### Flyway - Controle de Versão do Banco

O sistema utiliza Flyway para gerenciar a evolução do esquema do banco de dados de forma versionada e controlada.

#### Estrutura de Migrações

**V0__Create_Goat_Table.sql**
```sql
CREATE TABLE goat (
    registration_number VARCHAR(32) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    gender VARCHAR(16),
    breed VARCHAR(32),
    color VARCHAR(32),
    birth_date DATE,
    status VARCHAR(32),
    tod VARCHAR(32),
    toe VARCHAR(32),
    category VARCHAR(32),
    father_registration_number VARCHAR(32),
    mother_registration_number VARCHAR(32),
    farm_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**V1__Create_Security_Tables.sql**
- Criação das tabelas de segurança (`authority`, `role`, `users`)
- Tabelas de relacionamento (`tb_user_role`, `tb_role_authority`)

**V2__Insert_Default_Authorities_And_Roles.sql**
- Inserção de authorities padrão
- Criação de roles (ROLE_ADMIN, ROLE_OPERATOR)
- Associação de authorities às roles

Observação: O sistema utiliza apenas `ROLE_ADMIN` e `ROLE_OPERATOR`. Permissões do proprietário da fazenda são tratadas por verificação de posse (Ownership) e não por um papel dedicado.

**V4__Add_Unique_Constraint_Goat_Registration_Number.sql**
```sql
ALTER TABLE goat ADD CONSTRAINT uk_goat_registration_number UNIQUE (registration_number);
```

**V6__Create_Address_Table.sql**
- Criação da tabela de endereços
- Campos para logradouro completo

**V9__Create_Event_Table.sql**
- Criação da tabela de eventos
- Relacionamento com animais via registration_number

### Constraints Importantes

#### Unicidade
- `users.email`: Email único por usuário
- `users.cpf`: CPF único por usuário
- `goat.registration_number`: Número de registro único por animal
- `role.authority`: Nome da role único

#### Chaves Estrangeiras
- `goat.farm_id` → `goat_farm.id`
- `goat.user_id` → `users.id`
- `goat.father_registration_number` → `goat.registration_number`
- `goat.mother_registration_number` → `goat.registration_number`
- `event.goat_registration_number` → `goat.registration_number`

#### Índices para Performance
```sql
CREATE INDEX idx_goat_farm_id ON goat(farm_id);
CREATE INDEX idx_goat_user_id ON goat(user_id);
CREATE INDEX idx_event_goat_registration_number ON event(goat_registration_number);
```

### Configuração de Ambiente

#### Teste (H2)
```properties
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL
spring.jpa.hibernate.ddl-auto=validate
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
```

#### Desenvolvimento (PostgreSQL)
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/caprigestor_test
spring.jpa.hibernate.ddl-auto=validate
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
```

### Dados de Teste (import.sql)

Por padrão, o `import.sql` está desabilitado. Se necessário, habilite via `spring.sql.init.mode=always` e ajuste o perfil conforme o ambiente. O arquivo contém dados iniciais para desenvolvimento:
- Roles padrão (ROLE_ADMIN, ROLE_OPERATOR)
- Usuários de teste com senhas criptografadas
- Fazendas de exemplo
- Animais com genealogia completa
- Eventos de exemplo

---

## Conclusão

Esta documentação apresenta a arquitetura completa do backend do sistema GoatFarm, destacando:

- **Arquitetura em camadas** bem definida e organizada
- **Segurança robusta** com JWT e controle de acesso baseado em roles
- **Modelagem de dados** consistente com relacionamentos bem estabelecidos
- **API RESTful** com endpoints claros e bem documentados
- **Tratamento de erros** padronizado e informativo
- **Controle de versão do banco** com Flyway
- **Mapeamento automático** com MapStruct para reduzir código boilerplate

O sistema está preparado para evolução e manutenção, seguindo boas práticas de desenvolvimento e arquitetura de software.