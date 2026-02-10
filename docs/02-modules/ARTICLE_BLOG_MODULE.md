# Módulo Article/Blog
Última atualização: 2026-02-10
Escopo: leitura pública de artigos e gestão admin (publicar/destacar)
Links relacionados: [Portal](../INDEX.md), [Arquitetura](../01-architecture/ARCHITECTURE.md), [Padrões de API](../03-api/API_CONTRACTS.md), [Domínio](../00-overview/BUSINESS_DOMAIN.md)

## Visão Geral
Este módulo fornece leitura pública de artigos (sem autenticação) e gerenciamento administrativo via API. Segue arquitetura hexagonal (Controller → Port In → Business → Port Out → Adapter/Repository).

## Endpoints Públicos (sem login)

Base path: `/public/articles`

| Método | Endpoint | Descrição | Status |
|--------|----------|-----------|--------|
| **GET** | `/public/articles` | Lista artigos publicados (paginação + filtros). | `200 OK` |
| **GET** | `/public/articles/{slug}` | Detalhe de artigo publicado. | `200 OK` |
| **GET** | `/public/articles/highlights` | Retorna 3 destaques para a home. | `200 OK` |

### Parâmetros de consulta
- `page`, `size`, `sort` (padrão `publishedAt,desc`)
- `category` (opcional)
- `q` (busca por título/resumo, opcional)

### Exemplo de resposta (lista pública)
```json
{
  "content": [
    {
      "title": "Manejo eficiente",
      "slug": "manejo-eficiente",
      "excerpt": "Resumo do artigo",
      "category": "MANEJO",
      "coverImageUrl": "https://example.com/cover.jpg",
      "publishedAt": "2026-01-20T10:00:00"
    }
  ],
  "page": {
    "size": 12,
    "number": 0,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

### Exemplo de resposta (detalhe público)
```json
{
  "title": "Manejo eficiente",
  "slug": "manejo-eficiente",
  "excerpt": "Resumo do artigo",
  "category": "MANEJO",
  "coverImageUrl": "https://example.com/cover.jpg",
  "contentMarkdown": "# Conteúdo",
  "publishedAt": "2026-01-20T10:00:00"
}
```

## Endpoints Admin (login obrigatório)

Base path: `/api/articles`

| Método | Endpoint | Descrição | Status |
|--------|----------|-----------|--------|
| **POST** | `/api/articles` | Cria rascunho. | `201 Created` |
| **PUT** | `/api/articles/{id}` | Atualiza conteúdo/metadados. | `200 OK` |
| **GET** | `/api/articles` | Lista todos (inclui rascunhos). | `200 OK` |
| **GET** | `/api/articles/{id}` | Detalhe administrativo. | `200 OK` |
| **PATCH** | `/api/articles/{id}/publish` | Publica ou despublica. | `200 OK` |
| **PATCH** | `/api/articles/{id}/highlight` | Destaca ou remove destaque. | `200 OK` |
| **DELETE** | `/api/articles/{id}` | Remove artigo. | `204 No Content` |

## Segurança
- Endpoints públicos são anonimamente acessíveis.
- Endpoints `/api/articles/**` exigem `ROLE_ADMIN`.
- Preparado para futura expansão com `ROLE_EDITOR` sem mudanças estruturais.

## Regras de Negócio
- Apenas artigos com `published=true` são retornados em `/public`.
- `slug` é gerado pelo título e deve ser único.
- Conflito de slug retorna **409** com mensagem: "Já existe um artigo com este slug."
- Publicação exige conteúdo válido (título, resumo, conteúdo e categoria).
- Highlights: se existirem 3+ destacados, retorna os 3 mais recentes; caso contrário, completa com os mais recentes publicados sem duplicar.

## Códigos de Erro
- **404 Not Found**: artigo não encontrado ou não publicado em rotas públicas.
- **409 Conflict**: slug duplicado.
- **422 Unprocessable Entity**: validações de conteúdo.
- **403 Forbidden**: acesso sem permissão em rotas admin.
