# Módulo Milk Production

## Visão Geral
Este módulo é responsável pelo gerenciamento da produção leiteira das cabras, permitindo o registro diário de ordenhas, controle de lactações e análise de produtividade.

## Endpoints do CRUD

O módulo expõe uma API RESTful completa para gerenciamento das produções.
**Base Path:** `/api/goatfarms/{farmId}/goats/{goatId}/milk-productions`

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| **POST** | `/` | Cria um novo registro de produção de leite. |
| **GET** | `/` | Lista as produções de forma paginada (suporta filtros de data). |
| **GET** | `/{id}` | Busca os detalhes de uma produção específica por ID. |
| **PATCH** | `/{id}` | Atualiza parcialmente uma produção (apenas volume e observações). |
| **DELETE** | `/{id}` | Remove um registro de produção existente. |

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

## Fluxo de Exclusão (DELETE)

A operação de exclusão segue estritamente a arquitetura Hexagonal e as regras de segurança do projeto:

1.  **Validação de Escopo**: O sistema verifica se o ID da produção pertence à Cabra (`goatId`) e à Fazenda (`farmId`) informadas na URL.
2.  **Busca Segura**: Utiliza o método `findById(farmId, goatId, id)` no Business para garantir que o registro existe dentro do contexto autorizado.
3.  **Persistência**: Se encontrado, o registro é removido fisicamente do banco de dados via JPA.
4.  **Retorno**:
    *   **204 No Content**: Exclusão realizada com sucesso.
    *   **404 Not Found**: Se o registro não existir ou não pertencer ao escopo (Fazenda/Cabra) informado.

## Segurança e Consistência

*   **Escopo Obrigatório**: Todas as operações exigem `farmId` e `goatId`. Não é possível manipular produções apenas pelo ID global.
*   **Isolamento**: A arquitetura garante que um usuário não consiga excluir produções de outras fazendas, mesmo que adivinhe o ID sequencial, pois a validação de escopo falhará.
