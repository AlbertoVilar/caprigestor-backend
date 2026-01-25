UX Frontend Guide - GoatFarm/CapriGestor

0. Auditoria objetiva do backend

Controllers e endpoints confirmados (fontes: controllers + DTOs):

GoatFarmController - base `/api/goatfarms`
- POST /api/goatfarms
  - Request: GoatFarmFullRequestDTO (farm, user, address, phones)
  - Response: GoatFarmFullResponseDTO
  - Status: 201, 422, 409, 400
- PUT /api/goatfarms/{id}
  - Request: GoatFarmUpdateRequestDTO (farm, user, address, phones)
  - Response: GoatFarmFullResponseDTO
  - Status: 200, 401, 403, 404, 422, 409
- GET /api/goatfarms
  - Response: Page<GoatFarmFullResponseDTO>
  - Status: 200
- GET /api/goatfarms/{id}
  - Response: GoatFarmFullResponseDTO
  - Status: 200, 404
- GET /api/goatfarms/name?name=
  - Response: Page<GoatFarmFullResponseDTO>
  - Status: 200
- DELETE /api/goatfarms/{id}
  - Status: 204, 401, 403, 404
- GET /api/goatfarms/{farmId}/permissions
  - Response: FarmPermissionsDTO
  - Status: 200, 401, 403

GoatController - base `/api/goatfarms/{farmId}/goats`
- POST /api/goatfarms/{farmId}/goats
  - Request: GoatRequestDTO
  - Response: GoatResponseDTO
  - Status: 201, 401, 403, 422, 409, 404
- PUT /api/goatfarms/{farmId}/goats/{goatId}
  - Request: GoatRequestDTO
  - Response: GoatResponseDTO
  - Status: 200, 401, 403, 422, 404
- DELETE /api/goatfarms/{farmId}/goats/{goatId}
  - Status: 204, 401, 403, 404
- GET /api/goatfarms/{farmId}/goats/{goatId}
  - Response: GoatResponseDTO
  - Status: 200, 404
- GET /api/goatfarms/{farmId}/goats
  - Response: Page<GoatResponseDTO>
  - Status: 200
- GET /api/goatfarms/{farmId}/goats/search?name=
  - Response: Page<GoatResponseDTO>
  - Status: 200

GenealogyController - base `/api/goatfarms/{farmId}/goats/{goatId}/genealogies`
- GET /api/goatfarms/{farmId}/goats/{goatId}/genealogies
  - Response: GenealogyResponseVO (retorno direto)
  - Status: 200, 404

MilkProductionController - base `/api/goatfarms/{farmId}/goats/{goatId}/milk-productions`
- POST /api/goatfarms/{farmId}/goats/{goatId}/milk-productions
  - Request: MilkProductionRequestDTO
  - Response: MilkProductionResponseDTO
  - Status: 201, 401, 403, 422, 409, 404
- PATCH /api/goatfarms/{farmId}/goats/{goatId}/milk-productions/{id}
  - Request: MilkProductionUpdateRequestDTO
  - Response: MilkProductionResponseDTO
  - Status: 200, 401, 403, 422, 404
- GET /api/goatfarms/{farmId}/goats/{goatId}/milk-productions/{id}
  - Response: MilkProductionResponseDTO
  - Status: 200, 401, 403, 404
- GET /api/goatfarms/{farmId}/goats/{goatId}/milk-productions?from=&to=
  - Response: Page<MilkProductionResponseDTO>
  - Status: 200, 401, 403
- DELETE /api/goatfarms/{farmId}/goats/{goatId}/milk-productions/{id}
  - Status: 204, 401, 403, 404

LactationController - base `/api/goatfarms/{farmId}/goats/{goatId}/lactations`
- POST /api/goatfarms/{farmId}/goats/{goatId}/lactations
  - Request: LactationRequestDTO
  - Response: LactationResponseDTO
  - Status: 201, 401, 403, 422, 409, 404
- GET /api/goatfarms/{farmId}/goats/{goatId}/lactations/active
  - Response: LactationResponseDTO
  - Status: 200, 401, 403, 404
- PATCH /api/goatfarms/{farmId}/goats/{goatId}/lactations/{lactationId}/dry
  - Request: LactationDryRequestDTO
  - Response: LactationResponseDTO
  - Status: 200, 401, 403, 422, 404
- GET /api/goatfarms/{farmId}/goats/{goatId}/lactations/{lactationId}
  - Response: LactationResponseDTO
  - Status: 200, 401, 403, 404
- GET /api/goatfarms/{farmId}/goats/{goatId}/lactations
  - Response: Page<LactationResponseDTO>
  - Status: 200, 401, 403

EventController - base `/api/goatfarms/{farmId}/goats/{goatId}/events`
- POST /api/goatfarms/{farmId}/goats/{goatId}/events
  - Request: EventRequestDTO
  - Response: EventResponseDTO
  - Status: 201, 401, 403, 422, 404
- PUT /api/goatfarms/{farmId}/goats/{goatId}/events/{eventId}
  - Request: EventRequestDTO
  - Response: EventResponseDTO
  - Status: 200, 401, 403, 422, 404
- GET /api/goatfarms/{farmId}/goats/{goatId}/events/{eventId}
  - Response: EventResponseDTO
  - Status: 200, 401, 403, 404
- GET /api/goatfarms/{farmId}/goats/{goatId}/events
  - Response: Page<EventResponseDTO>
  - Status: 200, 401, 403
- GET /api/goatfarms/{farmId}/goats/{goatId}/events/filter?eventType=&startDate=&endDate=
  - Response: Page<EventResponseDTO>
  - Status: 200, 401, 403
- DELETE /api/goatfarms/{farmId}/goats/{goatId}/events/{eventId}
  - Status: 204, 401, 403, 404

ReproductionController - base `/api/goatfarms/{farmId}/goats/{goatId}/reproduction`
- POST /api/goatfarms/{farmId}/goats/{goatId}/reproduction/breeding
  - Request: BreedingRequestDTO
  - Response: ReproductiveEventResponseDTO
  - Status: 201, 401, 403, 422, 409, 404
- PATCH /api/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies/confirm
  - Request: PregnancyConfirmRequestDTO
  - Response: PregnancyResponseDTO
  - Status: 200, 401, 403, 422, 409, 404
- PATCH /api/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies/{pregnancyId}/close
  - Request: PregnancyCloseRequestDTO
  - Response: PregnancyResponseDTO
  - Status: 200, 401, 403, 422, 404
- GET /api/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies/active
  - Response: PregnancyResponseDTO
  - Status: 200, 401, 403, 404
- GET /api/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies/{pregnancyId}
  - Response: PregnancyResponseDTO
  - Status: 200, 401, 403, 404
- GET /api/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies
  - Response: Page<PregnancyResponseDTO>
  - Status: 200, 401, 403
- GET /api/goatfarms/{farmId}/goats/{goatId}/reproduction/events
  - Response: Page<ReproductiveEventResponseDTO>
  - Status: 200, 401, 403

Erros globais (GlobalExceptionHandler)
- 401: "Nao autorizado"
- 403: "Acesso negado"
- 404: "Recurso nao encontrado"
- 409: "Conflito de dados"
- 422: "Erro de validacao de dados" + lista por campo

Seguranca (SecurityConfig)
- Publicos (permitAll): GET /api/goatfarms, /api/goatfarms/{id}, /api/goatfarms/name, GET /api/goatfarms/{farmId}/goats, /api/goatfarms/{farmId}/goats/{goatId}, /api/goatfarms/{farmId}/goats/search, GET /api/goatfarms/{farmId}/goats/{goatId}/genealogies
- Privados: tudo em lactation, milk production, events, reproduction; e operacoes sensiveis (POST/PUT/PATCH/DELETE) no geral
- Ownership: aplicado por @PreAuthorize em Goat, Lactation, MilkProduction, Events e Phone/Address; no reproducao a checagem de ownership nao esta no controller

A. Visao geral do produto e navegacao

Sitemap sugerido (menu lateral/topbar)
- Publico
  - Lista de capris
  - Detalhe do capril (publico)
  - Cabras do capril (publico)
  - Genealogia de cabra (publico)
- Privado
  - Dashboard do capril
  - Cabras (CRUD)
  - Lactacao
  - Producao de leite
  - Eventos
  - Reproducao
  - Configuracoes do capril

Rotas do frontend (exemplo)
- /public/goatfarms
- /public/goatfarms/:farmId
- /public/goatfarms/:farmId/goats
- /public/goatfarms/:farmId/goats/:goatId
- /public/goatfarms/:farmId/goats/:goatId/genealogy
- /app/goatfarms/:farmId/dashboard
- /app/goatfarms/:farmId/edit
- /app/goatfarms/:farmId/goats
- /app/goatfarms/:farmId/goats/:goatId/lactations
- /app/goatfarms/:farmId/goats/:goatId/milk-productions
- /app/goatfarms/:farmId/goats/:goatId/reproduction
- /app/goatfarms/:farmId/goats/:goatId/events

B. Matriz de acesso

| Modulo | Publico | Token | Ownership | Sem token | Sem ownership |
| --- | --- | --- | --- | --- | --- |
| GoatFarm GET | Sim | Nao | Nao | 200 | 200 |
| GoatFarm PUT/DELETE | Nao | Sim | Sim | 401 | 403 |
| Goats GET | Sim | Nao | Nao | 200 | 200 |
| Goats POST/PUT/DELETE | Nao | Sim | Sim | 401 | 403 |
| Genealogy GET | Sim | Nao | Nao | 200 | 200 |
| Lactation (todos) | Nao | Sim | Sim | 401 | 403 |
| MilkProduction (todos) | Nao | Sim | Sim | 401 | 403 |
| Events (todos) | Nao | Sim | Sim | 401 | 403 |
| Reproduction (todos) | Nao | Sim | Nao no controller | 401 | 403 |

C. Padrao de chamadas HTTP do frontend

Token
- Armazenar access token em memoria (recomendado) ou storage seguro.
- Injetar `Authorization: Bearer <token>` em requests privadas.
- Se houver refresh token (AuthController), renovar ao receber 401 expirado.

Tratamento de erros por status
- 401: limpar sessao e redirecionar para login.
- 403: exibir "Sem permissao para acessar esta fazenda".
- 404: mostrar "Nao encontrado" e CTA para voltar.
- 409: exibir mensagem de conflito (duplicidade ou regra clinica).
- 422: exibir erros por campo (ValidationError.errors).

D. Telas e fluxos (por modulo)

D1. GoatFarm - Editar capril (PUT agregado)
Objetivo
- Atualizar dados do capril, owner, endereco e telefones em um unico envio.

UI sugerida
- Tabs: "Capril", "Proprietario", "Endereco", "Telefones"
- Botao: "Salvar alteracoes"

Estados
- loading: skeleton do formulario
- error: inline por campo + toast
- success: toast "Alteracoes salvas"

Calls HTTP
- GET /api/goatfarms/{id} (preencher formulario)
- PUT /api/goatfarms/{id} (salvar)

Payload (GoatFarmUpdateRequestDTO)
```json
{
  "farm": { "name": "Fazenda A", "tod": "ABCDE" },
  "user": { "name": "Maria", "email": "maria@example.com", "cpf": "12345678901" },
  "address": {
    "id": 50,
    "street": "Rua A",
    "neighborhood": "Centro",
    "city": "Sao Paulo",
    "state": "SP",
    "zipCode": "01000-000",
    "country": "Brasil"
  },
  "phones": [
    { "id": 10, "ddd": "11", "number": "99999999" },
    { "ddd": "11", "number": "98888888" }
  ]
}
```

Regras de validacao (frontend)
- `phones` minimo 1.
- `address.id` obrigatorio quando endereco ja existe.
- `phones[i].id` enviar para existentes; novos sem id.
- CPF, CEP, DDD e telefone enviar somente numeros.

Mensagens UX
- "A fazenda deve possuir ao menos um telefone."
- "Campo obrigatorio."

D2. Goats (animais)
Objetivo
- CRUD de cabras da fazenda.

UI sugerida
- Lista paginada + busca por nome
- Formulario de create/edit

Calls HTTP
- GET /api/goatfarms/{farmId}/goats
- GET /api/goatfarms/{farmId}/goats/search?name=
- POST /api/goatfarms/{farmId}/goats
- PUT /api/goatfarms/{farmId}/goats/{goatId}
- DELETE /api/goatfarms/{farmId}/goats/{goatId}

Payload (GoatRequestDTO)
```json
{
  "registrationNumber": "GOAT-001",
  "name": "Nina",
  "gender": "FEMEA",
  "breed": "SAANEN",
  "color": "BRANCA",
  "birthDate": "2023-06-10",
  "status": "ATIVO",
  "tod": "ABCDE",
  "toe": "XYZ",
  "category": "ADULTA",
  "fatherRegistrationNumber": "GOAT-010",
  "motherRegistrationNumber": "GOAT-011"
}
```

Validacoes frontend (espelho)
- registrationNumber: 1-12 chars
- name: 3-60 chars
- birthDate obrigatoria

D3. Genealogy (genealogia)
Objetivo
- Exibir arvore genealogica da cabra.

Call HTTP
- GET /api/goatfarms/{farmId}/goats/{goatId}/genealogies

Response (GenealogyResponseVO)
```json
{
  "goatName": "Nina",
  "goatRegistration": "GOAT-001",
  "breed": "SAANEN",
  "color": "BRANCA",
  "gender": "FEMEA",
  "status": "ATIVO",
  "birthDate": "2023-06-10",
  "fatherName": "Thor",
  "fatherRegistration": "GOAT-010",
  "motherName": "Luna",
  "motherRegistration": "GOAT-011"
}
```

D4. MilkProduction - Producao diaria de leite
Objetivo
- Registrar e acompanhar producao por cabra e periodo.

UI sugerida
- Lista paginada com filtro por data (from/to)
- Form de criacao e edicao (patch)

Calls HTTP
- GET /api/goatfarms/{farmId}/goats/{goatId}/milk-productions?from=&to=
- POST /api/goatfarms/{farmId}/goats/{goatId}/milk-productions
- PATCH /api/goatfarms/{farmId}/goats/{goatId}/milk-productions/{id}
- DELETE /api/goatfarms/{farmId}/goats/{goatId}/milk-productions/{id}

Payloads
```json
// create
{ "date": "2026-01-30", "shift": "MORNING", "volumeLiters": 2.5, "notes": "OK" }

// update (partial)
{ "volumeLiters": 2.8, "notes": "Ajuste" }
```

Regras frontend
- date obrigatoria no create
- converter data UI dd/MM/yyyy para ISO yyyy-MM-dd

D5. Lactation - Gestao de lactacao
Objetivo
- Abrir, consultar ativa, encerrar e ver historico.

Calls HTTP
- POST /api/goatfarms/{farmId}/goats/{goatId}/lactations
- GET /api/goatfarms/{farmId}/goats/{goatId}/lactations/active
- PATCH /api/goatfarms/{farmId}/goats/{goatId}/lactations/{lactationId}/dry
- GET /api/goatfarms/{farmId}/goats/{goatId}/lactations

Payloads
```json
// open
{ "startDate": "2026-01-01" }
// dry
{ "endDate": "2026-10-01" }
```

Mensagens tipicas
- "Ja existe uma lactacao ativa para esta cabra."
- "Data de inicio nao pode ser futura."

D6. Events - Eventos gerais
Objetivo
- Timeline e CRUD de eventos do animal.

Calls HTTP
- GET /api/goatfarms/{farmId}/goats/{goatId}/events
- GET /api/goatfarms/{farmId}/goats/{goatId}/events/filter?eventType=&startDate=&endDate=
- POST /api/goatfarms/{farmId}/goats/{goatId}/events
- PUT /api/goatfarms/{farmId}/goats/{goatId}/events/{eventId}
- DELETE /api/goatfarms/{farmId}/goats/{goatId}/events/{eventId}

Payload (EventRequestDTO)
```json
{
  "goatId": "GOAT-001",
  "eventType": "SAUDE",
  "date": "2025-05-10",
  "description": "Vacina aplicada",
  "location": "Capril",
  "veterinarian": "Dra Ana",
  "outcome": "Sem reacoes"
}
```

D7. Reproduction - Cobertura e gestacao
Objetivo
- Registrar cobertura, confirmar gestacao, encerrar e listar historico.

Calls HTTP
- POST /api/goatfarms/{farmId}/goats/{goatId}/reproduction/breeding
- PATCH /api/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies/confirm
- PATCH /api/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies/{pregnancyId}/close
- GET /api/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies/active
- GET /api/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies
- GET /api/goatfarms/{farmId}/goats/{goatId}/reproduction/events

Payloads
```json
// breeding
{ "eventDate": "2026-01-10", "breedingType": "NATURAL", "breederRef": "BODE-01", "notes": "OK" }
// confirm pregnancy
{ "checkDate": "2026-02-20", "checkResult": "POSITIVE", "notes": "USG" }
// close pregnancy
{ "closeDate": "2026-06-20", "status": "CLOSED", "closeReason": "BIRTH", "notes": "Parto normal" }
```

Mensagens tipicas
- "Data do evento nao pode ser futura"
- "Ja existe uma gestacao ativa para esta cabra"
- "Data de encerramento nao pode ser anterior a data de cobertura"

E. Padroes de UI e design system sugerido
- Listas: tabela com paginacao + filtros.
- Cards: resumo de status (ATIVA/ENCERRADA).
- Chips/badges para status (ATIVA, ENCERRADA, PERDIDA).
- Datas no UI: dd/MM/yyyy; enviar ISO yyyy-MM-dd no payload.
- Normalizacao: CPF, CEP, DDD e telefone somente numeros.
- Acessibilidade: labels visiveis, aria-invalid, mensagens inline.

F. Checklist antes de entregar (frontend)
- Verificar 401 (sem token) em endpoints privados.
- Verificar 403 (sem ownership) em endpoints privados.
- Verificar 422 e exibir erros por campo.
- Verificar 409 (conflitos) com mensagem amigavel.
- Validar minimo 1 telefone no PUT agregado.
- Enviar address.id quando existir.
- Enviar phones[i].id quando existir.
- Normalizar CPF/CEP/DDD/telefone no envio.
- Converter datas para ISO.
- Cobrir estados empty/loading/error/success.

Evidencias
- Comandos usados: `rg -n "class .*Controller"`, `Get-Content` nos controllers e DTOs.
- Arquivos auditados:
  - src/main/java/com/devmaster/goatfarm/farm/api/controller/GoatFarmController.java
  - src/main/java/com/devmaster/goatfarm/goat/api/controller/GoatController.java
  - src/main/java/com/devmaster/goatfarm/genealogy/api/controller/GenealogyController.java
  - src/main/java/com/devmaster/goatfarm/milk/api/controller/MilkProductionController.java
  - src/main/java/com/devmaster/goatfarm/milk/api/controller/LactationController.java
  - src/main/java/com/devmaster/goatfarm/events/api/controller/EventController.java
  - src/main/java/com/devmaster/goatfarm/reproduction/api/controller/ReproductionController.java
  - DTOs associados (GoatFarm*, Goat*, GenealogyResponseVO, MilkProduction*, Lactation*, Event*, Reproduction*)
- Endpoints confirmados: lista completa na secao 0.
