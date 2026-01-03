<div align="center">

# 🐐 CapriGestor – Backend

### Sistema completo para gestão de caprinos com arquitetura limpa, segura e escalável

[![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)](https://www.java.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?style=for-the-badge&logo=spring)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?style=for-the-badge&logo=postgresql)](https://www.postgresql.org)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?style=for-the-badge&logo=docker)](https://www.docker.com)

[📋 Documentação Técnica](./DOCUMENTACAO_BACKEND.md) • [🖥️ Frontend](https://github.com/albertovilar/caprigestor-frontend) • [📊 Swagger](http://localhost:8080/swagger-ui/index.html)

</div>

---

## 📊 Status do Projeto

> **Em Desenvolvimento** – MVP previsto para **02/10/2025**

---

## 📑 Índice

- [Sobre](#sobre-o-projeto)
- [Funcionalidades](#funcionalidades-principais)
- [Tecnologias](#tecnologias-utilizadas)
- [Arquitetura](#arquitetura-e-módulos)
- [Diagrama do Domínio](#diagrama-do-domínio-mermaid)
- [Diagrama de Classes](#diagrama-de-classes-mermaid)
- [Pré-requisitos](#pré-requisitos)
- [Instalação](#instalação)
- [Configuração](#configuração)
- [Perfis de Execução](#perfis-de-execução)
- [Uso](#como-usar)
- [Banco de Dados](#banco-de-dados)
- [Segurança](#segurança)
- [API](#api--documentação)
- [Testes](#testes)
- [Docker](#docker)
- [Licença](#licença)
- [Contato](#contato)
- [Mensageria](#-mensageria-de-eventos-rabbitmq)

---

## 📖 Sobre o Projeto

**CapriGestor** é uma API REST robusta e moderna desenvolvida para o gerenciamento completo de fazendas de caprinos. Construída com **Spring Boot 3** e seguindo os princípios de **arquitetura hexagonal** (ports & adapters), oferece uma solução escalável, segura e de fácil manutenção.

### 🎯 Objetivo

Fornecer uma plataforma centralizada para criadores de caprinos gerenciarem todos os aspectos de suas fazendas, desde o cadastro de animais até o rastreamento genealógico completo e controle de eventos.

---

## ✨ Funcionalidades Principais

### 🏡 Gestão de Fazendas
- ✅ Cadastro completo de fazendas com endereços e telefones
- ✅ Controle de propriedade e ownership
- ✅ Listagem e busca paginadas
- ✅ Gerenciamento de estábulos e locais

### 🛡️ GoatFarm Atomic Registration
O sistema implementa um fluxo de registro estrito e atômico para garantir consistência e segurança:

- **Domain Rule:** `GoatFarm` é o Aggregate Root. A criação de Fazenda, Endereço, Telefones e Usuário (no caso anônimo) é indivisível.
- **Fluxo Atômico:** Uma única transação engloba todas as entidades. Se qualquer validação falhar, nada é persistido (Rollback total).
- **Security & Privacy:**
  - **Authenticated Flow:** Se o usuário já está logado, ele se torna automaticamente o *Owner*. Qualquer dado de usuário enviado no payload é ignorado para prevenir *Account Takeover*.
  - **Anonymous Flow:** Cria automaticamente um novo usuário com `ROLE_USER`.
    - Bloqueia envio de campos sensíveis (`roles`, `admin`, `id`).
    - Se o e-mail já existe, retorna erro genérico para impedir *User Enumeration*.
  - **Anti-Mass Assignment:** DTOs de entrada são blindados contra injeção de propriedades não autorizadas.

### 🐐 Gestão de Animais
- ✅ Cadastro detalhado de caprinos com todas as informações relevantes
- ✅ Rastreamento genealógico completo (pai, mãe, avós)
- ✅ Visualização de árvore genealógica interativa
- ✅ Status e categorização (PO, PA, PC)
- ✅ Busca avançada e filtros

### 🧬 Regras de Negócio (Genealogia & Classificação)

O sistema valida a genealogia com base na classificação do animal:

| Classificação | Descrição | Exigência de Filiação |
| :--- | :--- | :--- |
| **PO** | *Puro de Origem* | 🔴 **Obrigatório** (Pai e Mãe) |
| **PC** | *Puro por Cruza* | 🔴 **Obrigatório** (Pai e Mãe) |
| **PA** | *Puro por Avaliação* | 🟢 **Opcional** (Permite cadastro sem filiação) |

> **Nota:** Os genitores (pai/mãe) podem pertencer a **outra fazenda**, permitindo o registro de animais adquiridos de terceiros ou inseminação externa.

### 🔐 Controle de Acesso
- ✅ Autenticação JWT stateless
- ✅ Autorização baseada em roles (ADMIN, OPERATOR)
- ✅ Proteção de endpoints sensíveis
- ✅ Integração OAuth2

### 📅 Eventos e Rastreabilidade
- ✅ Registro de nascimentos, coberturas e partos
- ✅ Controle de vacinações e tratamentos
- ✅ Histórico de pesagens
- ✅ Histórico completo por animal
- ✅ Filtros avançados por tipo e período

---

## 🛠️ Tecnologias Utilizadas

### Core
- **Java 21** – Linguagem de programação moderna e robusta
- **Spring Boot 3.x** – Framework principal para desenvolvimento
- **Spring Security** – Segurança e controle de acesso
- **Spring Data JPA** – Camada de persistência

### Banco de Dados
- **PostgreSQL 16** – Banco de dados relacional principal
- **Flyway** – Controle de versionamento do schema
- **Testcontainers** – Banco efêmero para testes de integração
- **H2 Database** – Apenas para testes unitários isolados (opcional)

### Segurança
- **JWT (JSON Web Tokens)** – Autenticação stateless
- **OAuth2** – Protocolo de autorização

### Documentação e Testes
- **Swagger/OpenAPI** – Documentação interativa da API
- **JUnit 5** – Framework de testes
- **Mockito** – Mocks para testes unitários
- **Testcontainers** – Infraestrutura de testes robusta

### DevOps
- **Docker** – Containerização
- **Docker Compose** – Orquestração de containers
- **Maven** – Gerenciamento de dependências e build

---

## 🏗️ Arquitetura e Módulos

O projeto segue a **arquitetura hexagonal** (ports & adapters), garantindo baixo acoplamento e alta coesão.

### 📦 Estrutura de Camadas

```
domain → application → infrastructure
```

### 🗂️ Módulos

| Módulo | Descrição |
|--------|-----------|
| **goat** | Regras de negócio e acesso a dados de caprinos |
| **events** | Gestão de eventos (nascimentos, coberturas, pesagens, etc.) |
| **genealogy** | Relacionamento e linhagem (Projeção On-Demand) |
| **farm** | Entidades e serviços de fazendas/estábulos/locais |
| **authority** | Autenticação, autorização, usuários e papéis |
| **shared** | Utilitários, DTOs comuns, exceções e infra compartilhada |

### 🧠 Filosofia Arquitetural (Hexagonal)

- Princípios: inversão de dependências, isolamento do domínio e Portas & Adaptadores.
- Convenção pragmática de nomes mapeada para hexagonal:
  - Controller → Adaptador de Entrada (Driving Adapter)
  - UseCase / Port → Porta de Entrada (Input Port)
  - Business → Serviço de Aplicação (Implementa Input Port)
  - Output Port → Porta de Saída (Interface para Infraestrutura)
  - Adapter / Repository → Adaptador de Saída (Driven Adapter)

---

## 🧭 Diagrama do Domínio (Mermaid)

```mermaid
erDiagram
  USER ||--o{ USER_ROLE : has
  USER_ROLE }o--|| ROLE : belongs_to
  USER ||--o{ GOAT_FARM : owns
  GOAT_FARM ||--|| ADDRESS : has
  GOAT_FARM ||--o{ PHONE : has
  GOAT_FARM ||--o{ GOAT : hosts
  GOAT ||--o{ EVENT : has
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
```

---

## 🧩 Diagrama de Classes (Mermaid)

```mermaid
classDiagram
    %% ========== MÓDULO FARM ==========
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

    %% ========== MÓDULO AUTHORITY ==========
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

    %% ========== MÓDULO GOAT ==========
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

    %% ========== RELACIONAMENTOS ==========
    GoatFarm "1" --> "1" Address : possui
    GoatFarm "1" --> "0..*" Phone : tem
    GoatFarm "1" --> "0..*" Goat : gerencia
    User "1" --> "0..*" GoatFarm : possui
```

---

## 📋 Pré-requisitos

Antes de começar, certifique-se de ter instalado:

- ☕ **Java 21** ou superior
- 🔧 **Maven 3.8+** (ou use o wrapper incluído)
- 🐳 **Docker & Docker Compose** (obrigatório para banco de dados e mensageria)
- 💻 **IDE**: IntelliJ IDEA, Eclipse ou VS Code

---

## 🚀 Instalação

1️⃣ **Clone o repositório**
```bash
git clone https://github.com/albertovilar/caprigestor-backend.git
cd caprigestor-backend
```

2️⃣ **Subir infraestrutura (PostgreSQL + RabbitMQ)**
```bash
## 🧪 Testes

O projeto possui uma suíte robusta de testes:

- **Testes Unitários:** Cobrem as regras de negócio (`*Business`), isolados de dependências externas (banco, web). Organizados espelhando a estrutura de pacotes de `src/main`.
- **Testes de Integração:** Validam o fluxo completo, incluindo banco de dados e endpoints REST (`@SpringBootTest`).
- **Arquitetura:** Testes que garantem a integridade da Arquitetura Hexagonal (dependências corretas entre camadas).

Para executar os testes:
```bash
./mvnw clean test
```

---

## 🐳 Docker

Para subir o ambiente completo (Banco de Dados + RabbitMQ):

```bash
cd docker
docker compose up -d
```
> **Serviços:**
> - PostgreSQL: `localhost:5432`
> - RabbitMQ UI: `http://localhost:15672` (admin/admin)
> - PgAdmin: `http://localhost:8081`

---

## ⚙️ Configuração

### Filosofia dos Perfis
O projeto adota uma estratégia estrita de perfis para evitar configurações implícitas e garantir consistência entre ambientes.

- **`default`**: Apenas configurações básicas (logging, jackson). **Não conecta ao banco.**
- **`dev`**: Ambiente de desenvolvimento. Conecta ao **PostgreSQL local** e roda **Flyway**.
- **`test`**: Ambiente de testes. Usa **Testcontainers** para subir um banco efêmero.
- **`prod`**: Ambiente de produção. Configurações via variáveis de ambiente.

---

## 💻 Perfis de Execução

O projeto está configurado para usar o perfil `dev` por padrão para facilitar o desenvolvimento.

| Perfil | Uso | Banco de Dados | Flyway | DDL Auto |
|--------|-----|----------------|--------|----------|
| `dev` | Desenvolvimento (Padrão) | PostgreSQL (Docker) | ✅ Habilitado | `validate` |
| `test` | Testes Automatizados | Testcontainers | ✅ Habilitado | `validate` |
| `prod` | Produção | PostgreSQL (AWS/Cloud) | ✅ Habilitado | `validate` |
| `default` | Base | ❌ Nenhum | ❌ Desabilitado | `none` |

### ▶️ Como Executar (Modo Dev)

**Via Maven Wrapper (Simples):**
O perfil `dev` é ativado automaticamente.
```bash
# Windows (PowerShell)
./mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

**Via Maven (Explícito):**
Caso queira forçar um perfil específico:
```bash
./mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

**Via JAR:**
```bash
java -jar target/CapriGestor-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

---

## 💻 Como Usar

Após iniciar com o perfil `dev`, a API estará disponível em:

- **API Base:** `http://localhost:8080/api`
- **Swagger UI:** `http://localhost:8080/swagger-ui/index.html`

> ⚠️ **Importante:** A maioria das operações requer autenticação via Bearer Token e os dados são isolados por `farmId`.

---

## 🗄️ Banco de Dados

### Versionamento (Flyway)
Todo o schema do banco é gerenciado pelo **Flyway**.
- Migrations em: `src/main/resources/db/migration`
- O Hibernate **apenas valida** o schema (`ddl-auto=validate`), nunca o altera.

### H2 Database
O H2 **não é usado** como banco de desenvolvimento para evitar divergências de SQL. Ele é reservado estritamente para testes unitários muito específicos, se necessário.

---

## 🔐 Segurança

- **OAuth2 + JWT:** Autenticação stateless robusta.
- **Roles:**
  - `ROLE_ADMIN`: Acesso total.
  - `ROLE_OPERATOR`: Acesso operacional à fazenda vinculada.
- **Header Obrigatório:**
  ```http
  Authorization: Bearer <seu-token-jwt>
  ```

---

## 🧪 Testes

Os testes de integração sobem a aplicação completa usando **Testcontainers** para garantir fidelidade ao ambiente real.

**Executar todos os testes:**
```bash
# Windows
./mvnw.cmd test

# Linux/Mac
./mvnw test
```
> *Nota: É necessário ter o Docker rodando para que os Testcontainers funcionem.*

---

## 🐳 Docker

Para subir todo o ecossistema (App + Banco + Mensageria):

```bash
cd docker
docker compose up -d
```

| Serviço | Porta | Descrição |
|---------|-------|-----------|
| API | 8080 | Backend Spring Boot |
| PostgreSQL | 5432 | Banco de Dados |
| RabbitMQ | 5672 | Mensageria (AMQP) |
| RabbitMQ UI | 15672 | Painel de Gestão |
| PgAdmin | 8081 | Gestão Visual do Banco |

---

## 📨 Mensageria de Eventos (RabbitMQ)

O sistema utiliza RabbitMQ para processamento assíncrono de eventos (nascimentos, atualizações), garantindo desacoplamento.

- **Exchange:** `events-exchange`
- **Fila:** `events-queue`
- **Routing Key:** `event.created`

Para monitorar, acesse o painel do RabbitMQ em `http://localhost:15672` (User/Pass: `admin`/`admin`).

---

## 📄 Licença

Este projeto é proprietário. Todos os direitos reservados.

---

## 👤 Contato

**José Alberto Vilar Pereira**

📧 Email: albertovilar1@gmail.com
💼 LinkedIn: [Alberto Vilar](https://www.linkedin.com/in/alberto-vilar-316725ab)
🐙 GitHub: [@albertovilar](https://github.com/albertovilar)

<div align="center">
Desenvolvido com ☕ e ❤️ por Alberto Vilar

⭐ Se este projeto foi útil para você, considere dar uma estrela!
</div>