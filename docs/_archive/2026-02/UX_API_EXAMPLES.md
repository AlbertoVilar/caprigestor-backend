UX API Examples - GoatFarm/CapriGestor

GoatFarm - PUT agregado
Request
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
Response (200)
```json
{
  "id": 1,
  "name": "Fazenda A",
  "tod": "ABCDE",
  "user": { "id": 7, "name": "Maria", "email": "maria@example.com", "cpf": "12345678901", "roles": ["ROLE_FARM_OWNER"] },
  "address": { "id": 50, "street": "Rua A", "neighborhood": "Centro", "city": "Sao Paulo", "state": "SP", "zipCode": "01000-000", "country": "Brasil" },
  "phones": [
    { "id": 10, "ddd": "11", "number": "99999999" },
    { "id": 11, "ddd": "11", "number": "98888888" }
  ],
  "createdAt": "2026-01-01T10:00:00",
  "updatedAt": "2026-01-02T10:00:00",
  "version": 1
}
```

Goats - POST
Request
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
Response (201)
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
  "fatherName": "Thor",
  "fatherRegistrationNumber": "GOAT-010",
  "motherName": "Luna",
  "motherRegistrationNumber": "GOAT-011",
  "userName": "Maria",
  "farmId": 1,
  "farmName": "Fazenda A"
}
```

Genealogy - GET
Response (200)
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

MilkProduction - POST
Request
```json
{ "date": "2026-01-30", "shift": "MORNING", "volumeLiters": 2.5, "notes": "OK" }
```
Response (201)
```json
{ "id": 100, "date": "2026-01-30", "shift": "MORNING", "volumeLiters": 2.5, "notes": "OK" }
```

MilkProduction - PATCH
Request
```json
{ "volumeLiters": 2.8, "notes": "Ajuste" }
```
Response (200)
```json
{ "id": 100, "date": "2026-01-30", "shift": "MORNING", "volumeLiters": 2.8, "notes": "Ajuste" }
```

Lactation - POST
Request
```json
{ "startDate": "2026-01-01" }
```
Response (201)
```json
{ "id": 200, "farmId": 1, "goatId": "GOAT-001", "status": "ACTIVE", "startDate": "2026-01-01", "endDate": null }
```

Lactation - PATCH dry
Request
```json
{ "endDate": "2026-10-01" }
```
Response (200)
```json
{ "id": 200, "farmId": 1, "goatId": "GOAT-001", "status": "CLOSED", "startDate": "2026-01-01", "endDate": "2026-10-01" }
```

Events - POST
Request
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
Response (201)
```json
{
  "id": 300,
  "goatId": "GOAT-001",
  "goatName": "Nina",
  "eventType": "SAUDE",
  "date": "2025-05-10",
  "description": "Vacina aplicada",
  "location": "Capril",
  "veterinarian": "Dra Ana",
  "outcome": "Sem reacoes"
}
```

Reproduction - POST breeding
Request
```json
{ "eventDate": "2026-01-10", "breedingType": "NATURAL", "breederRef": "BODE-01", "notes": "OK" }
```
Response (201)
```json
{
  "id": 400,
  "farmId": 1,
  "goatId": "GOAT-001",
  "eventType": "COVERAGE",
  "eventDate": "2026-01-10",
  "breedingType": "NATURAL",
  "breederRef": "BODE-01",
  "notes": "OK"
}
```

Reproduction - PATCH confirm pregnancy
Request
```json
{ "checkDate": "2026-02-20", "checkResult": "POSITIVE", "notes": "USG" }
```
Response (200)
```json
{
  "id": 500,
  "farmId": 1,
  "goatId": "GOAT-001",
  "status": "ACTIVE",
  "breedingDate": "2026-01-10",
  "confirmDate": "2026-02-20",
  "expectedDueDate": "2026-06-09",
  "closedAt": null,
  "closeReason": null,
  "notes": "USG"
}
```

Reproduction - PATCH close pregnancy
Request
```json
{ "closeDate": "2026-06-20", "status": "CLOSED", "closeReason": "BIRTH", "notes": "Parto normal" }
```
Response (200)
```json
{
  "id": 500,
  "farmId": 1,
  "goatId": "GOAT-001",
  "status": "CLOSED",
  "breedingDate": "2026-01-10",
  "confirmDate": "2026-02-20",
  "expectedDueDate": "2026-06-09",
  "closedAt": "2026-06-20",
  "closeReason": "BIRTH",
  "notes": "Parto normal"
}
```
