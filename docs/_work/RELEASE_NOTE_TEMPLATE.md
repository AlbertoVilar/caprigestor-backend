## Resumo Final — <Título da Entrega>

Auditoria concluída em **<YYYY-MM-DD>** para **<N> documentos oficiais** em `<escopo>` (excluindo `<pastas_excluídas>`), conforme inventário em `<arquivo_inventario>`.
Relatórios gerados: `<arquivo_before>` e `<arquivo_after>`.
Documentação oficial padronizada e publicada via PRs **mesclados (MERGED)** em `<branch_intermediaria>` e depois `<branch_final>`.

## Top 5 problemas encontrados (BEFORE)
- <Problema 1>
- <Problema 2>
- <Problema 3>
- <Problema 4>
- <Problema 5>

## Principais correções aplicadas
- <Correção 1>
- <Correção 2>
- <Correção 3>
- <Correção 4>
- <Correção 5>

## Métricas (BEFORE → AFTER)
- `<métrica_1>`: **<antes> → <depois>**
- `<métrica_2>`: **<antes> → <depois>**
- `<métrica_3>`: **<antes> → <depois>**
- `<métrica_4>`: **<antes> → <depois>**
- `<métrica_5>`: **<antes> → <depois>** (links externos removidos quando redundantes; referências externas relevantes devem ir para ADRs/`README.md` quando necessário)

## PRs
- PR #<id_1> (`<head_1>` → `<base_1>`) — **MERGED** em <YYYY-MM-DD>
- PR #<id_2> (`<head_2>` → `<base_2>`) — **MERGED** em <YYYY-MM-DD>

## Links dos PRs
- <url_pr_1>
- <url_pr_2>

## Estado final do repositório
- Working tree limpo: `git status -sb` → `<saida_status>`
- Branches sincronizadas local/remoto: `<branches>` alinhadas com `origin`
- Branch de feature removida após merge
- Verificação de sincronização: `git rev-list --left-right --count origin/<branch_final>...<branch_final>` → `<left> <right>`
