# Guia de Refatoração Frontend: Owner → User

**Data:** Janeiro 2025  
**Objetivo:** Alinhar frontend com refatoração Owner → User do backend  
**Status Backend:** ✅ Concluído  
**Status Frontend:** 🔄 Pendente

## 📋 Resumo das Mudanças no Backend

O backend foi completamente refatorado para unificar as entidades `Owner` e `User`:
- ✅ Entidade `Owner` substituída por `User` (com campo CPF adicionado)
- ✅ Relacionamentos `GoatFarm` e `Goat` agora usam `User`
- ✅ APIs atualizadas para trabalhar com `User`
- ✅ Migração de dados concluída
- ✅ Sistema funcionando na porta 8080

## 🎯 Mudanças Necessárias no Frontend

### 1. **Atualização de Endpoints da API**

#### Endpoints Removidos/Alterados:
```javascript
// ❌ REMOVER - Endpoints de Owner não existem mais
const OWNER_ENDPOINTS = {
    getAll: '/owners',
    getById: '/owners/{id}',
    create: '/owners',
    update: '/owners/{id}',
    delete: '/owners/{id}',
    getByUserId: '/owners/user/{userId}' // Este ainda existe temporariamente
};
```

#### Novos Endpoints para User:
```javascript
// ✅ USAR - Endpoints de User
const USER_ENDPOINTS = {
    getAll: '/users',
    getById: '/users/{id}',
    create: '/users',
    update: '/users/{id}',
    delete: '/users/{id}',
    // Novos campos disponíveis: name, email, cpf, password, roles
};
```

### 2. **Atualização de Modelos de Dados**

#### Modelo Owner (REMOVER):
```javascript
// ❌ REMOVER - Modelo Owner
class Owner {
    constructor(id, nome, email, cpf) {
        this.id = id;
        this.nome = nome;  // ← nome
        this.email = email;
        this.cpf = cpf;
    }
}
```

#### Modelo User (ATUALIZAR):
```javascript
// ✅ ATUALIZAR - Modelo User expandido
class User {
    constructor(id, name, email, cpf, password, roles) {
        this.id = id;
        this.name = name;      // ← name (não "nome")
        this.email = email;
        this.cpf = cpf;        // ← Novo campo
        this.password = password; // Para criação/atualização
        this.roles = roles;    // Array de roles
    }
}
```

### 3. **Atualização de Componentes React/Vue/Angular**

#### Componente OwnerList (REFATORAR):
```javascript
// ❌ ANTES - OwnerList.js
class OwnerList extends Component {
    async loadOwners() {
        const response = await fetch('/api/owners');
        const owners = await response.json();
        this.setState({ owners });
    }
    
    render() {
        return (
            <div>
                {this.state.owners.map(owner => (
                    <div key={owner.id}>
                        <h3>{owner.nome}</h3> {/* nome */}
                        <p>{owner.email}</p>
                        <p>{owner.cpf}</p>
                    </div>
                ))}
            </div>
        );
    }
}
```

```javascript
// ✅ DEPOIS - UserList.js (ou manter OwnerList mas usar User)
class UserList extends Component {
    async loadUsers() {
        const response = await fetch('/api/users');
        const users = await response.json();
        this.setState({ users });
    }
    
    render() {
        return (
            <div>
                {this.state.users.map(user => (
                    <div key={user.id}>
                        <h3>{user.name}</h3> {/* name, não nome */}
                        <p>{user.email}</p>
                        <p>{user.cpf}</p>
                        <p>Roles: {user.roles?.join(', ')}</p> {/* Novo */}
                    </div>
                ))}
            </div>
        );
    }
}
```

### 4. **Formulários de Cadastro/Edição**

#### Formulário Owner (REFATORAR):
```javascript
// ❌ ANTES - OwnerForm.js
class OwnerForm extends Component {
    constructor() {
        this.state = {
            nome: '',     // ← nome
            email: '',
            cpf: ''
        };
    }
    
    async submitOwner() {
        const ownerData = {
            nome: this.state.nome,
            email: this.state.email,
            cpf: this.state.cpf
        };
        
        await fetch('/api/owners', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(ownerData)
        });
    }
}
```

```javascript
// ✅ DEPOIS - UserForm.js
class UserForm extends Component {
    constructor() {
        this.state = {
            name: '',        // ← name
            email: '',
            cpf: '',
            password: '',    // ← Novo campo obrigatório
            roles: ['ROLE_OPERATOR'] // ← Novo campo
        };
    }
    
    async submitUser() {
        const userData = {
            name: this.state.name,
            email: this.state.email,
            cpf: this.state.cpf,
            password: this.state.password,
            roles: this.state.roles
        };
        
        await fetch('/api/users', {
            method: 'POST',
            headers: { 
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${getAuthToken()}` // Necessário
            },
            body: JSON.stringify(userData)
        });
    }
}
```

### 5. **Atualização de Relacionamentos**

#### GoatFarm com Owner (ATUALIZAR):
```javascript
// ❌ ANTES - GoatFarm model
class GoatFarm {
    constructor(id, name, ownerId, ownerName) {
        this.id = id;
        this.name = name;
        this.ownerId = ownerId;     // ← owner_id
        this.ownerName = ownerName; // ← owner.nome
    }
}

// ❌ ANTES - Buscar fazendas do proprietário
async getGoatFarmsByOwner(ownerId) {
    const response = await fetch(`/api/goatfarms?ownerId=${ownerId}`);
    return response.json();
}
```

```javascript
// ✅ DEPOIS - GoatFarm model
class GoatFarm {
    constructor(id, name, userId, userName) {
        this.id = id;
        this.name = name;
        this.userId = userId;       // ← user_id
        this.userName = userName;   // ← user.name
    }
}

// ✅ DEPOIS - Buscar fazendas do usuário
async getGoatFarmsByUser(userId) {
    const response = await fetch(`/api/goatfarms?userId=${userId}`);
    return response.json();
}
```

### 6. **Sistema de Autenticação e Autorização**

#### Verificação de Proprietário (ATUALIZAR):
```javascript
// ❌ ANTES - Lógica de verificação Owner
function isOwner(currentUserId, resourceOwnerId) {
    // Esta lógica estava causando erro: userIdNoToken=23 vs ownerIdNoRecurso=21
    return currentUserId === resourceOwnerId;
}

function canManageResource(resource) {
    const currentUserId = getCurrentUserId();
    return isOwner(currentUserId, resource.ownerId);
}
```

```javascript
// ✅ DEPOIS - Lógica de verificação User
function isResourceOwner(currentUserId, resourceUserId) {
    // Agora a comparação é direta: user_id === user_id
    return currentUserId === resourceUserId;
}

function canManageResource(resource) {
    const currentUserId = getCurrentUserId();
    const userRoles = getCurrentUserRoles();
    
    // Admin pode tudo
    if (userRoles.includes('ROLE_ADMIN')) {
        return true;
    }
    
    // Operator pode gerenciar seus próprios recursos
    if (userRoles.includes('ROLE_OPERATOR')) {
        return isResourceOwner(currentUserId, resource.userId);
    }
    
    return false;
}
```

### 7. **Atualização de Serviços/APIs**

#### Serviço Owner (REFATORAR):
```javascript
// ❌ ANTES - OwnerService.js
class OwnerService {
    async getAllOwners() {
        const response = await fetch('/api/owners');
        return response.json();
    }
    
    async getOwnerByUserId(userId) {
        const response = await fetch(`/api/owners/user/${userId}`);
        return response.json();
    }
    
    async createOwner(ownerData) {
        return fetch('/api/owners', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(ownerData)
        });
    }
}
```

```javascript
// ✅ DEPOIS - UserService.js
class UserService {
    async getAllUsers() {
        const response = await fetch('/api/users', {
            headers: { 'Authorization': `Bearer ${getAuthToken()}` }
        });
        return response.json();
    }
    
    async getUserById(userId) {
        const response = await fetch(`/api/users/${userId}`, {
            headers: { 'Authorization': `Bearer ${getAuthToken()}` }
        });
        return response.json();
    }
    
    async createUser(userData) {
        return fetch('/api/users', {
            method: 'POST',
            headers: { 
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${getAuthToken()}`
            },
            body: JSON.stringify(userData)
        });
    }
    
    async updateUser(userId, userData) {
        return fetch(`/api/users/${userId}`, {
            method: 'PUT',
            headers: { 
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${getAuthToken()}`
            },
            body: JSON.stringify(userData)
        });
    }
}
```

### 8. **Validações de Formulário**

#### Validações Owner (ATUALIZAR):
```javascript
// ❌ ANTES - Validações Owner
function validateOwner(ownerData) {
    const errors = {};
    
    if (!ownerData.nome || ownerData.nome.trim() === '') {
        errors.nome = 'Nome é obrigatório';
    }
    
    if (!ownerData.email || !isValidEmail(ownerData.email)) {
        errors.email = 'Email válido é obrigatório';
    }
    
    if (!ownerData.cpf || !isValidCPF(ownerData.cpf)) {
        errors.cpf = 'CPF válido é obrigatório';
    }
    
    return errors;
}
```

```javascript
// ✅ DEPOIS - Validações User
function validateUser(userData) {
    const errors = {};
    
    if (!userData.name || userData.name.trim() === '') {
        errors.name = 'Nome é obrigatório';
    }
    
    if (!userData.email || !isValidEmail(userData.email)) {
        errors.email = 'Email válido é obrigatório';
    }
    
    if (!userData.cpf || !isValidCPF(userData.cpf)) {
        errors.cpf = 'CPF válido é obrigatório';
    }
    
    if (!userData.password || userData.password.length < 6) {
        errors.password = 'Senha deve ter pelo menos 6 caracteres';
    }
    
    if (!userData.roles || userData.roles.length === 0) {
        errors.roles = 'Pelo menos uma role deve ser selecionada';
    }
    
    return errors;
}
```

### 9. **Atualização de Estados/Store (Redux/Vuex/etc)**

#### Redux Store (ATUALIZAR):
```javascript
// ❌ ANTES - Owner state
const initialState = {
    owners: [],
    selectedOwner: null,
    loading: false,
    error: null
};

const ownerReducer = (state = initialState, action) => {
    switch (action.type) {
        case 'LOAD_OWNERS_SUCCESS':
            return { ...state, owners: action.payload };
        case 'SELECT_OWNER':
            return { ...state, selectedOwner: action.payload };
        default:
            return state;
    }
};
```

```javascript
// ✅ DEPOIS - User state
const initialState = {
    users: [],
    selectedUser: null,
    loading: false,
    error: null
};

const userReducer = (state = initialState, action) => {
    switch (action.type) {
        case 'LOAD_USERS_SUCCESS':
            return { ...state, users: action.payload };
        case 'SELECT_USER':
            return { ...state, selectedUser: action.payload };
        case 'UPDATE_USER_SUCCESS':
            return {
                ...state,
                users: state.users.map(user => 
                    user.id === action.payload.id ? action.payload : user
                )
            };
        default:
            return state;
    }
};
```

### 10. **Rotas e Navegação**

#### Rotas Owner (ATUALIZAR):
```javascript
// ❌ ANTES - Rotas Owner
const routes = [
    { path: '/owners', component: OwnerList },
    { path: '/owners/new', component: OwnerForm },
    { path: '/owners/:id', component: OwnerDetail },
    { path: '/owners/:id/edit', component: OwnerForm }
];
```

```javascript
// ✅ DEPOIS - Rotas User (ou manter /owners mas usar User)
const routes = [
    { path: '/users', component: UserList },
    { path: '/users/new', component: UserForm },
    { path: '/users/:id', component: UserDetail },
    { path: '/users/:id/edit', component: UserForm },
    
    // OU manter URLs antigas para compatibilidade:
    { path: '/owners', component: UserList }, // Redireciona para users
    { path: '/owners/new', component: UserForm },
    { path: '/owners/:id', component: UserDetail },
    { path: '/owners/:id/edit', component: UserForm }
];
```

## 🔧 Checklist de Refatoração Frontend

### Fase 1: Preparação
- [ ] **Backup do código frontend atual**
- [ ] **Criar branch para refatoração**: `git checkout -b refactor/owner-to-user-frontend`
- [ ] **Documentar endpoints atuais em uso**

### Fase 2: Atualização de Modelos
- [ ] **Atualizar modelo User** (adicionar cpf, roles)
- [ ] **Remover/refatorar modelo Owner**
- [ ] **Atualizar modelos GoatFarm e Goat** (owner → user)

### Fase 3: Atualização de Serviços
- [ ] **Refatorar OwnerService → UserService**
- [ ] **Atualizar endpoints de API**
- [ ] **Adicionar headers de autorização**
- [ ] **Atualizar tratamento de erros**

### Fase 4: Atualização de Componentes
- [ ] **Refatorar OwnerList → UserList**
- [ ] **Refatorar OwnerForm → UserForm**
- [ ] **Atualizar OwnerDetail → UserDetail**
- [ ] **Corrigir campos nome → name**
- [ ] **Adicionar campos password e roles**

### Fase 5: Atualização de Lógica de Negócio
- [ ] **Corrigir verificação isOwner → isResourceOwner**
- [ ] **Atualizar canManageResource**
- [ ] **Corrigir comparações de IDs**
- [ ] **Implementar verificação de roles**

### Fase 6: Atualização de Estados
- [ ] **Refatorar Redux/Vuex store**
- [ ] **Atualizar actions e reducers**
- [ ] **Corrigir selectors**

### Fase 7: Atualização de Rotas
- [ ] **Atualizar definições de rotas**
- [ ] **Implementar redirecionamentos se necessário**
- [ ] **Atualizar navegação**

### Fase 8: Validação e Testes
- [ ] **Atualizar validações de formulário**
- [ ] **Testar criação de usuários**
- [ ] **Testar edição de usuários**
- [ ] **Testar listagem de usuários**
- [ ] **Testar relacionamentos (fazendas, cabras)**
- [ ] **Testar autenticação e autorização**

### Fase 9: Testes de Integração
- [ ] **Testar login com usuários migrados**
- [ ] **Testar CRUD completo de usuários**
- [ ] **Testar permissões ADMIN vs OPERATOR**
- [ ] **Testar relacionamentos com fazendas**
- [ ] **Testar relacionamentos com cabras**

### Fase 10: Finalização
- [ ] **Remover código obsoleto (Owner)**
- [ ] **Atualizar documentação**
- [ ] **Code review**
- [ ] **Deploy em ambiente de teste**
- [ ] **Testes de aceitação**

## 🚨 Pontos de Atenção Críticos

### 1. **Mudança de Campo: nome → name**
```javascript
// ❌ ERRO COMUM
owner.nome // Não existe mais

// ✅ CORRETO
user.name // Campo correto
```

### 2. **Autenticação Obrigatória**
```javascript
// ❌ ANTES - Endpoints Owner eram abertos
fetch('/api/owners')

// ✅ DEPOIS - Endpoints User requerem autenticação
fetch('/api/users', {
    headers: { 'Authorization': `Bearer ${token}` }
})
```

### 3. **Senha Obrigatória na Criação**
```javascript
// ❌ ANTES - Owner sem senha
{ nome: 'João', email: 'joao@email.com', cpf: '123.456.789-00' }

// ✅ DEPOIS - User com senha obrigatória
{ name: 'João', email: 'joao@email.com', cpf: '123.456.789-00', password: 'senha123' }
```

### 4. **Roles Obrigatórias**
```javascript
// ✅ SEMPRE incluir roles na criação
{
    name: 'João',
    email: 'joao@email.com',
    cpf: '123.456.789-00',
    password: 'senha123',
    roles: ['ROLE_OPERATOR'] // Obrigatório
}
```

### 5. **Verificação de Permissões**
```javascript
// ❌ ANTES - Comparação incorreta
if (currentUserId === resource.ownerId) // IDs diferentes

// ✅ DEPOIS - Comparação correta
if (currentUserId === resource.userId) // IDs iguais
```

## 📱 Exemplos de Implementação

### React Hooks Example:
```javascript
// UserList.jsx
import React, { useState, useEffect } from 'react';
import { UserService } from '../services/UserService';

const UserList = () => {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    
    useEffect(() => {
        loadUsers();
    }, []);
    
    const loadUsers = async () => {
        try {
            const userData = await UserService.getAllUsers();
            setUsers(userData);
        } catch (error) {
            console.error('Erro ao carregar usuários:', error);
        } finally {
            setLoading(false);
        }
    };
    
    if (loading) return <div>Carregando...</div>;
    
    return (
        <div>
            <h2>Usuários</h2>
            {users.map(user => (
                <div key={user.id} className="user-card">
                    <h3>{user.name}</h3>
                    <p>Email: {user.email}</p>
                    <p>CPF: {user.cpf}</p>
                    <p>Roles: {user.roles?.join(', ')}</p>
                </div>
            ))}
        </div>
    );
};

export default UserList;
```

### Vue.js Example:
```javascript
// UserList.vue
<template>
  <div>
    <h2>Usuários</h2>
    <div v-for="user in users" :key="user.id" class="user-card">
      <h3>{{ user.name }}</h3>
      <p>Email: {{ user.email }}</p>
      <p>CPF: {{ user.cpf }}</p>
      <p>Roles: {{ user.roles?.join(', ') }}</p>
    </div>
  </div>
</template>

<script>
import { UserService } from '@/services/UserService';

export default {
  name: 'UserList',
  data() {
    return {
      users: [],
      loading: true
    };
  },
  async mounted() {
    await this.loadUsers();
  },
  methods: {
    async loadUsers() {
      try {
        this.users = await UserService.getAllUsers();
      } catch (error) {
        console.error('Erro ao carregar usuários:', error);
      } finally {
        this.loading = false;
      }
    }
  }
};
</script>
```

## 🔗 Credenciais de Teste

Após a refatoração, use estas credenciais para testar:

```javascript
// Usuários disponíveis no sistema
const testUsers = [
    {
        email: 'albertovilar1@gmail.com',
        password: 'password123',
        roles: ['ROLE_ADMIN', 'ROLE_OPERATOR'],
        name: 'Alberto Vilar',
        cpf: '123.456.789-01'
    },
    {
        email: 'carlosmedeiros@email.com',
        password: 'password123',
        roles: ['ROLE_OPERATOR'],
        name: 'Carlos Medeiros',
        cpf: '987.654.321-00'
    }
];
```

## 📞 Suporte

- **Backend Status**: ✅ Funcionando na porta 8080
- **API Documentation**: http://localhost:8080/swagger-ui.html
- **Documentação Técnica**: `REFATORACAO_OWNER_USER.md`
- **Guia de Migração**: `MIGRATION_GUIDE.md`

---

**⚠️ IMPORTANTE**: Teste cada mudança incrementalmente e mantenha o backend rodando durante todo o processo de refatoração do frontend para validar as integrações.

**🎯 OBJETIVO**: Após esta refatoração, o frontend estará completamente alinhado com o backend, usando a entidade `User` unificada e eliminando toda referência à entidade `Owner`.