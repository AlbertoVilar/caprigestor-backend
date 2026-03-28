# Modulo Authority / acesso / recuperacao de senha
Ultima atualizacao: 2026-03-28
Escopo: autenticacao, refresh, cadastro inicial e recuperacao de senha do CapriGestor.

## Recuperacao de senha MVP
Entrou neste MVP:
- solicitacao publica de reset por email
- token aleatorio forte com hash persistido
- token de uso unico
- expiracao curta de 30 minutos
- invalidacao de tokens anteriores do mesmo usuario
- cooldown simples por email/usuario
- resposta neutra para nao revelar existencia de email
- envio de email com link de redefinicao
- tela publica de solicitacao e tela publica de redefinicao

## Endpoints
- `POST /api/v1/auth/password-reset/request`
- `POST /api/v1/auth/password-reset/confirm`

## Fluxo
1. O usuario informa o email na tela `Esqueci minha senha`.
2. O backend sempre responde com a mesma mensagem neutra.
3. Se o email existir e nao estiver em cooldown, o sistema invalida tokens anteriores, gera um novo token, persiste apenas o hash e envia o link por email.
4. O usuario abre o link recebido.
5. O frontend envia token bruto + nova senha + confirmacao para o backend.
6. O backend valida token, expiracao, uso unico e revogacao.
7. A senha e atualizada com o mesmo encoder BCrypt ja usado no projeto.
8. O token e marcado como utilizado e nao pode ser reutilizado.

## Configuracao local / HML
Variaveis relevantes:
- `MAIL_HOST`
- `MAIL_PORT`
- `MAIL_USER`
- `MAIL_PASSWORD`
- `MAIL_SMTP_AUTH`
- `MAIL_SMTP_STARTTLS`
- `MAIL_TIMEOUT_MS`
- `PASSWORD_RESET_FRONTEND_BASE_URL`
- `PASSWORD_RESET_FROM_ADDRESS`
- `PASSWORD_RESET_TTL_MINUTES`
- `PASSWORD_RESET_COOLDOWN_SECONDS`

## Validacao local com Mailpit
1. Suba o ambiente: `docker compose -f docker/docker-compose.yml up -d mailpit`
2. A interface do Mailpit fica em [http://localhost:8025](http://localhost:8025)
3. Solicite um reset com um email existente.
4. Abra o email capturado no Mailpit.
5. Siga o link de redefinicao.
6. Confirme que a nova senha funciona no login.

Padrao local recomendado:
- `PASSWORD_RESET_FRONTEND_BASE_URL=http://localhost:5173`

## O que ficou para fase 2
- invalidacao de JWTs/sessoes ja emitidos
- rate limit por IP
- captcha / anti-abuso adicional
- envio assincrono de email
- templates de email mais ricos
- observabilidade dedicada do fluxo

## Observacao de seguranca
Neste MVP, tokens JWT e sessoes ja emitidos **NAO** sao invalidados automaticamente quando a senha e redefinida. Essa melhoria fica explicitamente adiada para a fase 2.
