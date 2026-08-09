# Logs e rastreamento de requisições

## Objetivo

O CapriGestor usa a API SLF4J com Logback para registrar eventos da aplicação.
Cada requisição recebe um `correlationId`, permitindo localizar no console e no
arquivo todas as linhas relacionadas ao mesmo fluxo.

## Fluxo implementado

1. O cliente pode enviar o cabeçalho `X-Correlation-ID` com 1 a 64 caracteres
   alfanuméricos, ponto, hífen ou sublinhado.
2. Se o cabeçalho estiver ausente ou for inválido, a API gera um UUID.
3. O filtro grava o valor no MDC do SLF4J e o devolve no cabeçalho da resposta.
   O CORS expõe esse cabeçalho para que o frontend também possa lê-lo.
4. O padrão do Logback acrescenta `correlationId` às linhas emitidas durante a
   requisição.
5. Ao final, o filtro registra método, caminho, status HTTP e duração, e limpa o
   MDC para impedir vazamento entre requisições reutilizadas pela mesma thread.

Exemplo de linha:

```text
2026-08-06 20:00:00.000 INFO [http-nio-8080-exec-1] [correlationId=estudo-001] ... - event=http_request_completed method=GET path=/actuator/health status=200 durationMs=12
```

## Níveis

- `DEBUG`: diagnóstico detalhado, normalmente habilitado apenas em desenvolvimento.
- `INFO`: eventos normais relevantes, como conclusão de requisição e login bem-sucedido.
- `WARN`: situação esperada, porém anormal, como credenciais inválidas ou conflito de integridade.
- `ERROR`: falha inesperada que exige investigação; deve incluir a exceção e stack trace.

Use placeholders em vez de concatenação:

```java
logger.info("event=animal_created farmId={} animalId={}", farmId, animalId);
logger.warn("event=validation_failed field={}", fieldName);
logger.error("event=unexpected_failure operation={} exception={}",
        operation, exception.getClass().getSimpleName(), exception);
```

## Teste manual

Com a aplicação em execução:

```powershell
curl.exe -i -H "X-Correlation-ID: estudo-001" http://localhost:8080/actuator/health
```

A resposta deve conter `X-Correlation-ID: estudo-001`. Para acompanhar o arquivo:

```powershell
Get-Content -LiteralPath .\logs\application.log -Wait
```

Para localizar um fluxo específico:

```powershell
Select-String -LiteralPath .\logs\application.log -Pattern "correlationId=estudo-001"
```

## Segurança

Nunca registrar senha, token JWT, cabeçalho `Authorization`, segredo, corpo integral
de requisição ou dados pessoais como e-mail e CPF. Identificadores técnicos e dados
de contexto devem ser usados somente quando necessários para investigar o evento.

O arquivo ativo é `logs/application.log`. A rotação ocorre por dia ou ao atingir
10 MB, com retenção de sete arquivos diários e limite total de 100 MB.
