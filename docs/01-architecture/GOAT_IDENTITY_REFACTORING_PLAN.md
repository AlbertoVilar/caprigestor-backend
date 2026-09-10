# Plano de refatoração — identidade estrutural de Animal

**Status:** planejamento técnico; nenhuma mudança de código, schema ou dados é
autorizada por este documento.

**Origem:** auditoria ID1 — Goat Structural Identity Dependency Audit
(2026-09-10). A implementação atual é a fonte primária de verdade. Este plano
transforma os achados em uma sequência de decisão e execução segura; não
constitui decisão de migrar a identidade neste momento.

**Decisão de desenho relacionada:** a ID2 está registrada em
[ADR-004 — Modelo de identidade estrutural do animal](./ADR/ADR-004-goat-identity-model.md).
Ela define GoatId Long/BIGINT como alvo de identidade estrutural e mantém
aberta, por falta de evidência externa suficiente, a regra exata de unicidade
registral. Enquanto esse bloqueio existir, a ID3 de migration permanece
bloqueada.

## 1. Objetivo

Permitir que a identificação de negócio de um animal (RG/número de registro,
atualmente derivado de TOD + TOE) possa ser corrigida quando necessário, sem
usar esse valor mutável como chave estrutural do banco, das relações JPA e dos
contratos internos.

O resultado-alvo, se aprovado na fase de desenho, é separar:

| Conceito | Responsabilidade esperada |
| --- | --- |
| GoatId técnico | Identidade interna, imutável, sem significado de negócio. |
| RG / número de registro | Identificador de negócio exibido, pesquisável e sujeito a regras de correção. |
| TOD e TOE | Dados de identificação de origem do RG; a regra de derivação deve ser explicitada. |
| Identidade ABCC | Referência externa, associada à raça e à origem da consulta, sem substituir a identidade local. |

Este plano **não** pressupõe que todo animal possua cadastro ABCC, nem torna a
importação ABCC obrigatória. O cadastro manual continua um requisito do
produto.

## 2. Estado factual atual

### 2.1 Acoplamento estrutural ao RG

Hoje, Goat.registrationNumber é simultaneamente RG, identificador de negócio
e @Id JPA/PK de cabras:

- goat/persistence/entity/Goat.java;
- GoatRepository extends JpaRepository<Goat, String>;
- buscas, ownership e casos de uso recebem o RG com semântica de goatId;
- o mapper de atualização ignora registrationNumber, portanto o PUT atual não
  altera o RG.

Há 11 FKs diretas para cabras.num_registro, sem política de atualização em
cascata. Além da própria tabela de animais, os vínculos alcançam eventos,
pregnancy, reproductive_event, health_events, lactation, animal_sale e
operational_audit_entry; milk_production chega ao animal por meio da lactação.
Pai e mãe também são FKs de auto-referência em cabras.

As migrations relevantes são V7, V9, V13, V14, V15, V20, V28, V29, V36 e V38.

### 2.2 Superfície de integração

- 46 operações HTTP usam um valor de rota chamado goatId; na prática, ele é o
  RG.
- O frontend possui 17 rotas com :goatId ou :registrationNumber, contexto
  persistido de último animal e clientes HTTP que usam o RG em URLs e corpos.
- Reprodução, lactação, produção de leite, saúde, eventos, comercialização e
  auditoria usam strings com a semântica atual de identidade.
- Mensagens de evento carregam goatRegistrationNumber no payload, mas não o
  usam como chave de roteamento, correlação ou deduplicação.

### 2.3 Regras e integrações que precisam ser preservadas

- Genealogia local depende de pai e mãe; genealogia externa ABCC é uma consulta
  separada.
- A importação ABCC consulta por raça e RG e trabalha com um externalId
  transitório; esse valor ainda não é persistido no animal local.
- Um cadastro manual exige hoje um número de registro; não existe um modelo
  explícito para animal sem registro oficial.
- O banco trata o RG como globalmente único, enquanto a busca ABCC considera
  raça + RG. A política de unicidade de negócio precisa ser decidida antes de
  qualquer migration.
- O estado de venda, o histórico de saúde, reprodução, leite, eventos e
  auditoria não podem ser perdidos nem reinterpretados numa retificação.

## 3. Problema que a refatoração resolve

Um erro de RG descoberto após o cadastro não pode ser corrigido como uma
atualização ordinária porque alteraria a PK e todas as referências que a usam.
Excluir e recriar o animal também não é uma solução geral: pode fragmentar ou
apagar genealogia, lactações, produção, reprodução, saúde, vendas, eventos e
trilha de auditoria.

A refatoração busca tornar uma futura correção de RG uma alteração de dado de
negócio, mantendo as relações históricas apontando para a mesma identidade
técnica. Ela não libera automaticamente a edição de RG: a política de
retificação, histórico e auditoria deve ser definida antes.

## 4. Limites e não objetivos

Não fazem parte desta iniciativa, salvo aprovação explícita posterior:

- alterar RGs existentes diretamente;
- apagar dados de produção, reprodução ou histórico;
- exigir associação à ABCC ou consulta ABCC para cadastro manual;
- alterar a política de autorização de fazendas;
- tornar dados de teste uma premissa de produção;
- executar migrations destrutivas ou resetar o banco.

Uma eventual possibilidade de descartar dados de teste reduz o custo de
conversão de dados, mas **não** elimina a necessidade de migrar contratos,
restrições, testes e integrações de forma consistente.

## 5. Hipótese de arquitetura alvo

A decisão ID2 deve avaliar, no mínimo, o seguinte modelo:

1. GoatId técnico imutável como PK interna.
2. RG mantido como atributo de negócio com unicidade definida de forma
   explícita.
3. Relações internas e FKs apontando para GoatId, inclusive genealogia,
   reprodução, saúde, lactação, comercialização e auditoria.
4. APIs internas e URLs usando uma identidade técnica estável; RG continua
   disponível para busca e apresentação.
5. Um contrato específico para retificar RG, sujeito a autorização, validação,
   auditoria e, se necessário, histórico de identificadores.
6. A integração ABCC mantendo raça + RG como chave de consulta externa, sem
   acoplar a identidade técnica local ao identificador externo.

Não se deve escolher o tipo técnico (por exemplo, UUID ou BIGINT) antes da
ID2. A escolha precisa considerar portabilidade de URLs, exposição de
identificadores, volume esperado, ergonomia JPA, logs e contratos existentes.

## 6. Decisões arquiteturais pendentes (ID2)

| Decisão | Alternativas mínimas | Critério de aprovação |
| --- | --- | --- |
| Tipo de GoatId | UUID; bigint/identity; outra opção justificada | Imutabilidade, compatibilidade, exposição em API e operação. |
| Unicidade do RG | global; por raça; por fazenda; registro oficial + identificação local | Evidência de regra ABCC, requisitos do produto e prevenção de duplicidade. |
| Animal sem registro oficial | identificador local; RG opcional; fluxo separado | Cadastro manual sem falsificar um RG. |
| Retificação | editar RG atual; manter histórico; correção aprovada; correção limitada por estado | Rastreabilidade e integridade de todo ciclo de vida. |
| Integração ABCC | apenas consulta; persistir vínculo externo; sincronização futura | Não confundir fonte externa com identidade local. |
| Compatibilidade HTTP | migração única; rotas paralelas temporárias; tradução controlada | Evitar quebra silenciosa do frontend e de clientes. |
| Exclusão | proibição com histórico; inativação; exclusão condicionada | Integridade, auditoria e regras legais/operacionais. |

## 7. Plano em fases

### ID2 — modelo alvo e política de identidade

Produzir uma ADR aprovada com: modelo lógico, tipo de GoatId, semântica e
unicidade do RG, política para animal sem registro, retificação, exclusão,
compatibilidade HTTP e relação com ABCC. Esta é uma fase de desenho e revisão,
sem migration.

**Gate:** não iniciar código enquanto todas as decisões da seção 6 não tiverem
responsável e aceite explícito.

### ID3 — contrato e mapa de compatibilidade

Especificar DTOs, endpoints, rotas React, filtros, eventos e nomes de campos.
Para cada ocorrência da auditoria ID1, declarar se passa a ser:

- identificador estrutural;
- atributo de negócio;
- busca/lookup;
- apresentação;
- identidade externa ABCC;
- legado removido.

Definir como clientes existentes serão migrados e por quanto tempo, caso haja
compatibilidade transitória.

**Gate:** contratos backend e frontend revisados em conjunto; nenhuma rota pode
misturar silenciosamente os significados de RG e GoatId.

### ID4 — desenho de dados e migration reversível

Desenhar migrations Flyway incrementais e revisáveis, incluindo:

- nova chave técnica e sua população;
- chaves únicas e índices aprovados em ID2;
- troca controlada de FKs, inclusive auto-referências de genealogia;
- preservação de farm_id nas FKs compostas quando aplicável;
- validações de cardinalidade antes/depois;
- estratégia de rollback operacional compatível com Flyway;
- plano separado para dados de teste descartáveis e dados que precisem ser
  preservados.

Não usar atualização de PK em cascata como atalho: ela não resolve semântica de
contratos, histórico e dados externos.

**Gate:** migration ensaiada contra cópia representativa, com inventário de
contagens e FKs validado.

### ID5 — núcleo de domínio e persistência

Introduzir a identidade técnica no modelo, repositórios, ports e adapters.
Atualizar todas as relações JPA e consultas para que dependências internas usem
GoatId; manter RG como atributo de negócio. Não alterar a regra de autorização
apenas por esta refatoração.

**Gate:** testes unitários e de integração provam que relações históricas
mantêm o mesmo animal após uma retificação autorizada.

### ID6 — casos de uso, APIs e eventos

Migrar os casos de uso de todos os módulos: animal, genealogia, reprodução,
lactação, leite, saúde, eventos, comercialização e auditoria. Substituir a
semântica ambígua de goatId que hoje recebe RG. Definir payload versionado ou
compatibilidade explícita para eventos assíncronos.

**Gate:** contratos OpenAPI/DTO e testes de autorização por fazenda cobrem cada
endpoint migrado.

### ID7 — frontend

Migrar rotas, APIs, modelos e o contexto persistido do último animal para o
identificador técnico. Exibir e pesquisar por RG, sem usá-lo implicitamente
como chave de navegação. Corrigir a divergência atual em que o modelo
goatResponseDTO declara um id opcional que o backend não entrega.

**Gate:** fluxos de lista, detalhe, edição, genealogia, reprodução, leite,
saúde, eventos, venda e importação ABCC passam em testes de interface.

### ID8 — retificação, exclusão e auditoria

Somente após as fases anteriores, implementar o caso de uso de retificação
conforme a política ID2. Ele deve validar unicidade, preservar vínculos,
registrar antes/depois, autor e motivo, e atualizar a apresentação em todos os
fluxos. Tratar exclusão como decisão independente: restringir, inativar ou
permitir apenas quando não houver dependências, conforme ADR.

**Gate:** cenários de correção com e sem histórico, tentativas não autorizadas,
conflitos de unicidade e rollback são testados.

### ID9 — limpeza e governança

Remover adaptadores de compatibilidade após o prazo definido, revisar
documentação e eliminar código legado confirmado. Investigar também a query
legada EventRepository.deleteEventsFromOtherUsers, que referencia goat_id
embora a tabela de eventos use goat_registration_number.

**Gate final:** documentação, migrations, contratos e código descrevem a mesma
identidade; CI verde e revisão humana concluída.

## 8. Matriz de impacto prioritária

| Área | Dependência atual | Risco | Tratamento esperado |
| --- | --- | --- | --- |
| Goat / JPA | RG é @Id; pai/mãe usam RG | crítico | Nova PK e migração das auto-referências. |
| Flyway / PostgreSQL | 11 FKs diretas; FKs compostas por fazenda | crítico | Migration incremental com validação de integridade. |
| Repositórios e use cases | String goatId significa RG | alto | Tipagem e contratos explícitos. |
| APIs e frontend | URL/contexto usam RG como ID | alto | Rota por GoatId, RG para display/busca. |
| Reprodução e leite | históricos identificam o animal por string | alto | Migrar FKs e testes de histórico. |
| Saúde, venda e auditoria | registros legais/operacionais guardam RG | alto | Distinguir snapshot histórico de referência estrutural. |
| ABCC | busca por raça + RG; externalId transitório | médio | Manter adapter externo independente. |
| Mensageria | payload carrega RG | médio | Definir evento compatível/versionado. |
| Docs e queries legadas | promessa histórica de alterar RG; query suspeita | médio | Harmonizar e testar antes de remover. |

## 9. Cenários obrigatórios de validação

1. Animal manual sem consulta ABCC, conforme política aprovada.
2. Importação ABCC com raça e RG, incluindo resultado não encontrado e
   ambíguo.
3. Pai/mãe locais e referências externas de genealogia.
4. Animal com reprodução, lactação, leite, saúde, eventos, venda e auditoria.
5. Retificação autorizada de RG preservando todos os vínculos do mesmo GoatId.
6. Tentativa de RG duplicado sob a política de unicidade aprovada.
7. Operador vinculado à fazenda, owner e admin nos endpoints de animal.
8. Dados de teste descartáveis versus dados preservados, sem assumir que um
   ambiente futuro possa ser apagado.
9. Leitura de links antigos durante a compatibilidade, se ela for aprovada.
10. Falha parcial de migration/rollback operacional em ambiente não produtivo.

## 10. Evidências e rastreabilidade

| Evidência | Localização |
| --- | --- |
| Entidade e identidade atual | backend/src/main/java/com/devmaster/goatfarm/goat/persistence/entity/Goat.java |
| Repositório de animais | backend/src/main/java/com/devmaster/goatfarm/goat/persistence/repository/GoatRepository.java |
| Atualização que ignora RG | backend/src/main/java/com/devmaster/goatfarm/goat/business/mapper/GoatBusinessMapper.java |
| Ownership por animal | backend/src/main/java/com/devmaster/goatfarm/sharedkernel/security/OwnershipService.java |
| ABCC | backend/src/main/java/com/devmaster/goatfarm/goat/adapter/out/abcc/GoatAbccPublicHttpAdapter.java |
| Migrations | backend/src/main/resources/db/migration/V7__*.sql a V38__*.sql |
| Rotas e modelo do frontend | frontend/src/main.tsx, frontend/src/api/GoatAPI/goat.ts, frontend/src/Models/goatResponseDTO.ts, frontend/src/utils/appRoutes.ts |

## 11. Próximo passo recomendado

Submeter este plano e a auditoria ID1 a uma revisão arquitetural humana. Se a
revisão aprovar a iniciativa, executar **ID2 — modelo alvo e política de
identidade** como trabalho de desenho separado. Nenhuma alteração de schema,
PK, API, frontend ou dados deve ser iniciada antes desse aceite.
