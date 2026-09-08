# Integração pública ABCC

A integração com a consulta pública da ABCC está dividida em duas responsabilidades:

- `GoatAbccPublicHttpAdapter` mantém a sessão HTTP (cookies), composição dos formulários e parsing do HTML para os VOs da aplicação.
- `AbccHttpTransport` é a fronteira de transporte. Ela aplica timeout de conexão/requisição, limite de resposta e até duas tentativas para falhas transitórias de rede ou respostas HTTP 5xx. Não há retry para 4xx e não há circuit breaker sem evidência operacional que o justifique.

Falhas são tipadas para que o caso de uso não transforme indisponibilidade externa em erro de regra de negócio:

- `AbccTimeoutException`: o upstream excedeu o timeout;
- `AbccUnavailableException`: falha de rede ou status HTTP não bem-sucedido após as tentativas;
- `AbccMalformedResponseException`: HTML sem `viewstate`, genealogia ou corpo acima do limite.

Todas herdam de `ExternalServiceUnavailableException` e resultam em HTTP 503 pelo handler global, sem expor detalhes de transporte ao cliente. O caso de uso preserva essas exceções; erros de validação dos dados recebidos continuam sendo `BusinessRuleException`.

As propriedades são configuráveis por ambiente:

| Propriedade | Padrão | Variável |
| --- | ---: | --- |
| `caprigestor.abcc.connect-timeout-seconds` | 30 | `ABCC_CONNECT_TIMEOUT_SECONDS` |
| `caprigestor.abcc.request-timeout-seconds` | 60 | `ABCC_REQUEST_TIMEOUT_SECONDS` |
| `caprigestor.abcc.max-attempts` | 2 (máximo efetivo 3) | `ABCC_MAX_ATTEMPTS` |
| `caprigestor.abcc.retry-backoff-millis` | 150 | `ABCC_RETRY_BACKOFF_MILLIS` |
| `caprigestor.abcc.max-response-bytes` | 2 MiB (limite absoluto 10 MiB) | `ABCC_MAX_RESPONSE_BYTES` |

Os retries só envolvem chamadas idempotentes do fluxo de consulta/preview. A confirmação de importação é uma operação local do CapriGestor e não é reenviada ao upstream.
