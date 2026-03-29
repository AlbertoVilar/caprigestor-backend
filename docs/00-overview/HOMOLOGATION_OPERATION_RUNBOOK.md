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

## Smoke funcional: carencia sanitaria operacional

Script oficial:

```powershell
.\scripts\health-withdrawal-smoke.ps1
```

Objetivo:

- criar um evento sanitario com carencia em uma cabra com lactacao ativa;
- marcar o evento como realizado;
- provar que a carencia fica ativa no detalhe do evento e no status da cabra;
- provar que o alerta farm-level passa a exibir carencia de leite e carne;
- provar que o backend permite registrar a producao durante carencia ativa, mas marca o registro com snapshot sanitario e alerta forte;
- provar que a derivacao temporal libera a carencia em uma data de referencia futura.

Fluxo executado pelo script:

1. valida `health`;
2. faz login real;
3. valida que a cabra alvo possui lactacao ativa;
4. cria evento sanitario com `withdrawalMilkDays` e `withdrawalMeatDays`;
5. marca o evento como realizado;
6. valida o detalhe do evento e o status de carencia da cabra;
7. valida os alertas farm-level de carencia;
8. registra producao de leite durante carencia e exige `201` com snapshot sanitario no payload;
9. consulta o status com `referenceDate` futura e exige carencia expirada.

Parametros uteis:

```powershell
.\scripts\health-withdrawal-smoke.ps1 -FarmId 17 -GoatId QAT03281450
```

Quando usar:

- qualquer alteracao em `health` que toque `withdrawalMilkDays` ou `withdrawalMeatDays`;
- qualquer alteracao em `milk` que possa perder o snapshot sanitario da producao durante carencia;
- qualquer mudanca visual em detalhe da cabra, alertas de fazenda ou pagina de producao que dependa da leitura de carencia.
