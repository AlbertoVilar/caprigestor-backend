# Saneamento controlado da base para produção

## Objetivo

Este runbook prepara a base do CapriGestor para produção real, preservando somente:

- a fazenda `Capril Vilar`
- a fazenda `Capril Alto Paraíso`
- os usuários reais necessários para operar essas fazendas
- os animais pertencentes a essas fazendas
- os vínculos de acesso indispensáveis

O saneamento remove:

- fazendas extras de QA/desenvolvimento
- usuários fake de teste
- todos os dados operacionais/transacionais de teste
- restos de estoque, comercial, financeiro operacional e auditoria ligados ao ambiente de desenvolvimento

## Preservação alvo

### Fazendas

- `1` — `Capril Vilar`
- `14` — `Capril Alto Paraíso`

### Usuários

- `1` — `Alberto Vilar`
- `14` — `Leonardo Oliveira`

## O que é removido

O script apaga, de forma controlada:

- `animal_sale`
- `milk_sale`
- `commercial_customer`
- `operational_expense`
- `operational_audit_entry`
- `inventory_movement`
- `inventory_balance`
- `inventory_lot`
- `inventory_idempotency`
- `inventory_item`
- `farm_milk_production`
- `milk_production`
- `lactation`
- `health_events`
- `reproductive_event`
- `pregnancy`
- `eventos`
- `password_reset_token`
- `tb_farm_operator` inválido para fazendas/usuários não preservados
- `telefone` de fazendas extras
- animais de fazendas extras
- fazendas extras
- usuários extras
- endereços órfãos

## Pré-requisitos

- container PostgreSQL local disponível
- banco alvo conhecido
- backup gerado antes da execução
- frontend/backend podem estar parados durante a limpeza

## Backup obrigatório

Exemplo:

```powershell
powershell -ExecutionPolicy Bypass -File C:\Dev\GoatFarm\scripts\backup-postgres.ps1 `
  -Database caprigestor_dev `
  -Container caprigestor-postgres `
  -User admin `
  -Password admin123 `
  -OutputDir C:\Dev\GoatFarm\backups
```

## Execução do saneamento

Exemplo:

```powershell
docker exec -i caprigestor-postgres psql -U admin -d caprigestor_dev -v ON_ERROR_STOP=1 `
  -f /dev/stdin < C:\Dev\GoatFarm\scripts\sanitize-production-base.sql
```

Alternativa no PowerShell puro:

```powershell
Get-Content C:\Dev\GoatFarm\scripts\sanitize-production-base.sql -Raw |
  docker exec -i caprigestor-postgres psql -U admin -d caprigestor_dev -v ON_ERROR_STOP=1
```

## Segurança da operação

- o script roda em transação única
- aborta se as fazendas preservadas não existirem
- aborta se os usuários preservados não existirem
- não usa Flyway
- não deve ser promovido como migration automática
- foi desenhado para esta base específica

## Validação pós-saneamento

Validar no mínimo:

1. só restam as fazendas `Capril Vilar` e `Capril Alto Paraíso`
2. os animais dessas fazendas continuam íntegros
3. módulos de leite, reprodução e saúde estão limpos
4. comercial, estoque e financeiro operacional não ficaram contaminados
5. login continua funcionando
6. ownership das duas fazendas continua íntegro
7. dashboard e listagem de animais abrem sem erro

## Observação importante

Este saneamento é específico da base atual de preparação para produção. Ele não substitui política de dados, seed real, nem pipeline de implantação.
