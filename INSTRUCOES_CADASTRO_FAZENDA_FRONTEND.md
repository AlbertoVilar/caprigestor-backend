# Instruções para Implementação do Cadastro de Fazenda no Frontend

## Visão Geral
Este documento fornece instruções detalhadas para implementar o cadastro de fazenda no frontend, seguindo o fluxo de criação sequencial de entidades.

## Fluxo de Cadastro

O cadastro de fazenda deve seguir esta sequência obrigatória:
1. **Cadastrar Usuário** (se não existir)
2. **Cadastrar Endereço** (se não existir)
3. **Cadastrar Telefone(s)** (se não existir)
4. **Cadastrar Fazenda** (usando os IDs das entidades criadas)

## Endpoints da API

### 1. Cadastro de Usuário
```http
POST /users
Content-Type: application/json

{
  "name": "Leonardo Silva",
  "email": "leonardo@email.com",
  "password": "senha123",
  "cpf": "12345678901"
}
```

**Resposta de Sucesso (201):**
```json
{
  "id": 1,
  "name": "Leonardo Silva",
  "email": "leonardo@email.com",
  "cpf": "12345678901",
  "createdAt": "2025-01-09T05:26:38.404"
}
```

### 2. Cadastro de Endereço
```http
POST /addresses
Content-Type: application/json

{
  "street": "Rua das Flores, 123",
  "city": "São Paulo",
  "state": "SP",
  "zipCode": "01234-567",
  "country": "Brasil"
}
```

**Resposta de Sucesso (201):**
```json
{
  "id": 1,
  "street": "Rua das Flores, 123",
  "city": "São Paulo",
  "state": "SP",
  "zipCode": "01234-567",
  "country": "Brasil"
}
```

### 3. Cadastro de Telefone
```http
POST /phones
Content-Type: application/json

{
  "number": "11987654321",
  "type": "MOBILE"
}
```

**Resposta de Sucesso (201):**
```json
{
  "id": 1,
  "number": "11987654321",
  "type": "MOBILE"
}
```

### 4. Cadastro de Fazenda
```http
POST /goatfarms
Content-Type: application/json

{
  "name": "Fazenda Leonardo Silva",
  "tod": "FLS01",
  "addressId": 1,
  "userId": 1,
  "phoneIds": [1]
}
```

**Resposta de Sucesso (201):**
```json
{
  "id": 1,
  "name": "Fazenda Leonardo Silva",
  "tod": "FLS01",
  "address": {
    "id": 1,
    "street": "Rua das Flores, 123",
    "city": "São Paulo",
    "state": "SP",
    "zipCode": "01234-567",
    "country": "Brasil"
  },
  "user": {
    "id": 1,
    "name": "Leonardo Silva",
    "email": "leonardo@email.com",
    "cpf": "12345678901"
  },
  "phones": [
    {
      "id": 1,
      "number": "11987654321",
      "type": "MOBILE"
    }
  ]
}
```

## Implementação no Frontend

### 1. Estrutura do Formulário

Crie um formulário com as seguintes seções:

```html
<!-- Seção do Usuário -->
<div class="user-section">
  <h3>Dados do Proprietário</h3>
  <input type="text" name="userName" placeholder="Nome completo" required>
  <input type="email" name="userEmail" placeholder="Email" required>
  <input type="password" name="userPassword" placeholder="Senha" required>
  <input type="text" name="userCpf" placeholder="CPF" required>
</div>

<!-- Seção do Endereço -->
<div class="address-section">
  <h3>Endereço da Fazenda</h3>
  <input type="text" name="street" placeholder="Rua e número" required>
  <input type="text" name="city" placeholder="Cidade" required>
  <input type="text" name="state" placeholder="Estado" required>
  <input type="text" name="zipCode" placeholder="CEP" required>
  <input type="text" name="country" placeholder="País" value="Brasil" required>
</div>

<!-- Seção do Telefone -->
<div class="phone-section">
  <h3>Telefone de Contato</h3>
  <input type="tel" name="phoneNumber" placeholder="Número do telefone" required>
  <select name="phoneType" required>
    <option value="MOBILE">Celular</option>
    <option value="HOME">Residencial</option>
    <option value="WORK">Comercial</option>
  </select>
</div>

<!-- Seção da Fazenda -->
<div class="farm-section">
  <h3>Dados da Fazenda</h3>
  <input type="text" name="farmName" placeholder="Nome da fazenda" required>
  <input type="text" name="farmTod" placeholder="Código da fazenda" required>
</div>

<button type="submit">Cadastrar Fazenda</button>
```

### 2. Lógica JavaScript

```javascript
class FarmRegistration {
  constructor() {
    this.baseUrl = 'http://localhost:8080';
  }

  async registerFarm(formData) {
    try {
      // 1. Cadastrar usuário
      const user = await this.createUser({
        name: formData.userName,
        email: formData.userEmail,
        password: formData.userPassword,
        cpf: formData.userCpf
      });

      // 2. Cadastrar endereço
      const address = await this.createAddress({
        street: formData.street,
        city: formData.city,
        state: formData.state,
        zipCode: formData.zipCode,
        country: formData.country
      });

      // 3. Cadastrar telefone
      const phone = await this.createPhone({
        number: formData.phoneNumber,
        type: formData.phoneType
      });

      // 4. Cadastrar fazenda
      const farm = await this.createFarm({
        name: formData.farmName,
        tod: formData.farmTod,
        addressId: address.id,
        userId: user.id,
        phoneIds: [phone.id]
      });

      return farm;
    } catch (error) {
      throw new Error(`Erro no cadastro: ${error.message}`);
    }
  }

  async createUser(userData) {
    const response = await fetch(`${this.baseUrl}/users`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(userData)
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Erro ao cadastrar usuário');
    }

    return await response.json();
  }

  async createAddress(addressData) {
    const response = await fetch(`${this.baseUrl}/addresses`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(addressData)
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Erro ao cadastrar endereço');
    }

    return await response.json();
  }

  async createPhone(phoneData) {
    const response = await fetch(`${this.baseUrl}/phones`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(phoneData)
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Erro ao cadastrar telefone');
    }

    return await response.json();
  }

  async createFarm(farmData) {
    const response = await fetch(`${this.baseUrl}/goatfarms`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(farmData)
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Erro ao cadastrar fazenda');
    }

    return await response.json();
  }
}

// Uso do componente
const farmRegistration = new FarmRegistration();

document.getElementById('farm-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  
  const formData = new FormData(e.target);
  const data = Object.fromEntries(formData.entries());
  
  try {
    const result = await farmRegistration.registerFarm(data);
    alert('Fazenda cadastrada com sucesso!');
    console.log('Fazenda criada:', result);
  } catch (error) {
    alert(`Erro: ${error.message}`);
    console.error('Erro no cadastro:', error);
  }
});
```

### 3. Tratamento de Erros

Implemente tratamento específico para diferentes tipos de erro:

```javascript
function handleApiError(error, response) {
  if (response.status === 400) {
    // Erro de validação
    return 'Dados inválidos. Verifique os campos preenchidos.';
  } else if (response.status === 409) {
    // Conflito (ex: email já existe)
    return 'Já existe um registro com esses dados.';
  } else if (response.status === 500) {
    // Erro interno do servidor
    return 'Erro interno do servidor. Tente novamente mais tarde.';
  } else {
    return 'Erro desconhecido. Tente novamente.';
  }
}
```

### 4. Validações no Frontend

Implemente validações antes de enviar os dados:

```javascript
function validateForm(data) {
  const errors = [];
  
  // Validar CPF
  if (!isValidCPF(data.userCpf)) {
    errors.push('CPF inválido');
  }
  
  // Validar email
  if (!isValidEmail(data.userEmail)) {
    errors.push('Email inválido');
  }
  
  // Validar CEP
  if (!isValidZipCode(data.zipCode)) {
    errors.push('CEP inválido');
  }
  
  // Validar telefone
  if (!isValidPhone(data.phoneNumber)) {
    errors.push('Telefone inválido');
  }
  
  return errors;
}
```

## Considerações Importantes

1. **Sequência Obrigatória**: Sempre siga a ordem: Usuário → Endereço → Telefone → Fazenda
2. **Tratamento de Erros**: Implemente tratamento robusto para cada etapa
3. **Validação**: Valide os dados no frontend antes de enviar
4. **Feedback Visual**: Mostre progresso durante o cadastro
5. **Rollback**: Em caso de erro, considere implementar rollback das entidades já criadas
6. **Autenticação**: Estes endpoints são públicos, não requerem token de autenticação

## Exemplo de Uso Completo

```javascript
// Dados de exemplo para teste
const testData = {
  userName: 'Leonardo Silva',
  userEmail: 'leonardo@email.com',
  userPassword: 'senha123',
  userCpf: '12345678901',
  street: 'Rua das Flores, 123',
  city: 'São Paulo',
  state: 'SP',
  zipCode: '01234-567',
  country: 'Brasil',
  phoneNumber: '11987654321',
  phoneType: 'MOBILE',
  farmName: 'Fazenda Leonardo Silva',
  farmTod: 'FLS01'
};

// Executar cadastro
farmRegistration.registerFarm(testData)
  .then(result => console.log('Sucesso:', result))
  .catch(error => console.error('Erro:', error));
```

Este fluxo garante que todas as dependências sejam criadas corretamente antes do cadastro da fazenda.