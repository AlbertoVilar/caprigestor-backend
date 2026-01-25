GoatFarm Update UX Guide

Objetivo
Padronizar o fluxo de atualizacao da fazenda e sub-recursos, reduzindo erros 422 e garantindo a regra de dominio: a fazenda deve ter ao menos 1 telefone.

Fluxos recomendados
1) Update completo (PUT /api/goatfarms/{id})
- Use quando o formulario edita dados da fazenda, usuario owner, endereco e telefones no mesmo envio.
- Envie sempre a lista completa de telefones (minimo 1).
- Para telefones existentes, inclua o campo `id` para evitar duplicacao.

2) Updates parciais por sub-recurso
- Endereco: PUT /api/goatfarms/{farmId}/addresses/{addressId}
- Telefones: POST/PUT/DELETE /api/goatfarms/{farmId}/phones
- Senha: PATCH /api/users/{id}/password
- Roles: PATCH /api/users/{id}/roles (somente admin)

PUT vs PATCH
- PUT /api/goatfarms/{id} e um update completo do agregado (farm + user + address + phones).
- Nao existe PATCH para goatfarm; para mudanças parciais use os endpoints de sub-recursos.

Regras obrigatorias
- Senha: so enviar quando for alterar. Use o endpoint dedicado de senha.
- Telefones: minimo 1 sempre. A UI deve impedir remover o ultimo telefone.

UX recomendada (copys PT-BR)
- Remover ultimo telefone: "A fazenda deve possuir ao menos um telefone."
- Campos obrigatorios: "Campo obrigatorio."

Exemplos de payloads JSON

Create (POST /api/goatfarms)
```json
{
  "farm": { "name": "Fazenda A", "tod": "ABCDE" },
  "user": {
    "name": "Maria",
    "email": "maria@example.com",
    "cpf": "12345678901",
    "password": "senha123",
    "confirmPassword": "senha123"
  },
  "address": {
    "street": "Rua A",
    "neighborhood": "Centro",
    "city": "Sao Paulo",
    "state": "SP",
    "zipCode": "01000-000",
    "country": "Brasil"
  },
  "phones": [
    { "ddd": "11", "number": "99999999" }
  ]
}
```

Update completo (PUT /api/goatfarms/{id})
```json
{
  "farm": { "name": "Fazenda A", "tod": "ABCDE" },
  "user": {
    "name": "Maria",
    "email": "maria@example.com",
    "cpf": "12345678901"
  },
  "address": {
    "street": "Rua A",
    "neighborhood": "Centro",
    "city": "Sao Paulo",
    "state": "SP",
    "zipCode": "01000-000",
    "country": "Brasil"
  },
  "phones": [
    { "id": 10, "ddd": "11", "number": "99999999" },
    { "ddd": "11", "number": "988888888" }
  ]
}
```

Patch de senha (PATCH /api/users/{id}/password)
```json
{ "password": "novaSenha123", "confirmPassword": "novaSenha123" }
```

Erros e mensagens esperadas
- 401: Sem token ou token invalido -> "Nao autorizado"
- 403: Token valido sem ownership -> "Acesso negado"
- 422: Validacao de entrada -> "Erro de validacao" + detalhes por campo
- 409: Conflito de dados (ex.: duplicidade) -> "Conflito de dados"

