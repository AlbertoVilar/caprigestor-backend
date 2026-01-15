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
| GET | /pregnancies/active | Busca gestação ativa | - | `PregnancyResponseDTO` |
| GET | /pregnancies/{pregnancyId} | Busca gestação por ID | - | `PregnancyResponseDTO` |
| PATCH | /pregnancies/{pregnancyId}/close | Encerra gestação | `PregnancyCloseRequestDTO` | `PregnancyResponseDTO` |
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
  "notes": "Ultrassom"
}
```

> Regra de domínio: neste endpoint de confirmação, apenas `checkResult = POSITIVE` é aceito.  
> Qualquer requisição com `checkResult = NEGATIVE` resulta em erro de validação (nenhum evento ou pregnancy é criado).

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

## Flyway V16 – banco sujo (duplicated ACTIVE)

Quando o banco PostgreSQL já possui dados antigos, a migration `V16__enforce_single_active_pregnancy.sql` pode falhar ao criar o índice único parcial se existirem 2+ pregnancies com `status = 'ACTIVE'` para o mesmo `(farm_id, goat_id)`.

### Como detectar

```sql
SELECT farm_id, goat_id, COUNT(*) AS active_count
FROM pregnancy
WHERE status = 'ACTIVE'
GROUP BY farm_id, goat_id
HAVING COUNT(*) > 1;
```

### Como corrigir

1. Rodar o script manual versionado em `src/main/resources/db/manual/datafix_duplicate_active_pregnancy.sql` no banco PostgreSQL alvo (dev/prod).
2. Esse script mantém somente a pregnancy `ACTIVE` mais recente (critério: `breeding_date` e desempate por `id`) e fecha as demais como `CLOSED` com `close_reason = 'DATA_FIX_DUPLICATED_ACTIVE'`.
3. Após executar o script manual, subir a aplicação normalmente para que o Flyway aplique a V16.

Se a migration de verificação `V15_9__Assert_no_duplicate_active_pregnancy` encontrar duplicidades, ela falhará com uma mensagem explícita apontando para o script manual de data-fix.
