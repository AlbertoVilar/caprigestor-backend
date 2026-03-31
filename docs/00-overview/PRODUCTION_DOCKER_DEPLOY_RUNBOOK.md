# Runbook de deploy Docker em producao

## Objetivo

Padronizar o primeiro deploy real do CapriGestor com:

- backend Spring Boot em container
- frontend React empacotado em imagem Nginx
- PostgreSQL privado fora da internet
- RabbitMQ desligado
- segredos fora do repositorio e fora do Docker Hub

## Decisoes fixas

- `prod` continua sendo o profile real
- Docker Hub e registry guardam imagens, nao segredos
- JWT de producao fica fora da imagem
- `CAPRIGESTOR_MESSAGING_ENABLED=false`
- o banco restaurado deve vir da base saneada validada
- o Nginx do host publica `80/443`
- o frontend em container escuta apenas em `127.0.0.1:8088`
- o backend nao publica porta para internet

## Artefatos deste pacote

- [Dockerfile](C:\Dev\GoatFarm\Dockerfile)
- [docker-compose.prod.yml](C:\Dev\GoatFarm\docker\docker-compose.prod.yml)
- [.env.prod.example](C:\Dev\GoatFarm\docker\.env.prod.example)
- [caprigestor.app.conf.example](C:\Dev\GoatFarm\docker\caprigestor.app.conf.example)
- frontend:
  - [Dockerfile](C:\Dev\frontend\capril-vilar-react\capril-vilar\Dockerfile)
  - [nginx.conf](C:\Dev\frontend\capril-vilar-react\capril-vilar\nginx.conf)

## O que vai para o registry

Vai para o registry:

- imagem do backend
- imagem do frontend

Nao vai para o registry:

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

1. instalar Docker e Docker Compose plugin
2. instalar Nginx no host
3. criar diretorio de trabalho, por exemplo `/opt/caprigestor`
4. copiar para a VPS:
   - `docker/docker-compose.prod.yml`
   - `docker/.env.prod.example` como base para `.env.prod`
   - `docker/caprigestor.app.conf.example`
5. criar diretorio dos segredos JWT:

```bash
sudo mkdir -p /opt/caprigestor/secrets/jwt
sudo chmod 700 /opt/caprigestor/secrets/jwt
```

6. copiar `app.pub` e `app.key` de producao para esse diretorio

## Banco de dados

O banco de producao nao deve ser exposto publicamente.

Fluxo recomendado:

1. restaurar o backup saneado em um banco candidato
2. validar a base com o checklist de promocao
3. apontar `JDBC_URL`, `DB_USER` e `DB_PASSWORD` para esse banco

## Arquivo `.env.prod`

Crie `docker/.env.prod` na VPS a partir do exemplo e ajuste:

- imagens
- JDBC
- SMTP
- dominio real
- caminho do JWT

## Subida do stack

No diretorio `docker` da VPS:

```bash
docker compose --env-file .env.prod -f docker-compose.prod.yml pull
docker compose --env-file .env.prod -f docker-compose.prod.yml up -d
```

## Nginx do host

Copie o exemplo de virtual host e ajuste:

- dominio real
- caminhos do certificado
- porta local do frontend

Depois:

```bash
sudo nginx -t
sudo systemctl reload nginx
```

## Smoke obrigatorio antes do cutover

1. `GET /actuator/health` deve retornar `UP`
2. login com `Alberto Vilar`
3. abrir `Capril Vilar`
4. abrir `Capril Alto Paraiso`
5. dashboard
6. rebanho
7. detalhe do animal
8. genealogia publica
9. reproducao
10. lactacao
11. saude
12. producao consolidada da fazenda

## Rollback

Se algo falhar:

1. manter o dominio fechado
2. voltar a imagem anterior no `.env.prod`
3. subir novamente:

```bash
docker compose --env-file .env.prod -f docker-compose.prod.yml up -d
```

4. se o problema estiver no banco, restaurar o backup anterior ao cutover

## Gate para deploy

So seguir para o cutover publico quando:

- backend subir com `prod`
- `health` ficar `UP`
- `/actuator` raiz ficar fechado
- fazendas publicas nao vazarem email/cpf/endereco detalhado
- login real funcionar
- base saneada estiver validada
- Nginx do host estiver servindo HTTPS corretamente

