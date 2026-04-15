# CapriGestor Backend

Backend do CapriGestor, uma plataforma de gestão de caprinos construída com Java 21 e Spring Boot. O projeto cobre domínio real de fazenda, autenticação e autorização, rastreabilidade operacional, módulos de produção e saúde, e uma base arquitetural preparada para evolução contínua.

[![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)](https://www.java.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?style=for-the-badge&logo=spring)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?style=for-the-badge&logo=postgresql)](https://www.postgresql.org)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?style=for-the-badge&logo=docker)](https://www.docker.com)

[Portal de Documentação](./docs/INDEX.md) • [Arquitetura](./docs/01-architecture/ARCHITECTURE.md) • [Domínio](./docs/00-overview/BUSINESS_DOMAIN.md) • [Frontend](https://github.com/albertovilar/caprigestor-frontend) • [Roadmap](./docs/00-overview/ROADMAP.md)

## Visão Geral

O CapriGestor foi modelado para resolver fluxos operacionais reais de uma fazenda de caprinos, não apenas CRUD genérico. O backend concentra regras de domínio, segurança por ownership, governança arquitetural e persistência versionada.

Pontos fortes do projeto:

- domínio rico com múltiplos módulos de negócio integrados;
- arquitetura hexagonal com portas e adaptadores;
- segurança JWT com controle por perfil e por fazenda;
- PostgreSQL + Flyway com histórico de evolução do schema;
- mensageria RabbitMQ para eventos assíncronos;
- suíte de testes com unit, integração, ArchUnit e Testcontainers;
- documentação operacional e arquitetural mantida dentro do repositório.

## Principais Capacidades

### Gestão da fazenda e operação

- cadastro de fazendas, endereços, telefones e identidade visual;
- controle de ownership da fazenda e acesso por operador;
- fluxo atômico de registro da fazenda e bootstrap de usuário.

### Gestão de animais

- cadastro detalhado de caprinos;
- genealogia e classificação zootécnica;
- histórico operacional por animal;
- integração com importação ABCC.

### Produção, reprodução e saúde

- lactação e produção leiteira;
- ciclo reprodutivo com gestações, coberturas, alertas e correções;
- eventos de saúde, vacinas, tratamentos e cancelamentos;
- controle de retiradas e regras sanitárias;
- auditoria operacional e trilha de rastreabilidade.

### Comercial, conteúdo e administração

- vendas, despesas e visão comercial;
- blog e artigos públicos/administrativos;
- autenticação, papéis, operadores e reset de senha;
- módulo de auditoria para ações operacionais.

## Arquitetura

O projeto segue arquitetura hexagonal com separação explícita entre entrada, aplicação, domínio e persistência.

Convenção principal:

- `api`: controllers, DTOs e mapeamento de entrada/saída;
- `application`: portas de entrada e saída;
- `business`: regras de negócio e orquestração de casos de uso;
- `persistence` / `infrastructure`: adaptadores, entidades, repositórios e integrações.

Módulos com maior peso hoje:

- `authority`
- `farm`
- `goat`
- `health`
- `milk`
- `reproduction`
- `inventory`
- `commercial`
- `article`
- `audit`

O repositório possui gates de arquitetura para evitar regressões de dependência entre camadas.

## Stack Técnica

- Java 21
- Spring Boot 3
- Spring Security
- Spring Data JPA
- PostgreSQL
- Flyway
- RabbitMQ
- MapStruct
- Lombok
- Testcontainers
- ArchUnit
- Docker / Docker Compose

## Como Rodar

### Pré-requisitos

- Java 21
- Docker e Docker Compose

### 1. Clonar o projeto

```bash
git clone https://github.com/albertovilar/caprigestor-backend.git
cd caprigestor-backend
```

### 2. Subir a infraestrutura local

```bash
cd docker
docker compose up -d
cd ..
```

Serviços locais:

- API base: `http://localhost:8080/api/v1`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- PostgreSQL: `localhost:5432`
- RabbitMQ UI: `http://localhost:15672`
- PgAdmin: `http://localhost:8081`

### 3. Iniciar o backend

```bash
# Windows
./mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

O perfil `dev` é o padrão para desenvolvimento local.

## Segurança

- autenticação stateless baseada em JWT;
- autorização por papéis como `ROLE_ADMIN`, `ROLE_FARM_OWNER` e `ROLE_OPERATOR`;
- enforcement de ownership por fazenda;
- endpoint específico de permissões por fazenda;
- suporte a operadores vinculados pela tabela `tb_farm_operator`.

Para detalhes de regras de acesso e ownership:

- [AUTHORITY_ACCESS_MODULE.md](./docs/02-modules/AUTHORITY_ACCESS_MODULE.md)
- [ARCHITECTURE.md](./docs/01-architecture/ARCHITECTURE.md)

## Banco de Dados e Mensageria

- schema versionado com Flyway em [`src/main/resources/db/migration`](./src/main/resources/db/migration);
- PostgreSQL como banco principal em `dev`, `test` e `prod`;
- Hibernate configurado para validar schema, não gerar schema automaticamente;
- RabbitMQ usado para eventos assíncronos e desacoplamento operacional.

Referências úteis:

- [API Contracts](./docs/03-api/API_CONTRACTS.md)
- [MVP Ready](./docs/00-overview/MVP_READY.md)
- [Production Docker Deploy Runbook](./docs/00-overview/PRODUCTION_DOCKER_DEPLOY_RUNBOOK.md)

## Qualidade e Testes

A suíte cobre:

- testes unitários das regras de negócio;
- testes de integração com contexto Spring;
- testes arquiteturais com ArchUnit;
- testes com Testcontainers para fluxos dependentes de PostgreSQL;
- smoke tests e validações operacionais.

Execução local:

```bash
# Windows
./mvnw.cmd test

# Linux / macOS
./mvnw test
```

Comandos úteis:

```bash
./mvnw clean test
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/health/liveness
curl http://localhost:8080/actuator/health/readiness
```

## Documentação

O README foi mantido intencionalmente enxuto. O detalhe pesado do projeto está centralizado em `docs/`.

Pontos de entrada recomendados:

- [INDEX.md](./docs/INDEX.md)
- [ARCHITECTURE.md](./docs/01-architecture/ARCHITECTURE.md)
- [BUSINESS_DOMAIN.md](./docs/00-overview/BUSINESS_DOMAIN.md)
- [GOAT_FARM_MODULE.md](./docs/02-modules/GOAT_FARM_MODULE.md)
- [HEALTH_VETERINARY_MODULE.md](./docs/02-modules/HEALTH_VETERINARY_MODULE.md)
- [INVENTORY_MODULE.md](./docs/02-modules/INVENTORY_MODULE.md)
- [API_CONTRACTS.md](./docs/03-api/API_CONTRACTS.md)

## Frontend Relacionado

O frontend desse ecossistema está em:

- [caprigestor-frontend](https://github.com/albertovilar/caprigestor-frontend)

## Licença

Projeto proprietário. Todos os direitos reservados.

## Contato

José Alberto Vilar Pereira

- Email: `albertovilar1@gmail.com`
- LinkedIn: [Alberto Vilar](https://www.linkedin.com/in/alberto-vilar-316725ab)
- GitHub: [@AlbertoVilar](https://github.com/AlbertoVilar)
