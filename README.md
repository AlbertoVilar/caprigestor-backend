<div align="center">

<h1>🐐 CapriGestor — Backend</h1>

<p><i>Sistema completo para gestão de caprinos, com arquitetura limpa, segura e escalável.</i></p>

<a href="https://www.java.com" target="_blank"><img src="https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk" alt="Java 21"/></a>
<a href="https://spring.io/projects/spring-boot" target="_blank"><img src="https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?style=for-the-badge&logo=spring" alt="Spring Boot 3.x"/></a>
<a href="https://www.postgresql.org" target="_blank"><img src="https://img.shields.io/badge/PostgreSQL-16-blue?style=for-the-badge&logo=postgresql" alt="PostgreSQL 16"/></a>
<a href="https://www.docker.com" target="_blank"><img src="https://img.shields.io/badge/Docker-Ready-2496ED?style=for-the-badge&logo=docker" alt="Docker Ready"/></a>
<a href="./LICENSE" target="_blank"><img src="https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge" alt="MIT License"/></a>

<p>
  <a href="./DOCUMENTACAO_BACKEND.md">📋 Documentação Técnica</a> ·
  <a href="https://github.com/albertovilar/caprigestor-frontend">🖥️ Frontend</a> ·
  <a href="http://localhost:8080/swagger-ui/index.html">📊 Swagger (local)</a>
  
</p>

</div>

---

Status do projeto: Em desenvolvimento (MVP) até 02/10/2025.

---

## Sumário

- Sobre o Projeto
- Funcionalidades Principais
- Arquitetura e Módulos
- Perfis de Execução
- Banco de Dados
- Como Rodar
- Segurança (JWT + OAuth2)
- API & Swagger
- Frontend
- Dicas para DEV
- Contato

---

## Sobre o Projeto

CapriGestor é uma API REST robusta para gerenciamento completo de fazendas de caprinos. Foi construída com **Spring Boot 3**, segue princípios de **arquitetura hexagonal** (ports & adapters) e expõe **APIs seguras** documentadas via **Swagger**.

---

## Funcionalidades Principais

- Gestão de Fazendas
  - Cadastro de fazendas com endereços e telefones
  - Ownership e controle por proprietário
  - Listagem e busca paginadas
- Gestão de Animais
  - Cadastro detalhado de caprinos
  - Rastreamento genealógico (pai, mãe, avós)
  - Visualização de árvore genealógica
  - Status e categorização (PO, PA, PC)
- Controle de Acesso
  - Autenticação JWT stateless
  - Autorização baseada em roles (`ADMIN`, `OPERATOR`)
- Eventos e Rastreabilidade
  - Registro de nascimentos, vacinações, pesagens
  - Histórico completo por animal
  - Filtros avançados por tipo e período

---

## Arquitetura e Módulos

Camadas (hexagonal): `domain` · `application` · `infrastructure`

- `goat`: regras de negócio e acesso a dados de caprinos
- `events`: eventos (nascimentos, coberturas, pesagens, etc.)
- `genealogy`: relacionamento e linhagem (ascendência/descendência)
- `farm`: entidades e serviços de fazendas/estábulos/locais
- `authority`: autenticação, autorização, usuários e papéis
- `shared`: utilitários, DTOs comuns, exceções e infra compartilhada

Observação: foco em baixo acoplamento e alta coesão, com conversores e facades onde aplicável.

---

## Perfis de Execução

- `dev`: desenvolvimento local com dados de exemplo e logs verbosos
- `test`: testes automatizados (H2 em memória, configs isoladas)
- `prod`: produção (variáveis externas, segurança reforçada)

Ative via `spring.profiles.active`.

```bash
# Windows (PowerShell)
./mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
./mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=test

# Linux/Mac
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
./mvnw spring-boot:run -Dspring-boot.run.profiles=test
```

---

## Banco de Dados

- Migrações: `src/main/resources/db/migration` (Flyway)
- Seeds: `import.sql` (opcional; habilite `spring.sql.init.mode=always`)
- `test`: H2 em memória com `MODE=PostgreSQL`, `ddl-auto=validate`, Flyway
- `dev`: PostgreSQL com `ddl-auto=validate`, Flyway
- Credenciais e URL: `application-dev.properties`

As migrações versionadas (ex.: `V9__Create_Event_Table.sql`) garantem evolução consistente do schema.

---

## Como Rodar

Você pode rodar na IDE ou via Docker Compose.

- IDE (IntelliJ/Eclipse)
  - Java 21 instalado
  - Importar projeto Maven
  - Selecionar perfil (`dev`, `test`, `prod`)
  - Executar classe principal Spring Boot

- Maven CLI
  ```bash
  # Dev
  ./mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
  ```

- Docker Compose
  - Arquivo: `docker/docker-compose.yml`
  - Variáveis: `docker/.env` (base em `docker/.env.example`)
  - Comandos:
    ```bash
    # Windows (PowerShell)
    docker compose up -d
    # Para parar
    docker compose down
    ```
  - Exemplo de `docker/.env`:
    ```env
    POSTGRES_DB=caprigestor_dev
    POSTGRES_USER=admin
    POSTGRES_PASSWORD=changeme123
    PGADMIN_DEFAULT_EMAIL=admin@admin.com
    PGADMIN_DEFAULT_PASSWORD=changeme123
    ```

Após subir, a API estará acessível em `http://localhost:8080`.

---

## Segurança (JWT + OAuth2)

- Autenticação via OAuth2/JWT
- Autorização baseada em papéis (`ROLE_ADMIN`, `ROLE_OPERATOR`)
- Envie `Authorization: Bearer <token>` para endpoints protegidos
- Políticas de acesso nas configurações de segurança

### Endpoints Públicos (apenas leitura)

- `GET /api/goatfarms` — lista fazendas
- `GET /api/goatfarms/{farmId}` — detalhes da fazenda
- `GET /api/goatfarms/name` — busca por nome
- `GET /api/goatfarms/{farmId}/goats` — lista cabras da fazenda
- `GET /api/goatfarms/{farmId}/goats/{goatId}` — detalhes da cabra
- `GET /api/goatfarms/{farmId}/goats/search` — busca por nome na fazenda
- `GET /api/goatfarms/{farmId}/goats/{goatId}/genealogies` — genealogia da cabra

Observações:
- Não existem endpoints globais entre fazendas
- Todas as operações são agregadas por `farmId`

---

## API & Swagger

- UI local: `http://localhost:8080/swagger-ui/index.html`
- Explore e teste endpoints REST com schemas e exemplos

---

## Frontend

Repositório associado: `https://github.com/albertovilar/caprigestor-frontend`

---

## Dicas para DEV

Se os testes estiverem falhando enquanto você valida endpoints e segurança, execute ignorando testes:

```bash
# Windows (PowerShell)
./mvnw.cmd -DskipTests spring-boot:run -Dspring-boot.run.profiles=dev

# Linux/Mac
./mvnw -DskipTests spring-boot:run -Dspring-boot.run.profiles=dev
```

Validar acesso público a genealogias (sem token):

```
GET http://localhost:8080/api/goatfarms/1/goats/XYZ/genealogies
# Esperado: 404 se não existir, mas NÃO 401 (sem token)
```

---

## Contato

- Nome: José Alberto Vilar Pereira
- E-mail: [albertovilar1@gmail.com](mailto:albertovilar1@gmail.com)
- LinkedIn: [linkedin.com/in/alberto-vilar-316725ab](https://www.linkedin.com/in/alberto-vilar-316725ab)
- GitHub: [github.com/albertovilar](https://github.com/albertovilar)

---

## 📸 Prints ou GIFs

Espaço reservado para screenshots, GIFs de uso e observações futuras sobre UX e integração.