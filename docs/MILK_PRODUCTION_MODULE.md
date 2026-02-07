# Módulo Milk Production

## Visão Geral
Este módulo é responsável pelo gerenciamento da produção leiteira das cabras, permitindo o registro diário de ordenhas, controle de lactações e análise de produtividade.

## Endpoints do CRUD

O módulo expõe uma API RESTful completa para gerenciamento das produções.
**Base Path:** `/api/goatfarms/{farmId}/goats/{goatId}/milk-productions`

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| **POST** | `/` | Cria um novo registro de produção de leite. |
| **GET** | `/` | Lista as produções de forma paginada (por padrão apenas ACTIVE; use includeCanceled=true para incluir canceladas). |
| **GET** | `/{id}` | Busca os detalhes de uma produção específica por ID. |
| **PATCH** | `/{id}` | Atualiza parcialmente uma produção (apenas volume e observações). |
| **DELETE** | `/{id}` | Cancela um registro de produção (soft delete). |


## Regras de Domínio
- A produção de leite é registrada via **POST** e nunca é iniciada automaticamente por PATCH de lactação.
- Para registrar produção, é obrigatório informar `date`, `shift` e `volumeLiters`.
- A produção exige lactação ativa para a cabra no momento do registro.
- Registros cancelados não aparecem em GET / por padrão (use includeCanceled=true para incluir).
- `includeCanceled=true` retorna registros **ACTIVE** + **CANCELED** na listagem paginada.

## Segurança e Autorização

- Todos os endpoints de produção de leite são privados e exigem token JWT válido.
- Chamadas sem token retornam **401 Unauthorized** para qualquer rota de produção.
- Usuários com **ROLE_ADMIN** têm acesso total, independentemente do `farmId`.
- Usuários com **ROLE_OPERATOR** ou **ROLE_FARM_OWNER** só acessam produções da fazenda se forem proprietários (`ownershipService.isFarmOwner(farmId)`).
- Quando o token é válido mas o usuário não é proprietário da fazenda, a API retorna **403 Forbidden**.

## Fluxo de Atualização (PATCH)

A operação de atualização permite modificar dados mutáveis de uma produção existente:

1.  **Campos Mutáveis**: Apenas `volumeLiters` e `notes` podem ser alterados.
2.  **Imutabilidade**: `date` e `shift` não podem ser alterados via PATCH para garantir a integridade das validações de duplicidade.
3.  **Validação de Escopo**: Assim como no DELETE, o sistema verifica se o registro pertence ao `farmId` e `goatId` informados.
4.  **Bloqueio**: Registros cancelados não podem ser alterados (422).

## Fluxo de Exclusão (DELETE)

A exclusão é um **cancelamento lógico** para preservar auditoria:

1.  **Validação de Escopo**: O sistema verifica se o ID da produção pertence à Cabra (`goatId`) e à Fazenda (`farmId`) informadas na URL.
2.  **Busca Segura**: Utiliza o método `findById(farmId, goatId, id)` no Business para garantir que o registro existe dentro do contexto autorizado.
3.  **Persistência**: O registro **não é removido fisicamente**. Ele é marcado como `status = CANCELED`, com `canceledAt` preenchido e `canceledReason` nulo (v1).
4.  **Retorno**:
    *   **204 No Content**: Cancelamento realizado com sucesso.
    *   **404 Not Found**: Se o registro não existir ou não pertencer ao escopo (Fazenda/Cabra) informado.

## Observações de Cancelamento

- `GET /{id}` retorna **200 OK** mesmo para registros cancelados, com `status = CANCELED`.
- `GET /` lista apenas `ACTIVE` por padrão. Para incluir cancelados, usar `includeCanceled=true`.
- `PATCH /{id}` em registro cancelado retorna **422** com mensagem: "Registro cancelado não pode ser alterado."

### Erros comuns (status codes)
- **422 Unprocessable Entity**: sem lactação ativa no momento do registro; ou tentativa de PATCH em registro cancelado ("Registro cancelado não pode ser alterado.").
- **409 Conflict**: já existe produção **ACTIVE** para a mesma combinação (`date`, `shift`) (unicidade parcial para ACTIVE).
