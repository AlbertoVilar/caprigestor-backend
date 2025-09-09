# 🔐 PERMISSÕES PARA EVENTOS DE CABRAS

## 📋 RESUMO DAS PERMISSÕES

O endpoint `/goats/{registrationNumber}/events` tem **DIFERENTES PERMISSÕES** para cada operação:

### 🔓 VISUALIZAR EVENTOS (GET)
```
GET /goats/{registrationNumber}/events
```
**❌ PROBLEMA IDENTIFICADO:** Este endpoint **NÃO TEM** `@PreAuthorize` no código, mas está sendo **BLOQUEADO** pela configuração de segurança!

**🚨 CAUSA:** O endpoint não está listado como público no `ResourceServerConfig.java`, então por padrão requer autenticação.

### 🔒 CRIAR EVENTOS (POST)
```
POST /goats/{registrationNumber}/events
```
**Permissões:** `ROLE_ADMIN` ou `ROLE_OPERATOR`
**Autenticação:** Obrigatória

### 🔒 ATUALIZAR EVENTOS (PUT)
```
PUT /goats/{registrationNumber}/events/{id}
```
**Permissões:** `ROLE_ADMIN` ou `ROLE_OPERATOR`
**Autenticação:** Obrigatória

### 🔒 DELETAR EVENTOS (DELETE)
```
DELETE /goats/{registrationNumber}/events/{id}
```
**Permissões:** Apenas `ROLE_ADMIN`
**Autenticação:** Obrigatória

---

## 🚨 PROBLEMA ATUAL

### ❌ Endpoint GET Bloqueado
O endpoint `GET /goats/{registrationNumber}/events` está retornando **401 Não Autorizado** porque:

1. **Não tem `@PreAuthorize`** no controller (deveria ser público)
2. **Não está configurado como público** no `ResourceServerConfig.java`
3. **Por padrão, todos os endpoints requerem autenticação**

### ✅ Soluções Possíveis

#### Opção 1: Tornar o endpoint público
Adicionar no `ResourceServerConfig.java`:
```java
.requestMatchers(HttpMethod.GET, "/goats/*/events").permitAll()
```

#### Opção 2: Manter privado e exigir autenticação
O frontend deve incluir token:
```javascript
const response = await fetch(`http://localhost:8080/goats/2114517012/events`, {
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
});
```

---

## 📝 INSTRUÇÕES PARA O FRONTEND

### 🔐 Para Visualizar Eventos (Atual - Requer Token)
```javascript
// ✅ CORRETO - Com autenticação
const token = localStorage.getItem('authToken');

if (!token) {
  console.error('Usuário não está logado!');
  return;
}

const response = await fetch(`http://localhost:8080/goats/2114517012/events`, {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
});

if (response.ok) {
  const eventos = await response.json();
  console.log('Eventos encontrados:', eventos);
} else if (response.status === 401) {
  console.error('Token inválido ou expirado');
  // Redirecionar para login
} else {
  console.error('Erro ao buscar eventos:', response.status);
}
```

### 🔒 Para Criar/Editar Eventos (Sempre Requer Permissões)
```javascript
// Para POST, PUT, DELETE - sempre precisa de ROLE_ADMIN ou ROLE_OPERATOR
const response = await fetch(`http://localhost:8080/goats/2114517012/events`, {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    eventType: 'SAUDE',
    date: '2025-01-15',
    description: 'Vacinação anual'
  })
});
```

---

## 🎯 RECOMENDAÇÃO

**Para o sistema atual:** O endpoint de visualização de eventos deveria ser **PÚBLICO** (sem autenticação), pois é informação básica sobre os animais.

**Configuração sugerida:**
- 🔓 **GET** `/goats/{registrationNumber}/events` → Público
- 🔒 **POST/PUT** `/goats/{registrationNumber}/events` → ROLE_ADMIN ou ROLE_OPERATOR  
- 🔒 **DELETE** `/goats/{registrationNumber}/events/{id}` → Apenas ROLE_ADMIN

---

## 📊 FILTROS DISPONÍVEIS

O endpoint GET suporta filtros opcionais:
```
GET /goats/{registrationNumber}/events?eventType=SAUDE&startDate=2025-01-01&endDate=2025-12-31
```

**Parâmetros:**
- `eventType`: COBERTURA, PARTO, MORTE, SAUDE, VACINACAO, TRANSFERENCIA, MUDANCA_PROPRIETARIO, PESAGEM, OUTRO
- `startDate`: Data inicial (formato: YYYY-MM-DD)
- `endDate`: Data final (formato: YYYY-MM-DD)
- Paginação automática

---

**🔧 STATUS ATUAL:** O endpoint GET está bloqueado e precisa de token para funcionar.