# Módulo Lactation

## Visão Geral
Este módulo é responsável pelo ciclo de vida produtivo das cabras, gerenciando a abertura e o encerramento de lactações (períodos em que o animal está produzindo leite). É fundamental para garantir a consistência dos registros de produção leiteira.

## Endpoints do CRUD

O módulo expõe uma API RESTful completa para gerenciamento das lactações.
**Base Path:** `/api/goatfarms/{farmId}/goats/{goatId}/lactations`

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| **POST** | `/` | Abre uma nova lactação (Início do ciclo produtivo). |
| **PATCH** | `/{id}/dry` | Encerra uma lactação ativa (Secagem do animal). |
| **GET** | `/active` | Busca a lactação atualmente ativa para a cabra. |
| **GET** | `/{id}` | Busca os detalhes de uma lactação específica por ID. |
| **GET** | `/` | Lista o histórico de lactações de forma paginada. |
| **GET** | `/{id}/summary` | Retorna o sumário da lactação (produção e recomendação). |
| **GET** | `/active/summary` | Retorna o sumário da lactação ativa. |

## Segurança e Autorização

- Todos os endpoints de lactação são privados e exigem token JWT válido.
- Chamadas sem token para qualquer rota de lactação retornam **401 Unauthorized**.
- Usuários com **ROLE_ADMIN** têm acesso total, em qualquer fazenda.
- Usuários com **ROLE_OPERATOR** ou **ROLE_FARM_OWNER** só acessam dados da fazenda se forem proprietários (`ownershipService.isFarmOwner(farmId)`).
- Quando o token é válido mas o usuário não é proprietário da fazenda, a API retorna **403 Forbidden**.

## Regras de Domínio

### 1. Unicidade de Lactação Ativa
*   Uma cabra só pode ter **uma** lactação com status `ACTIVE` por vez.
*   Tentativas de abrir uma nova lactação enquanto existe uma ativa resultarão em erro (`ValidationException`).

### 2. Abertura de Lactação (Open)
*   **Status Inicial:** `ACTIVE`.
*   **Data de Início (`startDate`):** Obrigatória e não pode ser futura.
*   **Data de Fim (`endDate`):** Inicializada como `null`.

### 3. Encerramento de Lactação (Dry/Close)
*   **Ação:** Transforma o status de `ACTIVE` para `CLOSED`.
*   **Data de Fim (`endDate`):** Obrigatória, não pode ser nula, e deve ser posterior ou igual à `startDate`.
*   **Consistência:** Apenas lactações ativas podem ser encerradas.

### 4. Regra "Somente Fêmeas"
*   Apenas cabras **fêmeas** podem ter lactação ativa, registrar produção e operar endpoints de lactação.
*   Tentativas com cabras macho retornam **422 Unprocessable Entity** com mensagem: "Apenas fêmeas podem ter lactação.".

### 5. Recomendações de Manejo (Sumário)
*   Se houver prenhez ativa e a lactação estiver **ACTIVE**, o sumário calcula `gestationDays`.
*   Quando `gestationDays >= 90`, o sumário marca `dryOffRecommendation = true` e informa `recommendedDryOffDate`.

## Estrutura de Dados

### Lactation (Entity)
*   `id`: Identificador único.
*   `farmId`: Identificador da fazenda (Multi-tenancy).
*   `goatId`: Identificador da cabra.
*   `status`: `ACTIVE` ou `CLOSED`.
*   `startDate`: Data de início da lactação.
*   `endDate`: Data de fim (secagem).
*   `pregnancyStartDate`: (Opcional) Data de início de prenhez associada.
*   `dryStartDate`: (Opcional) Data prevista ou efetiva de secagem.

| Método | Rota | Descrição | Status Codes | DTOs (Request/Response) |
|--------|------|-----------|--------------|-------------------------|
| **POST** | `/` | Abre uma nova lactação (Início do ciclo). | `201 Created` | `LactationRequestDTO` -> `LactationResponseDTO` |
| **PATCH** | `/{id}/dry` | Encerra uma lactação ativa (Secagem). | `200 OK` | `LactationDryRequestDTO` -> `LactationResponseDTO` |
| **GET** | `/active` | Busca a lactação atualmente ativa. | `200 OK` | N/A -> `LactationResponseDTO` |
| **GET** | `/{id}` | Busca detalhes de uma lactação por ID. | `200 OK` | N/A -> `LactationResponseDTO` |
| **GET** | `/` | Lista histórico de lactações (paginado). | `200 OK` | N/A -> `Page<LactationResponseDTO>` |
| **GET** | `/{id}/summary` | Sumário da lactação. | `200 OK` | N/A -> `LactationSummaryResponseDTO` |
| **GET** | `/active/summary` | Sumário da lactação ativa. | `200 OK` | N/A -> `LactationSummaryResponseDTO` |

### Exemplo de resposta do Sumário

```json
{
  "lactation": {
    "lactationId": 10,
    "goatId": "GOAT-001",
    "startDate": "2026-01-01",
    "endDate": null,
    "status": "ACTIVE"
  },
  "production": {
    "totalLiters": 25.5,
    "daysInLactation": 30,
    "daysMeasured": 12,
    "averagePerDay": 2.13,
    "peakLiters": 3.8,
    "peakDate": "2026-01-12"
  },
  "pregnancy": {
    "gestationDays": 95,
    "dryOffRecommendation": true,
    "recommendedDryOffDate": "2026-01-15",
    "message": "Prenhez confirmada com 95 dias. Recomenda-se secagem."
  }
}
```

## DTOs, VOs e Entidades

### DTOs (Data Transfer Objects)
*   **LactationRequestDTO**: Usado para abrir lactação (`startDate`, `pregnancyStartDate`).
*   **LactationDryRequestDTO**: Usado para secagem (`endDate`, `dryReason`).
*   **LactationResponseDTO**: Retorno padrão para clientes da API.
*   **LactationSummaryResponseDTO**: Retorno do sumário (lactation + production + pregnancy).

### VOs (Value Objects - Business Layer)
*   **LactationRequestVO**: Objeto de negócio para criação.
*   **LactationResponseVO**: Objeto de negócio para resposta.

### Entity (Persistência)
*   **Lactation**: Entidade JPA mapeada para a tabela `lactation`.
*   `id`: Long (PK)
*   `farmId`: Long (Tenant)
*   `goatId`: String (FK lógica)
*   `status`: Enum (`ACTIVE`, `CLOSED`)

## Fluxo Hexagonal

```mermaid
graph LR
    Client --> Controller
    Controller -- DTO -> VO --> UseCase
    UseCase -- VO --> Business
    Business -- Entity --> Repository
    Repository --> Database
```

1.  **Controller**: `LactationController` recebe a requisição HTTP e valida o DTO.
2.  **Mapper**: Converte DTO para VO.
3.  **UseCase**: `LactationCommandUseCase` (escrita) ou `LactationQueryUseCase` (leitura).
4.  **Business**: `LactationBusiness` aplica regras de domínio (ex: verificar se já existe lactação ativa).
5.  **Repository**: `LactationRepository` persiste os dados.

## Erros e Exceções

*   **404 Not Found**: Se a cabra ou a lactação não forem encontradas.
*   **400 Bad Request**: Erros de validação (ex: data futura, campos nulos).
*   **422 Unprocessable Entity** (ou 400 mapeado): Regras de negócio violadas (ex: já existe lactação ativa, data de fim anterior ao início).
*   **403 Forbidden**: Usuário autenticado sem ownership da fazenda.

## Como testar

Sequência sugerida para testes manuais (Postman):

1.  **Criar Lactação**: `POST /api/goatfarms/1/goats/G001/lactations` com corpo `{ "startDate": "2023-01-01" }`.
2.  **Verificar Ativa**: `GET /api/goatfarms/1/goats/G001/lactations/active`.
3.  **Tentar Criar Duplicada**: Repetir o passo 1 (deve falhar).
4.  **Encerrar (Secar)**: `PATCH /api/goatfarms/1/goats/G001/lactations/{id}/dry` com corpo `{ "endDate": "2023-10-01" }`.
5.  **Verificar Histórico**: `GET /api/goatfarms/1/goats/G001/lactations`.

## Integração com Milk Production

O módulo de Produção de Leite (`MilkProduction`) depende diretamente deste módulo. O registro de produção leiteira geralmente requer uma lactação ativa (embora a validação estrita possa variar conforme a configuração da fazenda, a regra de negócio sugere vínculo).

## Recomendações de Manejo

- Em muitos manejos, a **secagem** é planejada aproximadamente **90 dias após a cobertura** ou cerca de **60 dias antes da data prevista de parto**.
- Essas referências de 90/60 dias são **recomendações de manejo** e não bloqueios da API: o sistema apenas valida consistência básica de datas (não futuras, `endDate` não anterior a `startDate`) e permite que a fazenda ajuste as datas conforme sua realidade.

### 4. Secagem manual (nao automatizada)
*   A secagem e uma decisao do proprietario. O sistema **nao** encerra lactacao automaticamente com base na confirmacao de prenhez.
*   A confirmacao de prenhez pode orientar o manejo, mas a data de secagem continua sendo informada manualmente via PATCH.

## Alertas farm-level de secagem

**Base Path:** `/api/goatfarms/{farmId}/milk/alerts`

| Metodo | Endpoint | Descricao |
|--------|----------|-----------|
| **GET** | `/dry-off` | Lista alertas de secagem para lactacoes ativas da fazenda. |

### Query params

- `referenceDate` (opcional, formato ISO `yyyy-MM-dd`, padrao: data atual)
- `page` (opcional, padrao: `0`)
- `size` (opcional, padrao: `20`)

### Regras de inclusao (as-of)

- Considera somente lactacao com `status = ACTIVE`.
- Para cada cabra, seleciona a prenhez mais recente com:
  - `startDatePregnancy = COALESCE(breeding_date, confirm_date)`
  - `startDatePregnancy <= referenceDate`
  - desempate por `id` (maior primeiro).
- Prenhez entra no alerta apenas se estiver ativa na data de referencia:
  - `status = ACTIVE`
  - e `closed_at IS NULL OR closed_at > referenceDate`.
- Recomendacao de secagem:
  - `gestationDays = referenceDate - startDatePregnancy`
  - `threshold = dryAtPregnancyDays` da lactacao (fallback 90)
  - entra no alerta quando `gestationDays >= threshold`
  - `dryOffDate = startDatePregnancy + threshold`
  - `daysOverdue = max(0, referenceDate - dryOffDate)`.

### Exemplo de request

`GET /api/goatfarms/1/milk/alerts/dry-off?referenceDate=2026-02-01&page=0&size=20`

### Exemplo de response

```json
{
  "totalPending": 2,
  "alerts": [
    {
      "lactationId": 101,
      "goatId": "GOAT-001",
      "startDatePregnancy": "2025-10-20",
      "breedingDate": "2025-10-20",
      "confirmDate": "2025-12-20",
      "dryOffDate": "2026-01-18",
      "dryAtPregnancyDays": 90,
      "gestationDays": 104,
      "daysOverdue": 14,
      "dryOffRecommendation": true
    }
  ]
}
```
