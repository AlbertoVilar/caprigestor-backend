# Instruções permanentes — backend CAPRIGESTOR

## Fontes oficiais de contexto

Antes de iniciar uma tarefa, consulte primeiro as fontes versionadas do
repositorio. A hierarquia e:

1. codigo, migrations, testes, configuracao e CI (verdade tecnica);
2. `docs/00-overview/PROJECT_STATUS.md` (estado funcional humano);
3. `docs/00-overview/BUSINESS_DOMAIN.md` (dominio global);
4. `docs/01-architecture/ARCHITECTURE.md` (arquitetura atual);
5. `docs/02-modules/**` (regras por contexto);
6. `docs/03-api/**` (contratos HTTP);
7. `docs/04-security/**` (seguranca operacional);
8. `docs/INDEX.md` (portal e roteador).

`docs/00-overview/CAPRIGESTOR_CURRENT_STATE.md` e um artefato local ignorado
opcional. Ele pode ser usado como conveniencia de continuidade no ambiente que
o possui, mas nao e fonte autoritativa e nao pode ser necessario para entender
ou trabalhar em um clone limpo. Nao o versione, nao o adicione ao indice e nao
crie copias concorrentes dele.

## Responsabilidades deste repositório

- Manter backend, domínio, contratos de API, banco de dados, migrations, segurança,
  mensageria existente e deploy.
- Respeitar a arquitetura hexagonal, o isolamento entre módulos e a propriedade dos
  dados por fazenda (`farmId`).
- Preservar contratos públicos e decisões registradas; não reabrir uma decisão
  fechada sem solicitação explícita ou bloqueio técnico demonstrável.
- Ao concluir uma tarefa que altere o produto, atualize a documentacao versionada
  afetada com fatos verificaveis, incluindo modulos, banco, seguranca, deploy,
  testes, riscos e proximo passo. O estado local ignorado nao e obrigatorio.

## Regras de continuidade

- Se o estado local estiver presente, trate-o como contexto auxiliar e confirme
  qualquer fato contra codigo, migrations, testes, configuracoes, documentacao
  versionada e historico Git. Registre somente fatos confirmados, sem suposicoes,
  segredos, credenciais, tokens ou dados pessoais.
- Antes de alterar código, identifique impacto em módulos, API, migrations, segurança,
  testes e documentação. Ao fim, registre a validação realmente executada.
- Não faça commit, push, merge, bypass de proteção ou mudança de infraestrutura sem
  autorização explícita da pessoa usuária.
