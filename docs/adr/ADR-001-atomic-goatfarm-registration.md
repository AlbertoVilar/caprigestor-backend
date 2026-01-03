# ADR-001: Atomic GoatFarm Registration Flow

## Status
Accepted

## Context
O sistema permite o cadastro de fazendas (`GoatFarm`) tanto por usuários já autenticados quanto por usuários novos (anônimos). Anteriormente, a criação de entidades relacionadas (User, Address, Phones) poderia ocorrer de forma fragmentada ou não transacional, gerando inconsistência de dados e potenciais falhas de segurança (como *Privilege Escalation* ou *IDOR* - Insecure Direct Object References).

O requisito de negócio define que `GoatFarm` é o **Aggregate Root** e sua existência está intrinsecamente ligada a um `User` (proprietário), um `Address` e `Phones`.

## Decision
Decidimos implementar um **único método público de criação atômica** (`createGoatFarm`) que orquestra todo o processo dentro de uma única transação `@Transactional`.

### Detalhes da Implementação

1.  **Single Entry Point:** Apenas o endpoint `POST /api/goatfarms` (mapeado para `GoatFarmBusiness.createGoatFarm`) é responsável pela criação. Não existem endpoints públicos para criar endereços ou usuários isoladamente no contexto de registro de fazenda.
2.  **Validação Acumulada:** O sistema valida a presença de todos os componentes obrigatórios (`Farm`, `Address`, `Phones` e `User` condicionalmente) antes de iniciar qualquer persistência, retornando todos os erros de uma vez.
3.  **Owner Resolution Strategy:**
    *   **Autenticado:** O `owner` é resolvido via `OwnershipService.getCurrentUser()`. Qualquer dado de usuário enviado no corpo da requisição é **ignorado**.
    *   **Anônimo:** O sistema exige dados de usuário.
        *   Cria um novo usuário.
        *   Força a role `ROLE_USER`.
        *   Rejeita explicitamente campos de privilégio (`roles`, `admin`, etc).
        *   Se o email já existir, lança `DuplicateEntityException` com mensagem genérica para evitar enumeração.

## Consequences

### Positivos
*   **Consistência:** Garantia de que não existirão "fazendas órfãs" ou "usuários sem fazenda" criados pela metade devido a falhas no meio do processo.
*   **Segurança:** Elimina vetores de ataque comuns em registros multipartes. O backend detém controle total sobre as permissões atribuídas.
*   **Manutenibilidade:** Lógica centralizada em um único método de negócio, facilitando testes e auditoria.

### Negativos/Trade-offs
*   **Complexidade do Payload:** O frontend precisa enviar um JSON aninhado e completo (`GoatFarmFullRequestDTO`), o que pode ser mais complexo de montar do que chamadas sequenciais simples.
*   **Acoplamento:** A criação de usuário está acoplada à criação da fazenda neste fluxo específico, o que é aceitável dado o domínio, mas requer cuidado se no futuro quisermos permitir usuários sem fazenda.

## Alternativas Consideradas

### Criação em Etapas (Wizard)
Permitir criar User -> obter ID -> criar Farm com ID.
*   **Rejeitado por:** Risco de criar usuários "zumbis" se o usuário desistir na etapa 2. Risco de segurança ao permitir associação via ID (IDOR).

### Endpoint de Registro de Usuário Separado
*   **Rejeitado por:** O domínio exige que, no contexto de `GoatFarm`, o usuário nasça junto com a fazenda (ou já exista). Separar o registro complicaria a transação atômica.
