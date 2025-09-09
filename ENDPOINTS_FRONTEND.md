# 📋 Endpoints para Atualização do Frontend

## 🔓 ENDPOINTS PÚBLICOS (Sem Autenticação)

### Usuários
```
POST /users
```

### Fazendas (GoatFarms)
```
GET  /goatfarms
GET  /goatfarms/{id}
GET  /goatfarms/name
```

### Cabras (Goats)
```
GET  /goatfarms/goats/name
GET  /goatfarms/goats/registration/{number}
```

### Genealogia
```
GET  /genealogies/{registrationNumber}
```

---

## 🔒 ENDPOINTS PRIVADOS (Requerem Authorization: Bearer {token})

### Usuários
```
GET  /users/me
```
**Permissões**: ROLE_ADMIN ou ROLE_OPERATOR

### Fazendas (GoatFarms)
```
POST /goatfarms/full
POST /goatfarms
PUT  /goatfarms/{id}
DELETE /goatfarms/{id}
```
**Permissões**: ROLE_ADMIN ou ROLE_OPERATOR

### Cabras (Goats)
```
GET    /goatfarms/{farmId}/goats
POST   /goatfarms/{farmId}/goats
PUT    /goatfarms/{farmId}/goats/{id}
DELETE /goatfarms/{farmId}/goats/{id}
```
**Permissões**: ROLE_ADMIN ou ROLE_OPERATOR

### Endereços (Address)
```
GET    /address
GET    /address/{id}
POST   /address
PUT    /address/{id}
```
**Permissões**: ROLE_ADMIN ou ROLE_OPERATOR

### Telefones (Phones)
```
GET    /phones
POST   /phones
```
**Permissões**: ROLE_ADMIN ou ROLE_OPERATOR

### Genealogia
```
POST /genealogies/{registrationNumber}
```

---

## ❌ ENDPOINTS REMOVIDOS (NÃO USAR)

```
❌ GET    /owners
❌ GET    /owners/{id}
❌ GET    /owners/user/{userId}
❌ POST   /owners
❌ PUT    /owners/{id}
❌ DELETE /owners/{id}
❌ GET    /owners/search
```

---

## 🔐 AUTENTICAÇÃO

### OAuth2 Token
```
POST /oauth2/token
Content-Type: application/x-www-form-urlencoded

grant_type=password&username={email}&password={password}&client_id={id}&client_secret={secret}
```

---

## 📝 HEADERS OBRIGATÓRIOS

### Para Endpoints Privados
```javascript
const headers = {
  'Authorization': `Bearer ${token}`,
  'Content-Type': 'application/json'
};
```

---

## 🔄 MUDANÇAS DE ESTRUTURA

### User (novo modelo)
```json
{
  "id": 1,
  "name": "João Silva",
  "email": "joao@email.com",
  "cpf": "123.456.789-00",
  "roles": ["ROLE_OPERATOR"]
}
```

### GoatFarm (atualizado)
```json
{
  "id": 1,
  "nome": "Fazenda ABC",
  "userId": 23,
  "user": {
    "id": 23,
    "name": "João Silva"
  }
}
```