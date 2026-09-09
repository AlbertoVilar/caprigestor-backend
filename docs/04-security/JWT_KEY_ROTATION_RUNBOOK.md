# Rotacao de chaves JWT

Ultima atualizacao: 2026-09-07
Escopo: pares RSA usados para assinar e validar access tokens e refresh tokens.
Links relacionados: [Portal](../INDEX.md), [Módulo Authority](../02-modules/AUTHORITY_ACCESS_MODULE.md), [Resposta a incidentes](./SECURITY_INCIDENT_RESPONSE.md), [Evidências W0](./W0_SECURITY_EVIDENCE.md)

## Regras invariantes

- Gere cada par fora do repositorio e fora do contexto de build da imagem.
- Use um par diferente por ambiente.
- Monte `app.key` e `app.pub` como arquivos externos somente leitura, ou forneca
  localizacoes equivalentes por mecanismo de secrets do ambiente.
- Registre apenas o SHA-256 da chave publica DER. Nunca registre a chave privada,
  token, senha ou conteudo PEM.
- Uma chave suspeita nunca pode ser usada como rollback.

## Preparacao

1. Identifique todos os emissores e validadores JWT do ambiente.
2. Registre o fingerprint publico do par atualmente ativo.
3. Inventarie replicas, jobs, imagens, volumes, secrets, caches e integracoes.
4. Confirme uma janela de implantacao e um responsavel por ambiente.
5. Prepare um segundo par novo como contingencia segura.

Fingerprint permitido para evidencia:

```bash
openssl pkey -pubin -in app.pub -outform DER | openssl dgst -sha256
```

## Rotacao

1. Gere um novo par RSA por ambiente em local protegido.
2. Valide que a chave publica corresponde a privada pelo fingerprint derivado.
3. Atualize o secret externo ou o diretorio montado; nao copie o par para a
   arvore Git nem para o contexto Docker.
4. Reinicie todas as replicas que emitem ou validam JWT.
5. Confirme em cada replica o fingerprint esperado por metadado operacional,
   sem imprimir o material da chave.

## Aceitacao

1. Login com a chave nova retorna access token e refresh token validos.
2. `/api/v1/auth/me` aceita o access token novo.
3. Refresh com o token novo funciona conforme o contrato vigente.
4. Access token assinado pelo par anterior e rejeitado.
5. Refresh token assinado pelo par anterior e rejeitado.
6. ADMIN, FARM_OWNER e OPERATOR vinculado passam pelos smokes autorizados; um
   operador sem vinculo continua bloqueado.
7. Logs e artefatos nao contem token, chave, senha ou identidade desnecessaria.

Registre ambiente, horario, resultado, fingerprint e correlation ID sanitizado.
Nao anexe tokens a evidencia.

## Falha e contingencia

- Se o novo par falhar, implante o segundo par novo preparado previamente.
- Nao restaure o par suspeito, ainda que isso prolongue a indisponibilidade.
- Se replicas divergirem, retire as divergentes de trafego ate a convergencia.
- Preserve logs sanitizados e abra incidente se tokens antigos forem aceitos.

## Historico Git

A rotacao invalida o segredo, mas nao remove o material do historico. A decisao
de reescrever deve incluir backup verificado, forks, clones, tags, branches,
artefatos e comunicacao de novo clone. Sem essa coordenacao, mantenha a
revogacao comprovada e registre formalmente a excecao e o risco residual.
