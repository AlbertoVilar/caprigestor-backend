# W0 - Evidencias de encerramento de incidente

Ultima atualizacao: 2026-09-07
Estado: **EM ANDAMENTO - NAO ENCERRADO**

Este documento registra somente metadados sanitizados. Nenhuma chave, senha,
credencial ou token deve ser anexado aqui.

## Escopo confirmado

- Uma chave privada JWT esteve rastreada em
  `src/main/resources/app.key`, introduzida no commit
  `e3ba91c8cf4b73fa8d27a4b397e11365b3d029d9` e removida no commit
  `7c9fea53eeb101b75447c52addb27603e594de79`.
- O fingerprint SHA-256 da chave publica derivada do par historico e
  `350553efa9a143c1bdca8e6cc75f3c685eba3852cff0c6a780f4f188ee0ca75d`.
- O par externo instalado no ambiente local em 2026-09-07 tem fingerprint
  publico `f27a9698dea6ae2d54a3d96844e292c6bff018c8035bd061ca8c2171b4417154`.
  Os fingerprints sao diferentes; isso nao prova, sozinho, o estado de HML ou
  producao.
- Credenciais literais foram localizadas no runbook de homologacao e em scripts
  operacionais rastreados. O estado atual da W0 remove os valores padrao e exige
  injecao externa.
- Uma varredura Gitleaks 8.30.1 de todo o historico, com a politica da W0,
  encontrou dez achados: uma chave privada historica, tres achados genericos em
  documentos antigos e seis senhas literais historicas em scripts PowerShell.
  Os valores nao foram reproduzidos. A varredura dos arquivos alterados da W0
  nao encontrou achados.
- O repositorio publico nao possuia forks listados pela API do GitHub na data da
  verificacao. Clones locais ou caches externos nao podem ser inventariados pela
  API e continuam sujeitos a coordenacao.

## Criterios de aceite

| Criterio | Estado | Evidencia segura | Pendencia |
|---|---|---|---|
| Ambientes e imagens inventariados por fingerprint | PARCIAL | Par local e historico identificados | Docker local indisponivel; HML, producao, registry, replicas e caches sem evidencia |
| Novos pares externos implantados | PARCIAL | Par local externo difere do historico | Implantacao e convergencia de HML/producao nao comprovadas |
| Tokens antigos rejeitados | PENDENTE | Fingerprints local e historico divergem | Executar login/refresh novo e rejeicao de access/refresh antigos em cada ambiente |
| Credencial de homologacao inerte ou revogada | PENDENTE | Literais removidos do estado atual | Revogar/rotacionar na origem e revisar logs/artefatos sem expor o valor |
| Secret scanning e push protection | PARCIAL | Recursos nativos ativos; workflow Gitleaks fixado por SHA e versao preparado | Integrar o workflow e comprovar o check em PR; non-provider patterns/validity checks indisponiveis pela API |
| Historico sanitizado ou excecao formal | PENDENTE | Commits e fingerprint historicos identificados; zero forks publicos listados | Decidir reescrita coordenada ou excecao formal apos revogacao; tratar clones/caches |
| CI obrigatorio e bypass restrito | CONCLUIDO | `main` e `develop` exigem `deny_root_markdown`, `Clean test` e `Secret scan`, uma aprovacao, aprovacao do ultimo push, conversas resolvidas e aplicacao a administradores | Auditar periodicamente alteracoes nas regras |

## Alteracoes preventivas preparadas

- `.gitleaks.toml` estende as regras oficiais e detecta senha literal em
  parametro PowerShell sem allowlist ampla.
- `.github/workflows/secret_scan.yml` fixa `actions/checkout` e
  `gitleaks/gitleaks-action` por SHA, fixa Gitleaks 8.30.1 e desabilita
  comentarios/artefatos de achados.
- Os scripts de homologacao, lactacao, carencia, backup e restore recebem
  credenciais por ambiente e fazem validacao fail-fast.
- O smoke de homologacao deixou de imprimir a identidade autenticada e reduz a
  retencao de senha, payload e token em variaveis locais ao finalizar.

## Validacao local executada

- Parser PowerShell: seis scripts alterados sem erro de sintaxe.
- Fail-fast: os tres smokes e os tres utilitarios de backup/restore rejeitam a
  execucao quando a credencial externa correspondente esta ausente.
- Gitleaks 8.30.1: zero achados no staging da W0; dez achados preservados no
  historico para tratamento coordenado.
- actionlint 1.7.12: todos os workflows validos, incluindo o novo gate.
- Maven: `clean verify` com 540 testes, 0 falhas, 0 erros, 3 ignorados e
  `BUILD SUCCESS`.
- Artefato: nenhum arquivo de chave no JAR de producao; zero achados Gitleaks
  em `target/classes` e no JAR, com leitura de arquivos compactados habilitada.
- Docker: validacao de imagem nao executada porque o daemon local estava
  indisponivel. Nenhuma imagem foi publicada.

## Evidencias ainda necessarias para fechamento

1. Inventario assinado de HML, producao, registry, imagens, replicas, secrets,
   logs, artefatos e caches.
2. Rotacao/revogacao da credencial de homologacao na origem.
3. Rotacao JWT por ambiente e prova de convergencia de todas as replicas.
4. Prova de rejeicao dos access tokens e refresh tokens antigos.
5. Revisao sanitizada de logs e artefatos que possam conter credenciais/tokens.
6. Execucao verde do check `Secret scan` em Pull Request.
7. Decisao formal sobre reescrita de historico, incluindo clones e caches.
8. Aceite final do responsavel pelo incidente.

Enquanto qualquer item permanecer pendente, a W1 e as ondas seguintes nao
devem ser iniciadas.
