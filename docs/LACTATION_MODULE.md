# Módulo Lactation

## Visão Geral
Este módulo é responsável pelo ciclo de vida produtivo das cabras, gerenciando a abertura e o encerramento de lactações (períodos em que o animal está produzindo leite). É fundamental para garantir a consistência dos registros de produção leiteira.

## Endpoints do CRUD

O módulo expõe uma API RESTful completa para gerenciamento das lactações.
**Base Path:** `/api/v1/farms/{farmId}/goats/{goatId}/lactations`

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| **POST** | `/` | Abre uma nova lactação (Início do ciclo produtivo). |
| **PATCH** | `/{id}/dry` | Encerra uma lactação ativa (Secagem do animal). |
| **GET** | `/active` | Busca a lactação atualmente ativa para a cabra. |
| **GET** | `/{id}` | Busca os detalhes de uma lactação específica por ID. |
| **GET** | `/` | Lista o histórico de lactações de forma paginada. |

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

## Integração com Milk Production
O módulo de Produção de Leite (`MilkProduction`) depende diretamente deste módulo. O registro de produção leiteira geralmente requer uma lactação ativa (embora a validação estrita possa variar conforme a configuração da fazenda, a regra de negócio sugere vínculo).
