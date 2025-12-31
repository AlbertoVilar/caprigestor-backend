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
- [Uso](#como-usar)
- [Perfis de Execução](#perfis-de-execução)
- [Banco de Dados](#banco-de-dados)
- [Segurança](#segurança)
- [API](#api--documentação)
- [Testes](#testes)
- [Docker](#docker)
- [Licença](#licença)
- [Contato](#contato)
- [Mensageria (RabbitMQ)](#-mensageria-de-eventos-rabbitmq)

---

## 📖 Sobre o Projeto

**CapriGestor** é uma API REST robusta e moderna desenvolvida para o gerenciamento completo de fazendas de caprinos. Construída com **Spring Boot 3** e seguindo os princípios de **arquitetura hexagonal** (ports & adapters), oferece uma solução escalável, segura e de fácil manutenção.

### 🎯 Objetivo

Fornecer uma plataforma centralizada para criadores de caprinos gerenciarem todos os aspectos de suas fazendas, desde o cadastro de animais até o rastreamento genealógico e controle de eventos.

---

## ✨ Funcionalidades Principais

### 🏡 Gestão de Fazendas
- ✅ Cadastro completo de fazendas com endereços e telefones
- ✅ Controle de propriedade e ownership
- ✅ Listagem e busca paginadas
- ✅ Gerenciamento de estábulos e locais

### 🐐 Gestão de Animais
- ✅ Cadastro detalhado de caprinos com informações relevantes
- ✅ Rastreamento genealógico (pai/mãe e ancestrais quando disponíveis)
- ✅ Status e classificação: **PO, PC, PA**
- ✅ Busca avançada e filtros

### 🧬 Regras de Domínio (Genealogia + Classificação)
Classificações:
- **PO** — *Puro de Origem*
- **PC** — *Puro por Cruza*
- **PA** — *Puro por Avaliação*

Regras:
- Se **PO** ou **PC** ⇒ **pai e mãe são obrigatórios**
- Se **PA** ⇒ **o sistema deve aceitar sem inserir pai e mãe** (pais podem ser desconhecidos)
- **Pai/mãe podem ser de outra fazenda** (ex.: reprodutor comprado de fora)

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
- **Java 21**
- **Spring Boot 3.x**
- **Spring Security**
- **Spring Data JPA**

### Banco de Dados
- **PostgreSQL 16** – banco principal
- **Flyway** – versionamento do schema
- **H2 Database (opcional)** – apenas para **testes unitários isolados** (não recomendado para desenvolvimento diário)

### Segurança
- **JWT (JSON Web Tokens)**
- **OAuth2**

### Documentação e Testes
- **Swagger/OpenAPI**
- **JUnit 5**
- **Mockito**
- **Testcontainers** – PostgreSQL efêmero para testes de integração (recomendado)

### DevOps
- **Docker**
- **Docker Compose**
- **Maven**

---

## 🏗️ Arquitetura e Módulos

O projeto segue a **arquitetura hexagonal** (ports & adapters), garantindo baixo acoplamento e alta coesão.

### 📦 Estrutura de Camadas

domain → application → infrastructure

lua
Copiar código

### 🗂️ Módulos

| Módulo | Descrição |
|--------|-----------|
| **goat** | Regras de negócio e acesso a dados de caprinos |
| **events** | Gestão de eventos (nascimentos, coberturas, pesagens, etc.) |
| **genealogy** | Relacionamento e linhagem (ascendência/descendência) |
| **farm** | Entidades e serviços de fazendas/estábulos/locais |
| **authority** | Autenticação, autorização, usuários e papéis |
| **shared** | Utilitários, DTOs comuns, exceções e infra compartilhada |

### 🧠 Filosofia Arquitetural (Hexagonal)

- Princípios: inversão de dependências, isolamento do domínio e Portas & Adaptadores.
- Convenção pragmática de nomes mapeada para hexagonal:
  - Controller → Adaptador de Entrada (Driving Adapter)
  - Facade → Porta de Entrada (Input Port)
  - Business → Serviço de Aplicação/Domínio
  - DAO → Porta de Saída (Output Port)
  - Repository (implementado pelo DAO) → Adaptador de Saída (Driven Adapter)

> "A arquitetura não está nos nomes das pastas, mas nas DEPENDÊNCIAS entre camadas." – Uncle Bob (Clean Architecture)  
> "O objetivo é isolar a lógica de negócio. Como você organiza as pastas é detalhe de implementação." – Alistair Cockburn (Arquitetura Hexagonal)

- Testabilidade: regras de negócio testadas sem Spring (ex.: `@ExtendWith(MockitoExtension.class)`), provando baixo acoplamento com infraestrutura.

---

## 🧭 Diagrama do Domínio (Mermaid)

> Renderize este bloco com Mermaid. (Pode existir também em `docs/diagrams/domain.mmd`, se você mantiver essa pasta.)

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
🧩 Diagrama de Classes (Mermaid)
Se você tiver arquivo standalone: docs/diagrams/class.mmd

mermaid
Copiar código
classDiagram
     %% ========== MÓDULO FARM ==========
     class GoatFarm {
         +Long id
         +String name
         +Long ownerId
         +Long addressId
         +Instant createdAt
         +Instant updatedAt
     }

     class Address {
         +Long id
         +String street
         +String number
         +String neighborhood
         +String city
         +String state
         +String zipcode
         +String country
     }

     class Phone {
         +Long id
         +String number
         +PhoneType type
         +Long ownerId
     }

     class PhoneType {
         <<enumeration>>
         MOBILE
         LANDLINE
     }

     class Stable {
         +Long id
         +String name
         +Long farmId
     }

     %% ========== MÓDULO AUTHORITY ==========
     class User {
         +Long id
         +String username
         +String email
         -String password
         +boolean enabled
     }

     class Role {
         <<enumeration>>
         ADMIN
         OPERATOR
     }

     class UserRole {
         +Long userId
         +Role role
     }

     %% ========== MÓDULO GOAT ==========
     class Goat {
         +Long id
         +Long farmId
         +String registrationNumber
         +String name
         +Gender gender
         +GoatLifeStatus status
         +GoatClassification classification
         +LocalDate birthDate
         +Long fatherId
         +Long motherId
         +String notes
     }

     class Gender {
         <<enumeration>>
         MALE
         FEMALE
     }

     class GoatLifeStatus {
         <<enumeration>>
         ACTIVE
         INACTIVE
         DECEASED
     }

     class GoatClassification {
         <<enumeration>>
         PO
         PC
         PA
     }

     %% ========== MÓDULO EVENTS ==========
     class Event {
         +Long id
         +Long farmId
         +Long goatId
         +EventType type
         +LocalDate eventDate
         +String payload
     }

     class EventType {
         <<enumeration>>
         BIRTH
         COVERAGE
         PARTURITION
         VACCINATION
         WEIGHT
         TREATMENT
     }

     %% ========== RELACIONAMENTOS ==========
     GoatFarm "1" --> "1" Address : possui
     GoatFarm "1" --> "0..*" Phone : tem
     GoatFarm "1" --> "0..*" Stable : contém
     GoatFarm "1" --> "0..*" Goat : gerencia

     User "1" --> "0..*" GoatFarm : possui
     User "1" --> "0..*" UserRole : tem
     UserRole "*" --> "1" Role : referencia

     Goat "0..1" --> "0..1" Goat : pai (fatherId)
     Goat "0..1" --> "0..1" Goat : mãe (motherId)
     Goat --> Gender : tem
     Goat --> GoatLifeStatus : possui
     Goat --> GoatClassification : classificação

     Goat "1" --> "0..*" Event : registra
     Event --> EventType : tipo
     GoatFarm "1" --> "0..*" Event : monitora

     note for Goat "Regras:\n- Se classification=PO ou PC: pai e mãe obrigatórios.\n- Se classification=PA: pai e mãe opcionais.\n- Pai/mãe podem ser de outra fazenda.\n- fatherId deve referenciar Goat com gender=MALE.\n- motherId deve referenciar Goat com gender=FEMALE."

     note for Event "Invariantes:\n- farmId deve corresponder à fazenda do Goat.\n- goatId deve referenciar Goat válido.\n- payload varia conforme EventType."
📋 Pré-requisitos
Antes de começar, certifique-se de ter instalado:

☕ Java 21 ou superior

🔧 Maven 3.8+ (ou use o wrapper incluído)

🐳 Docker & Docker Compose (recomendado)

💻 IDE: IntelliJ IDEA, Eclipse ou VS Code

🚀 Instalação
1️⃣ Clone o repositório
bash
Copiar código
git clone https://github.com/albertovilar/caprigestor-backend.git
cd caprigestor-backend
2️⃣ Subir infraestrutura (PostgreSQL + RabbitMQ + PgAdmin)
Ajuste o caminho conforme seu projeto (ex.: docker/).

bash
Copiar código
cd docker
docker compose up -d
Serviços:

PostgreSQL: localhost:5432

PgAdmin: http://localhost:8081

RabbitMQ UI: http://localhost:15672 (admin/admin)

⚙️ Configuração
🧩 Filosofia dos Perfis (sem confusão)
A regra do projeto é:

default: apenas configurações cross-cutting (RabbitMQ, logging, etc.) e SEM datasource

dev: desenvolvimento real com PostgreSQL + Flyway

test: testes com PostgreSQL via Testcontainers + Flyway

prod: produção com variáveis de ambiente + Flyway

✅ O objetivo é eliminar ambiguidades e impedir que H2 “roube” execuções por engano.

🧪 Perfis de Execução
Perfil	Uso	Banco	Flyway	DDL
default	base	nenhum	❌	none (ou equivalente)
dev	desenvolvimento	PostgreSQL local	✅	validate
test	testes	PostgreSQL (Testcontainers)	✅	validate
prod	produção	PostgreSQL	✅	validate

🔧 Como ativar
bash
Copiar código
# Windows (PowerShell)
./mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev

# Linux/Mac
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
💻 Como Usar
Após iniciar, a API estará disponível em:

API: http://localhost:8080

Swagger UI: http://localhost:8080/swagger-ui/index.html

🗄️ Banco de Dados
✅ Política de dados (Padrão do projeto)
Schema é 100% versionado por Flyway em src/main/resources/db/migration/

Sem ddl-auto=update/create em qualquer perfil oficial

Seeds (se existirem) devem ser migrations V###__seed_*.sql

import.sql não é usado (evita divergência silenciosa entre ambientes)

📍 Perfil dev (PostgreSQL)
Banco recomendado: caprigestor_dev

ddl-auto=validate

spring.sql.init.mode=never

spring.flyway.enabled=true

spring.flyway.locations=classpath:db/migration

spring.flyway.clean-disabled=true (proteção)

Se você usa .env no Docker Compose, injete usuário/senha por variáveis de ambiente no application-dev.properties.

🧪 Perfil test (Testcontainers)
Banco PostgreSQL efêmero criado/destruído automaticamente

Flyway aplicando schema no container

Sem configuração manual de URL/credenciais

⚠️ H2 pode existir apenas como perfil extra e explícito (ex.: test-h2) para testes unitários isolados — não é perfil oficial.

🔐 Segurança
🛡️ Autenticação e Autorização
OAuth2 + JWT

Roles típicas: ROLE_ADMIN, ROLE_OPERATOR

Token stateless

Header esperado
http
Copiar código
Authorization: Bearer <seu-token-jwt>
📡 API & Documentação
Swagger UI: http://localhost:8080/swagger-ui/index.html

Documentação Técnica Completa: DOCUMENTACAO_BACKEND.md

⚠️ Importante: Operações são agregadas por farmId (evita vazamento entre fazendas).

🧪 Testes
▶️ Executar todos os testes
bash
Copiar código
# Windows
./mvnw.cmd test

# Linux/Mac
./mvnw test
Características
Banco: PostgreSQL via Testcontainers

Isolamento: banco efêmero por execução

Schema: carregado pelo Flyway

Requisito: Docker rodando

Executar testes específicos
bash
Copiar código
./mvnw test -Dtest=GoatControllerTest
🐳 Docker
🚀 Subir serviços
bash
Copiar código
cd docker
docker compose up -d
📋 Serviços disponíveis
Serviço	Porta	Descrição
API	8080	Backend Spring Boot
PostgreSQL	5432	Banco de dados
PgAdmin	8081	Interface do PostgreSQL
RabbitMQ	5672	AMQP
RabbitMQ UI	15672	Painel do RabbitMQ

🛑 Parar serviços
bash
Copiar código
docker compose down
🗑️ Limpar volumes
bash
Copiar código
docker compose down -v
🔗 Links Relacionados
🖥️ Frontend do CapriGestor

📋 Documentação Técnica Completa

📄 Licença
Este projeto ainda não possui licença definida. Até uma licença ser escolhida (por exemplo, MIT), todos os direitos permanecem reservados.

👤 Contato
José Alberto Vilar Pereira

📧 Email: albertovilar1@gmail.com

💼 LinkedIn: https://www.linkedin.com/in/alberto-vilar-316725ab

🐙 GitHub: https://github.com/albertovilar

📸 Screenshots
💡 Espaço reservado para capturas de tela, GIFs demonstrativos e observações sobre UX e integração.

<div align="center">
Desenvolvido com ☕ e ❤️ por Alberto Vilar

⭐ Se este projeto foi útil para você, considere dar uma estrela!

</div>
📨 Mensageria de Eventos (RabbitMQ)
Este projeto integra processamento assíncrono de eventos usando RabbitMQ, seguindo a Arquitetura Hexagonal (Portas e Adaptadores).

Visão Geral
Porta EventPublisher define o contrato de publicação de eventos.

Adaptador RabbitMQEventPublisher publica eventos no exchange com confirmações (publisher confirms) e retornos (returns) habilitados.

EventConsumer consome mensagens da fila e aciona o fluxo de negócio.

EventMessage é o DTO padronizado para trafegar os dados de evento.

Subir RabbitMQ
Via Docker Compose: docker/docker-compose.yml

No diretório docker/, execute: docker compose up -d

UI: http://localhost:15672 (credenciais padrão: admin/admin)

Executar em modo desenvolvimento
Ative o perfil dev:

Windows PowerShell: ./mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev

Linux/Mac: ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

Logs: conforme configurado no seu application-dev.properties (ex.: logging.file.name=logs/dev.log)

Diagnóstico de Publicação/Consumo
Ao publicar um evento, espere ver logs indicando:

publicação com exchange + routingKey + ID do evento

confirmação do broker (confirm)

consumo pelo listener (EVENT RECEIVED FROM QUEUE)

Se aparecer unroutable, verifique exchange/routingKey e binding da fila.

Estrutura de Pacotes (mensageria)
less
Copiar código
com.devmaster.goatfarm.events.messaging
├── config        # RabbitTemplate, confirms/returns, listener config
├── consumer      # EventConsumer (@RabbitListener)
├── dto           # EventMessage
└── publisher     # RabbitMQEventPublisher
Notas
Logs em arquivo ajudam troubleshooting (não versionar logs/).

Confirmações e retornos do publisher ficam habilitados para facilitar diagnóstico.

Copiar código






