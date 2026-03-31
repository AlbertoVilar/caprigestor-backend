# Runbook de saneamento da base promovivel

## Objetivo

Definir como a base que vai para producao deve ser preparada e validada.

Este runbook nao eh uma migration da aplicacao.
Ele descreve uma operacao controlada de base.

## Regra fixa

A producao deve nascer de um backup saneado.

Nao eh aceitavel:

- subir banco vazio
- subir base com fazendas fake
- subir base com historico operacional de teste
- rodar limpeza destrutiva diretamente na base ja exposta ao publico

## Estado final esperado da base promovivel

Fazendas preservadas:

- `1` - `Capril Vilar`
- `14` - `Capril Alto Paraiso`

Usuarios reais preservados:

- `1` - `Alberto Vilar`
- `14` - `Leonardo Oliveira`

Animais preservados:

- rebanho real de `Capril Vilar`
- rebanho real de `Capril Alto Paraiso`, se existir na base saneada final

Dados operacionais que nao devem seguir para producao:

- lactacoes
- producoes individuais de leite
- producoes consolidadas da fazenda
- prenhezes
- eventos reprodutivos
- eventos sanitarios
- estoque fake
- comercial fake
- financeiro operacional fake
- auditoria operacional fake

## Sequencia segura

1. restaurar o backup fonte em banco candidato
2. executar o saneamento apenas no banco candidato
3. validar integridade do resultado
4. gerar o backup final saneado
5. promover esse backup saneado para o banco de producao

## O que nao fazer

- nao transformar o saneamento em Flyway
- nao limpar na base de producao aberta
- nao apagar animais reais
- nao preservar fazendas extras
- nao promover base sem validacao

## Validacao obrigatoria

Antes de promover o backup saneado, confirmar:

- so existem `Capril Vilar` e `Capril Alto Paraiso`
- so existem os usuarios reais necessarios
- os animais preservados continuam integros
- ownership continua coerente
- nao restou lixo operacional fake

## Script de apoio

Para checagem apos o restore, usar:

- [production-db-integrity-check.sql](C:\Dev\GoatFarm\scripts\production-db-integrity-check.sql)

## Gate

So considerar a base pronta para promocao se:

- a validacao de integridade estiver limpa
- o restore candidato estiver coerente
- o backup final saneado tiver sido gerado a partir desse estado validado
