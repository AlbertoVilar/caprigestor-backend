# Checklist de promocao da base para producao

## Objetivo

Executar a promocao da base saneada para o banco de producao sem improviso.

## Precondicoes

- backup final saneado disponivel
- servidor de producao pronto
- secrets reais preparados
- banco PostgreSQL privado pronto
- janela de cutover definida

## Checklist

### Antes do restore

- [ ] Escrita congelada na origem
- [ ] Backup final saneado gerado
- [ ] Backup anterior ao cutover separado para rollback
- [ ] Banco de destino confirmado como privado
- [ ] Usuario da aplicacao com privilegio adequado definido

### Restore

- [ ] Banco de producao criado
- [ ] Restore concluido sem erro fatal
- [ ] Script de integridade executado

### Integridade obrigatoria

- [ ] Existem apenas `Capril Vilar` e `Capril Alto Paraiso`
- [ ] Existem apenas os usuarios reais necessarios
- [ ] O rebanho preservado esta integro
- [ ] Tabelas transacionais fake estao zeradas
- [ ] Nao ha referencia quebrada relevante

### Subida da aplicacao

- [ ] `.env.prod` configurado no servidor
- [ ] Chaves JWT reais montadas no servidor
- [ ] `CAPRIGESTOR_MESSAGING_ENABLED=false`
- [ ] Backend sobe em `prod`
- [ ] Frontend sobe apontando para a API real
- [ ] `/actuator` bloqueado
- [ ] `/actuator/health` ok

### Smoke fechado

- [ ] Login com Alberto Vilar
- [ ] Capril Vilar acessivel
- [ ] Capril Alto Paraiso acessivel
- [ ] Rebanho acessivel
- [ ] Detalhe do animal acessivel
- [ ] Reproducao acessivel
- [ ] Lactacao acessivel
- [ ] Saude acessivel
- [ ] Producao consolidada acessivel
- [ ] Payload publico sanitizado
- [ ] Ownership sem vazamento cross-farm
- [ ] Sem erro estrutural de logs

## Go / No-Go

### GO somente se

- todos os itens acima estiverem marcados

### NO-GO se

- qualquer item critico de restore, integridade, auth, ownership, actuator ou smoke falhar
