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

## 4. Contrato da API (Endpoints)

### AuthController (/api/auth)

#### POST /api/auth/login
**Descrição**: Autentica usuário e retorna tokens JWT
**Permissões**: Público
**DTO de Requisição**:
```json
{
  "email": "usuario@email.com",
  "password": "senha123"
}
```
**DTO de Resposta**:
```json
{
  "accessToken": "eyJhbGciOiJSUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJSUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "user": {
    "id": 1,
    "name": "Nome do Usuário",
    "email": "usuario@email.com",
    "roles": ["ROLE_ADMIN"]
  }
}
```

#### POST /api/auth/register-farm
**Descrição**: Registra nova fazenda com usuário
**Permissões**: Público
**DTO de Requisição**: `GoatFarmFullRequestDTO`
**DTO de Resposta**: `GoatFarmFullResponseDTO`

### UserController (/api/users)

#### GET /api/users
**Descrição**: Lista todos os usuários
**Permissões**: `@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_OPERATOR')")`
**DTO de Resposta**: `List<UserResponseDTO>`

#### GET /api/users/{id}
**Descrição**: Busca usuário por ID
**Permissões**: `@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_OPERATOR')")`
**DTO de Resposta**: `UserResponseDTO`

#### POST /api/users
**Descrição**: Cria novo usuário
**Permissões**: `@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_OPERATOR')")`
**DTO de Requisição**: `UserRequestDTO`
**DTO de Resposta**: `UserResponseDTO`

#### PUT /api/users/{id}
**Descrição**: Atualiza usuário existente
**Permissões**: `@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_OPERATOR')")`
**DTO de Requisição**: `UserRequestDTO`
**DTO de Resposta**: `UserResponseDTO`

#### DELETE /api/users/{id}
**Descrição**: Remove usuário
**Permissões**: `@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_OPERATOR')")`

### GoatFarmController (/api/goatfarms)

#### GET /api/goatfarms
**Descrição**: Lista paginada de fazendas (leitura pública)
**Permissões**: Público
**DTO de Resposta**: `Page<GoatFarmFullResponseDTO>`

#### GET /api/goatfarms/name
**Descrição**: Busca paginada por nome
**Permissões**: Público
**Parâmetros**: `name` (query), `page`, `size`
**DTO de Resposta**: `Page<GoatFarmFullResponseDTO>`

#### GET /api/goatfarms/{id}
**Descrição**: Busca fazenda por ID
**Permissões**: Público
**DTO de Resposta**: `GoatFarmFullResponseDTO`

#### POST /api/goatfarms
**Descrição**: Cria nova fazenda
**Permissões**: `@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_OPERATOR')")`
**DTO de Requisição**: `GoatFarmRequestDTO`
**DTO de Resposta**: `GoatFarmResponseDTO`

#### PUT /api/goatfarms/{id}
**Descrição**: Atualiza fazenda completa
**Permissões**: `@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_OPERATOR')")`
**DTO de Requisição**: `GoatFarmFullRequestDTO`
**DTO de Resposta**: `GoatFarmFullResponseDTO`

#### DELETE /api/goatfarms/{id}
**Descrição**: Remove fazenda
**Permissões**: `@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_OPERATOR')")`

### GoatController (/api/goats)

#### GET /api/goats
**Descrição**: Lista paginada de animais (leitura pública)
**Permissões**: Público
**DTO de Resposta**: `Page<GoatResponseDTO>`

#### GET /api/goats/{registrationNumber}
**Descrição**: Busca animal por número de registro
**Permissões**: Público
**DTO de Resposta**: `GoatResponseDTO`

#### POST /api/goats
**Descrição**: Cadastra novo animal
**Permissões**: `@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_OPERATOR')")`
**DTO de Requisição**: `GoatRequestDTO`
**DTO de Resposta**: `GoatResponseDTO`

#### PUT /api/goats/{registrationNumber}
**Descrição**: Atualiza animal existente
**Permissões**: `@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_OPERATOR')")`
**DTO de Requisição**: `GoatRequestDTO`
**DTO de Resposta**: `GoatResponseDTO`

#### DELETE /api/goats/{registrationNumber}
**Descrição**: Remove animal
**Permissões**: `@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_OPERATOR')")`

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
- Tabelas de relacionamento (`tb_role_authority`, `tb_user_role`)

**V2__Insert_Default_Authorities_And_Roles.sql**
- Inserção de authorities padrão
- Criação de roles (ROLE_ADMIN, ROLE_OPERATOR)
- Associação de authorities às roles

**V3__Convert_Farm_Owner_To_Operator.sql**
- Migração de ROLE_FARM_OWNER para ROLE_OPERATOR
- Atualização de associações existentes

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