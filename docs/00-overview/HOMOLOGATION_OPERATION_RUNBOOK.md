# Homologacao e operacao minima

Ultima atualizacao: 2026-03-28  
Escopo: rotina minima, objetiva e reproduzivel para smoke de restore, smoke de homologacao do backend e smoke funcional critico de lactacao x prenhez x secagem.

## Objetivo

Garantir que o ambiente local/HML consiga:

- restaurar banco PostgreSQL de forma previsivel;
- validar o schema com Flyway apos restore;
- subir o backend;
- responder `health`;
- autenticar;
- executar uma leitura autenticada basica;
- validar o comportamento critico de lactacao, secagem e bloqueios com prenhez ativa.

## Pre-requisitos

- Docker em execucao
- container `caprigestor-postgres` ativo
- backend configurado para usar PostgreSQL local
- credenciais de teste validas

## Restore smoke do banco

Script oficial:

```powershell
.\scripts\restore-smoke-postgres.ps1
```

Fluxo executado pelo script:

1. gera backup do banco fonte;
2. restaura esse backup em um banco temporario;
3. executa `flyway:validate` no banco restaurado.

Parametros uteis:

```powershell
.\scripts\restore-smoke-postgres.ps1 -SourceDatabase caprigestor_dev -TargetDatabase caprigestor_restore_smoke
```

## Smoke de homologacao do backend

Script oficial:

```powershell
.\scripts\homologation-smoke.ps1
```

Fluxo executado pelo script:

1. valida `/actuator/health`;
2. faz login real;
3. valida `/api/v1/auth/me`;
4. executa uma leitura autenticada minima em `/api/v1/goatfarms`.

Parametros uteis:

```powershell
.\scripts\homologation-smoke.ps1 -BaseUrl http://localhost:8080 -Email albertovilar1@gmail.com -Password 132747
```

## Smoke funcional: lactacao x prenhez x secagem

Script oficial:

```powershell
.\scripts\lactation-dryoff-smoke.ps1
```

Objetivo:

- validar uma cabra ativa com recomendacao de secagem disponivel;
- validar uma cabra seca com prenhez ainda ativa;
- provar que a secagem pausa a producao;
- provar que nao e permitido abrir nova lactacao ou retomar a lactacao enquanto a prenhez continuar ativa.

Fluxo executado pelo script:

1. valida `health`;
2. faz login real;
3. consulta o resumo da cabra ativa e exige `recommendedDryOffDate`;
4. consulta o alerta farm-level de secagem e exige a cabra ativa no resultado;
5. valida que a cabra seca nao possui lactacao ativa;
6. valida que o historico da cabra seca esta em `DRY`;
7. tenta registrar leite na cabra seca e exige `422`;
8. tenta abrir nova lactacao na cabra seca com prenhez ativa e exige `422`;
9. tenta retomar a lactacao seca com prenhez ativa e exige `422`.

Parametros uteis:

```powershell
.\scripts\lactation-dryoff-smoke.ps1 -FarmId 17 -ActiveGoatId QAT03281450 -DriedGoatId QA0328145701
```

Quando usar:

- qualquer alteracao em `milk`, `reproduction` ou na UI que dependa de `recommendedDryOffDate`;
- qualquer hotfix envolvendo secagem, retomada de lactacao ou bloqueio de producao com prenhez ativa.

## Checklist minima antes de promover alteracoes

1. `.\mvnw.cmd -U -T 1C clean test`
2. backend em execucao com `health = UP`
3. `.\scripts\restore-smoke-postgres.ps1`
4. `.\scripts\homologation-smoke.ps1`
5. se a entrega tocar lactacao/reproduction/milk: `.\scripts\lactation-dryoff-smoke.ps1`
6. validar pelo menos um fluxo funcional afetado pela entrega no frontend quando houver impacto visual

## O que nao fazer

- nao pular backup antes de operacoes arriscadas no banco;
- nao editar migration historica para contornar drift;
- nao usar `flyway:repair` sem evidenciar o diff;
- nao promover sem health, login e leitura autenticada funcionando;
- nao tratar secagem como encerramento definitivo do ciclo quando ainda existir prenhez ativa;
- nao liberar nova lactacao ou retomada durante prenhez ativa apos secagem confirmada.
