# Módulo de Reprodução (Reproduction)

Este módulo é responsável por gerenciar o ciclo reprodutivo das cabras, incluindo coberturas (breeding), diagnósticos de gestação e partos/encerramentos.

## Estrutura do Módulo

O módulo segue a Arquitetura Hexagonal com a seguinte estrutura de pacotes:

- `api/controller`: Controladores REST.
- `api/dto`: Objetos de Transferência de Dados (Request/Response) da API.
- `business/reproductionservice`: Implementação das regras de negócio (Core).
- `business/bo`: Objetos de Negócio (Value Objects) usados internamente.
- `mapper`: Mapeadores (MapStruct) entre DTOs, VOs e Entidades.
- `model/entity`: Entidades JPA.
- `model/repository`: Interfaces Spring Data JPA.
- `enums`: Enumerações de domínio (Status, Tipos).
- `application/ports/in`: Interfaces de entrada (Use Cases).
- `application/ports/out`: Interfaces de saída (Portas de Persistência).

## Endpoints

Base Path: `/api/goatfarms/{farmId}/goats/{goatId}/reproduction`

| Método | Rota | Descrição | Payload (Request) | Response |
|---|---|---|---|---|
| POST | `/breeding` | Registra uma cobertura | `BreedingRequestDTO` | `ReproductiveEventResponseDTO` |
| PATCH | `/pregnancies/confirm` | Confirma prenhez | `PregnancyConfirmRequestDTO` | `PregnancyResponseDTO` |
| GET | `/pregnancies/active` | Busca gestação ativa | - | `PregnancyResponseDTO` |
| PATCH | `/pregnancies/{pregnancyId}/close` | Encerra gestação | `PregnancyCloseRequestDTO` | `PregnancyResponseDTO` |
| GET | `/events` | Histórico de eventos | - | `Page<ReproductiveEventResponseDTO>` |
| GET | `/pregnancies` | Histórico de gestações | - | `Page<PregnancyResponseDTO>` |

## Payloads (Resumo)

### BreedingRequestDTO
```json
{
  "eventDate": "2026-01-01",
  "breedingType": "NATURAL",
  "breederRef": "Bode Alpha",
  "notes": "Cobertura assistida"
}
```

### PregnancyConfirmRequestDTO
```json
{
  "checkDate": "2026-02-01",
  "checkResult": "POSITIVE",
  "notes": "Ultrassom",
  "expectedDueDate": "2026-06-01"
}
```

### PregnancyCloseRequestDTO
```json
{
  "closeDate": "2026-06-01",
  "status": "CLOSED",
  "closeReason": "BIRTH",
  "notes": "Parto normal"
}
```

### Response Objects
**PregnancyResponseDTO**
- `confirmedAt` renamed to `confirmDate`.
- `recommendedDryDate` removed.
- `closeReason` added.
- `notes` added.

**ReproductiveEventResponseDTO**
- `checkDate` removed.
- `pregnancyId` added.
