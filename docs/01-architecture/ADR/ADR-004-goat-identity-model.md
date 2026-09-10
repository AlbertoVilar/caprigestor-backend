# ADR-004 — Modelo de identidade estrutural do animal

Última atualização: 2026-09-10
Escopo: decisão arquitetural de desenho para a identidade do animal no
backend GoatFarm/CapriGestor.

Links relacionados: [Portal](../../INDEX.md),
[Arquitetura](../ARCHITECTURE.md),
[Plano de refatoração da identidade](../GOAT_IDENTITY_REFACTORING_PLAN.md),
[Integração ABCC](../ABCC_INTEGRATION.md)

## 1. Status

- **ID2.1 encerrada — decisão aprovada para revisão arquitetural.**
- A estratégia limitada **B — MINIMAL DOMAIN ENRICHMENT DURING GOATID MIGRATION**
  foi aprovada: enriquecer gradualmente o modelo `Goat` sem separar agora a
  entidade de domínio da entidade JPA e sem transformar a migração de identidade
  em uma reescrita do módulo.
- Design only: não implementada.
- Não cria GoatId, migration, alteração de entidade, mudança de API ou
  alteração de dados.
- A decisão de identidade estrutural e o modelo de domínio limitado são
  definidos aqui; o desenho de contratos e migration permanece para a ID3.
- **Resultado da reavaliação:** `READY FOR ID3` para planejamento e desenho,
  mas nenhuma migration, alteração de schema ou implementação da ID3 está
  autorizada por esta ADR.

## 2. Contexto

A auditoria ID1 confirmou que o valor chamado registrationNumber/RG exerce hoje
mais de um papel:

- identificador de negócio;
- campo TOD + TOE utilizado na identificação registral;
- @Id da entidade JPA Goat;
- PK de cabras;
- alvo de FKs e auto-referências de genealogia;
- valor passado como goatId em contratos HTTP, casos de uso e frontend.

O código atual tem 11 FKs diretas para cabras.num_registro e dependências em
genealogia, eventos, reprodução, lactação, produção de leite, saúde,
comercialização e auditoria. Não existe política de atualização em cascata para
essa PK. O mapper atual de atualização ignora registrationNumber, portanto o
PUT não corrige o RG.

Há uma regra de negócio agora considerada autoritativa: um animal pode ser
cadastrado manualmente no CapriGestor com TOD + TOE e RG antes de existir na
ABCC. Ter um RG no CapriGestor não significa estar confirmado na ABCC.

## 3. Resumo das evidências da ID1

| Evidência | Fato confirmado |
| --- | --- |
| Goat.java | registrationNumber é String, @Id e coluna num_registro. |
| GoatRepository e GoatPersistencePort | JpaRepository e ports usam String como ID; findById e consultas de filhos recebem RG. |
| Migrations V7, V9, V13–V15, V20, V28–V29, V36 e V38 | cabras possui PK textual; as relações do animal usam RG textual, FKs simples ou compostas por fazenda. |
| GoatBusinessMapper | updateEntity ignora registrationNumber. |
| GoatFarm.java / GoatFarmRepository | a fazenda possui TOD próprio, único, usado como escopo de registro. |
| GoatAbccImportBusiness | importação não administrativa exige TOD da fazenda e rejeita divergência; consulta ABCC usa raça e dados registrais. |
| GoatAbccPublicHttpAdapter | ABCC permanece uma fronteira externa; externalId retornado é transitório no fluxo atual. |
| APIs e frontend | aproximadamente 46 operações HTTP e 17 rotas frontend usam goatId/registrationNumber com a semântica atual de RG. |
| Igualdade JPA | Goat não sobrescreve equals/hashCode; Lombok fornece apenas getters/setters. Não há igualdade explícita baseada em RG. |

## 4. Problema

Se o RG errado for atualizado enquanto permanecer PK, a operação altera a
identidade estrutural e pode quebrar FKs, pai/mãe, filhos, histórico de
produção, reprodução, saúde, venda, eventos e auditoria. Excluir e recriar o
animal também perde continuidade biológica e histórica.

O domínio precisa representar o mesmo indivíduo durante todo o ciclo de vida,
mesmo quando seus dados registrais são corrigidos, atribuídos posteriormente ou
divergem temporariamente da consulta externa.

## 5. Definições de identidade do domínio

### 5.1 Identidade estrutural

GoatId responde à pergunta: “qual animal de domínio é este?”.

Propriedades:

- interno ao CapriGestor;
- técnico;
- imutável durante toda a vida do animal;
- independente da ABCC;
- independente de TOD, TOE e RG;
- usado nas PKs/FKs do modelo-alvo;
- usado para igualdade persistente da entidade.

### 5.2 Identidade registral

TOD, TOE e RG respondem à pergunta: “qual identificação registral pertence
atualmente a este animal?”.

Propriedades:

- dados de negócio visíveis;
- relevantes para o domínio;
- utilizados na comunicação com a ABCC;
- sujeitos a normalização e retificação;
- nunca redefinem o GoatId.

A raça participa do contexto de consulta e da identidade externa ABCC, mas não
substitui a identidade estrutural local.

### 5.3 Presença/confirmação ABCC

Existir ou não na ABCC é um estado de integração externa, não uma condição para
existir como Goat no CapriGestor. O registro manual pode anteceder a presença
na ABCC.

## 6. Decisão do GoatId estrutural

Adotar, como modelo-alvo, um GoatId interno do tipo Long no Java e BIGINT
gerado pelo PostgreSQL.

Racional:

- mantém PKs e índices menores que UUID;
- é suportado diretamente por JPA/Hibernate;
- simplifica joins, debugging, logs e migration;
- atende criação online no cenário atual;
- não há requisito confirmado de geração distribuída, criação offline, merge
  de bancos independentes ou replicação multi-região;
- imprevisibilidade do identificador não é mecanismo de autorização.

UUID só deverá ser reaberto se ID2/ID3 encontrar requisito operacional concreto
que justifique a complexidade adicional. A escolha do tipo não autoriza ainda
qualquer alteração de schema ou código.

## 7. Decisão da identidade registral

O modelo-alvo terá uma identidade registral conceitual composta por TOD, TOE e
RG, com a seguinte política:

1. TOD e TOE são os componentes de entrada da identificação registral quando
   o cadastro os possui.
2. RG é a representação normalizada derivada de TOD + TOE segundo a regra de
   negócio do CapriGestor.
3. Uma correção deve alterar a identidade registral do mesmo GoatId e manter
   os valores anterior e novo rastreáveis.
4. TOD, TOE e RG não podem permanecer contraditórios sem um estado de exceção
   explícito e auditado.
5. Animal manual pode possuir TOD, TOE e RG mesmo sem confirmação na ABCC.
6. Animal sem ABCC não deve receber RG artificial apenas para satisfazer
   persistência; ausência ou presença de registro externo é independente do
   RG local.

O conceito pode ser modelado futuramente como um value object
RegistrationIdentity, mas nenhuma classe é criada nesta ADR.

### Normalização

O comportamento-alvo deve normalizar espaços, formato e comparação de dados
registrais de forma determinística. A verificação ID2.1 confirmou, no código e
nos testes atuais, a seguinte normalização canônica para lookup e comparação:

- remover espaços nas extremidades;
- remover separadores de espaço internos;
- converter letras para maiúsculas com `Locale.ROOT`;
- preservar zeros à esquerda;
- não remover hífens ou outros sinais que o contrato do fluxo não autorize;
- não fazer padding, truncamento ou inferência de dígitos.

Essa regra é observável em `GoatAbccPublicHttpAdapter.normalizeRegistration`,
em `GoatAbccImportBusiness.normalizeRegistrationForLookup` e nos testes que
convertem `" 1635719026a "` em `"1635719026A"` e
`"12345 67890"` em `"1234567890"`.

O adapter da ABCC divide um registro normalizado nos cinco primeiros dígitos
de TOD e no restante de TOE. O fluxo de parto acrescenta a validação de
10–12 caracteres, somente números e uma letra final opcional, e exige TOD de
cinco dígitos no início. Exemplos de formato observados nos testes e nas
verificações operacionais são `12345 + 67890 = 1234567890`,
`1635719026A` e `16432 + 22003 = 1643222003`.

Esses fatos definem a normalização canônica do CapriGestor para a ID2.1. Eles
não constituem uma nova especificação oficial da ABCC: a ID3 deve confrontar a
regra com os contratos externos e decidir a validação de cada fluxo. Em
particular, o cadastro manual atual ainda aceita uma faixa mais ampla que o
fluxo de parto; a ID3 deve harmonizar essa diferença sem inventar padding ou
alterar silenciosamente registros existentes.

Não há evidência de que raça, hífen ou remoção de zeros faça parte da identidade
registral local. A raça continua sendo filtro do lookup externo da ABCC, não
componente do RG local.

### 7.1 Fechamento da avaliação ID2.1-C

| Item | Decisão encerrada |
| --- | --- |
| Modelo | **B — enriquecimento mínimo durante a migração do GoatId** |
| Entidade JPA versus domínio separado | Manter `Goat` como entidade JPA e enriquecer apenas as invariantes de identidade nesta onda |
| `GoatId` | Long/BIGINT imutável como identidade estrutural futura |
| `RegistrationIdentity` | Conceito de domínio aprovado; representação `@Embeddable` fica para depois da migração |
| RG | Permanece armazenado durante a transição e deve ser coerente com TOD + TOE |
| Criação | `Goat.register(...)` é direção futura; o MapStruct e os testes atuais exigem adaptação controlada |
| Retificação | `rectifyRegistration(...)` é comportamento futuro do Goat, mantendo o mesmo GoatId |
| Setters críticos | Restrição gradual de `setRegistrationNumber`, `setTod` e `setToe`; não há remoção em massa nesta onda |
| ABCC | Continua adapter externo; cadastro manual não depende de confirmação ABCC |
| Genealogia e ciclo de vida | Mantidos fora do enriquecimento limitado, para ondas futuras |

O fechamento foi feito sem alterar Java, testes, DTOs, mappers, banco,
migrations, frontend, branches ou commits de implementação.

## 8. TOD, TOE e RG: invariantes

São invariantes obrigatórias do modelo-alvo:

1. O mesmo animal biológico conserva o mesmo GoatId.
2. Alterar TOD, TOE ou RG nunca cria outro Goat.
3. A correção preserva genealogia, pai/mãe, filhos, reprodução, partos,
   lactação, leite, saúde, movimentações, venda, eventos e auditoria.
4. RG não é PK/FK estrutural de longo prazo.
5. ABCC não define se o Goat existe internamente.
6. Goat pode existir antes da ABCC.
7. TOD + TOE determinam o RG atual segundo a regra canônica do CapriGestor.
8. Estados registrais contraditórios devem ser rejeitados ou explicitamente
   encaminhados para retificação auditada.
9. Toda retificação registra antes/depois, autor, instante, motivo e origem.

## 9. Semântica e propriedade do TOD

O código demonstra dois níveis relacionados, mas não idênticos:

- GoatFarm.tod é um código único da fazenda/capril e é usado como escopo de
  registro e validação da integração ABCC.
- Goat.tod é persistido junto à identidade registral de cada animal.

Para usuários não administrativos, a importação ABCC exige TOD configurado na
fazenda e rejeita animal cujo TOD ABCC diverge dele. Na criação manual atual,
`GoatBusiness` recebe TOD no request do animal; a cópia automática não existe
no código atual.

Decisão de domínio encerrada na ID2.1: `GoatFarm.tod` é a fonte de origem e
escopo registral **somente para animais que se originam/nascem naquele
criatório**. Ele não é fonte do TOD de todo animal que é cadastrado no banco
daquela fazenda. Para um animal adquirido, transferido ou de origem externa,
`Goat.tod`, `Goat.toe` e RG vêm da identidade registral já pertencente ao
indivíduo, mesmo que seja a primeira vez que ele aparece no CapriGestor. No
modelo-alvo, o TOD da fazenda é copiado para `Goat.tod` apenas no fluxo de
criação cuja origem seja aquele criatório; a identidade registral do animal
passa então a ser própria e não deve ser sincronizada automaticamente com
alterações posteriores na fazenda.

Consequentemente:

- alteração de ownership ou transferência de fazenda não altera `Goat.tod`,
  `Goat.toe` ou RG;
- o TOD da fazenda continua servindo para validação do fluxo ABCC e para a
  origem de novas crias, sem substituir o TOD de animais adquiridos;
- divergência entre `GoatFarm.tod` e `Goat.tod` de um animal existente não é
  corrigida por sincronização implícita; deve ser tratada por política explícita
  de retificação/reconciliação na ID3;
- o código atual ainda aceita TOD no request manual, portanto a ID3 deve
  especificar a origem de escrita e o backfill sem apagar a proveniência dos
  dados existentes.

Essa distinção evita transformar o TOD da fazenda em PK do animal e evita
assumir, sem evidência, que qualquer ausência na ABCC elimina o RG local.

## 10. Fronteira ABCC

A ABCC permanece uma fronteira de registro externo:

CapriGestor:

- GoatId interno;
- RegistrationIdentity com TOD, TOE, RG e contexto de raça;

Adapter ABCC:

- race + RG/TOD/TOE;
- identificadores externos retornados pela ABCC.

Decisões:

- GoatId não é enviado à ABCC.
- Consulta e importação continuam utilizando dados registrais compatíveis com
  o protocolo externo.
- O resultado ABCC deve ser resolvido contra um Goat existente quando a
  identidade registral indicar que se trata do mesmo animal.
- Confirmação ABCC não cria duplicata quando a retificação se refere ao mesmo
  indivíduo.
- externalId continua classificado como transitório no comportamento atual;
  persistência futura é uma decisão separada.
- Mantêm-se os estados FOUND, NOT_FOUND e AMBIGUOUS, seleção por raça,
  confirmação explícita, ausência de save automático e proibição de escolher
  silenciosamente o primeiro resultado.

## 11. Cadastro manual antes da ABCC

Cadastro manual é um fluxo legítimo e não é fallback excepcional.

O animal pode ser criado no CapriGestor com os dados registrais disponíveis
mesmo quando a ABCC ainda não o conhece. ABCC pode posteriormente:

- confirmar os dados;
- retornar registro corrigido;
- não encontrar o animal;
- retornar resultado ambíguo.

Em todos os casos, o GoatId local permanece a identidade do indivíduo. Uma
retificação posterior não deve criar uma segunda entidade.

## 12. Política de retificação registral

O futuro caso de uso será chamado Registro de Retificação ou Registration
Rectification, não uma edição genérica da identidade do Goat.

Sem implementar agora, o contrato conceitual é:

GoatId estável + identidade antiga -> identidade nova

O registro deve conter, no mínimo:

- GoatId;
- TOD anterior e novo;
- TOE anterior e novo;
- RG anterior e novo;
- motivo;
- origem da correção;
- usuário responsável;
- timestamp.

Origens possíveis incluem correção manual, verificação ABCC e reconciliação de
importação. A autorização deverá ser definida pelo modelo de segurança vigente
e não pelo tipo ou pela imprevisibilidade do GoatId.

## 13. Rastreabilidade do RG antigo

Um RG legitimamente associado a um Goat não deve continuar ativo depois da
retificação, mas deve permanecer historicamente pesquisável para responder:

“Qual animal usou anteriormente o RG X?”

Decisão de requisito: o alvo deve possuir um conceito de histórico registral
ligado ao GoatId, com valor anterior, intervalo/instante, origem, motivo e
classificação do valor anterior.

A classificação precisa separar dois casos:

- **RG anteriormente válido:** o registro pertenceu legitimamente ao animal e
  foi substituído por retificação, atualização oficial ou reconciliação. Deve
  permanecer historicamente pesquisável como registro que o animal utilizou,
  sem continuar ativo.
- **RG digitado/incorreto:** o valor foi associado por erro e nunca identificou
  legitimamente aquele animal. Deve ser preservado como evidência de correção
  no audit trail, marcado como `ERRONEOUS` no histórico quando este existir e
  não deve reservar o número nem responder que o animal o utilizou validamente.

Em ambos os casos, a operação mantém o mesmo GoatId, grava antes/depois, autor,
origem, motivo e instante. Um valor histórico não se torna automaticamente
uma chave ativa nem pode ser reutilizado sem a política de colisão aprovada.

Uma tabela dedicada é preferível a depender apenas da auditoria operacional,
porque consultas históricas de registro são uma necessidade de domínio. O
formato físico e a relação com a auditoria serão definidos na ID3.

## 14. Continuidade do ciclo de vida

### Correção/retificação

Mesmo animal, dado registral incorreto ou alterado. Resultado: mesmo GoatId,
mesmo histórico, nova identidade registral e auditoria.

### Desativação/saída

Animal deixou a operação por venda, morte, transferência, descarte, doação ou
outra razão de ciclo de vida. Resultado: mesmo GoatId, histórico preservado e
status de ciclo alterado.

### Exclusão

Exclusão física é excepcional. Não é mecanismo de correção de RG. Só pode ser
considerada para registro que nunca deveria ter existido e após verificar
dependências e histórico conforme uma política própria de purge/merge.

## 15. Igualdade de entidade

Regra do modelo-alvo:

Dois objetos persistidos representam a mesma entidade de domínio se possuem o
mesmo GoatId imutável. RG nunca define igualdade de entidade.

O estado atual não sobrescreve equals/hashCode em Goat e, portanto, não contém
uma igualdade explícita por RG. A migração exigirá atenção especial para
collections, persistence context, proxies Hibernate, caches e testes. Qualquer
implementação futura deve evitar usar RG como chave de igualdade ou de
identidade técnica.

## 16. Unicidade registral

### Evidência atual

- Banco: num_registro é PK global de cabras.
- V38 adiciona também unicidade composta capril_id + num_registro, que é
  redundante enquanto a PK global existir.
- O código de criação verifica existência por registrationNumber.
- Lookup ABCC filtra por raça + RG normalizado.

### Decisão encerrada na ID2.1

O RG atual normalizado é um identificador de negócio **globalmente único no
CapriGestor**, independentemente de raça, fazenda ou status. A raça não faz
parte da unicidade local: ela é somente um critério de consulta da ABCC.

Essa decisão é sustentada por três evidências complementares:

- `cabras.num_registro` é a PK global desde a V7;
- `GoatBusiness` verifica existência pelo número de registro antes de criar e
  o repository expõe `existsByRegistrationNumber`/`existsById` sem escopo de
  fazenda;
- a combinação raça + RG existe apenas no lookup externo, que pode retornar
  `NOT_FOUND` ou `AMBIGUOUS` e não define a identidade local.

A constraint composta `capril_id + num_registro` adicionada na V38 é
redundante enquanto a PK global existir. Na migração futura, a unicidade ativa
deverá ser transferida para o RG normalizado, com a estratégia de histórico e
registros antigos definida na ID3/ID4. O banco atual ainda compara o texto
armazenado exatamente e o fluxo manual não aplica a normalização canônica de
lookup; isso é uma lacuna de implementação a ser tratada antes da migration,
não uma mudança de decisão.

Esta decisão é sobre o namespace local. Ela não afirma que a ABCC garanta
unicidade global sem raça, nem transforma a consulta externa em fonte de
existência do animal.

## 17. Semântica alvo de HTTP/API

Vocabulário canônico futuro:

- goatId = GoatId técnico imutável;
- registrationNumber = RG atual;
- pesquisa por RG = operação de lookup registral explícita.

As APIs futuras devem distinguir:

1. identificador estrutural de recurso;
2. lookup por registro;
3. lookup público por registro;
4. compatibilidade legada.

Endpoints existentes baseados em registrationNumber podem coexistir
temporariamente com endpoints estruturais por GoatId. A ID2 não renomeia
controllers nem quebra URLs em lote.

## 18. Semântica alvo do frontend

O frontend poderá continuar exibindo RG como identificador humano principal.
GoatId não precisa aparecer como dado de negócio na interface.

Internamente:

- rotas e contexto estrutural devem usar GoatId;
- pesquisa, labels e relatórios podem usar registrationNumber;
- cache e estado não devem confundir os dois significados;
- links antigos baseados em RG só permanecem durante compatibilidade explícita.

A divergência atual do modelo goatResponseDTO, que declara id numérico
opcional enquanto o backend não o entrega, deve ser resolvida durante a
transição, não interpretada como evidência de um GoatId já existente.

## 19. Modelo de banco alvo — apenas conceitual

Goat:

- goat_id: PK técnica BIGINT;
- registration data: TOD, TOE, RG e demais atributos de negócio;
- farm_id e demais relações de contexto.

Tabelas relacionadas:

- goat_id: FK estrutural para Goat.goat_id;
- valores de RG em auditoria ou snapshots: dados históricos/presentacionais,
  não FKs estruturais;
- pai e mãe: referências por GoatId no vínculo interno;
- referências parentais não cadastradas: identidade externa registral.

Este desenho não escolhe nomes de sequence, constraints, colunas definitivas,
ordem de backfill ou SQL. Tudo isso pertence à ID3.

## 20. Princípios de compatibilidade

- Migração inicial aditiva e reversível.
- Não atualizar PK textual em cascata como atalho.
- Não remover rotas ou campos antigos antes de haver tradução e métricas de uso.
- Não manter dois significados silenciosos para goatId.
- Versionar ou compatibilizar eventos que atualmente carregam RG.
- Migrar frontend e backend em contratos coordenados.
- Validar contagens e FKs antes/depois de cada etapa.
- Retificar registro sem trocar o Goat.

## 21. Implicações do reset antes da HML

O dado atual do Capril Vilar é considerado, neste planejamento, massa de
desenvolvimento/teste potencialmente descartável antes da homologação. Um
reset controlado poderia reduzir:

- backfill de animais;
- reconciliação de FKs de dados de teste;
- tratamento de inconsistências históricas de teste;
- rollback de dados operacionais.

O reset não reduz de forma equivalente o trabalho arquitetural. Mesmo com banco
vazio ainda será necessário migrar entidades, JPA, repositories, ports,
business, DTOs, controllers, contratos, frontend, testes, eventos, relatórios
e a fronteira ABCC.

O reset só deve ocorrer em procedimento operacional separado e aprovado. Esta
ADR não executa nem autoriza reset.

## 22. Segurança e integridade

GoatId melhora potencialmente:

- integridade referencial;
- continuidade histórica;
- estabilidade da identidade JPA;
- separação entre domínio interno e registro externo.

GoatId não melhora por si só:

- JWT;
- RBAC;
- autenticação;
- @FarmOwnerOnly;
- canManageFarm;
- canAdministerFarm.

Autorização continua baseada em usuário, papel e ownership da fazenda. A
mudança de identidade é uma decisão de integridade estrutural, não uma medida
de controle de acesso.

## 23. Consequências

### Positivas

- correção de RG sem criar outro animal;
- histórico biológico e operacional preservado;
- JPA com identidade estável;
- ABCC isolada como adapter externo;
- cadastro manual antes da ABCC representado naturalmente;
- contratos futuros com semântica inequívoca.

### Custos

- migração de 11 FKs e auto-referências;
- transição de repositories, ports, casos de uso e APIs;
- compatibilidade temporária de URLs e payloads;
- atualização coordenada do frontend;
- testes de continuidade e retificação;
- decisão e eventual histórico de registro.

## 24. Riscos

| Risco | Severidade | Mitigação |
| --- | --- | --- |
| Constraint de RG escolhida sem regra externa suficiente | crítica | Bloquear ID3/ID4 até obter evidência e decisão. |
| Coexistência silenciosa de RG e GoatId | alta | Vocabulário canônico, contratos explícitos e compatibilidade temporária documentada. |
| Backfill incorreto de pai/mãe ou histórico | alta | Migration ensaiada, contagens, FKs e testes de ciclo de vida. |
| TOD da fazenda e TOD do animal divergentes | alta | Definir fonte canônica e regra de reconciliação na ID3. |
| Eventos e auditoria interpretarem RG como identidade técnica | média | Classificar payloads e versionar compatibilidade. |
| Exposição de GoatId ser tratada como segurança | média | Manter autorização farm-scoped independente do ID. |
| Dados de teste serem confundidos com dados descartáveis em HML | média | Procedimento de reset separado, com inventário e aprovação. |

## 25. Decisões remanescentes para ID3/ID4

1. Transformar a normalização canônica em validações coerentes por fluxo,
   incluindo a diferença atual entre cadastro manual e parto.
2. Definir formato físico do histórico registral e da classificação
   `ERRONEOUS`/anteriormente válido.
3. Desenhar a migração reversível e o backfill de `GoatId`/FKs.
4. Definir compatibilidade e prazo das rotas baseadas em RG.
5. Definir schema/payload de eventos durante a transição.
6. Confirmar se haverá consumidores externos reais antes da HML.
7. Confirmar procedimento e critérios do reset de massa de teste.
8. Confrontar a normalização observada com qualquer contrato oficial ABCC que
   seja disponibilizado antes da implementação.

## 26. Pré-condições para ID3

Antes de desenhar migrations e compatibilidade, devem estar aprovados:

- GoatId Long/BIGINT e imutabilidade;
- separação entre identidade estrutural, registral e ABCC;
- cadastro manual antes da ABCC;
- retificação no mesmo GoatId;
- continuidade de ciclo de vida;
- igualdade por GoatId;
- fronteira ABCC;
- semântica HTTP e frontend;
- distinção TOD de fazenda versus TOD registral do animal;
- normalização canônica de comparação, preservação de zeros e ausência de
  padding implícito;
- unicidade local global do RG normalizado, independente de raça;
- distinção histórica entre RG anteriormente válido e valor digitado incorreto.

### Resultado de entrada

**READY FOR ID3** para planejamento, contratos e desenho técnico. A ID2.1
fechou a direção B, a normalização observada, a unicidade local independente de
raça, a semântica de `GoatFarm.tod` e a distinção histórica entre RG válido e
RG errôneo.

Isso não libera implementação estrutural. A ID3 ainda deve produzir o mapa de
compatibilidade, critérios de backfill, migration reversível e contratos
coordenados antes de qualquer alteração de código ou schema. A validação de
qualquer regra oficial adicional da ABCC também permanece como gate de
implementação, sem reabrir a decisão local de identidade sem evidência nova.

## 27. Alternativas rejeitadas

### A) Manter RG permanentemente como PK e alterá-lo em produção

Rejeitada: mantém acoplamento de JPA, 11 FKs, APIs, frontend e histórico; uma
correção continua sendo mutação de identidade estrutural.

### B) Inativar/excluir e recriar o animal para corrigir RG

Rejeitada: confunde correção registral com ciclo de vida e pode quebrar a
continuidade de genealogia, reprodução, leite, saúde, vendas e auditoria.

### C) Tornar ABCC a fonte de existência do Goat

Rejeitada: cadastro manual antes da presença na ABCC é regra válida do produto.

### D) Criar RG sintético/falso somente para satisfazer a PK

Rejeitada: a restrição técnica não deve distorcer a identidade registral nem
impedir animais manuais sem confirmação externa.

### E) Substituir todas as FKs, APIs e rotas em um big bang

Rejeitada: aumenta risco de quebra, dificulta rollback e cria período
incontrolado de identidade dupla.

## 28. Verificação DDD, Hexagonal, SOLID, Clean Code, YAGNI e KISS

- DDD: identidade de entidade fica independente de atributos registrais
  mutáveis; ciclo de vida e vocabulário ficam explícitos.
- Hexagonal: ABCC continua adapter/anti-corruption boundary; o domínio não
  depende do protocolo externo.
- SOLID: identidade estrutural, registro de negócio e integração externa têm
  responsabilidades separadas.
- Clean Code: goatId significa GoatId; registrationNumber significa RG.
- YAGNI: não há proposta de infraestrutura multi-registro ou IDs distribuídos
  sem requisito.
- KISS: Long/BIGINT é o tipo mais simples compatível com o cenário conhecido.

## 29. Escopo explicitamente não alterado

Esta ADR não:

- cria ou altera classes Java;
- altera entidades ou repositories;
- cria migration ou constraint;
- muda banco ou dados;
- muda controllers, endpoints ou eventos;
- muda frontend;
- altera autenticação/autorização;
- implementa retificação, exclusão ou inativação;
- inicia ID3 automaticamente.

## 30. Conclusão

O CapriGestor deve separar a identidade estrutural do animal da sua identidade
registral. O alvo aprovado para avaliação é GoatId Long/BIGINT imutável,
enquanto TOD, TOE e RG permanecem dados de negócio opcionais, corrigíveis e
auditáveis, independentes da confirmação na ABCC.

A ID2.1 está encerrada com a estratégia B aprovada. A identidade estrutural
futura é GoatId Long/BIGINT imutável; TOD, TOE e RG formam a identidade
registral de negócio, com RG armazenado durante a transição, normalização
determinística e unicidade local global independente de raça. `GoatFarm.tod` é
origem/escopo de criação, não um valor sincronizado durante todo o ciclo de
vida. Retificações mantêm o mesmo GoatId e distinguem histórico legítimo de
valor digitado incorretamente.

A ADR está pronta para revisão humana e a ID3 está liberada somente para
planejamento e desenho. Nenhuma migration, alteração de schema, código,
frontend ou reset de dados começa por esta decisão.
