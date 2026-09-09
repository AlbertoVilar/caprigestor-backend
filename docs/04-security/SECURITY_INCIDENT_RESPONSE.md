# Resposta a incidentes de seguranca

Ultima atualizacao: 2026-09-07
Escopo: segredos, credenciais, tokens JWT, logs e artefatos do backend CapriGestor.
Links relacionados: [Portal](../INDEX.md), [Rotação de chaves JWT](./JWT_KEY_ROTATION_RUNBOOK.md), [Evidências W0](./W0_SECURITY_EVIDENCE.md)

## Principios

- Trate qualquer segredo rastreado, publicado, registrado em log ou copiado para
  artefato como comprometido, mesmo que a exposicao tenha sido breve.
- Revogue ou rotacione primeiro. Remover o texto do `HEAD` nao invalida uma
  credencial nem elimina copias do historico, forks, clones, caches ou imagens.
- Nunca reproduza o valor suspeito em issue, Pull Request, commit, documento,
  terminal compartilhado ou evidencia de encerramento.
- Registre somente metadados seguros: tipo, ambiente, responsavel, periodo,
  fingerprint, estado de revogacao e prova de rejeicao.
- Nao reescreva historico sem backup verificado, inventario de consumidores e
  coordenacao explicita com todos os clones, forks, branches e ambientes.

## Fluxo obrigatorio

1. **Conter:** interromper novas publicacoes, ativar protecao contra novos
   segredos e restringir o acesso ao material suspeito.
2. **Inventariar:** localizar ambientes, imagens, branches, tags, artefatos,
   logs, forks, clones e integracoes que possam conter ou consumir o segredo.
3. **Rotacionar ou revogar:** gerar material novo fora do repositorio, implantar
   por ambiente e revogar o material suspeito.
4. **Provar rejeicao:** demonstrar que credenciais/tokens antigos falham e que o
   fluxo com material novo continua funcional.
5. **Sanitizar:** remover literais do estado atual, tratar logs/artefatos e
   decidir entre reescrita coordenada do historico ou excecao formal apos a
   revogacao comprovada.
6. **Prevenir recorrencia:** manter secret scanning, push protection e check de
   CI obrigatorio, sem allowlists amplas.
7. **Encerrar:** reunir evidencias sanitizadas e obter aceite do responsavel pelo
   incidente. Ausencia de evidencia deve ser registrada como pendencia, nunca
   como sucesso presumido.

## Evidencias minimas

| Evidencia | Conteudo permitido |
|---|---|
| Inventario | ambiente, imagem/digest, branch/tag e fingerprint |
| Rotacao | data, responsavel, fingerprint anterior e novo, sem material bruto |
| Rejeicao | ambiente, horario, operacao, resultado e correlation ID sanitizado |
| Scanner | versao, configuracao, escopo, resultado e excecoes vigentes |
| Historico | decisao, backup, coordenacao de clones/forks e impacto |
| GitHub | protecoes ativas, checks exigidos e politica de bypass |

## Classificacao de excecoes

Uma excecao de scanner so pode ser aceita quando todos os campos abaixo forem
registrados na revisao:

- fingerprint exato do achado;
- caminho exato, quando aplicavel;
- justificativa tecnica;
- responsavel;
- data de expiracao;
- prova de que o valor e sintetico ou ja foi revogado.

Allowlist por diretorio amplo, regra inteira, extensao ou commit sem prazo nao e
aceitavel.

## Referencias

- [Rotacao de chaves JWT](./JWT_KEY_ROTATION_RUNBOOK.md)
- [Operacao minima de homologacao](../00-overview/HOMOLOGATION_OPERATION_RUNBOOK.md)
- [Deploy Docker de producao](../00-overview/PRODUCTION_DOCKER_DEPLOY_RUNBOOK.md)
