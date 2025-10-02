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

# Linux/Mac
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

## 5. Banco de dados

- Migrações: `src/main/resources/db/migration` (controladas pelo Flyway).
- Seed inicial: `import.sql` (carregado para dados básicos quando aplicável).
- Banco padrão: PostgreSQL. Configure credenciais e URL no `application-<profile>.properties`.

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