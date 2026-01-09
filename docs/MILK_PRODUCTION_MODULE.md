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
| **DELETE** | `/{id}` | Remove um registro de produção existente. |

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
