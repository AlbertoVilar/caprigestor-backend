<div align="center">

# ðŸ CapriGestor â€“ Backend

### Sistema completo para gestÃ£o de caprinos com arquitetura limpa, segura e escalÃ¡vel

[![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)](https://www.java.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?style=for-the-badge&logo=spring)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?style=for-the-badge&logo=postgresql)](https://www.postgresql.org)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?style=for-the-badge&logo=docker)](https://www.docker.com)

[ðŸ“š Portal de DocumentaÃ§Ã£o](./docs/INDEX.md) â€¢ [ðŸ“‹ Arquitetura](./docs/01-architecture/ARCHITECTURE.md) â€¢ [ðŸ’¼ DomÃ­nio](./docs/00-overview/BUSINESS_DOMAIN.md) â€¢ [ðŸ–¥ï¸ Frontend](https://github.com/albertovilar/caprigestor-frontend) â€¢ [ðŸ“Š Swagger](http://localhost:8080/swagger-ui/index.html)

</div>

---

## ðŸ“Š Status do Projeto

> **Em Desenvolvimento** â€“ MVP previsto para **02/10/2025**

---

## ðŸ“‘ Ãndice

- [Sobre](#sobre-o-projeto)
- [Funcionalidades](#funcionalidades-principais)
- [Tecnologias](#tecnologias-utilizadas)
- [Arquitetura](#arquitetura-e-mÃ³dulos)
- [Diagrama do DomÃ­nio](#diagrama-do-domÃ­nio-mermaid)
- [Diagrama de Classes](#diagrama-de-classes-mermaid)
- [PrÃ©-requisitos](#prÃ©-requisitos)
- [InstalaÃ§Ã£o](#instalaÃ§Ã£o)
- [ConfiguraÃ§Ã£o](#configuraÃ§Ã£o)
- [Perfis de ExecuÃ§Ã£o](#perfis-de-execuÃ§Ã£o)
- [Uso](#como-usar)
- [Banco de Dados](#banco-de-dados)
- [SeguranÃ§a](#seguranÃ§a)
- [API](#api--documentaÃ§Ã£o)
- [Testes](#testes)
- [Docker](#docker)
- [LicenÃ§a](#licenÃ§a)
- [Contato](#contato)
- [Mensageria](#-mensageria-de-eventos-rabbitmq)

---

## ðŸ“– Sobre o Projeto

**CapriGestor** Ã© uma API REST robusta e moderna desenvolvida para o gerenciamento completo de fazendas de caprinos. ConstruÃ­da com **Spring Boot 3** e seguindo os princÃ­pios de **arquitetura hexagonal** (ports & adapters), oferece uma soluÃ§Ã£o escalÃ¡vel, segura e de fÃ¡cil manutenÃ§Ã£o.

### ðŸŽ¯ Objetivo

Fornecer uma plataforma centralizada para criadores de caprinos gerenciarem todos os aspectos de suas fazendas, desde o cadastro de animais atÃ© o rastreamento genealÃ³gico completo e controle de eventos.

---

## âœ¨ Funcionalidades Principais

### ðŸ¡ GestÃ£o de Fazendas
- âœ… Cadastro completo de fazendas com endereÃ§os e telefones
- âœ… Controle de propriedade e ownership
- âœ… Listagem e busca paginadas
- âœ… Gerenciamento de estÃ¡bulos e locais
- âœ… **Logo do Capril:** campo `logoUrl` vÃ¡lido (http/https) em cadastros e atualizaÃ§Ãµes de fazenda para exibir identidade visual

### ðŸ“° Blog e Artigos
- âœ… **MÃ³dulo de Artigos:** endpoints pÃºblicos (`/public/articles`) para listagem de notÃ­cias e dicas
- âœ… GestÃ£o administrativa completa com `ROLE_ADMIN` (detalhes em `docs/02-modules/ARTICLE_BLOG_MODULE.md`)

### ðŸ›¡ï¸ GoatFarm Atomic Registration
O sistema implementa um fluxo de registro estrito e atÃ´mico para garantir consistÃªncia e seguranÃ§a:

- **Domain Rule:** `GoatFarm` Ã© o Aggregate Root. A criaÃ§Ã£o de Fazenda, EndereÃ§o, Telefones e UsuÃ¡rio (no caso anÃ´nimo) Ã© indivisÃ­vel.
- **Fluxo AtÃ´mico:** Uma Ãºnica transaÃ§Ã£o engloba todas as entidades. Se qualquer validaÃ§Ã£o falhar, nada Ã© persistido (Rollback total).
- **Security & Privacy:**
  - **Authenticated Flow:** Se o usuÃ¡rio jÃ¡ estÃ¡ logado, ele se torna automaticamente o *Owner*. Qualquer dado de usuÃ¡rio enviado no payload Ã© ignorado para prevenir *Account Takeover*.
  - **Anonymous Flow:** Cria automaticamente um novo usuÃ¡rio com `ROLE_USER`.
    - Bloqueia envio de campos sensÃ­veis (`roles`, `admin`, `id`).
    - Se o e-mail jÃ¡ existe, retorna erro genÃ©rico para impedir *User Enumeration*.
  - **Anti-Mass Assignment:** DTOs de entrada sÃ£o blindados contra injeÃ§Ã£o de propriedades nÃ£o autorizadas.

### ðŸ GestÃ£o de Animais
- âœ… Cadastro detalhado de caprinos com todas as informaÃ§Ãµes relevantes
- âœ… Rastreamento genealÃ³gico completo (pai, mÃ£e, avÃ³s)
- âœ… VisualizaÃ§Ã£o de Ã¡rvore genealÃ³gica interativa
- âœ… Status e categorizaÃ§Ã£o (PO, PA, PC)
- âœ… Busca avanÃ§ada e filtros

### ðŸ¥› GestÃ£o de ProduÃ§Ã£o Leiteira e LactaÃ§Ã£o
- âœ… **LactaÃ§Ã£o:** Ciclo de vida produtivo (abertura, secagem, status ativo/fechado)
- âœ… **SumÃ¡rio de LactaÃ§Ã£o:** novos endpoints `/active/summary` e `/lactations/{id}/summary` para visÃ£o consolidada
- âœ… **ProduÃ§Ã£o DiÃ¡ria:** Registro de ordenhas por turno (ManhÃ£/Tarde)
- âœ… Controle de volume e observaÃ§Ãµes
- âœ… HistÃ³rico completo de lactaÃ§Ãµes e produÃ§Ãµes
- âœ… ValidaÃ§Ã£o de duplicidade e regras de negÃ³cio

### ðŸ§¬ Regras de NegÃ³cio (Genealogia & ClassificaÃ§Ã£o)

O sistema valida a genealogia com base na classificaÃ§Ã£o do animal:

| ClassificaÃ§Ã£o | DescriÃ§Ã£o | ExigÃªncia de FiliaÃ§Ã£o |
| :--- | :--- | :--- |
| **PO** | *Puro de Origem* | ðŸ”´ **ObrigatÃ³rio** (Pai e MÃ£e) |
| **PC** | *Puro por Cruza* | ðŸ”´ **ObrigatÃ³rio** (Pai e MÃ£e) |
| **PA** | *Puro por AvaliaÃ§Ã£o* | ðŸŸ¢ **Opcional** (Permite cadastro sem filiaÃ§Ã£o) |

> **Nota:** Os genitores (pai/mÃ£e) podem pertencer a **outra fazenda**, permitindo o registro de animais adquiridos de terceiros ou inseminaÃ§Ã£o externa.

### ðŸ“… Eventos e Rastreabilidade
- âœ… Registro de nascimentos, coberturas e partos
- âœ… HistÃ³rico de pesagens
- âœ… HistÃ³rico completo por animal
- âœ… Filtros avanÃ§ados por tipo e perÃ­odo

### ðŸ©º GestÃ£o de SaÃºde (Health Module)
- âœ… **Vacinas e Tratamentos:** Registro completo de eventos sanitÃ¡rios.
- âœ… **Agendamento:** Suporte a eventos agendados (futuros) e realizados.
- âœ… **Status:** Controle de fluxo (AGENDADO, REALIZADO, CANCELADO).
- âœ… **Endpoints:**
  - `POST /api/goatfarms/{farmId}/goats/{goatId}/health-events` (Agendar/Registrar)
  - `PUT /.../health-events/{eventId}` (Editar dados)
  - `PATCH /.../health-events/{eventId}/done` (Marcar como realizado)
  - `PATCH /.../health-events/{eventId}/cancel` (Cancelar evento)
  - `GET /.../health-events/{eventId}` (Detalhes)
  - `GET /.../health-events` (Listagem por animal com filtros de data/status)
  - *Planejado:* Endpoint de calendÃ¡rio geral da fazenda (`listCalendar`).

### ðŸ” Controle de Acesso
- âœ… AutenticaÃ§Ã£o JWT stateless
- âœ… AutorizaÃ§Ã£o baseada em roles (ADMIN, FARM_OWNER, OPERATOR)
- âœ… ProteÃ§Ã£o de endpoints sensÃ­veis
- âœ… IntegraÃ§Ã£o OAuth2

**PermissÃµes por perfil (resumo):**
- `ROLE_ADMIN`: Acesso total ao sistema.
- `ROLE_FARM_OWNER`: Acesso total aos recursos da **prÃ³pria fazenda** (`farmId`).
- `ROLE_OPERATOR`: Acesso operacional restrito Ã s fazendas onde possui vÃ­nculo explÃ­cito.
  - O vÃ­nculo Ã© persistido na tabela `tb_farm_operator`.
  - A validaÃ§Ã£o Ã© feita via `OwnershipService.canManageFarm(farmId)`, garantindo que o operador sÃ³ acesse fazendas permitidas.

**Endpoint de permissÃµes da fazenda:**
- `GET /api/goatfarms/{farmId}/permissions` disponÃ­vel para `ROLE_ADMIN`, `ROLE_OPERATOR` e `ROLE_FARM_OWNER`.

---

## ðŸ› ï¸ Tecnologias Utilizadas

### Core
- **Java 21** â€“ Linguagem de programaÃ§Ã£o moderna e robusta
- **Spring Boot 3.x** â€“ Framework principal para desenvolvimento
- **Spring Security** â€“ SeguranÃ§a e controle de acesso
- **Spring Data JPA** â€“ Camada de persistÃªncia

### Banco de Dados
- **PostgreSQL 16** â€“ Banco de dados relacional principal
- **Flyway** â€“ Controle de versionamento do schema
- **Testcontainers** â€“ Banco efÃªmero para testes de integraÃ§Ã£o
- **H2 Database** â€“ Apenas para testes unitÃ¡rios isolados (opcional)

### SeguranÃ§a
- **JWT (JSON Web Tokens)** â€“ AutenticaÃ§Ã£o stateless
- **OAuth2** â€“ Protocolo de autorizaÃ§Ã£o

### DocumentaÃ§Ã£o e Testes
- **Swagger/OpenAPI** â€“ DocumentaÃ§Ã£o interativa da API
- **JUnit 5** â€“ Framework de testes
- **Mockito** â€“ Mocks para testes unitÃ¡rios
- **Testcontainers** â€“ Infraestrutura de testes robusta

### DevOps
- **Docker** â€“ ContainerizaÃ§Ã£o
- **Docker Compose** â€“ OrquestraÃ§Ã£o de containers
- **Maven** â€“ Gerenciamento de dependÃªncias e build

---

## ðŸ—ï¸ Arquitetura e MÃ³dulos

O projeto segue a **arquitetura hexagonal** (ports & adapters), garantindo baixo acoplamento e alta coesÃ£o.

### ðŸ“¦ Estrutura de Camadas

```
domain â†’ application â†’ infrastructure
```

### ðŸ—‚ï¸ MÃ³dulos

| MÃ³dulo | DescriÃ§Ã£o |
|--------|-----------|
| **goat** | Regras de negÃ³cio e acesso a dados de caprinos |
| **reproduction** | Ciclo reprodutivo (coberturas, gestaÃ§Ãµes, eventos reprodutivos) |
| **milk** | GestÃ£o de produÃ§Ã£o de leite e lactaÃ§Ãµes |
| **events** | GestÃ£o de eventos (nascimentos, coberturas, pesagens, etc.) |
| **health** | GestÃ£o sanitÃ¡ria e veterinÃ¡ria (vacinas, tratamentos) |
| **genealogy** | Relacionamento e linhagem (ProjeÃ§Ã£o On-Demand) |
| **farm** | Entidades e serviÃ§os de fazendas/estÃ¡bulos/locais |
| **address** | GestÃ£o de endereÃ§os e localizaÃ§Ãµes |
| **phone** | GestÃ£o de contatos telefÃ´nicos |
| **article** | Blog e gerenciamento de conteÃºdo informativo |
| **authority** | AutenticaÃ§Ã£o, autorizaÃ§Ã£o, usuÃ¡rios e papÃ©is |
| **shared** | UtilitÃ¡rios, DTOs comuns, exceÃ§Ãµes e infra compartilhada |

### ðŸ§  Filosofia Arquitetural (Hexagonal)

- PrincÃ­pios: inversÃ£o de dependÃªncias, isolamento do domÃ­nio e Portas & Adaptadores.
- ConvenÃ§Ã£o pragmÃ¡tica de nomes mapeada para hexagonal:
  - Controller â†’ Adaptador de Entrada (Driving Adapter)
  - UseCase / Port â†’ Porta de Entrada (Input Port)
  - Business â†’ ServiÃ§o de AplicaÃ§Ã£o (Implementa Input Port)
  - Output Port â†’ Porta de SaÃ­da (Interface para Infraestrutura)
  - Adapter / Repository â†’ Adaptador de SaÃ­da (Driven Adapter)

### âœ… Regra de DependÃªncia (Hexagonal)

- `business` **nunca** importa `api.*`.
- Mapeamentos separados: `api.mapper` (DTO â†” VO) e `business.mapper` (VO â†” Entity).
- **Health** Ã© a referÃªncia de implementaÃ§Ã£o.
- Gate automÃ¡tico: `HexagonalArchitectureGuardTest` falha o build em qualquer regressÃ£o.

---

## ðŸ§­ Diagrama do DomÃ­nio (Mermaid)

```mermaid
erDiagram
  USER ||--o{ USER_ROLE : has
  USER_ROLE }o--|| ROLE : belongs_to
  USER ||--o{ GOAT_FARM : owns
  GOAT_FARM ||--|| ADDRESS : has
  GOAT_FARM ||--o{ PHONE : has
  GOAT_FARM ||--o{ GOAT : hosts
  GOAT ||--o{ EVENT : has
  GOAT ||--o{ LACTATION : has
  GOAT ||--o{ MILK_PRODUCTION : produces
  GOAT ||--o| GOAT : father
  GOAT ||--o| GOAT : mother

  USER {
    int id PK
    string username
    string email
    string password
    boolean enabled
  }

  ROLE {
    int id PK
    string name
    string description
  }

  USER_ROLE {
    int id PK
    int user_id FK
    int role_id FK
  }

  GOAT_FARM {
    int id PK
    string name
    int owner_user_id FK
    int address_id FK
    datetime created_at
    datetime updated_at
  }

  ADDRESS {
    int id PK
    string street
    string number
    string neighborhood
    string city
    string state
    string zip_code
    string country
  }

  PHONE {
    int id PK
    string ddd
    string number
    string type
    int farm_id FK
  }

  GOAT {
    string registration_number PK
    string name
    string gender
    date birth_date
    string color
    string notes
    string status
    string classification
    int farm_id FK
    string father_id
    string mother_id
  }

  EVENT {
    int id PK
    string event_type
    date event_date
    string observation
    string goat_registration_number FK
    int farm_id FK
  }

  LACTATION {
    int id PK
    date start_date
    date end_date
    string status
    int goat_id FK
    int farm_id FK
  }

  MILK_PRODUCTION {
    int id PK
    date date
    string shift
    float volume_liters
    int goat_id FK
    int farm_id FK
  }
  
  PREGNANCY {
    int id PK
    int farm_id FK
    string goat_id FK
    string status
    date breeding_date
    date confirm_date
    date expected_due_date
    date closed_at
    string close_reason
  }

  REPRODUCTIVE_EVENT {
    int id PK
    int farm_id FK
    string goat_id FK
    int pregnancy_id FK
    string event_type
    date event_date
    string breeding_type
    string breeder_ref
    string notes
    date check_scheduled_date
    string check_result
  }

  FARM_OPERATOR {
    int id PK
    int farm_id FK
    int user_id FK
    datetime created_at
  }

  HEALTH_EVENT {
    int id PK
    int farm_id FK
    string goat_id FK
    string event_type
    date date
    string status
    string notes
    string cost
  }

  GOAT ||--o{ PREGNANCY : has
  GOAT ||--o{ REPRODUCTIVE_EVENT : has
  GOAT_FARM ||--o{ PREGNANCY : hosts
  GOAT_FARM ||--o{ REPRODUCTIVE_EVENT : hosts
  PREGNANCY ||--o{ REPRODUCTIVE_EVENT : lifecycle
  
  GOAT_FARM ||--o{ FARM_OPERATOR : has_operators
  USER ||--o{ FARM_OPERATOR : is_operator_at
  GOAT ||--o{ HEALTH_EVENT : has
  GOAT_FARM ||--o{ HEALTH_EVENT : records
```

---

## ðŸ§© Diagrama de Classes (Mermaid)

```mermaid
classDiagram
    %% ========== MÃ“DULO FARM ==========
    class GoatFarm {
        +Long id
        +String name
        +Long ownerId
        +Long addressId
    }

    class Address {
        +Long id
        +String street
        +String city
        +String state
    }

    class Phone {
        +Long id
        +String number
        +PhoneType type
    }

    %% ========== MÃ“DULO AUTHORITY ==========
    class User {
        +Long id
        +String email
        +boolean enabled
    }

    class Role {
        <<enumeration>>
        ADMIN
        OPERATOR
    }

    %% ========== MÃ“DULO GOAT ==========
    class Goat {
        +String registrationNumber
        +String name
        +Gender gender
        +GoatLifeStatus status
        +GoatClassification classification
    }

    class Gender {
        <<enumeration>>
        MALE
        FEMALE
    }

    %% ========== MÃ“DULO REPRODUCTION ==========
    class Pregnancy {
        +Long id
        +Long farmId
        +String goatId
        +PregnancyStatus status
        +LocalDate breedingDate
        +LocalDate confirmDate
        +LocalDate expectedDueDate
        +LocalDate closedAt
        +PregnancyCloseReason closeReason
    }

    class ReproductiveEvent {
        +Long id
        +Long farmId
        +String goatId
        +Long pregnancyId
        +ReproductiveEventType eventType
        +LocalDate eventDate
        +BreedingType breedingType
    }

    class PregnancyStatus {
        <<enumeration>>
        ACTIVE
        CLOSED
    }

    class PregnancyCloseReason {
        <<enumeration>>
        BIRTH
        LOST
        ABORTED
        DATA_FIX_DUPLICATED_ACTIVE
    }

    class ReproductiveEventType {
        <<enumeration>>
        COVERAGE
        PREGNANCY_CHECK
        PREGNANCY_CLOSE
    }

    class BreedingType {
        <<enumeration>>
        NATURAL
        AI
    }

    %% ========== MÃ“DULO MILK ==========
    class Lactation {
        +Long id
        +LocalDate startDate
        +LocalDate endDate
        +LactationStatus status
    }

    class MilkProduction {
        +Long id
        +LocalDate date
        +String shift
        +double volumeLiters
    }

    class LactationStatus {
        <<enumeration>>
        ACTIVE
        CLOSED
    }

    %% ========== RELACIONAMENTOS ==========
    GoatFarm "1" --> "1" Address : possui
    GoatFarm "1" --> "0..*" Phone : tem
    GoatFarm "1" --> "0..*" Goat : gerencia
    Goat "1" --> "0..*" Lactation : possui
    Goat "1" --> "0..*" MilkProduction : produz
    Goat "1" --> "0..*" Pregnancy : gestacoes
    Goat "1" --> "0..*" ReproductiveEvent : eventosReprodutivos
    Pregnancy "1" --> "0..*" ReproductiveEvent : eventos
    User "1" --> "0..*" GoatFarm : possui
```

---

## ðŸ“‹ PrÃ©-requisitos

Antes de comeÃ§ar, certifique-se de ter instalado:

- â˜• **Java 21** ou superior
- ðŸ”§ **Maven 3.8+** (ou use o wrapper incluÃ­do)
- ðŸ³ **Docker & Docker Compose** (obrigatÃ³rio para banco de dados e mensageria)
- ðŸ’» **IDE**: IntelliJ IDEA, Eclipse ou VS Code

---

## ðŸš€ InstalaÃ§Ã£o

1ï¸âƒ£ **Clone o repositÃ³rio**
```bash
git clone https://github.com/albertovilar/caprigestor-backend.git
cd caprigestor-backend
```

2ï¸âƒ£ **Subir infraestrutura (PostgreSQL + RabbitMQ)**
```bash
cd docker
docker compose up -d
```

## ðŸ§ª Testes

> **Nota sobre Warnings:** Ã‰ comum ver avisos do "Mockito inline agent" (Byte Buddy) durante a execuÃ§Ã£o dos testes em Java 21+. Isso nÃ£o afeta o resultado. Para suprimir, use a flag `-XX:+EnableDynamicAgentLoading`.

O projeto possui uma suÃ­te robusta de testes:

- **Testes UnitÃ¡rios:** Cobrem as regras de negÃ³cio (`*Business`), isolados de dependÃªncias externas (banco, web). Organizados espelhando a estrutura de pacotes de `src/main`.
- **Testes de IntegraÃ§Ã£o:** Validam o fluxo completo, incluindo banco de dados e endpoints REST (`@SpringBootTest`).
- **Arquitetura:** Testes que garantem a integridade da Arquitetura Hexagonal (dependÃªncias corretas entre camadas).

Para executar os testes:
```bash
./mvnw clean test
```

---

## ðŸ³ Docker

Para subir o ambiente completo (Banco de Dados + RabbitMQ):

```bash
cd docker
docker compose up -d
```
> **ServiÃ§os:**
> - PostgreSQL: `localhost:5432`
> - RabbitMQ UI: `http://localhost:15672` (admin/admin)
> - PgAdmin: `http://localhost:8081`

---

## âš™ï¸ ConfiguraÃ§Ã£o

### Filosofia dos Perfis
O projeto adota uma estratÃ©gia estrita de perfis para evitar configuraÃ§Ãµes implÃ­citas e garantir consistÃªncia entre ambientes.

- **`default`**: Apenas configuraÃ§Ãµes bÃ¡sicas (logging, jackson). **NÃ£o conecta ao banco.**
- **`dev`**: Ambiente de desenvolvimento. Conecta ao **PostgreSQL local** e roda **Flyway**.
- **`test`**: Ambiente de testes. Usa **Testcontainers** para subir um banco efÃªmero.
- **`prod`**: Ambiente de produÃ§Ã£o. ConfiguraÃ§Ãµes via variÃ¡veis de ambiente.

---

## ðŸ’» Perfis de ExecuÃ§Ã£o

O projeto estÃ¡ configurado para usar o perfil `dev` por padrÃ£o para facilitar o desenvolvimento.

| Perfil | Uso | Banco de Dados | Flyway | DDL Auto |
|--------|-----|----------------|--------|----------|
| `dev` | Desenvolvimento (PadrÃ£o) | PostgreSQL (Docker) | âœ… Habilitado | `validate` |
| `test` | Testes Automatizados | Testcontainers | âœ… Habilitado | `validate` |
| `prod` | ProduÃ§Ã£o | PostgreSQL (AWS/Cloud) | âœ… Habilitado | `validate` |
| `default` | Base | âŒ Nenhum | âŒ Desabilitado | `none` |

### â–¶ï¸ Como Executar (Modo Dev)

**Via Maven Wrapper (Simples):**
O perfil `dev` Ã© ativado automaticamente.
```bash
# Windows (PowerShell)
./mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

**Via Maven (ExplÃ­cito):**
Caso queira forÃ§ar um perfil especÃ­fico:
```bash
./mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

**Via JAR:**
```bash
java -jar target/CapriGestor-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

---

## ðŸ’» Como Usar

ApÃ³s iniciar com o perfil `dev`, a API estarÃ¡ disponÃ­vel em:

- **API Base:** `http://localhost:8080/api`
- **Swagger UI:** `http://localhost:8080/swagger-ui/index.html`

> âš ï¸ **Importante:** A maioria das operaÃ§Ãµes requer autenticaÃ§Ã£o via Bearer Token e os dados sÃ£o isolados por `farmId`.

---

## ðŸ—„ï¸ Banco de Dados

### Versionamento (Flyway)
Todo o schema do banco Ã© gerenciado pelo **Flyway**.
- Migrations em: `src/main/resources/db/migration`
- O Hibernate **apenas valida** o schema (`ddl-auto=validate`), nunca o altera.

### H2 Database
O H2 Ã© utilizado em dois cenÃ¡rios:
1.  **Testes UnitÃ¡rios**: ExecuÃ§Ã£o rÃ¡pida e isolada.
2.  **Smoke Tests**: ValidaÃ§Ã£o rÃ¡pida do build (`profile: smoke`), permitindo rodar a aplicaÃ§Ã£o em memÃ³ria sem depender do Docker.

### Flyway V16 â€“ banco sujo com ACTIVE duplicada

A migration `V16` cria um Ã­ndice Ãºnico para garantir apenas **uma gestaÃ§Ã£o ativa por cabra**. Em bancos de dados "sujos" (com duplicatas existentes), essa migration falharÃ¡.

O fluxo recomendado Ã© totalmente manual e estÃ¡ documentado em:
- `src/main/resources/db/manual/datafix_duplicate_active_pregnancy.sql`  
  (contÃ©m **diagnÃ³stico**, **fix seguro** e **verificaÃ§Ã£o final**)

**Procedimento de CorreÃ§Ã£o (ambiente dev com PostgreSQL Docker):**

1.  **Rodar diagnÃ³stico (verificar se hÃ¡ duplicidades):**

    ```sql
    SELECT farm_id, goat_id, COUNT(*) AS active_count
    FROM pregnancy
    WHERE status = 'ACTIVE'
    GROUP BY farm_id, goat_id
    HAVING COUNT(*) > 1;
    ```

    - Se o resultado vier vazio, nÃ£o hÃ¡ problema para a V16.
    - Se houver linhas, existem gestaÃ§Ãµes `ACTIVE` duplicadas que precisam ser corrigidas.

    Exemplo usando o container padrÃ£o do projeto:

    ```bash
    docker exec -it caprigestor-postgres \
      psql -U admin -d caprigestor_test \
      -c "SELECT farm_id, goat_id, COUNT(*) AS active_count FROM pregnancy WHERE status = 'ACTIVE' GROUP BY farm_id, goat_id HAVING COUNT(*) > 1;"
    ```

2.  **Executar Data Fix (fechar gestaÃ§Ãµes duplicadas mais antigas):**

    - Execute o script manual em `src/main/resources/db/manual/datafix_duplicate_active_pregnancy.sql`
      diretamente no banco (via `psql`, PgAdmin ou outra ferramenta SQL).
    - O script mantÃ©m apenas a gestaÃ§Ã£o `ACTIVE` mais recente por `(farm_id, goat_id)` e fecha as demais.

3.  **Rodar verificaÃ§Ã£o final:**

    - Reexecute o SELECT de diagnÃ³stico (ou o bloco **C) VerificaÃ§Ã£o final** do script manual).
    - O resultado deve estar vazio antes de subir a aplicaÃ§Ã£o.

4.  **Subir aplicaÃ§Ã£o normalmente:**

    - Com o banco jÃ¡ corrigido, a aplicaÃ§Ã£o subirÃ¡ e o Flyway aplicarÃ¡ a `V16` com sucesso.

---

## ðŸ” SeguranÃ§a

- **OAuth2 + JWT:** AutenticaÃ§Ã£o stateless robusta.
- **Roles:**
  - `ROLE_ADMIN`: Acesso total.
  - `ROLE_OPERATOR`: Acesso operacional Ã  fazenda vinculada.
- **Header ObrigatÃ³rio:**
  ```http
  Authorization: Bearer <seu-token-jwt>
  ```

---

## ðŸ§ª Testes

Os testes de integraÃ§Ã£o sobem a aplicaÃ§Ã£o completa usando **Testcontainers** para garantir fidelidade ao ambiente real.

**Executar todos os testes:**
```bash
# Windows
./mvnw.cmd test

# Linux/Mac
./mvnw test
```
> *Nota: Ã‰ necessÃ¡rio ter o Docker rodando para que os Testcontainers funcionem.*

---

## ðŸ³ Docker

Para subir todo o ecossistema (App + Banco + Mensageria):

```bash
cd docker
docker compose up -d
```

| ServiÃ§o | Porta | DescriÃ§Ã£o |
|---------|-------|-----------|
| API | 8080 | Backend Spring Boot |
| PostgreSQL | 5432 | Banco de Dados |
| RabbitMQ | 5672 | Mensageria (AMQP) |
| RabbitMQ UI | 15672 | Painel de GestÃ£o |
| PgAdmin | 8081 | GestÃ£o Visual do Banco |

---

## ðŸ“¨ Mensageria de Eventos (RabbitMQ)

O sistema utiliza RabbitMQ para processamento assÃ­ncrono de eventos (nascimentos, atualizaÃ§Ãµes), garantindo desacoplamento.

- **Exchange:** `events-exchange`
- **Fila:** `events-queue`
- **Routing Key:** `event.created`

Para monitorar, acesse o painel do RabbitMQ em `http://localhost:15672` (User/Pass: `admin`/`admin`).

---

## ðŸ“„ LicenÃ§a

Este projeto Ã© proprietÃ¡rio. Todos os direitos reservados.

---

## ðŸ‘¤ Contato

**JosÃ© Alberto Vilar Pereira**

ðŸ“§ Email: albertovilar1@gmail.com
ðŸ’¼ LinkedIn: [Alberto Vilar](https://www.linkedin.com/in/alberto-vilar-316725ab)
ðŸ™ GitHub: [@albertovilar](https://github.com/albertovilar)

<div align="center">
Desenvolvido com â˜• e â¤ï¸ por Alberto Vilar

â­ Se este projeto foi Ãºtil para vocÃª, considere dar uma estrela!
</div>
