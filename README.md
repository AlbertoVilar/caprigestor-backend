# 🐐 CapriGestor — Backend

> 🚧 **Status:** Em desenvolvimento (MVP) — previsão até **02/10/2025**

---

## 📬 Contato

- **Nome:** José Alberto Vilar Pereira  
- **E-mail:** [albertovilar1@gmail.com](mailto:albertovilar1@gmail.com)  
- **LinkedIn:** [linkedin.com/in/alberto-vilar-316725ab](https://www.linkedin.com/in/alberto-vilar-316725ab)  
- **GitHub:** [github.com/albertovilar](https://github.com/albertovilar)

---

## 1️⃣ Descrição

O **CapriGestor** é um sistema backend para **gerenciamento de caprinos**, permitindo cadastro, acompanhamento de eventos zootécnicos e **controle genealógico** (pai, mãe, categoria racial, origem).

O backend é desenvolvido com **Spring Boot 3** e segue rigorosamente os **princípios da Arquitetura Hexagonal (Ports & Adapters)**, garantindo:

- Código limpo e desacoplado  
- Evolução segura do domínio  
- Independência entre regra de negócio e infraestrutura  
- API REST segura e documentada com Swagger  

---

### 🎓 Filosofia Arquitetural

O que define a arquitetura **não são os nomes dos pacotes**, mas sim **a direção das dependências**.

Este projeto segue estritamente a **Regra da Dependência**, onde o núcleo de negócio **não conhece frameworks, banco de dados ou web**.

> _“A arquitetura não está nos nomes das pastas, mas nas **DEPENDÊNCIAS** entre as camadas.”_  
> — **Robert C. Martin (Uncle Bob)** — *Clean Architecture*

> _“O objetivo é isolar a lógica de negócio. Como você organiza as pastas é um detalhe de implementação.”_  
> — **Alistair Cockburn** — *Arquitetura Hexagonal*

---

## 2️⃣ Tecnologias Utilizadas

- ☕ **Java 21**
- 🌱 **Spring Boot 3**
- 🔐 **JWT + OAuth2**
- 🐘 **PostgreSQL**
- 🧭 **Flyway** (migrações de banco de dados)
- 🧪 **H2 Database** *(uso restrito a testes unitários isolados)*

---

## 3️⃣ Organização dos Pacotes

O projeto é organizado por **módulos de domínio**.  
Dentro de cada módulo, a separação de responsabilidades **implementa funcionalmente a Arquitetura Hexagonal**, mesmo com nomes pragmáticos:

- **Controller** (`api.controller`) → *Driving Adapter*  
- **Facade** (`facade`) → *Input Port*  
- **Business** (`business`) → *Serviço de Aplicação / Domínio (núcleo)*  
- **DAO** (`dao`) → *Output Port*  
- **Repository** (`repository`) → *Driven Adapter*  

📌 **Regra central:**  
> O domínio **não depende** de Web, JPA, Spring ou banco de dados.

---

## 4️⃣ Perfis de Execução

O projeto utiliza **perfis explícitos e sem ambiguidade**.  
Cada perfil tem um propósito claro.

### 🔧 Perfis disponíveis

- **`default`**  
  - Contém apenas configurações *cross-cutting* (logging, RabbitMQ).  
  - **Não inicializa datasource**.  
  - Flyway e Hibernate DDL desativados.

- **`dev`**  
  - Desenvolvimento local.  
  - **PostgreSQL real** (normalmente via Docker).  
  - Flyway **habilitado**.  
  - `ddl-auto=validate`.  
  - **Não possui seed automático**.

- **`test`**  
  - Testes automatizados.  
  - **PostgreSQL via Testcontainers**.  
  - Flyway **habilitado**.  
  - `ddl-auto=validate`.  

- **`prod`**  
  - Produção.  
  - Configuração exclusivamente via **variáveis de ambiente**.  
  - Flyway **habilitado**.  
  - Logs e SQL reduzidos.

📌 **Não existe perfil `dev-h2`.**  
📌 **H2 não é utilizado para desenvolvimento.**

---

### ▶️ Ativação de perfil

```bash
# Windows (PowerShell)
./mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev

# Linux / macOS
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
