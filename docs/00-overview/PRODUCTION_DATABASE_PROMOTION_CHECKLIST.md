# Checklist final de promoção da base para produção

## Objetivo

Promover a base saneada do CapriGestor para o ambiente de produção, preservando:

- `Capril Vilar`
- `Capril Alto Paraíso`
- usuários reais necessários
- animais reais preservados

Sem reintroduzir dados fake de desenvolvimento, QA ou homologação.

## Pré-condições obrigatórias

- o saneamento já foi executado na base origem
- o backup da base saneada já existe
- backend e frontend do ambiente de produção estão prontos
- variáveis de ambiente de produção já foram definidas
- o ambiente de produção usa `SPRING_PROFILES_ACTIVE=prod`
- a produção real está configurada sem RabbitMQ:
  - `CAPRIGESTOR_MESSAGING_ENABLED=false`

## Checklist técnico

### 1. Congelar escrita na base origem

- parar lançamentos manuais no ambiente origem
- não executar smoke que grave dados
- garantir que ninguém esteja operando durante o corte

### 2. Gerar backup final da base saneada

Executar:

```powershell
powershell -ExecutionPolicy Bypass -File C:\Dev\GoatFarm\scripts\backup-postgres.ps1 `
  -Database caprigestor_dev `
  -Container caprigestor-postgres `
  -User admin `
  -Password admin123 `
  -OutputDir C:\Dev\GoatFarm\backups
```

Validar:

- arquivo SQL gerado com timestamp correto
- tamanho do arquivo compatível com a base

### 3. Preparar o banco de produção

- criar o banco de produção
- validar credenciais de acesso
- validar conectividade da aplicação ao PostgreSQL de produção
- garantir que a versão do PostgreSQL seja compatível

### 4. Restaurar o backup no banco de produção

Usar o procedimento padrão de restore do projeto ou `psql`/`pg_restore`, conforme o formato do backup.

Validar logo após o restore:

- existem apenas as fazendas:
  - `Capril Vilar`
  - `Capril Alto Paraíso`
- existem apenas os usuários:
  - `Alberto Vilar`
  - `Leonardo Oliveira`
- o rebanho preservado está acessível

### 5. Subir o backend em produção

Configuração mínima esperada:

```env
SPRING_PROFILES_ACTIVE=prod
CAPRIGESTOR_MESSAGING_ENABLED=false
JDBC_URL=jdbc:postgresql://<host>:5432/<database>
DB_USER=<user>
DB_PASSWORD=<password>
MAIL_HOST=<smtp-host>
MAIL_PORT=587
MAIL_USER=<smtp-user>
MAIL_PASSWORD=<smtp-password>
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS=true
PASSWORD_RESET_FRONTEND_BASE_URL=https://<dominio>
PASSWORD_RESET_FROM_ADDRESS=no-reply@<dominio>
cors.origins=https://<dominio>
```

Validar:

- aplicação sobe sem erro
- `GET /actuator/health` retorna `UP`

### 6. Publicar o frontend

- buildar o frontend com `VITE_API_BASE_URL` apontando para o backend real
- publicar os assets estáticos no servidor/Nginx
- confirmar que a UI aponta para a API correta

### 7. Smoke mínimo pós-promoção

Executar com o usuário real principal:

- login com `Alberto Vilar`
- abertura do `Capril Vilar`
- abertura do dashboard
- listagem de animais
- detalhe de um animal real
- abertura das telas:
  - reprodução
  - lactação
  - saúde
- validação de ausência de histórico fake

Executar também:

- acesso ao `Capril Alto Paraíso`
- validação de ownership/permissão por fazenda

### 8. Checks de base após a promoção

Validar:

- não existem fazendas extras
- não existem usuários fake
- não existem dados transacionais antigos de:
  - lactação
  - produção individual
  - produção consolidada
  - reprodução
  - saúde
  - estoque
  - comercial
  - financeiro operacional

### 9. Critério de aceite

A base só pode ser considerada promovida se:

- login funcionar
- as duas fazendas corretas existirem
- os animais preservados estiverem íntegros
- ownership continuar válido
- dashboard e telas principais abrirem
- não houver sujeira operacional fake remanescente

## Rollback

Se qualquer validação crítica falhar:

- retirar a aplicação do ar
- restaurar o backup anterior do banco de produção
- revalidar `health`
- revalidar login
- revalidar acesso à fazenda principal

## Observação

Este checklist é específico para a promoção da base saneada atual. Ele não substitui processo permanente de DevOps, nem pipeline automatizado.
