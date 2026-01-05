# Domínio de Negócio - GoatFarm

Este documento descreve as entidades principais, regras de negócio e requisitos não funcionais do sistema.

## 1. Entidades e Agregados

### GoatFarm (Fazenda)
*   **Descrição**: O núcleo do sistema. Representa o capril.
*   **Responsabilidades**: Agrupa animais, proprietário e dados de contato.
*   **Regras**:
    *   Pertence a um único `User` (Proprietário).
    *   Possui identificador único `TOD` (Tatuagem Orelha Direita da fazenda).
    *   Endereço e telefones são carregados sob demanda.

### Goat (Caprino)
*   **Descrição**: Animal individual (cabra ou bode).
*   **Atributos Chave**: `registrationNumber` (ID), `name`, `gender`, `breed`, `birthDate`.
*   **Relacionamentos**:
    *   Pai e Mãe (Auto-relacionamento).
    *   Fazenda de origem.
*   **Regras**:
    *   Registro deve ser único.
    *   Controle de genealogia via referências diretas (`father`, `mother`).

### User & Authority (Segurança)
*   **User**: Usuário do sistema (login via email/CPF).
*   **Roles**:
    *   `ROLE_ADMIN`: Acesso total e manutenção.
    *   `ROLE_OPERATOR`: Gestão da própria fazenda.
*   **Ownership**: Um usuário só pode manipular dados da sua própria fazenda (validado via `OwnershipService`).

### Event (Eventos de Manejo)
*   **Tipos**: Parto, Cobertura, Vacinação, Pesagem, Morte, Transferência.
*   **Fluxo**: Eventos são registrados e podem disparar processamentos assíncronos (via RabbitMQ) para atualizações de status ou notificações.

### Genealogy (Genealogia)
*   **Conceito**: Não é uma entidade persistida, mas uma **projeção**.
*   **Funcionamento**:
    *   Calculada em tempo real (On-Demand).
    *   Constrói árvore com até 3 níveis (Pais, Avós, Bisavós).
    *   Otimizada para evitar problemas de N+1 queries.

## 2. Requisitos Não Funcionais

### Performance
*   **Lazy Loading**: Entidades relacionadas não são carregadas a menos que necessário.
*   **Queries Otimizadas**: Uso de `JOIN FETCH` para carregar grafos complexos (ex: Genealogia) em uma única query.
*   **Paginação**: Todas as listagens suportam `Pageable`.

### Segurança
*   **Autenticação**: JWT (Stateless).
*   **Proteção de Senha**: BCrypt.
*   **Isolamento**: Filtros de segurança garantem que um usuário não acesse dados de outro (`OwnershipService`).

### Observabilidade
*   **Logs**: Uso de SLF4J/Logback. Logs estruturados em pontos críticos (Auth, Erros, Eventos).
*   **Health Checks**: Disponíveis via Spring Actuator (se habilitado).

### Testes
*   **Unitários**: JUnit 5 + Mockito.
*   **Integração**: `@SpringBootTest` com H2 Database.
*   **Cobertura**: Foco em Regras de Negócio e Casos de Uso.
