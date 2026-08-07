# Encerramento da migração para API v1

Última atualização: 2026-08-07
Escopo: estado final do versionamento das rotas HTTP do CapriGestor.

Links relacionados: [API_CONTRACTS](./API_CONTRACTS.md), [Status do projeto](../00-overview/PROJECT_STATUS.md), [Prontidão do MVP](../00-overview/MVP_READY.md)

## Estado atual

- `/api/v1` é o prefixo único dos endpoints de aplicação.
- Rotas públicas deliberadas permanecem em namespaces próprios, como `/public/articles`.
- O frontend utiliza somente a base versionada.
- Não existe retry ou fallback para o antigo prefixo não versionado.
- O proxy Nginx encaminha apenas `/api/v1/` ao backend e responde `404` aos demais caminhos sob `/api/`.

## Encerramento realizado

Em 2026-08-07 foram removidos:

- dual mappings dos controllers;
- matchers legados da configuração de segurança;
- fallback condicional do cliente Axios;
- variável de ambiente e argumentos Docker associados ao fallback;
- referências ativas da documentação operacional.

O teste `ApiVersioningGuardTest` impede novas referências ao prefixo não versionado no código de produção Java.

## Regra para consumidores

Clientes devem configurar a base como:

```text
http://localhost:8080/api/v1
```

Uma chamada feita fora do contrato versionado não deve ser repetida automaticamente em outro prefixo. Erros `404` devem ser tratados como rota inexistente ou contrato desatualizado.
