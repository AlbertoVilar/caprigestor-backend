# Documentação Técnica - Sistema GoatFarm

## Visão Geral

O **GoatFarm** é um sistema de gestão de caprinocultura desenvolvido em Java com Spring Boot, projetado para gerenciar fazendas de cabras, proprietários, animais, eventos e genealogias. O sistema implementa autenticação OAuth2 com JWT e controle de acesso baseado em roles.

## Arquitetura do Sistema

### Stack Tecnológica

- **Backend**: Java 17, Spring Boot 3.x
- **Framework**: Spring Framework (Web, Security, Data JPA)
- **Banco de Dados**: PostgreSQL (produção), H2 (testes)
- **Autenticação**: OAuth2 + JWT
- **Documentação API**: OpenAPI/Swagger
- **Build**: Maven
- **Validação**: Bean Validation (Hibernate Validator)

### Padrão Arquitetural

O sistema segue uma **Arquitetura Hexagonal** (Ports and Adapters):

```
                    ┌─────────────────┐
                    │   Controllers   │ ← Primary Adapters (REST API)
                    │   (Web Layer)   │
                    └─────────┬───────┘
                              │
                    ┌─────────▼───────┐
                    │     Facades     │ ← Application Layer
                    │ (Orchestration) │
                    └─────────┬───────┘
                              │
              ┌───────────────▼───────────────┐
              │          CORE DOMAIN          │
              │                               │
              │  ┌─────────────────────────┐  │
              │  │    Business Logic       │  │ ← Domain Services
              │  │     (Business)          │  │
              │  └─────────────────────────┘  │
              │                               │
              │  ┌─────────────────────────┐  │
              │  │    Domain Entities      │  │ ← Domain Model
              │  │     (Entities)          │  │
              │  └─────────────────────────┘  │
              └───────────────┬───────────────┘
                              │
                    ┌─────────▼───────┐
                    │      DAOs       │ ← Secondary Ports
                    │ (Domain Ports)  │
                    └─────────┬───────┘
                              │
                    ┌─────────▼───────┐
                    │  Repositories   │ ← Secondary Adapters
                    │ (JPA/Database)  │   (Infrastructure)
                    └─────────────────┘
```

#### Características da Arquitetura Hexagonal:

- **Core Domain**: Isolado de dependências externas
- **Primary Ports**: Interfaces de entrada (Controllers/REST)
- **Secondary Ports**: Interfaces de saída (DAOs/Repositories)
- **Adapters**: Implementações concretas dos ports
- **Dependency Inversion**: Core não depende de infraestrutura

## Modelo de Dados

### Entidades Principais

#### 1. User (Usuário)
```java
@Entity
@Table(name = "users")
public class User implements UserDetails {
    private Long id;
    private String name;
    private String email; // unique
    private String password;
    private Set<Role> roles; // ManyToMany
}
```

#### 2. Role (Perfil)
```java
@Entity
public class Role {
    private Long id;
    private String authority; // ROLE_ADMIN, ROLE_OPERATOR
}
```

#### 3. GoatFarm (Fazenda)
```java
@Entity
@Table(name = "capril")
public class GoatFarm {
    private Long id;
    private String name;
    private String tod; // Título de Origem Definitivo
    private Owner owner; // OneToOne
    private Address address; // OneToOne
    private List<Phone> phones; // OneToMany
    private List<Goat> goats; // OneToMany
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

#### 4. Owner (Proprietário)
```java
@Entity
public class Owner {
    private Long id;
    private String name;
    private String cpf; // unique
    private String email; // unique
}
```

#### 5. Goat (Cabra)
```java
@Entity
@Table(name = "cabras")
public class Goat {
    private String registrationNumber; // PK
    private String name;
    private LocalDate birthDate;
    private GoatBreed breed;
    private String color;
    private GoatGender gender;
    private GoatCategory category;
    private GoatStatus status;
    private String tod;
    private String toe;
    private Goat father; // ManyToOne (self-reference)
    private Goat mother; // ManyToOne (self-reference)
    private Owner owner; // ManyToOne
    private GoatFarm farm; // ManyToOne
}
```

#### 6. Address (Endereço)
```java
@Entity
public class Address {
    private Long id;
    private String street;
    private String neighborhood;
    private String city;
    private String state;
    private String postalCode;
    private String country;
}
```

#### 7. Phone (Telefone)
```java
@Entity
public class Phone {
    private Long id;
    private String ddd;
    private String number;
    private GoatFarm goatFarm; // ManyToOne
}
```

### Relacionamentos

- **User ↔ Role**: ManyToMany (tb_user_role)
- **GoatFarm → Owner**: OneToOne (owner_id)
- **GoatFarm → Address**: OneToOne (address_id)
- **GoatFarm ↔ Phone**: OneToMany (goat_farm_id)
- **GoatFarm ↔ Goat**: OneToMany (capril_id)
- **Goat → Owner**: ManyToOne (owner_id)
- **Goat → Goat**: ManyToOne (pai_id, mae_id) - Auto-relacionamento

## Sistema de Segurança

### Autenticação OAuth2 + JWT

#### Configuração do Authorization Server
- **Grant Type**: Custom Password Grant
- **Token Format**: Self-contained JWT
- **Token Duration**: Configurável (padrão: 86400s = 24h)
- **Client Credentials**: Configurável via environment variables

#### Roles e Permissões
- **ROLE_ADMIN**: Acesso completo ao sistema
- **ROLE_OPERATOR**: Operações básicas (CRUD)

#### Endpoints de Autenticação
```
POST /oauth2/token
Content-Type: application/x-www-form-urlencoded

grant_type=password&
username={email}&
password={password}&
client_id={client_id}&
client_secret={client_secret}
```

### Controle de Acesso

Todos os endpoints são protegidos com `@PreAuthorize`:

```java
@PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_OPERATOR')")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_OPERATOR')")
```

## APIs REST

### 1. User Management

#### Endpoints
```
GET    /users/me                    # Dados do usuário logado
POST   /users                       # Criar usuário
```

### 2. Owner Management

#### Endpoints
```
POST   /owners                      # Criar proprietário
GET    /owners                      # Listar proprietários (paginado)
GET    /owners/{id}                 # Buscar por ID
GET    /owners/search               # Buscar por nome
PUT    /owners/{id}                 # Atualizar proprietário
DELETE /owners/{id}                 # Excluir proprietário
GET    /owners/user/{userId}        # Buscar proprietário por user ID
```

### 3. GoatFarm Management

#### Endpoints
```
POST   /goatfarms                   # Criar fazenda
GET    /goatfarms                   # Listar fazendas (paginado)
GET    /goatfarms/{id}              # Buscar por ID
GET    /goatfarms/search            # Buscar por nome
PUT    /goatfarms/{id}              # Atualizar fazenda
DELETE /goatfarms/{id}              # Excluir fazenda
GET    /goatfarms/{id}/goats        # Listar cabras da fazenda
```

### 4. Goat Management

#### Endpoints
```
POST   /goatfarms/{farmId}/goats           # Criar cabra
GET    /goatfarms/{farmId}/goats           # Listar cabras (paginado)
GET    /goatfarms/{farmId}/goats/{regNum}  # Buscar por registro
PUT    /goatfarms/{farmId}/goats/{regNum}  # Atualizar cabra
DELETE /goatfarms/{farmId}/goats/{regNum} # Excluir cabra
GET    /goats/{regNum}/genealogy           # Genealogia da cabra
```

### 5. Event Management

#### Endpoints
```
POST   /goats/{regNum}/events       # Criar evento
GET    /goats/{regNum}/events       # Listar eventos (com filtros)
GET    /goats/{regNum}/events/{id}  # Buscar evento por ID
PUT    /goats/{regNum}/events/{id}  # Atualizar evento
DELETE /goats/{regNum}/events/{id}  # Excluir evento
```

### 6. Address Management

#### Endpoints
```
POST   /addresses                   # Criar endereço
GET    /addresses                   # Listar endereços
GET    /addresses/{id}              # Buscar por ID
PUT    /addresses/{id}              # Atualizar endereço
DELETE /addresses/{id}              # Excluir endereço
```

### 7. Phone Management

#### Endpoints
```
POST   /phones                      # Criar telefone
GET    /phones                      # Listar telefones
GET    /phones/{id}                 # Buscar por ID
PUT    /phones/{id}                 # Atualizar telefone
DELETE /phones/{id}                 # Excluir telefone
```

## Funcionalidades Implementadas

### 1. Gestão de Usuários
- ✅ Autenticação OAuth2 + JWT
- ✅ Controle de acesso baseado em roles
- ✅ Cadastro de usuários
- ✅ Consulta de dados do usuário logado

### 2. Gestão de Proprietários
- ✅ CRUD completo de proprietários
- ✅ Validação de CPF e email únicos
- ✅ Busca por nome (paginada)
- ✅ Associação usuário-proprietário por email

### 3. Gestão de Fazendas
- ✅ CRUD completo de fazendas
- ✅ Validação de nome e TOD únicos
- ✅ Relacionamento com proprietário e endereço
- ✅ Gestão de telefones associados
- ✅ Busca por nome (paginada)

### 4. Gestão de Cabras
- ✅ CRUD completo de cabras
- ✅ Validação de número de registro único
- ✅ Relacionamento pai/mãe (genealogia)
- ✅ Associação com fazenda e proprietário
- ✅ Busca por número de registro
- ✅ Filtros por fazenda

### 5. Sistema de Eventos
- ✅ Registro de eventos das cabras
- ✅ Tipos de eventos (SAUDE, REPRODUCAO, etc.)
- ✅ Filtros por tipo e período
- ✅ Histórico completo por animal

### 6. Genealogia
- ✅ Árvore genealógica das cabras
- ✅ Relacionamento pai/mãe
- ✅ Consulta de ancestrais

### 7. Gestão de Endereços e Telefones
- ✅ CRUD de endereços
- ✅ CRUD de telefones
- ✅ Associação com fazendas

## Configurações

### Profiles de Ambiente

#### Development (application-dev.properties)
```properties
# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/caprigestor_test
spring.datasource.username=admin
spring.datasource.password=admin123

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Security
security.jwt.duration=86400

# CORS
cors.origins=http://127.0.0.1:5500,http://localhost:5500
```

#### Test (application-test.properties)
```properties
# H2 Database
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=create-drop
spring.sql.init.mode=always
```

### Variáveis de Ambiente
```bash
CLIENT_ID=myclientid
CLIENT_SECRET=myclientsecret
JWT_DURATION=86400
```

## Validações e Regras de Negócio

### Validações Implementadas
1. **CPF único** por proprietário
2. **Email único** por proprietário e usuário
3. **Nome único** por fazenda
4. **TOD único** por fazenda
5. **Número de registro único** por cabra
6. **Telefone único** por fazenda
7. **Associação usuário-proprietário** por email

### Regras de Negócio
1. **Cascade Delete**: Exclusão de fazenda remove proprietário, endereço e telefones
2. **Orphan Removal**: Entidades órfãs são automaticamente removidas
3. **Lazy Loading**: Relacionamentos carregados sob demanda
4. **Auditoria**: Timestamps automáticos (createdAt, updatedAt)
5. **Transações**: Operações críticas são transacionais

## Tratamento de Erros

### Exceções Customizadas
- `ResourceNotFoundException`: Recurso não encontrado
- `DuplicateEntityException`: Violação de unicidade
- `DatabaseException`: Erros de banco de dados
- `ValidationException`: Erros de validação

### Códigos de Status HTTP
- `200 OK`: Operação bem-sucedida
- `201 Created`: Recurso criado
- `400 Bad Request`: Dados inválidos
- `401 Unauthorized`: Não autenticado
- `403 Forbidden`: Sem permissão
- `404 Not Found`: Recurso não encontrado
- `409 Conflict`: Conflito de dados
- `500 Internal Server Error`: Erro interno

## Documentação da API

### Swagger/OpenAPI
A documentação interativa da API está disponível em:
```
http://localhost:8080/swagger-ui.html
```

### Anotações Swagger
- `@Operation`: Descrição do endpoint
- `@Parameter`: Descrição dos parâmetros
- `@ApiResponse`: Códigos de resposta
- `@Tag`: Agrupamento de endpoints

## Testes

### Configuração de Testes
- **Profile**: `test`
- **Database**: H2 in-memory
- **Data**: SQL scripts de inicialização
- **Security**: Configuração específica para testes

## Melhorias Futuras

### Funcionalidades Pendentes
1. **Dashboard**: Métricas e relatórios
2. **Notificações**: Sistema de alertas
3. **Backup**: Rotinas de backup automático
4. **Logs**: Sistema de auditoria completo
5. **Cache**: Implementação de cache Redis
6. **Monitoramento**: Métricas de performance

### Otimizações Técnicas
1. **Paginação**: Otimização de queries grandes
2. **Índices**: Criação de índices de performance
3. **Connection Pool**: Configuração otimizada
4. **Lazy Loading**: Otimização de relacionamentos
5. **DTO Projection**: Redução de dados transferidos

## Conclusão

O sistema GoatFarm implementa uma solução completa para gestão de caprinocultura, com arquitetura robusta, segurança adequada e APIs bem estruturadas. O sistema está preparado para expansão e pode ser facilmente integrado com frontends web ou mobile.

---

**Versão**: 1.0  
**Data**: Janeiro 2025  
**Autor**: Sistema GoatFarm Development Team