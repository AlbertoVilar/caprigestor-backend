# CapriGestor — Backend

Sistema backend do CapriGestor, plataforma completa para gestão de caprinos (cabras), genealogia animal, fazendas, eventos e controle por usuário. Este serviço expõe uma API REST segura, integrando-se ao frontend React e ao PostgreSQL.

> Nota: Este repositório é o **Backend** do sistema CapriGestor. Para o **Frontend** (aplicação React), consulte: https://github.com/albertovilar/caprigestor-frontend

## ✅ Nome do Projeto e Descrição
- Nome: `CapriGestor — Backend`
- Descrição: API REST em Java 21 + Spring Boot para cadastro e gestão de animais, fazendas, eventos e genealogia, com autenticação baseada em OAuth2 + JWT e autorização por roles.

## ⚙️ Tecnologias Utilizadas
- `Java 21`
- `Spring Boot` (Web, Validation)
- `Spring Security` + `OAuth2 Resource Server` + `JWT`
- `PostgreSQL` (produção/dev) e `H2` (testes)
- `Spring Data JPA` + `Hibernate`
- `Flyway` (migrações SQL)
- `MapStruct` (mapeamento DTO/VO/Entity)
- `Springdoc OpenAPI` (Swagger UI)
- `Docker` (Postgres e pgAdmin)

## 📋 Principais Funcionalidades
- Cadastro e gestão de caprinos, fazendas, proprietários, telefones, endereços
- Registro de eventos por animal (parto, cobertura, vacinação, etc.)
- Árvore genealógica via API (ancestralidade até bisavós)
- Sistema de permissões baseado em roles (`ROLE_ADMIN`, `ROLE_OPERATOR`)
- CRUD completo com validações e exceções padronizadas
- Autenticação com OAuth2 + JWT (Bearer Token)
- Integração com frontend (React) via RESTful API

## 🧩 Arquitetura Hexagonal
O projeto adota uma estrutura por camadas inspirada na Arquitetura Hexagonal, separando responsabilidades e facilitando testes e evolução:
- `Controller (API)`: expõe endpoints REST, orquestra requisições e respostas.
- `Facade`: camada de coordenação de casos de uso; agrega chamadas a serviços de negócio.
- `Business`: regras de negócio, validações e políticas.
- `DAO/Repository`: persistência e consultas (Spring Data JPA).
- `Mapper`: conversões entre DTOs/VOs/Entities (MapStruct).
- `Config`: segurança, inicialização, exceções e infraestrutura.

Fluxo típico: `Controller → Facade → Business → Repository`.

## 📁 Estrutura de Pacotes (visão geral)
- `com.devmaster.goatfarm.authority` — usuários, roles, autenticação
  - `api.controller` — `AuthController`, endpoints de login/refresh
  - `business` — `AuthBusiness` e casos de uso
  - `model.entity` — entidades `User`, `Role`
  - `model.repository` — `UserRepository`, `RoleRepository`
- `com.devmaster.goatfarm.goat` — caprinos
  - `api.controller`, `facade`, `business`, `mapper`, `model.entity`, `model.repository`
- `com.devmaster.goatfarm.farm` — fazendas
  - `api.controller`, `facade`, `business`, `mapper`, `model.entity`, `model.repository`
- `com.devmaster.goatfarm.events` — eventos de animais
  - `api.controller`, `facade`, `business`, `mapper`, `model.entity`, `model.repository`
- `com.devmaster.goatfarm.address` e `phone` — endereços e telefones
- `com.devmaster.goatfarm.config.security` — `SecurityConfig`, `OwnershipService`, filtros
- `com.devmaster.goatfarm.config.exceptions` — exceções e `GlobalExceptionHandler`

## 🔐 Segurança e Autenticação
- Padrão: `OAuth2 + JWT` com chaves RSA.
- Roles: `ROLE_ADMIN`, `ROLE_OPERATOR` (controle por endpoint e `@PreAuthorize`).
- Geração/validação de tokens: `JwtEncoder`/`JwtDecoder` com `app.key` e `app.pub`.
- Usuário admin padrão:
  - Email: `albertovilar1@gmail.com`
  - Senha: `132747`
  - Roles: `ROLE_ADMIN`, `ROLE_OPERATOR`
- Principais endpoints:
  - `POST /api/auth/login` — autenticação por email/senha
  - `POST /api/auth/refresh` — renovação de token (se aplicável)
  - Endpoints protegidos exigem `Authorization: Bearer <accessToken>`

Configurações relevantes (perfil `dev`):
- `src/main/resources/application.properties`:
  - `spring.profiles.active=dev`
- `src/main/resources/application-dev.properties`:
  - `spring.datasource.url=jdbc:postgresql://localhost:5432/caprigestor_test`
  - `spring.datasource.username=admin`, `spring.datasource.password=admin123`
  - `spring.jpa.hibernate.ddl-auto=validate`
  - `spring.flyway.enabled=true`
  - `jwt.public.key=classpath:app.pub`, `jwt.private.key=classpath:app.key`
  - `cors.origins=http://localhost:3000,http://localhost:5173,...`

## 🔧 Como Rodar Localmente

### Opção A — Com Docker (Banco de Dados + pgAdmin)
1. Requisitos: `Docker` e `Docker Compose` instalados.
2. Suba Postgres e pgAdmin:
   - `docker compose -f ./docker/docker-compose.yml up -d`
3. Acesse pgAdmin:
   - `http://localhost:8081`
   - Email: `admin@admin.com` | Senha: `admin123`
4. Banco disponível em `localhost:5432`:
   - DB: `caprigestor_test`
   - User: `admin` | Password: `admin123`
5. Execute o backend (perfil `dev`):
   - Com Maven instalado: `mvn spring-boot:run`
   - Com wrapper (Windows): `./mvnw.cmd spring-boot:run`
   - Com wrapper (Linux/Mac): `./mvnw spring-boot:run`
6. API disponível em: `http://localhost:8080`

### Opção B — Manualmente (sem Docker)
1. Instale PostgreSQL localmente e crie o banco `caprigestor_test`.
2. Configure usuário/senha conforme `application-dev.properties` ou ajuste variáveis.
3. Execute migrações Flyway automaticamente ao subir o app (perfil `dev`).
4. Suba o backend:
   - `mvn spring-boot:run`
   - ou: `mvn clean package` e `java -jar target/<seu-jar>.jar` (use o JAR gerado pelo build)

### Variáveis de Ambiente (opcionais)
- `CLIENT_ID` (default `myclientid`)
- `CLIENT_SECRET` (default `myclientsecret`)
- `JWT_DURATION` (segundos; default `86400`)

### Teste Rápido (login e uso do token)
1. Login e obtenção de token:
   - `curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d '{"email":"albertovilar1@gmail.com","password":"132747"}'`
2. Use o `accessToken` retornado para chamar endpoints protegidos:
   - `curl -H "Authorization: Bearer <accessToken>" http://localhost:8080/api/goats/2114517012`

## 🔄 Link com o Frontend
- Repositório frontend (React): `https://github.com/albertovilar/caprigestor-frontend`

## 🚧 Status do Projeto
- Em desenvolvimento ativo.
- Funcionalidades principais implementadas: cadastro de animais/fazendas, eventos, genealogia e segurança JWT.
- Documentação e cobertura de testes em evolução.

## 🙋 Autor e Contato
- Autor: **José Alberto Vilar Pereira**
- Email: `albertovilar1@gmail.com`
- LinkedIn: [LinkedIn](https://www.linkedin.com/in/alberto-vilar-316725ab)

## 📎 Documentação da API / Swagger (placeholder)
- Quando em execução: `http://localhost:8080/swagger-ui.html`
- Em ambientes futuros, serão adicionados exemplos de requisições e prints.