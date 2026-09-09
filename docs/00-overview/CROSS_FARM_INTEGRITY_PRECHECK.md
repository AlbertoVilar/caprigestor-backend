# Precheck de integridade entre fazendas

Antes de criar ou validar uma constraint composta, execute
`scripts/sql/cross-farm-integrity-precheck.sql` no banco de homologação e no
alvo de produção com uma credencial somente leitura.

O script retorna apenas contagens agregadas. O aceite de uma migration exige
que todas estejam em zero, que o resultado seja associado ao SHA do artefato e
que os responsáveis pelo banco confirmem volume e orçamento de lock. Não copie
linhas identificáveis, URLs de conexão, usuários ou senhas para a evidência.

Uma contagem diferente de zero interrompe a migration. A correção exige decisão
de negócio sobre a fazenda correta e uma operação de saneamento aprovada; nunca
use `flyway repair`, update em massa ou inferência automática para mascarar a
divergência.

O script cobre relações que hoje possuem chaves estrangeiras independentes ou
não possuem chave composta: reprodução/cabra/eventos, saúde/cabra, comercial/
cliente e cabra, estoque/item/lote, lactação/produção de leite e auditoria
operacional/cabra. Ele retorna 22 contagens agregadas no estado atual. É somente
diagnóstico: não substitui backup, `flyway info`/`validate`, teste de upgrade nem
o restore smoke.
