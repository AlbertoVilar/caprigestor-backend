# Documentação do Sistema de Segurança - GoatFarm

## Visão Geral

Este documento descreve a implementação completa do sistema de segurança e autorização do projeto GoatFarm, incluindo autenticação JWT, controle de acesso baseado em roles e verificação de ownership de recursos.

## Arquitetura de Segurança

### Componentes Principais

1. **Spring Security** - Framework base para autenticação e autorização
2. **JWT (JSON Web Tokens)** - Tokens para autenticação stateless
3. **BCrypt** - Algoritmo de hash para senhas
4. **Role-Based Access Control (RBAC)** - Controle de acesso baseado em papéis
5. **Ownership Verification** - Verificação de propriedade de recursos

## Estrutura do Banco de Dados

### Tabelas de Segurança

```sql
-- Tabela de Authorities (Permissões)
CREATE TABLE authority (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- Tabela de Roles (Papéis)
CREATE TABLE role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    authority VARCHAR(50) NOT NULL UNIQUE
);

-- Tabela de Usuários
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    enabled BOOLEAN DEFAULT TRUE
);

-- Tabela de Relacionamento Role-Authority
CREATE TABLE tb_role_authority (
    role_id BIGINT,
    authority_id BIGINT,
    PRIMARY KEY (role_id, authority_id)
);

-- Tabela de Relacionamento User-Role
CREATE TABLE tb_user_role (
    user_id BIGINT,
    role_id BIGINT,
    PRIMARY KEY (user_id, role_id)
);
```

### Dados Padrão

```sql
-- Authorities
INSERT INTO authority (name) VALUES 
('READ_GOATS'), ('WRITE_GOATS'), ('DELETE_GOATS'),
('READ_FARMS'), ('WRITE_FARMS'), ('DELETE_FARMS'),
('READ_GENEALOGY'), ('WRITE_GENEALOGY'), ('DELETE_GENEALOGY'),
('ADMIN_ACCESS');

-- Roles
INSERT INTO role (authority) VALUES 
('ROLE_ADMIN'), ('ROLE_FARM_OWNER');
```

## Roles e Permissões

### ROLE_ADMIN
- **Descrição**: Administrador do sistema
- **Permissões**: Acesso total a todos os recursos
- **Endpoints**: Todos os endpoints, incluindo `/api/admin/**`

### ROLE_FARM_OWNER
- **Descrição**: Proprietário de fazenda
- **Permissões**: CRUD apenas para recursos da própria fazenda
- **Endpoints**: Operações em fazendas, caprinos e genealogia próprios

### PUBLIC (Não autenticado)
- **Descrição**: Usuários não autenticados
- **Permissões**: Apenas leitura (GET) de recursos públicos
- **Endpoints**: `GET /api/goats/**`, `GET /api/genealogy/**`, `GET /api/farms/**`

## Configuração de Endpoints

### Endpoints Públicos (Sem Autenticação)
```java
.requestMatchers("/api/auth/**").permitAll()
.requestMatchers("/h2-console/**").permitAll()
```

### Endpoints de Leitura Pública
```java
.requestMatchers(HttpMethod.GET, "/api/goats/**").permitAll()
.requestMatchers(HttpMethod.GET, "/api/genealogy/**").permitAll()
.requestMatchers(HttpMethod.GET, "/api/farms/**").permitAll()
```

### Endpoints Administrativos
```java
.requestMatchers("/api/admin/**").hasRole("ADMIN")
```

### Endpoints CRUD Protegidos
```java
// Fazendas
.requestMatchers(HttpMethod.POST, "/api/farms/**").hasAnyRole("FARM_OWNER", "ADMIN")
.requestMatchers(HttpMethod.PUT, "/api/farms/**").hasAnyRole("FARM_OWNER", "ADMIN")
.requestMatchers(HttpMethod.DELETE, "/api/farms/**").hasAnyRole("FARM_OWNER", "ADMIN")

// Caprinos
.requestMatchers(HttpMethod.POST, "/api/goats/**").hasAnyRole("FARM_OWNER", "ADMIN")
.requestMatchers(HttpMethod.PUT, "/api/goats/**").hasAnyRole("FARM_OWNER", "ADMIN")
.requestMatchers(HttpMethod.DELETE, "/api/goats/**").hasAnyRole("FARM_OWNER", "ADMIN")

// Genealogia
.requestMatchers(HttpMethod.POST, "/api/genealogy/**").hasAnyRole("FARM_OWNER", "ADMIN")
.requestMatchers(HttpMethod.PUT, "/api/genealogy/**").hasAnyRole("FARM_OWNER", "ADMIN")
.requestMatchers(HttpMethod.DELETE, "/api/genealogy/**").hasAnyRole("FARM_OWNER", "ADMIN")
```

## Middleware de Autorização

### OwnershipService

O `OwnershipService` é responsável por verificar se um usuário tem permissão para acessar/modificar recursos específicos.

#### Métodos Principais

```java
// Obter usuário atual
public User getCurrentUser()

// Verificar se é ADMIN
public boolean isCurrentUserAdmin()

// Verificar ownership de fazenda
public void verifyFarmOwnership(GoatFarm farm)

// Verificar ownership de cabra
public void verifyGoatOwnership(Goat goat)

// Verificar acesso a dados de usuário
public void verifyUserAccess(Long userId)
```

#### Integração nos Controllers

```java
@Autowired
private OwnershipService ownershipService;

@PutMapping("/farms/{id}")
public ResponseEntity<GoatFarmFullResponseDTO> updateGoatFarm(@PathVariable Long id, @RequestBody GoatFarmUpdateRequestDTO requestDTO) {
    // Verificar ownership antes de atualizar
    GoatFarmFullResponseVO existingFarm = farmFacade.findGoatFarmById(id);
    ownershipService.verifyFarmOwnership(existingFarm.getFarm());
    
    // Continuar com a atualização...
}
```

## Fluxo de Autenticação

### 1. Login
```
POST /api/auth/login
{
  "email": "usuario@exemplo.com",
  "password": "senha123"
}
```

### 2. Resposta com JWT
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "expiresIn": 3600
}
```

### 3. Uso do Token
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

## Fluxo de Autorização

### 1. Verificação de Role
- Spring Security verifica se o usuário possui a role necessária
- Baseado na configuração de endpoints no `SecurityConfig`

### 2. Verificação de Ownership
- Para operações CRUD, o `OwnershipService` verifica se o usuário pode acessar o recurso
- ADMIN tem acesso a tudo
- FARM_OWNER só pode acessar recursos da própria fazenda

### 3. Tratamento de Exceções
- `ResourceNotFoundException` para recursos não encontrados ou sem permissão
- `RuntimeException` para usuários não autenticados

## Casos de Uso

### Usuário Público
- ✅ Visualizar lista de fazendas
- ✅ Visualizar caprinos
- ✅ Consultar genealogia
- ❌ Criar, editar ou deletar qualquer recurso

### Proprietário de Fazenda
- ✅ Todas as operações públicas
- ✅ CRUD na própria fazenda
- ✅ CRUD nos caprinos da própria fazenda
- ✅ CRUD na genealogia dos próprios caprinos
- ❌ Acessar recursos de outras fazendas
- ❌ Acessar área administrativa

### Administrador
- ✅ Acesso total a todos os recursos
- ✅ Área administrativa
- ✅ Gerenciar qualquer fazenda/caprino
- ✅ Operações de sistema

## Segurança Implementada

### Proteções
- **Autenticação JWT**: Tokens seguros para identificação
- **Autorização por Role**: Controle granular de acesso
- **Verificação de Ownership**: Usuários só acessam próprios recursos
- **Validação de Entrada**: Validação de dados nos DTOs
- **CORS Configurado**: Controle de origem das requisições
- **Senhas Criptografadas**: BCrypt para hash de senhas

### Boas Práticas
- Princípio do menor privilégio
- Separação de responsabilidades
- Tratamento adequado de exceções
- Logs de segurança (implementar se necessário)
- Tokens com expiração

## Testes de Segurança

### Cenários de Teste

1. **Acesso sem autenticação**
   - Verificar endpoints públicos funcionam
   - Verificar endpoints protegidos retornam 401

2. **Acesso com role inadequada**
   - FARM_OWNER tentando acessar área admin
   - Usuário tentando acessar fazenda de outro

3. **Ownership verification**
   - Proprietário acessando própria fazenda
   - Proprietário tentando acessar fazenda alheia
   - Admin acessando qualquer fazenda

4. **Token JWT**
   - Token válido
   - Token expirado
   - Token malformado

## Manutenção e Evolução

### Adicionando Novos Endpoints
1. Definir role necessária no `SecurityConfig`
2. Implementar verificação de ownership se necessário
3. Adicionar testes de segurança

### Adicionando Novas Roles
1. Criar migration para nova role
2. Atualizar `SecurityConfig`
3. Implementar lógica específica no `OwnershipService`

### Monitoramento
- Logs de tentativas de acesso negado
- Métricas de autenticação
- Auditoria de operações sensíveis

## Arquivos Relacionados

- `SecurityConfig.java` - Configuração principal de segurança
- `OwnershipService.java` - Service de verificação de ownership
- `CustomUserDetailsService.java` - Carregamento de usuários
- `V1__Create_Security_Tables.sql` - Criação das tabelas
- `V2__Insert_Default_Authorities_And_Roles.sql` - Dados padrão
- Controllers com integração de segurança

---

**Última atualização**: Janeiro 2025
**Versão**: 1.0
**Autor**: Sistema de Desenvolvimento GoatFarm