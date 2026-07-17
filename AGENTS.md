# Instruções permanentes — backend CAPRIGESTOR

## Fonte oficial de estado

Antes de iniciar qualquer tarefa, leia `docs/00-overview/CAPRIGESTOR_CURRENT_STATE.md`.
Esse é o único estado operacional global do produto CAPRIGESTOR. Não crie cópias
paralelas no frontend, em documentos de trabalho ou na raiz do repositório.

## Responsabilidades deste repositório

- Manter backend, domínio, contratos de API, banco de dados, migrations, segurança,
  mensageria existente e deploy.
- Respeitar a arquitetura hexagonal, o isolamento entre módulos e a propriedade dos
  dados por fazenda (`farmId`).
- Preservar contratos públicos e decisões registradas; não reabrir uma decisão
  fechada sem solicitação explícita ou bloqueio técnico demonstrável.
- Ao concluir toda tarefa, atualizar o documento canônico com fatos verificáveis,
  incluindo módulos afetados, banco, segurança, deploy, testes, riscos e próximo passo.

## Regras de continuidade

- O estado canônico é local e ignorado pelo Git: não o adicione ao índice, não o
  versione e não tente contornar essa regra.
- Se ele estiver ausente ou inacessível, audite código, migrations, testes,
  configurações, documentação e histórico Git antes de recriá-lo. Registre somente
  fatos confirmados, sem suposições, segredos, credenciais, tokens ou dados pessoais.
- Antes de alterar código, identifique impacto em módulos, API, migrations, segurança,
  testes e documentação. Ao fim, registre a validação realmente executada.
- Não faça commit, push, merge, bypass de proteção ou mudança de infraestrutura sem
  autorização explícita da pessoa usuária.
