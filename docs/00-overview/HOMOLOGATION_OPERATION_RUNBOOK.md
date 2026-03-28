# Homologacao e operacao minima

Ultima atualizacao: 2026-03-28  
Escopo: rotina minima, objetiva e reproduzivel para smoke de restore e smoke de homologacao do backend.

## Objetivo

Garantir que o ambiente local/HML consiga:

- restaurar banco PostgreSQL de forma previsivel;
- validar o schema com Flyway apos restore;
- subir o backend;
- responder `health`;
- autenticar;
- executar uma leitura autenticada basica.

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

## Checklist minima antes de promover alteracoes

1. `.\mvnw.cmd -U -T 1C clean test`
2. backend em execucao com `health = UP`
3. `.\scripts\restore-smoke-postgres.ps1`
4. `.\scripts\homologation-smoke.ps1`
5. validar pelo menos um fluxo funcional afetado pela entrega

## O que nao fazer

- nao pular backup antes de operacoes arriscadas no banco;
- nao editar migration historica para contornar drift;
- nao usar `flyway:repair` sem evidenciar o diff;
- nao promover sem health, login e leitura autenticada funcionando.
