# Integração pública ABCC

Última atualização: 2026-09-09
Escopo: transporte HTTP, parsing e regras de indisponibilidade da consulta pública ABCC.
Links relacionados: [Portal](../INDEX.md), [Módulo Goat/Farm](../02-modules/GOAT_FARM_MODULE.md), [Contratos da API](../03-api/API_CONTRACTS.md)

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

O lookup de registro (`registration-lookup`) permanece na mesma fronteira hexagonal: o caso
de uso solicita `searchByRegistration(raceId, registrationNumber)` à porta de saída e o
adapter deriva TOD/TOE apenas para compor o formulário da ABCC. O resultado é filtrado pela
raça selecionada e pelo RG normalizado; o preview é carregado e validado novamente antes do
status `FOUND`. `NOT_FOUND` e `AMBIGUOUS` são resultados funcionais, enquanto timeout ou
resposta inválida continuam sendo indisponibilidade externa (HTTP 503).

## Situação registral ABCC e status operacional local

`situacao` retornada pela ABCC é informação registral externa. Ela não é e não
determina o `GoatStatus` operacional do CapriGestor, com uma única exceção de
domínio: uma situação ABCC explicitamente normalizada como falecimento
(`FALECIDO`, `FALECIDA`, `MORTO`, `MORTA`, `ÓBITO` ou `DECEASED`) fixa o status
local da importação em `FALECIDO`. A fronteira anti-corrupção normaliza os dados
registráveis (como raça, sexo, categoria e identificação), mas não infere o
ciclo de vida local a partir de `RGD`, `SEM RGD`, `ATIVO`, `VENDIDO`, `SUSPENSO`
ou qualquer outro valor externo.

A importação individual recebe o status local explícito dentro de `goat.status`.
No lote, cada item deve enviar `{ externalId, status }`; não há status global
para a requisição. A validação estrutural de status ocorre antes dos previews
ABCC. Depois de válida, a operação continua best-effort: cada item consulta um
preview novo e pode importar, ser ignorado pela duplicidade/TOD atual ou falhar
independentemente dos demais. Valores ausentes, desconhecidos ou incomuns de
`situacao` não bloqueiam a importação por si só.

No frontend, uma situação de morte retornada pela ABCC é exibida apenas como
aviso informativo e não pré-seleciona nem bloqueia o status local. Cada item
continua exigindo escolha explícita do usuário. Na confirmação, o backend usa
um preview novo e aplica a exceção de morte apenas se essa consulta fresca
confirmar a situação; dados ABCC vistos anteriormente não são tratados como
escolha local do usuário.
