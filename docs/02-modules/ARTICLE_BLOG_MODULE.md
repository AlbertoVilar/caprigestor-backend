# Modulo Article/Blog
Ultima atualizacao: 2026-02-10
Escopo: leitura publica de artigos e gestao administrativa de conteudo.
Links relacionados: [Portal](../INDEX.md), [Arquitetura](../01-architecture/ARCHITECTURE.md), [API_CONTRACTS](../03-api/API_CONTRACTS.md), [Dominio](../00-overview/BUSINESS_DOMAIN.md)

## Visao geral
O modulo de artigos expone endpoints publicos sem autenticacao e endpoints administrativos para criacao, publicacao, destaque e remocao.

## Regras / Contratos
- Rotas publicas usam base `/public/articles`.
- Rotas administrativas usam base `/api/articles` e exigem `ROLE_ADMIN`.
- Apenas artigos com `published=true` aparecem no catalogo publico.
- `slug` e derivado do titulo e deve ser unico.

## Endpoints
### Publicos
Base URL: `/public/articles`

| Metodo | URL | Query params | Retorno |
|---|---|---|---|
| `GET` | `/public/articles` | `page`, `size`, `sort`, `category`, `q` | `200 OK` (pagina de artigos publicados) |
| `GET` | `/public/articles/highlights` | - | `200 OK` (ate 3 destaques) |
| `GET` | `/public/articles/{slug}` | - | `200 OK` (detalhe publicado) |

Contrato curto (lista publica):
- URL: `GET /public/articles?page=0&size=12&sort=publishedAt,desc`
- Query params: `category` (opcional), `q` (opcional)
- Response curto:

```json
{
  "content": [
    {
      "title": "Manejo eficiente",
      "slug": "manejo-eficiente",
      "category": "MANEJO",
      "publishedAt": "2026-01-20T10:00:00"
    }
  ],
  "totalElements": 1
}
```

### Administrativos
Base URL: `/api/articles`

| Metodo | URL | Query params | Retorno |
|---|---|---|---|
| `POST` | `/api/articles` | - | `201 Created` |
| `PUT` | `/api/articles/{id}` | - | `200 OK` |
| `GET` | `/api/articles` | `page`, `size`, `sort` | `200 OK` |
| `GET` | `/api/articles/{id}` | - | `200 OK` |
| `PATCH` | `/api/articles/{id}/publish` | - | `200 OK` |
| `PATCH` | `/api/articles/{id}/highlight` | - | `200 OK` |
| `DELETE` | `/api/articles/{id}` | - | `204 No Content` |

Contrato curto (criacao admin):
- URL: `POST /api/articles`
- Request curto:

```json
{
  "title": "Manejo eficiente",
  "excerpt": "Resumo",
  "contentMarkdown": "# Conteudo",
  "category": "MANEJO"
}
```

- Response curto: objeto do artigo criado com `id`.

## Fluxos principais
1. Leitura publica:
   `PublicArticleController` retorna somente artigos publicados.
2. Publicacao:
   `PATCH /api/articles/{id}/publish` muda estado de publicacao.
3. Destaque:
   `PATCH /api/articles/{id}/highlight` marca/desmarca destaque.

Observacao de performance:
- Listagens sao paginadas e ordenadas no banco.
- Endpoint de highlights limita retorno para home sem carregar catalogo completo.

## Erros/Status
- `404 Not Found`: artigo inexistente ou nao publicado em rota publica.
- `409 Conflict`: `slug` duplicado.
- `422 Unprocessable Entity`: validacao de conteudo.
- `403 Forbidden`: acesso sem permissao em rotas admin.
- Padrao de payload de erro: [API_CONTRACTS](../03-api/API_CONTRACTS.md).

## Referencias internas
- Controller publico: [src/main/java/com/devmaster/goatfarm/article/api/controller/PublicArticleController.java](../../src/main/java/com/devmaster/goatfarm/article/api/controller/PublicArticleController.java)
- Controller admin: [src/main/java/com/devmaster/goatfarm/article/api/controller/ArticleAdminController.java](../../src/main/java/com/devmaster/goatfarm/article/api/controller/ArticleAdminController.java)
