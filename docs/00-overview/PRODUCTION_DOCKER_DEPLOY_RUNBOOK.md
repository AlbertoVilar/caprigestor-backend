# Runbook de deploy Docker em producao

## Objetivo

Executar o primeiro cutover real do CapriGestor com:

- frontend e backend no mesmo dominio
- Nginx do host como unico ponto publico
- backend Spring Boot privado atras do proxy
- PostgreSQL privado
- RabbitMQ desligado
- base restaurada a partir do backup saneado
- smoke fechado antes de abrir o dominio ao publico

## Decisoes fixas

- `prod` e o profile real
- `CAPRIGESTOR_MESSAGING_ENABLED=false`
- Docker Hub/registry guarda apenas imagens
- segredos reais ficam fora do repositorio
- o banco promovido nasce do backup saneado validado
- o backend nao publica porta publica
- PostgreSQL nao fica exposto a internet
- `/actuator` deve ficar bloqueado externamente
- somente `/actuator/health` pode ser consultado no smoke

## Artefatos deste pacote

- [Dockerfile](C:\Dev\GoatFarm\Dockerfile)
- [docker-compose.prod.yml](C:\Dev\GoatFarm\docker\docker-compose.prod.yml)
- [.env.prod.example](C:\Dev\GoatFarm\docker\.env.prod.example)
- [caprigestor.app.conf.example](C:\Dev\GoatFarm\docker\caprigestor.app.conf.example)
- frontend:
  - [Dockerfile](C:\Dev\frontend\capril-vilar-react\capril-vilar\Dockerfile)
  - [nginx.conf](C:\Dev\frontend\capril-vilar-react\capril-vilar\nginx.conf)
- base:
  - [PRODUCTION_BASE_SANITIZATION_RUNBOOK.md](C:\Dev\GoatFarm\docs\00-overview\PRODUCTION_BASE_SANITIZATION_RUNBOOK.md)
  - [PRODUCTION_DATABASE_PROMOTION_CHECKLIST.md](C:\Dev\GoatFarm\docs\00-overview\PRODUCTION_DATABASE_PROMOTION_CHECKLIST.md)

## O que vai para o registry

Vai:

- imagem do backend
- imagem do frontend

Nao vai:

- `DB_PASSWORD`
- `MAIL_PASSWORD`
- `JWT_PRIVATE_KEY`
- `JWT_PUBLIC_KEY`
- `.env.prod`
- backups de banco

## Build e push das imagens

### Backend

```bash
docker build -t seu-usuario/caprigestor-backend:2026-03-31 C:/Dev/GoatFarm
docker push seu-usuario/caprigestor-backend:2026-03-31
```

### Frontend

```bash
docker build ^
  --build-arg VITE_API_BASE_URL=https://app.seu-dominio.com ^
  -t seu-usuario/caprigestor-frontend:2026-03-31 ^
  C:/Dev/frontend/capril-vilar-react/capril-vilar

docker push seu-usuario/caprigestor-frontend:2026-03-31
```

## Preparacao da VPS

### Runtime minimo

1. instalar Docker e Docker Compose plugin
2. instalar Nginx no host
3. instalar PostgreSQL no host ou em rede privada estritamente controlada
4. garantir que apenas `80/443` estejam publicos
5. garantir que o backend nao tenha porta publica publicada
6. garantir que `5432` nao esteja publica

### Estrutura de diretorios

```bash
sudo mkdir -p /opt/caprigestor/docker
sudo mkdir -p /opt/caprigestor/secrets/jwt
sudo chmod 700 /opt/caprigestor/secrets/jwt
```

### Arquivos a copiar

Copiar para a VPS:

- `docker/docker-compose.prod.yml`
- `docker/.env.prod.example` como base para `.env.prod`
- `docker/caprigestor.app.conf.example`
- `app.pub` e `app.key` de producao para `/opt/caprigestor/secrets/jwt`

## Secrets e variaveis

Criar `/opt/caprigestor/docker/.env.prod` a partir do exemplo.

Preencher, no minimo:

- `BACKEND_IMAGE`
- `FRONTEND_IMAGE`
- `JDBC_URL`
- `DB_USER`
- `DB_PASSWORD`
- `JWT_KEYS_DIR=/opt/caprigestor/secrets/jwt`
- `CORS_ORIGINS=https://app.seu-dominio.com`
- `PASSWORD_RESET_FRONTEND_BASE_URL=https://app.seu-dominio.com`
- `PASSWORD_RESET_FROM_ADDRESS=no-reply@seu-dominio.com`
- `MAIL_HOST`
- `MAIL_PORT`
- `MAIL_USER`
- `MAIL_PASSWORD`
- `CAPRIGESTOR_BOOTSTRAP_ADMIN_ENABLED=false`
- `CAPRIGESTOR_BOOTSTRAP_ADMIN_RESET_PASSWORD=false`
- `CAPRIGESTOR_MESSAGING_ENABLED=false`

## Promocao da base saneada

### Regra obrigatoria

A producao nao nasce de banco vazio nem da base local alterada manualmente.

Fluxo correto:

1. congelar escrita na origem
2. gerar backup final da base saneada
3. restaurar esse backup em banco candidato/prod
4. validar a base restaurada
5. so depois subir a aplicacao

### Restore

Exemplo:

```bash
createdb -U postgres caprigestor_prod
psql -U postgres -d caprigestor_prod -f /caminho/backup-saneado.sql
```

### Checks obrigatorios no banco restaurado

Executar antes de subir a aplicacao:

```sql
SELECT id, nome FROM capril ORDER BY id;
SELECT id, name, email FROM users ORDER BY id;
SELECT farm_id, COUNT(*) AS goats FROM cabras GROUP BY farm_id ORDER BY farm_id;
SELECT COUNT(*) FROM lactation;
SELECT COUNT(*) FROM milk_production;
SELECT COUNT(*) FROM farm_milk_production;
SELECT COUNT(*) FROM pregnancy;
SELECT COUNT(*) FROM reproductive_event;
SELECT COUNT(*) FROM health_events;
```

Criterio de sucesso:

- existem apenas `Capril Vilar` e `Capril Alto Paraiso`
- existem apenas os usuarios reais necessarios
- os animais preservados seguem integros
- dados transacionais fake seguem zerados

## Subida do stack em modo fechado

No diretorio `/opt/caprigestor/docker`:

```bash
docker compose --env-file .env.prod -f docker-compose.prod.yml pull
docker compose --env-file .env.prod -f docker-compose.prod.yml up -d
docker compose --env-file .env.prod -f docker-compose.prod.yml ps
```

### Checkpoints imediatos

```bash
docker compose --env-file .env.prod -f docker-compose.prod.yml logs --tail=200 backend
docker compose --env-file .env.prod -f docker-compose.prod.yml logs --tail=200 frontend
curl -i http://127.0.0.1:8088/actuator
curl -s http://127.0.0.1:8088/actuator/health
curl -I http://127.0.0.1:8088/
```

Criterio de sucesso:

- backend sem erro estrutural no boot
- sem tentativa de Rabbit bloqueando subida
- `/actuator` retorna `403`
- `/actuator/health` retorna `UP`
- frontend responde localmente

## Nginx do host

1. copiar o virtual host de exemplo
2. ajustar:
   - dominio real
   - caminhos do certificado
   - porta local do frontend
3. validar:

```bash
sudo nginx -t
sudo systemctl reload nginx
```

### Checkpoints

```bash
curl -I http://app.seu-dominio.com
curl -I https://app.seu-dominio.com
curl -i https://app.seu-dominio.com/actuator
curl -s https://app.seu-dominio.com/actuator/health
```

Criterio de sucesso:

- HTTP redireciona para HTTPS
- `/actuator` fica bloqueado
- `/actuator/health` responde
- frontend abre pelo dominio final

## Smoke fechado obrigatorio

Executar com o dominio ainda nao divulgado publicamente.

### Autenticacao

1. login com `Alberto Vilar`
2. confirmar `accessToken`
3. confirmar sessao operacional valida

### Fazendas e ownership

1. abrir `Capril Vilar`
2. abrir `Capril Alto Paraiso`
3. validar ausencia de vazamento cross-farm
4. validar payload publico sanitizado
5. validar payload privado completo somente para usuario autorizado

### Aplicacao

1. dashboard
2. listagem de animais
3. detalhe do animal
4. genealogia publica
5. reproducao
6. lactacao
7. saude
8. producao consolidada
9. ausencia de historico fake

### Infra

1. `/actuator` bloqueado
2. `/actuator/health` ok
3. backend sem erro critico em logs
4. frontend apontando para a API real
5. sem erro critico de console

## GO / NO-GO

### GO somente se

- secrets reais estiverem configurados no servidor
- banco restaurado estiver integro
- apenas `Capril Vilar` e `Capril Alto Paraiso` existirem
- usuarios reais necessarios estiverem corretos
- animais preservados estiverem integros
- `/actuator` estiver bloqueado
- `/actuator/health` estiver ok
- payload publico estiver sanitizado
- ownership estiver correto
- smoke fechado estiver limpo
- rollback estiver pronto

### NO-GO se

- PostgreSQL estiver exposto
- backend estiver exposto diretamente
- secrets estiverem inseguros
- base restaurada estiver inconsistente
- houver vazamento de dados publicos
- houver falha de ownership
- modulos criticos quebrarem
- houver resquicio operacional fake relevante
- rollback nao estiver definido

## Rollback

### Gatilhos

Abortar o cutover se houver:

- falha estrutural no backend
- falha de login
- base inconsistente
- `/actuator` exposto
- vazamento publico de dados
- falha cross-farm

### Passos

1. manter o dominio fechado ao publico
2. voltar as tags anteriores no `.env.prod`
3. subir novamente:

```bash
docker compose --env-file .env.prod -f docker-compose.prod.yml up -d
```

4. se o problema estiver no banco:
   - parar a aplicacao
   - restaurar o backup anterior ao cutover
   - validar o restore
   - so entao reconsiderar nova tentativa

## Gate final

So seguir para abertura publica quando todos os checkpoints acima estiverem verdes.
