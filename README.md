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

# Test
./mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=test

# Linux/Mac
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
./mvnw spring-boot:run -Dspring-boot.run.profiles=test
```

## 5. Banco de dados

- Migrações: `src/main/resources/db/migration` (controladas pelo Flyway).
- Seed inicial: `import.sql` (desabilitado por padrão; habilite `spring.sql.init.mode=always` se necessário).
- Perfis: `test` usa H2 em memória com `MODE=PostgreSQL`, `ddl-auto=validate` e `Flyway` habilitado; `dev` usa PostgreSQL com `ddl-auto=validate` e `Flyway` habilitado.
- Banco padrão (dev): PostgreSQL. Configure credenciais e URL no `application-dev.properties`.

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

### Endpoints Públicos

- `POST /api/auth/login` — autenticação
- `POST /api/auth/register` — criação de usuário
- `POST /api/auth/refresh` — renovação de token
- `POST /api/auth/register-farm` — criação pública de fazenda completa
- `POST /api/goatfarms/full` — criação pública de fazenda completa (fazenda, usuário, endereço, telefones)
- `GET /api/goatfarms` — lista fazendas
- `GET /api/goatfarms/{id}` — detalhes da fazenda
- `GET /api/goatfarms/name` — busca por nome
- `GET /api/goatfarms/{farmId}/goats` — lista cabras da fazenda
- `GET /api/goatfarms/{farmId}/goats/{goatId}` — detalhes da cabra
- `GET /api/goatfarms/{farmId}/goats/search` — busca por nome na fazenda
- `GET /api/goatfarms/{farmId}/goats/{goatId}/genealogies` — genealogia

Observação: As demais rotas sob `/api/**` exigem autenticação e papéis válidos.

### Cadastro Completo da Fazenda (público)

- `POST /api/goatfarms/full` e `POST /api/auth/register-farm` executam o mesmo fluxo de criação completa.
- Regras:
  - É obrigatório informar ao menos um telefone em `phones`.
  - No retorno, `updatedAt` vem nulo na criação e `version` retorna o valor de controle de concorrência otimista.
  - O backend usa `@Version` (concorrência otimista) em `GoatFarm`; conflitos retornam `409 Conflict`.

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

## 3. Configurações do Projeto

### `pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		 xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>3.4.4</version>
		<relativePath/>
	</parent>
	<groupId>com.devmaster</groupId>
	<artifactId>CapriGestor</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<name>GoatFarm</name>
	<description>Demo project for Spring Boot</description>

	<properties>
		<java.version>21</java.version>
	</properties>

	<dependencies>
		<!-- Essential dependencies -->
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-jpa</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>

		<!-- Database -->
		<dependency>
			<groupId>org.flywaydb</groupId>
			<artifactId>flyway-core</artifactId>
		</dependency>
		<dependency>
			<groupId>org.flywaydb</groupId>
			<artifactId>flyway-database-postgresql</artifactId>
		</dependency>
		<dependency>
			<groupId>org.postgresql</groupId>
			<artifactId>postgresql</artifactId>
			<scope>runtime</scope>
		</dependency>
		<dependency>
			<groupId>com.h2database</groupId>
			<artifactId>h2</artifactId>
			<scope>runtime</scope>
		</dependency>


		<!-- Validation -->
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-validation</artifactId>
		</dependency>

		<!-- Security -->
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-security</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.security</groupId>
			<artifactId>spring-security-oauth2-jose</artifactId>
		</dependency>

		<!-- Logging (explicit to ensure ThrowableProxy availability) -->
		<dependency>
			<groupId>ch.qos.logback</groupId>
			<artifactId>logback-classic</artifactId>
		</dependency>

		<!-- Lombok -->
		<dependency>
			<groupId>org.projectlombok</groupId>
			<artifactId>lombok</artifactId>
			<optional>true</optional>
		</dependency>

		<!-- OpenAPI/Swagger -->
		<dependency>
			<groupId>io.swagger</groupId>
			<artifactId>swagger-annotations</artifactId>
			<version>2.0.0-rc2</version>
		</dependency>
		<dependency>
			<groupId>org.springdoc</groupId>
			<artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
			<version>2.8.6</version>
		</dependency>

		<!-- Testing -->
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.springframework.security</groupId>
			<artifactId>spring-security-test</artifactId>
			<scope>test</scope>
		</dependency>
		<!-- MapStruct -->
		<dependency>
			<groupId>org.mapstruct</groupId>
			<artifactId>mapstruct</artifactId>
			<version>1.5.5.Final</version>
		</dependency>
		<dependency>
			<groupId>org.mapstruct</groupId>
			<artifactId>mapstruct-processor</artifactId>
			<version>1.5.5.Final</version>
			<scope>provided</scope>
		</dependency>
	</dependencies>

	<build>
		<plugins>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-compiler-plugin</artifactId>
				<version>3.10.1</version>
				<configuration>
					<source>21</source>
					<target>21</target>
					<annotationProcessorPaths>
						<path>
							<groupId>org.projectlombok</groupId>
							<artifactId>lombok</artifactId>
							<version>1.18.30</version>
						</path>
						<path>
							<groupId>org.mapstruct</groupId>
							<artifactId>mapstruct-processor</artifactId>
							<version>1.5.5.Final</version>
						</path>
					</annotationProcessorPaths>
				</configuration>
			</plugin>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
				<configuration>
					<excludes>
						<exclude>
							<groupId>org.projectlombok</groupId>
							<artifactId>lombok</artifactId>
						</exclude>
					</excludes>
				</configuration>
			</plugin>
			<plugin>
				<groupId>org.flywaydb</groupId>
				<artifactId>flyway-maven-plugin</artifactId>
				<configuration>
					<url>jdbc:h2:mem:testdb</url>
					<user>sa</user>
					<password></password>
					<locations>
						<location>classpath:db/migration</location>
					</locations>
				</configuration>
			</plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.2.5</version>
                <configuration>
                    <systemPropertyVariables>
                        <spring.profiles.active>test</spring.profiles.active>
                    </systemPropertyVariables>
                </configuration>
            </plugin>
        </plugins>
	</build>
</project>
```

### `application.properties`

```properties
spring.application.name=GoatFarm
spring.jpa.open-in-view=false
spring.jackson.serialization.fail-on-empty-beans=false
```

### `application-dev.properties`

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/caprigestor_test
spring.datasource.username=postgres
spring.datasource.password=132747
spring.jpa.hibernate.ddl-auto=validate
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
```

### `application-test.properties`

```properties
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL
spring.jpa.hibernate.ddl-auto=validate
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
```

## 4. Migrações do Banco de Dados (Flyway)

### `V1__Create_Authority_Table.sql`

```sql
CREATE TABLE authority (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    permission VARCHAR(255),
    CONSTRAINT pk_authority PRIMARY KEY (id)
);
```

### `V2__Create_Role_Table.sql`

```sql
CREATE TABLE role (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    authority VARCHAR(255),
    description VARCHAR(255),
    CONSTRAINT pk_role PRIMARY KEY (id)
);

CREATE TABLE tb_role_authority (
    role_id BIGINT NOT NULL,
    authority_id BIGINT NOT NULL,
    CONSTRAINT pk_tb_role_authority PRIMARY KEY (role_id, authority_id)
);

ALTER TABLE tb_role_authority ADD CONSTRAINT fk_tbrolaut_on_authority FOREIGN KEY (authority_id) REFERENCES authority (id);
ALTER TABLE tb_role_authority ADD CONSTRAINT fk_tbrolaut_on_role FOREIGN KEY (role_id) REFERENCES role (id);
```

### `V3__Create_Users_Table.sql`

```sql
CREATE TABLE users (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    name VARCHAR(255),
    email VARCHAR(255),
    cpf VARCHAR(255),
    password VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

CREATE TABLE tb_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    CONSTRAINT pk_tb_user_role PRIMARY KEY (user_id, role_id)
);

ALTER TABLE tb_user_role ADD CONSTRAINT fk_tbuserol_on_role FOREIGN KEY (role_id) REFERENCES role (id);
ALTER TABLE tb_user_role ADD CONSTRAINT fk_tbuserol_on_user FOREIGN KEY (user_id) REFERENCES users (id);
```

### `V4__Create_Address_Table.sql`

```sql
CREATE TABLE endereco (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    street VARCHAR(255),
    neighborhood VARCHAR(255),
    city VARCHAR(255),
    state VARCHAR(255),
    zip_code VARCHAR(255),
    country VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_endereco PRIMARY KEY (id)
);
```

### `V5__Create_GoatFarm_Table.sql`

```sql
CREATE TABLE capril (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    name VARCHAR(255) NOT NULL,
    tod VARCHAR(5),
    user_id BIGINT NOT NULL,
    address_id BIGINT,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    version INTEGER,
    CONSTRAINT pk_capril PRIMARY KEY (id)
);

ALTER TABLE capril ADD CONSTRAINT uc_capril_name UNIQUE (name);
ALTER TABLE capril ADD CONSTRAINT uc_capril_tod UNIQUE (tod);
ALTER TABLE capril ADD CONSTRAINT FK_CAPRIL_ON_ADDRESS FOREIGN KEY (address_id) REFERENCES endereco (id);
ALTER TABLE capril ADD CONSTRAINT FK_CAPRIL_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);
```

### `V6__Create_Phone_Table.sql`

```sql
CREATE TABLE telefone (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    ddd VARCHAR(255),
    number VARCHAR(255),
    goat_farm_id BIGINT,
    CONSTRAINT pk_telefone PRIMARY KEY (id)
);

ALTER TABLE telefone ADD CONSTRAINT FK_TELEFONE_ON_GOAT_FARM FOREIGN KEY (goat_farm_id) REFERENCES capril (id);
```

### `V7__Create_Goat_Table.sql`

```sql
CREATE TABLE cabras (
    num_registro VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    gender VARCHAR(255),
    breed VARCHAR(255),
    color VARCHAR(255),
    birth_date date,
    status VARCHAR(255),
    tod VARCHAR(255),
    toe VARCHAR(255),
    category VARCHAR(255),
    father_id VARCHAR(255),
    mother_id VARCHAR(255),
    farm_id BIGINT,
    user_id BIGINT,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_cabras PRIMARY KEY (num_registro)
);

ALTER TABLE cabras ADD CONSTRAINT FK_CABRAS_ON_FARM FOREIGN KEY (farm_id) REFERENCES capril (id);
ALTER TABLE cabras ADD CONSTRAINT FK_CABRAS_ON_FATHER FOREIGN KEY (father_id) REFERENCES cabras (num_registro);
ALTER TABLE cabras ADD CONSTRAINT FK_CABRAS_ON_MOTHER FOREIGN KEY (mother_id) REFERENCES cabras (num_registro);
ALTER TABLE cabras ADD CONSTRAINT FK_CABRAS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);
```

### `V8__Create_Genealogy_Table.sql`

```sql
CREATE TABLE genealogia (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    goat_name VARCHAR(255),
    goat_registration VARCHAR(255),
    goat_breed VARCHAR(255),
    goat_coat_color VARCHAR(255),
    goat_status VARCHAR(255),
    goat_sex VARCHAR(255),
    goat_category VARCHAR(255),
    goat_tod VARCHAR(255),
    goat_toe VARCHAR(255),
    goat_birth_date VARCHAR(255),
    goat_creator VARCHAR(255),
    goat_owner VARCHAR(255),
    father_name VARCHAR(255),
    father_registration VARCHAR(255),
    paternal_grandfather_name VARCHAR(255),
    paternal_grandfather_registration VARCHAR(255),
    paternal_great_grandfather1_name VARCHAR(255),
    paternal_great_grandfather1_registration VARCHAR(255),
    paternal_great_grandmother1_name VARCHAR(255),
    paternal_great_grandmother1_registration VARCHAR(255),
    paternal_grandmother_name VARCHAR(255),
    paternal_grandmother_registration VARCHAR(255),
    paternal_great_grandfather2_name VARCHAR(255),
    paternal_great_grandfather2_registration VARCHAR(255),
    paternal_great_grandmother2_name VARCHAR(255),
    paternal_great_grandmother2_registration VARCHAR(255),
    mother_name VARCHAR(255),
    mother_registration VARCHAR(255),
    maternal_grandfather_name VARCHAR(255),
    maternal_grandfather_registration VARCHAR(255),
    maternal_great_grandfather1_name VARCHAR(255),
    maternal_great_grandfather1_registration VARCHAR(255),
    maternal_great_grandmother1_name VARCHAR(255),
    maternal_great_grandmother1_registration VARCHAR(255),
    maternal_grandmother_name VARCHAR(255),
    maternal_grandmother_registration VARCHAR(255),
    maternal_great_grandfather2_name VARCHAR(255),
    maternal_great_grandfather2_registration VARCHAR(255),
    maternal_great_grandmother2_name VARCHAR(255),
    maternal_great_grandmother2_registration VARCHAR(255),
    CONSTRAINT pk_genealogia PRIMARY KEY (id)
);
```

### `V9__Create_Event_Table.sql`

```sql
CREATE TABLE eventos (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    event_type VARCHAR(255),
    event_date date,
    description VARCHAR(255),
    goat_id VARCHAR(255),
    CONSTRAINT pk_eventos PRIMARY KEY (id)
);

ALTER TABLE eventos ADD CONSTRAINT FK_EVENTOS_ON_GOAT FOREIGN KEY (goat_id) REFERENCES cabras (num_registro);
```

### `V10__Update_Enums_To_Portuguese.sql`

```sql
-- Migration to update enum values to Portuguese
-- This is an example and should be adapted to the actual needs of the application
```

## 5. Código-Fonte Completo

### `GoatFarmApplication.java`

```java
package com.devmaster.goatfarm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GoatFarmApplication {

	public static void main(String[] args) {
		SpringApplication.run(GoatFarmApplication.class, args);
	}

}
```

### Módulo `address`

#### `address/api/controller/AddressController.java`

```java
package com.devmaster.goatfarm.address.api.controller;

import com.devmaster.goatfarm.address.api.dto.AddressRequestDTO;
import com.devmaster.goatfarm.address.api.dto.AddressResponseDTO;
import com.devmaster.goatfarm.address.facade.AddressFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goatfarms/{farmId}/addresses")
public class AddressController {

    @Autowired
    private AddressFacade addressFacade;

    @Operation(summary = "Create a new address for a farm", description = "New address data")
    @PostMapping
    public ResponseEntity<AddressResponseDTO> createAddress(@PathVariable Long farmId, @RequestBody @Valid AddressRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(addressFacade.createAddress(farmId, requestDTO));
    }

    @Operation(summary = "Update an existing address for a farm", description = "Updated address data")
    @PutMapping("/{addressId}")
    public ResponseEntity<AddressResponseDTO> updateAddress(
            @PathVariable Long farmId,
            @PathVariable("addressId") Long addressId,
            @RequestBody @Valid AddressRequestDTO requestDTO) {
        return ResponseEntity.ok(addressFacade.updateAddress(farmId, addressId, requestDTO));
    }

    @Operation(summary = "Find an address by ID for a farm")
    @GetMapping("/{addressId}")
    public ResponseEntity<AddressResponseDTO> findAddressById(
            @PathVariable Long farmId,
            @Parameter(description = "ID of the address to be searched", example = "1") @PathVariable Long addressId) {
        return ResponseEntity.ok(addressFacade.findAddressById(farmId, addressId));
    }

    @Operation(summary = "Remove an address by ID for a farm")
    @DeleteMapping("/{addressId}")
    public ResponseEntity<String> deleteAddress(
            @PathVariable Long farmId,
            @Parameter(description = "ID of the address to be removed", example = "1") @PathVariable Long addressId) {
        return ResponseEntity.ok(addressFacade.deleteAddress(farmId, addressId));
    }

    // Este endpoint pode precisar ser revisto se a intenção é listar apenas endereços de uma fazenda específica
    @Operation(summary = "List all registered addresses (consider if this should be farm-specific)")
    @GetMapping("/all") // Mudei o path para evitar conflito com o GET /api/goatfarms/{farmId}/addresses
    public ResponseEntity<List<AddressResponseDTO>> findAllAddresses() {
        return ResponseEntity.ok(addressFacade.findAllAddresses());
    }
}
```

#### `address/api/dto/AddressDTO.java`

```java
package com.devmaster.goatfarm.address.api.dto;

import lombok.Data;

@Data
public class AddressDTO {
    private Long id;
    private String street;
    private String neighborhood;
    private String city;
    private String state;
    private String zipCode;
    private String country;
}
```

#### `address/api/dto/AddressRequestDTO.java`

```java
package com.devmaster.goatfarm.address.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddressRequestDTO {
    @NotBlank
    private String street;
    @NotBlank
    private String neighborhood;
    @NotBlank
    private String city;
    @NotBlank
    private String state;
    @NotBlank
    private String zipCode;
    private String country;
}
```

#### `address/api/dto/AddressResponseDTO.java`

```java
package com.devmaster.goatfarm.address.api.dto;

import lombok.Data;

@Data
public class AddressResponseDTO {
    private Long id;
    private String street;
    private String neighborhood;
    private String city;
    private String state;
    private String zipCode;
    private String country;
}
```

#### `address/business/AddressBusiness.java`

```java
package com.devmaster.goatfarm.address.business;

import com.devmaster.goatfarm.address.business.bo.AddressRequestVO;
import com.devmaster.goatfarm.address.business.bo.AddressResponseVO;
import com.devmaster.goatfarm.address.dao.AddressDAO;
import com.devmaster.goatfarm.address.mapper.AddressMapper;
import com.devmaster.goatfarm.address.model.entity.Address;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.config.exceptions.custom.ValidationError;
import com.devmaster.goatfarm.config.exceptions.custom.ValidationException;
import com.devmaster.goatfarm.config.security.OwnershipService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AddressBusiness {

    @Autowired
    private AddressDAO addressDAO;

    @Autowired
    private AddressMapper addressMapper;

    @Autowired
    private OwnershipService ownershipService;

    public AddressResponseVO createAddress(Long farmId, AddressRequestVO requestVO) {
        ownershipService.verifyFarmOwnership(farmId);
        validateAddressData(requestVO);
        Address entity = addressMapper.toEntity(requestVO);
        Address saved = addressDAO.createAddress(entity);
        return addressMapper.toResponseVO(saved);
    }

    public AddressResponseVO updateAddress(Long farmId, Long addressId, AddressRequestVO requestVO) {
        ownershipService.verifyFarmOwnership(farmId);
        validateAddressData(requestVO);
        Address current = addressDAO.findByIdAndFarmId(addressId, farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Endereço com ID " + addressId + " não encontrado na fazenda " + farmId));
        addressMapper.toEntity(current, requestVO);
        Address updated = addressDAO.updateAddress(addressId, current);
        return addressMapper.toResponseVO(updated);
    }

    public AddressResponseVO findAddressById(Long farmId, Long addressId) {
        ownershipService.verifyFarmOwnership(farmId);
        Address entity = addressDAO.findByIdAndFarmId(addressId, farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Endereço com ID " + addressId + " não encontrado na fazenda " + farmId));
        return addressMapper.toResponseVO(entity);
    }

    @Transactional(readOnly = true)
    public Address getAddressEntityById(Long farmId, Long addressId) {
        ownershipService.verifyFarmOwnership(farmId);
        return addressDAO.findByIdAndFarmId(addressId, farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Endereço com ID " + addressId + " não encontrado na fazenda " + farmId));
    }

    public String deleteAddress(Long farmId, Long addressId) {
        ownershipService.verifyFarmOwnership(farmId);
        Address entity = addressDAO.findByIdAndFarmId(addressId, farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Endereço com ID " + addressId + " não encontrado na fazenda " + farmId));
        return addressDAO.deleteAddress(addressId);
    }

    public Address findOrCreateAddressEntity(AddressRequestVO requestVO) {
        validateAddressData(requestVO);
        return addressDAO.searchExactAddress(
                        requestVO.getStreet(),
                        requestVO.getNeighborhood(),
                        requestVO.getCity(),
                        requestVO.getState(),
                        requestVO.getZipCode()
                )
                .orElseGet(() -> addressDAO.createAddress(addressMapper.toEntity(requestVO)));
    }

    private void validateAddressData(AddressRequestVO requestVO) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        ValidationError validationError = new ValidationError(Instant.now(), 422, "Erro de validação", request.getRequestURI());

        if (requestVO.getZipCode() != null) {
            String cep = requestVO.getZipCode().replaceAll("[^0-9]", "");
            if (!cep.matches("^\\d{8}$")) {
                validationError.addError("zipCode", "CEP deve conter exatamente 8 dígitos numéricos");
            }
        }
        if (requestVO.getState() != null) {
            if (!isValidBrazilianState(requestVO.getState())) {
                validationError.addError("state", "Estado deve ser uma sigla válida (ex: SP, RJ, MG) ou nome completo (ex: São Paulo, Rio de Janeiro, Minas Gerais)");
            }
        }
        if (requestVO.getCountry() != null &&
                !requestVO.getCountry().trim().equalsIgnoreCase("Brasil") &&
                !requestVO.getCountry().trim().equalsIgnoreCase("Brazil")) {
            validationError.addError("country", "Por enquanto, apenas endereços do Brasil são aceitos");
        }

        if (!validationError.getErrors().isEmpty()) {
            throw new ValidationException(validationError);
        }
    }

    private boolean isValidBrazilianState(String state) {
        // ... (lógica mantida como está)
    }
}
```

#### `address/business/bo/AddressRequestVO.java`

```java
package com.devmaster.goatfarm.address.business.bo;

import lombok.Data;

@Data
public class AddressRequestVO {
    private String street;
    private String neighborhood;
    private String city;
    private String state;
    private String zipCode;
    private String country;
}
```

#### `address/business/bo/AddressResponseVO.java`

```java
package com.devmaster.goatfarm.address.business.bo;

import lombok.Data;

@Data
public class AddressResponseVO {
    private Long id;
    private String street;
    private String neighborhood;
    private String city;
    private String state;
    private String zipCode;
    private String country;
}
```

#### `address/business/bo/AddressVO.java`

```java
package com.devmaster.goatfarm.address.business.bo;

import lombok.Data;

@Data
public class AddressVO {
    private Long id;
    private String street;
    private String neighborhood;
    private String city;
    private String state;
    private String zipCode;
    private String country;
}
```

#### `address/dao/AddressDAO.java`

```java
package com.devmaster.goatfarm.address.dao;

import com.devmaster.goatfarm.address.mapper.AddressMapper;
import com.devmaster.goatfarm.address.model.entity.Address;
import com.devmaster.goatfarm.address.model.repository.AddressRepository;
import com.devmaster.goatfarm.config.exceptions.custom.DatabaseException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AddressDAO {

    @Autowired
    private AddressRepository adressRepository;

    @Autowired
    private AddressMapper addressMapper;

    @Transactional
    public Address createAddress(Address address) {
        if (address == null) {
            throw new IllegalArgumentException("A entidade Address para criação não pode ser nula.");
        }
        try {
            return adressRepository.save(address);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Erro ao salvar o endereço: " + e.getMessage());
        }
    }

    @Transactional
    public Address updateAddress(Long id, Address address) {
        Address addressToUpdate = adressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Endereço com ID " + id + " não encontrado."));
        addressToUpdate.setStreet(address.getStreet());
        addressToUpdate.setNeighborhood(address.getNeighborhood());
        addressToUpdate.setCity(address.getCity());
        addressToUpdate.setState(address.getState());
        addressToUpdate.setZipCode(address.getZipCode());
        addressToUpdate.setCountry(address.getCountry());
        try {
            return adressRepository.save(addressToUpdate);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Erro ao atualizar o endereço com ID " + id + ": " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Address findAddressById(Long id) {
        return adressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Endereço com ID " + id + " não encontrado."));
    }

    @Transactional(readOnly = true)
    public Optional<Address> findByIdAndFarmId(Long id, Long farmId) {
        return adressRepository.findByIdAndGoatFarmId(id, farmId);
    }

    @Transactional(readOnly = true)
    public List<Address> findAllAddresses() {
        return adressRepository.findAll();
    }

    @Transactional
    public String deleteAddress(Long id) {
        if (!adressRepository.existsById(id)) {
            throw new ResourceNotFoundException("Endereço com ID " + id + " não encontrado.");
        }
        try {
            adressRepository.deleteById(id);
            return "Endereço com ID " + id + " foi deletado com sucesso.";
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Não é possível deletar o endereço com ID " + id + " porque ele possui relacionamentos com outras entidades.");
        }
    }

    @Transactional(readOnly = true)
    public Optional<Address> searchExactAddress(String street,
                                                String neighborhood,
                                                String city,
                                                String state,
                                                String zipCode) {
        return adressRepository.searchExactAddress(street, neighborhood, city, state, zipCode);
    }

    @Transactional
    public void deleteAddressesFromOtherUsers(Long adminId) {
        adressRepository.deleteAddressesFromOtherUsers(adminId);
    }
}
```

#### `address/facade/AddressFacade.java`

```java
package com.devmaster.goatfarm.address.facade;

import com.devmaster.goatfarm.address.api.dto.AddressRequestDTO;
import com.devmaster.goatfarm.address.api.dto.AddressResponseDTO;
import com.devmaster.goatfarm.address.business.AddressBusiness;
import com.devmaster.goatfarm.address.mapper.AddressMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AddressFacade {

    @Autowired
    private AddressBusiness addressBusiness;

    @Autowired
    private AddressMapper addressMapper;

    public AddressResponseDTO createAddress(Long farmId, AddressRequestDTO requestDTO) {
        return addressMapper.toDTO(addressBusiness.createAddress(farmId, addressMapper.toVO(requestDTO)));
    }

    public AddressResponseDTO updateAddress(Long farmId, Long addressId, AddressRequestDTO requestDTO) {
        return addressMapper.toDTO(addressBusiness.updateAddress(farmId, addressId, addressMapper.toVO(requestDTO)));
    }

    public AddressResponseDTO findAddressById(Long farmId, Long addressId) {
        return addressMapper.toDTO(addressBusiness.findAddressById(farmId, addressId));
    }

    public List<AddressResponseDTO> findAllAddresses() {
        // Este método não tem farmId, pois lista todos os endereços. Manter como está ou refatorar se necessário.
        return addressBusiness.findAllAddresses().stream()
                .map(addressMapper::toDTO)
                .collect(Collectors.toList());
    }

    public String deleteAddress(Long farmId, Long addressId) {
        return addressBusiness.deleteAddress(farmId, addressId);
    }
}
```

#### `address/mapper/AddressMapper.java`

```java
package com.devmaster.goatfarm.address.mapper;

import com.devmaster.goatfarm.address.api.dto.AddressRequestDTO;
import com.devmaster.goatfarm.address.api.dto.AddressResponseDTO;
import com.devmaster.goatfarm.address.business.bo.AddressRequestVO;
import com.devmaster.goatfarm.address.business.bo.AddressResponseVO;
import com.devmaster.goatfarm.address.model.entity.Address;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AddressMapper {
    AddressResponseDTO toDTO(AddressResponseVO vo);
    AddressRequestVO toVO(AddressRequestDTO dto);
    AddressResponseVO toResponseVO(Address entity);
    Address toEntity(AddressRequestVO vo);
    void toEntity(@MappingTarget Address entity, AddressRequestVO vo);
}
```

#### `address/model/entity/Address.java`

```java
package com.devmaster.goatfarm.address.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "endereco")
@Getter
@Setter
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String street;
    private String neighborhood;
    private String city;
    private String state;
    private String zipCode;
    private String country;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
```

#### `address/model/repository/AddressRepository.java`

```java
package com.devmaster.goatfarm.address.model.repository;

import com.devmaster.goatfarm.address.model.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {

    @Query("SELECT a FROM Address a WHERE a.street = :street AND a.neighborhood = :neighborhood AND a.city = :city AND a.state = :state AND a.zipCode = :zipCode")
    Optional<Address> searchExactAddress(@Param("street") String street,
                                       @Param("neighborhood") String neighborhood,
                                       @Param("city") String city,
                                       @Param("state") String state,
                                       @Param("zipCode") String zipCode);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "DELETE FROM endereco WHERE id IN (SELECT c.address_id FROM capril c WHERE c.user_id != :adminId AND c.address_id IS NOT NULL)")
    void deleteAddressesFromOtherUsers(@Param("adminId") Long adminId);

    // NOVO: Busca por ID do endereço e ID da fazenda
    Optional<Address> findByIdAndGoatFarmId(Long id, Long goatFarmId);
}
```

### Módulo `authority`

... (e assim por diante para todos os outros módulos e arquivos) ...
