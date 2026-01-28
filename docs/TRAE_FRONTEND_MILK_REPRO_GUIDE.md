# Guia Frontend (TRAE) - Producao de Leite, Lactacao e Reproducao

Este documento descreve os endpoints e contratos reais do backend para implementar as telas de Producao de Leite (Milk), Lactacao e Reproducao. Use exatamente estes payloads e autorizacoes.

Base comum de rotas:
`/api/goatfarms/{farmId}/goats/{goatId}`

## Autenticacao e autorizacao (obrigatorio)
- Todos os endpoints abaixo exigem JWT valido.
- Header: `Authorization: Bearer <token>`
- Roles:
  - `ROLE_ADMIN`: acesso total.
  - `ROLE_OPERATOR` e `ROLE_FARM_OWNER`: apenas se for owner da fazenda (`farmId`).
- Sem token: 401.
- Sem ownership: 403.

## Modulo: Producao de Leite (Milk Production)
Base: `/api/goatfarms/{farmId}/goats/{goatId}/milk-productions`

### POST /milk-productions
Cria um registro de producao diaria.
- Request DTO: `MilkProductionRequestDTO`
- Campos:
  - `date` (LocalDate, obrigatorio, formato `yyyy-MM-dd`)
  - `shift` (enum `MilkingShift`, obrigatorio)
  - `volumeLiters` (BigDecimal, obrigatorio)
  - `notes` (String, opcional)
- Enums:
  - `MilkingShift`: `TOTAL_DAY`, `MORNING`, `AFTERNOON`
- Response DTO: `MilkProductionResponseDTO`

Exemplo:
```json
{
  "date": "2026-01-30",
  "shift": "MORNING",
  "volumeLiters": 2.5,
  "notes": "Ordenha da manha"
}
```

### PATCH /milk-productions/{id}
Atualiza parcialmente um registro (apenas volume e observacoes).
- Request DTO: `MilkProductionUpdateRequestDTO`
- Campos:
  - `volumeLiters` (BigDecimal, opcional)
  - `notes` (String, opcional)

Exemplo:
```json
{
  "volumeLiters": 2.8,
  "notes": "Ajuste de volume"
}
```

### GET /milk-productions/{id}
Busca um registro por ID.

### GET /milk-productions
Lista registros com filtros opcionais por data.
- Query params:
  - `from` (LocalDate `yyyy-MM-dd`, opcional)
  - `to` (LocalDate `yyyy-MM-dd`, opcional)
- Paginacao padrao (Spring): `page`, `size`, `sort`.

### DELETE /milk-productions/{id}
Remove um registro.

Observacoes de negocio:
- Producao e registrada via POST em `milk-productions` (nao via PATCH de lactacao).
- Pode haver validacao de lactacao ativa no backend (se falhar, retorna erro 4xx).

## Modulo: Lactacao
Base: `/api/goatfarms/{farmId}/goats/{goatId}/lactations`

### POST /lactations
Abre uma nova lactacao.
- Request DTO: `LactationRequestDTO`
- Campos:
  - `startDate` (LocalDate, obrigatorio, formato `yyyy-MM-dd`)
- Response DTO: `LactationResponseDTO`

Exemplo:
```json
{
  "startDate": "2026-01-01"
}
```

### GET /lactations/active
Busca a lactacao ativa (se existir).

### PATCH /lactations/{lactationId}/dry
Marca uma lactacao como seca (secagem manual).
- Request DTO: `LactationDryRequestDTO`
- Campos:
  - `endDate` (LocalDate, obrigatorio, formato `yyyy-MM-dd`)

Exemplo:
```json
{
  "endDate": "2026-10-01"
}
```

### GET /lactations/{lactationId}
Busca lactacao por ID.

### GET /lactations
Lista historico de lactacoes (paginado).

Observacoes de negocio:
- A secagem e uma decisao do proprietario. Nao existe secagem automatica ao confirmar prenhez.

## Modulo: Reproducao
Base: `/api/goatfarms/{farmId}/goats/{goatId}/reproduction`

### POST /reproduction/breeding
Registra evento de cobertura.
- Request DTO: `BreedingRequestDTO`
- Campos:
  - `eventDate` (LocalDate, obrigatorio, `yyyy-MM-dd`)
  - `breedingType` (enum `BreedingType`, obrigatorio)
  - `breederRef` (String, opcional)
  - `notes` (String, opcional)
- Enums:
  - `BreedingType`: `NATURAL`, `AI`
- Response DTO: `ReproductiveEventResponseDTO`

Exemplo:
```json
{
  "eventDate": "2026-02-01",
  "breedingType": "NATURAL",
  "breederRef": "BODE-123",
  "notes": "Cobertura no pasto"
}
```

### PATCH /reproduction/pregnancies/confirm
Confirma prenhez.
- Request DTO: `PregnancyConfirmRequestDTO`
- Campos:
  - `checkDate` (LocalDate, obrigatorio)
  - `checkResult` (enum `PregnancyCheckResult`, obrigatorio)
  - `notes` (String, opcional)
- Enums:
  - `PregnancyCheckResult`: `PENDING`, `POSITIVE`, `NEGATIVE`
- Response DTO: `PregnancyResponseDTO`

Exemplo:
```json
{
  "checkDate": "2026-03-15",
  "checkResult": "POSITIVE",
  "notes": "Confirmacao por ultrassom"
}
```

### GET /reproduction/pregnancies/active
Busca prenhez ativa (se existir).

### GET /reproduction/pregnancies/{pregnancyId}
Busca prenhez por ID.

### PATCH /reproduction/pregnancies/{pregnancyId}/close
Encerra prenhez.
- Request DTO: `PregnancyCloseRequestDTO`
- Campos:
  - `closeDate` (LocalDate, obrigatorio)
  - `status` (enum `PregnancyStatus`, obrigatorio)
  - `closeReason` (enum `PregnancyCloseReason`, opcional)
  - `notes` (String, opcional)
- Enums:
  - `PregnancyStatus`: `ACTIVE`, `CLOSED`
  - `PregnancyCloseReason`: `BIRTH`, `ABORTION`, `LOSS`, `OTHER`, `DATA_FIX_DUPLICATED_ACTIVE`
- Response DTO: `PregnancyResponseDTO`

Exemplo:
```json
{
  "closeDate": "2026-08-20",
  "status": "CLOSED",
  "closeReason": "BIRTH",
  "notes": "Parto normal"
}
```

### GET /reproduction/events
Lista eventos reprodutivos (paginado).

### GET /reproduction/pregnancies
Lista historico de gesta??es (paginado).

Observacoes de negocio:
- Confirmacao de prenhez nao aciona secagem automaticamente. A secagem e feita via PATCH em lactacoes.

## Padrao de erros (frontend)
- 401: sem token ou token invalido.
- 403: usuario autenticado sem ownership da fazenda.
- 404: recurso nao encontrado.
- 422: validacao (campos obrigatorios, formato de data, etc.).

## Referencias de codigo (backend)
- `src/main/java/com/devmaster/goatfarm/milk/api/controller/MilkProductionController.java`
- `src/main/java/com/devmaster/goatfarm/milk/api/controller/LactationController.java`
- `src/main/java/com/devmaster/goatfarm/reproduction/api/controller/ReproductionController.java`
- DTOs:
  - `src/main/java/com/devmaster/goatfarm/milk/api/dto/MilkProductionRequestDTO.java`
  - `src/main/java/com/devmaster/goatfarm/milk/api/dto/MilkProductionUpdateRequestDTO.java`
  - `src/main/java/com/devmaster/goatfarm/milk/api/dto/LactationRequestDTO.java`
  - `src/main/java/com/devmaster/goatfarm/milk/api/dto/LactationDryRequestDTO.java`
  - `src/main/java/com/devmaster/goatfarm/reproduction/api/dto/BreedingRequestDTO.java`
  - `src/main/java/com/devmaster/goatfarm/reproduction/api/dto/PregnancyConfirmRequestDTO.java`
  - `src/main/java/com/devmaster/goatfarm/reproduction/api/dto/PregnancyCloseRequestDTO.java`

