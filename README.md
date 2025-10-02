# Caprigestor Backend

Backend do Caprigestor (Spring Boot), responsável por APIs, regras de negócio, migrações de banco (Flyway) e integração com infraestrutura local via Docker.

## Requisitos
- Java 17+
- Banco de dados compatível (ex.: PostgreSQL)
- Maven ou Gradle (conforme configurado no projeto)
- Docker (opcional) para subir serviços locais

## Configuração
- Arquivos de propriedades:
  - `src/main/resources/application.properties`
  - `src/main/resources/application-dev.properties`
  - `src/main/resources/application-test.properties`
- Ajuste credenciais de banco e variáveis necessárias (porta, host, usuário/senha) nos arquivos acima.
- Seed inicial: `src/main/resources/import.sql` (inclui dados básicos como usuário admin, perfis, etc.).

## Migrações (Flyway)
- Migrações residem em `src/main/resources/db/migration/` com versão `V{N}__Descricao.sql`.
- Em ambientes novos, utilize baseline conforme configuração do projeto (se necessário).
- Após atualizar/organizar migrações, rode a aplicação para aplicar `migrate` automaticamente.

## Desenvolvimento
1. Compile e rode com sua ferramenta de build (Maven/Gradle):
   - Maven: `mvn spring-boot:run`
   - Gradle: `./gradlew bootRun`
2. Ajuste perfis (`dev`, `test`) conforme necessário via `application-*.properties`.

## Infraestrutura
- Docker Compose: `docker/docker-compose.yml` para subir serviços de apoio (ex.: banco de dados).
- Atualize variáveis de ambiente e volumes conforme seu ambiente local.

## Notas
- Este README foi corrigido para remover referência indevida ao frontend.
- Repositório remoto correto: `git@github.com:AlbertoVilar/caprigestor-backend.git`.