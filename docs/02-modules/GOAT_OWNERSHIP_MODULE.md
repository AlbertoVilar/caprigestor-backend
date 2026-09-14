# Módulo Goat Ownership

Última atualização: 2026-09-14

## Identidade e autoridade

`GoatId` (`cabras.id`) é a identidade técnica estável do animal biológico. O
RG (`num_registro`) continua sendo identificador registral/ABCC e snapshot de
negócio; não é a fonte da identidade estrutural do histórico.

O `GoatOwnershipPeriod` aberto é a autoridade canônica para o proprietário
atual. `cabras.capril_id` permanece uma projeção transitória de compatibilidade,
atualizada pelo adapter e nunca usada para derivar a origem de uma transferência.
Snapshots históricos de `farm_id` em produção, reprodução, saúde, auditoria e
demais registros são imutáveis.

`CreatorReference` representa a origem/registro de criação do animal e é
separado da propriedade atual. Uma transferência não altera CreatorReference,
GoatId, RG/TOD/TOE, genealogia, status ou campos de saída.

## Transferências internas

O W6 implementa o núcleo transacional de `INTERNAL_TRANSFER`. O W7 expõe esse
workflow na API HTTP e fornece a consulta autorizada de transferências de entrada
e saída:

- `POST /api/v1/ownership-transfers` solicita a transferência sem aceitar
  `sourceFarmId` no payload;
- `GET /api/v1/ownership-transfers/{transferId}` permite leitura a ADMIN e ao
  administrador da fazenda de origem ou destino;
- `POST .../{id}/accept`, `reject` e `cancel` delegam aos casos de uso W6;
- `GET /api/v1/goatfarms/{farmId}/ownership-transfers` lista inbox/outbox apenas
  para ADMIN ou FARM_OWNER que administra a fazenda.

OPERATOR não recebe capacidade patrimonial por estar vinculado à fazenda. As
decisões de autorização permanecem no application core através de
`CurrentPrincipalQueryUseCase`, `FarmAuthorizationUseCase` e da política
`canAdministerFarm`.

O lifecycle público é `REQUESTED -> COMPLETED`, `REJECTED` ou `CANCELLED`.
Repetições exatas usam `idempotencyKey` por solicitante e não criam novo
registro. O ledger mantém períodos `TRANSFER_OUT` e `TRANSFER_IN` no mesmo
instante efetivo; a projeção `cabras.capril_id` é sincronizada somente como
compatibilidade.

## Limites de escopo

W7 é backend-only e não ativa `INTERNAL_SALE`, `RETURN`, `EXTERNAL_CLAIM`,
notificações, e-mail, ABCC ou alterações de schema. As migrations V1–V48 são
imutáveis; nenhuma V49 é necessária para a superfície de consulta atual.

O controller traduz HTTP e DTOs. O caso de uso orquestra autorização e regras;
o adapter é o único componente que conhece Spring Data/JPA e converte a página
para o modelo neutro da aplicação.
