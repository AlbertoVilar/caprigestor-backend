# 🚨 INSTRUÇÕES URGENTES PARA O FRONTEND - BUSCA DE CABRAS

## ❌ PROBLEMA IDENTIFICADO

O endpoint `GET /goatfarms/{farmId}/goats` **NÃO ESTÁ FUNCIONANDO** no frontend porque:

1. **O endpoint NÃO é público** - Está configurado como PRIVADO no backend
2. **Precisa de autenticação** - Requer token Bearer
3. **Documentação estava incorreta** - Listava como público mas na verdade é privado

## ✅ SOLUÇÃO IMEDIATA

### 1. **ADICIONAR AUTENTICAÇÃO**

Para buscar cabras de uma fazenda, o frontend DEVE incluir o token de autenticação:

```javascript
// ✅ CORRETO - Com autenticação
const token = localStorage.getItem('authToken'); // ou onde você armazena o token

const response = await fetch(`http://localhost:8080/goatfarms/1/goats`, {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
});

if (response.ok) {
  const cabras = await response.json();
  console.log('Cabras encontradas:', cabras);
} else {
  console.error('Erro ao buscar cabras:', response.status);
}
```

### 2. **VERIFICAR SE O USUÁRIO ESTÁ LOGADO**

```javascript
// Verificar se tem token antes de fazer a requisição
if (!token) {
  console.error('Usuário não está logado!');
  // Redirecionar para login ou mostrar mensagem
  return;
}
```

### 3. **TRATAR ERROS DE AUTENTICAÇÃO**

```javascript
if (response.status === 401) {
  console.error('Token inválido ou expirado');
  // Limpar token e redirecionar para login
  localStorage.removeItem('authToken');
  window.location.href = '/login';
}

if (response.status === 403) {
  console.error('Usuário sem permissão para acessar este recurso');
}
```

## 🔐 ENDPOINTS QUE PRECISAM DE AUTENTICAÇÃO

**TODOS os endpoints de cabras precisam de token:**

```
🔒 GET    /goatfarms/{farmId}/goats     ← ESTE ENDPOINT!
🔒 POST   /goatfarms/{farmId}/goats
🔒 PUT    /goatfarms/{farmId}/goats/{id}
🔒 DELETE /goatfarms/{farmId}/goats/{id}
```

## 🔓 ENDPOINTS REALMENTE PÚBLICOS (Sem token)

```
✅ GET /goatfarms                    ← Listar fazendas
✅ GET /goatfarms/{id}               ← Detalhes da fazenda
✅ GET /goatfarms/name               ← Buscar fazenda por nome
✅ GET /goatfarms/goats/name         ← Buscar cabra por nome (global)
✅ GET /goatfarms/goats/registration/{number} ← Buscar por registro
```

## 🚀 EXEMPLO COMPLETO DE IMPLEMENTAÇÃO

```javascript
class GoatService {
  constructor() {
    this.baseURL = 'http://localhost:8080';
  }

  getAuthHeaders() {
    const token = localStorage.getItem('authToken');
    if (!token) {
      throw new Error('Usuário não autenticado');
    }
    return {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    };
  }

  // ✅ MÉTODO CORRETO para buscar cabras da fazenda
  async getGoatsByFarm(farmId, page = 0, size = 12) {
    try {
      const response = await fetch(
        `${this.baseURL}/goatfarms/${farmId}/goats?page=${page}&size=${size}`,
        {
          method: 'GET',
          headers: this.getAuthHeaders()
        }
      );

      if (!response.ok) {
        throw new Error(`Erro ${response.status}: ${response.statusText}`);
      }

      return await response.json();
    } catch (error) {
      console.error('Erro ao buscar cabras:', error);
      throw error;
    }
  }

  // ✅ MÉTODO para buscar cabra por registro (público)
  async getGoatByRegistration(registrationNumber) {
    const response = await fetch(
      `${this.baseURL}/goatfarms/goats/registration/${registrationNumber}`
    );
    
    if (!response.ok) {
      throw new Error(`Cabra não encontrada: ${registrationNumber}`);
    }
    
    return await response.json();
  }
}

// Uso:
const goatService = new GoatService();

// Buscar cabras da fazenda ID 1
goatService.getGoatsByFarm(1)
  .then(result => {
    console.log('Cabras da fazenda:', result.content);
    console.log('Total de páginas:', result.totalPages);
  })
  .catch(error => {
    console.error('Falha ao carregar cabras:', error.message);
  });
```

## ⚠️ IMPORTANTE

1. **Sempre verificar se o usuário está logado** antes de chamar endpoints privados
2. **Incluir o token Bearer** em TODOS os endpoints de cabras por fazenda
3. **Tratar erros 401/403** adequadamente
4. **Usar os endpoints públicos** quando não precisar de autenticação

## 🔧 CORREÇÃO NO ARQUIVO DE ENDPOINTS

O arquivo `ENDPOINTS_FRONTEND.md` foi **CORRIGIDO**. O endpoint `/goatfarms/{farmId}/goats` está agora listado corretamente como **PRIVADO**.

---

**🎯 RESUMO: O frontend precisa adicionar autenticação (token Bearer) para buscar cabras por fazenda!**