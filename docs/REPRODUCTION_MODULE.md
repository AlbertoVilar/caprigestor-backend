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
| POST | `/breeding/{coverageEventId}/corrections` | Corrige data de cobertura | `CoverageCorrectionRequestDTO` | `ReproductiveEventResponseDTO` |
| PATCH | `/pregnancies/confirm` | Confirma prenhez | `PregnancyConfirmRequestDTO` | `PregnancyResponseDTO` |
| GET | `/pregnancies/diagnosis-recommendation` | Recomendação de diagnóstico | - | `DiagnosisRecommendationResponseDTO` |
| GET | /pregnancies/active | Busca gestação ativa | - | `PregnancyResponseDTO` |
| GET | /pregnancies/{pregnancyId} | Busca gestação por ID | - | `PregnancyResponseDTO` |
| PATCH | /pregnancies/{pregnancyId}/close | Encerra gestação | `PregnancyCloseRequestDTO` | `PregnancyResponseDTO` |
| GET | `/events` | Histórico de eventos | - | `Page<ReproductiveEventResponseDTO>` |
| GET | `/pregnancies` | Histórico de gestações | - | `Page<PregnancyResponseDTO>` |

## Segurança e Autorização

- Todos os endpoints deste módulo são privados e exigem token JWT válido.
- Nenhuma rota de reprodução é pública. Chamadas sem token retornam **401 Unauthorized**.
- Usuários com **ROLE_ADMIN** possuem acesso total, independentemente do `farmId`.
- Usuários com **ROLE_OPERATOR** ou **ROLE_FARM_OWNER** só acessam dados se forem proprietários da fazenda (`ownershipService.isFarmOwner(farmId)`).
- Quando o token é válido mas o usuário não é proprietário da fazenda da URL, a API retorna **403 Forbidden**.

## Integracao com Lactacao
- A confirmacao de prenhez **nao** encerra lactacao automaticamente.
- A secagem e uma decisao do proprietario e deve ser feita via PATCH no modulo de lactacao.
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
### Regras de Cobertura (Breeding)
- Quando existir gesta??o **ACTIVE**, novas coberturas s?o bloqueadas.
- ? permitido **registro tardio** apenas quando `eventDate` for **anterior** ? cobertura efetiva da gesta??o (effectiveCoverageDate via corre??o ou `breedingDate`).
- Ap?s encerramento/corre??o da gesta??o (ex.: `FALSE_POSITIVE`, `ABORTION` ou qualquer `CLOSED`), as coberturas s?o liberadas novamente.
- **Erro 422**: "N?o ? permitido registrar nova cobertura: existe uma gesta??o ativa para esta cabra. Encerre/corrija a gesta??o atual (ex.: falso positivo/aborto) para liberar novas coberturas."


### PregnancyConfirmRequestDTO
```json
{
  "checkDate": "2026-02-01",
  "checkResult": "POSITIVE",
  "notes": "Ultrassom"
}
```

> Regra de dom�nio: neste endpoint de confirma��o, apenas `checkResult = POSITIVE` � aceito.
> Para diagn�stico `NEGATIVE`, utilize `/pregnancies/checks` (nenhum evento ou gesta��o � criada aqui).

### PregnancyCheckRequestDTO
```json
{
  "checkDate": "2026-02-01",
  "checkResult": "NEGATIVE",
  "notes": "Sem evid�ncias de gesta��o"
}
```

> Regra de dom�nio: neste endpoint apenas `checkResult = NEGATIVE` � aceito.
> A API valida a janela m�nima de 60 dias ap�s a �ltima cobertura efetiva (retorna 422 quando violada).

### PregnancyCloseRequestDTO
```json
{
  "closeDate": "2026-06-01",
  "status": "CLOSED",
  "closeReason": "BIRTH",
  "notes": "Parto normal"
}
```

### CoverageCorrectionRequestDTO
```json
{
  "correctedDate": "2026-01-05",
  "notes": "Cobertura registrada com data incorreta"
}
```

### DiagnosisRecommendationResponseDTO
```json
{
  "status": "ELIGIBLE_PENDING",
  "eligibleDate": "2026-03-01",
  "lastCoverage": {
    "id": 10,
    "eventDate": "2026-01-01",
    "effectiveDate": "2026-01-01",
    "breedingType": "NATURAL",
    "breederRef": "Bode Alpha"
  },
  "lastCheck": null,
  "warnings": []
}
```

> Par�metro opcional: `referenceDate=YYYY-MM-DD` (default: data atual do servidor).

### Response Objects
**PregnancyResponseDTO**
- `confirmedAt` renamed to `confirmDate`.
- `closedAt` � o campo de data de encerramento no response (n�o `closeDate`).
- `recommendedDryDate` removed.
- `closeReason` added.
- `notes` added.

**ReproductiveEventResponseDTO**
- `checkDate` removed.
- `pregnancyId` added.
- `relatedEventId` and `correctedEventDate` added for corrections.

## POST /pregnancies/checks

Endpoint para registrar diagn�stico `NEGATIVE` de prenhez e corrigir falso positivo quando existir gesta��o ativa.

- **M�todo**: POST
- **Rota completa**:
  `/api/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies/checks`

### Payload (PregnancyCheckRequestDTO)
```json
{
  "checkDate": "2026-02-01",
  "checkResult": "NEGATIVE",
  "notes": "Sem evid�ncias de gesta��o"
}
```

### Regras
- Apenas `checkResult = NEGATIVE` � aceito (resultado POSITIVE deve usar `/pregnancies/confirm`).
- `checkDate` n�o pode ser futura.
- `checkDate` deve ser >= 60 dias ap�s a �ltima cobertura efetiva.
- Se existir gesta��o ativa, ela � encerrada como `FALSE_POSITIVE` e um evento `PREGNANCY_CLOSE` � registrado.
- Ap�s o fechamento, `/pregnancies/active` retorna 404.

### Status codes
- **201 Created** - diagn�stico registrado com sucesso.
- **200 OK** - n�o utilizado no fluxo atual; reservado para respostas idempotentes.
- **422 Unprocessable Entity** - regra de 60 dias violada ou cobertura v�lida inexistente.
- **409 Conflict** - conflito de integridade (ex.: constraint de unicidade).
- **404 Not Found** - cabra ou fazenda n�o encontrada.

## GET /pregnancies/{pregnancyId}

Endpoint para buscar o detalhe de uma gestação específica de uma cabra.

- **Método**: GET  
- **Rota completa**:  
  `/api/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies/{pregnancyId}`

### Exemplo de request

```http
GET /api/goatfarms/1/goats/GOAT-001/reproduction/pregnancies/10 HTTP/1.1
Host: api.caprigestor.local
Accept: application/json
```

### Exemplo de response 200

```json
{
  "id": 10,
  "farmId": 1,
  "goatId": "GOAT-001",
  "status": "ACTIVE",
  "breedingDate": "2026-01-01",
  "confirmDate": "2026-02-01",
  "expectedDueDate": "2026-05-31",
  "closedAt": null,
  "closeReason": null,
  "notes": "Gesta��o confirmada por ultrassom"
}
```

### Status codes

- **200 OK** – gestação encontrada para o `farmId` informado.  
- **400 Bad Request** – `pregnancyId` inválido (null ou <= 0).  
- **404 Not Found** – gestação não encontrada ou não pertence ao `farmId` informado.

## Pagina��o e Ordena��o

- `/events`: ordenado por `eventDate` DESC e `id` DESC (desempate est�vel).
- `/pregnancies`: ordenado por `breedingDate` DESC e `id` DESC (desempate est�vel).

## Concurrency Safety (Blindagem)

Para garantir que a regra “apenas 1 gestação ativa por cabra” seja respeitada mesmo em cenários de concorrência extrema:

1. **Unique Index Partial**: o banco de dados possui um índice único `(farm_id, goat_id) WHERE status = 'ACTIVE'`.  
2. **Pre-check no Business**: a aplicação verifica duplicidade antes de salvar uma nova pregnancy ativa.  
3. **Handler 409**: se ainda assim ocorrer uma race condition e o índice único for violado:
   - quando a violação vem do índice `ux_pregnancy_single_active_per_goat`, o handler retorna **HTTP 409 Conflict** com `field = "status"` e mensagem `"J� existe uma gesta��o ativa para esta cabra"`;  
   - para outros constraints, o handler mantém **HTTP 409 Conflict**, mas utiliza `field = "integrity"` e mensagem genérica `"Viola��o de integridade no banco de dados"`.

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

## Recomendações de Manejo

- O diagnóstico de prenhez só pode ser registrado a partir de **60 dias após a última cobertura efetiva**.
- A recomendação de diagnóstico pode ser consultada no endpoint `/pregnancies/diagnosis-recommendation`.
- A janela de 45 dias � uma recomenda��o de manejo; a API continua exigindo 60 dias e retorna 422 para checks antes desse prazo.

## Farm-level alerts endpoint

This route is farm-level (not goat-level):

- GET /api/goatfarms/{farmId}/reproduction/alerts/pregnancy-diagnosis

Query params:

- referenceDate (optional, format YYYY-MM-DD, default server current date)
- page (optional, default 0)
- size (optional, default 20)

Example curl:

    curl -X GET "http://localhost:8080/api/goatfarms/1/reproduction/alerts/pregnancy-diagnosis?referenceDate=2026-02-08&page=0&size=20" \
      -H "Authorization: Bearer <token>"

Example response:

    {
      "totalPending": 2,
      "alerts": [
        {
          "goatId": "GOAT-001",
          "eligibleDate": "2025-12-31",
          "daysOverdue": 39,
          "lastCoverageDate": "2025-11-01",
          "lastCheckDate": null
        },
        {
          "goatId": "GOAT-010",
          "eligibleDate": "2026-01-05",
          "daysOverdue": 34,
          "lastCoverageDate": "2025-11-06",
          "lastCheckDate": null
        }
      ]
    }
