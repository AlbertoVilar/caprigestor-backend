# ID3 — Goat Identity Migration & Compatibility Design

**Status:** DESIGN ONLY — pronto para planejamento de implementação; nenhuma alteração de código, banco, schema, API, frontend, dados ou migration é feita por este documento.

**Baseline:** `origin/develop` em `d6ea139fc0f33770d109b9197edf76752c6c32e6`

**Branch:** `docs/id3-goat-identity-migration-design`

**Fonte de verdade:** código, migrations Flyway, testes, configuração e CI. ID1, [GOAT_IDENTITY_REFACTORING_PLAN](./GOAT_IDENTITY_REFACTORING_PLAN.md) e [ADR-004](./ADR/ADR-004-goat-identity-model.md) são rastreabilidade; não substituem o código atual.

## 1. Objetivo e limites

Este design transforma a decisão aprovada na ID2/ID2.1 em uma sequência futura e revisável para migrar:

```text
atual: cabras.num_registro VARCHAR PK
alvo:  cabras.id BIGINT PK
       cabras.num_registro VARCHAR UNIQUE — RG atual
```

O mesmo animal mantém o mesmo `GoatId` quando TOD, TOE ou RG forem corrigidos. `GoatId` é identidade estrutural interna; `registrationNumber` é identidade de negócio, humana e compatível com a ABCC.

Esta ID3 não implementa `GoatId`, `RegistrationIdentity`, Java/JPA, repository, port, use case, controller, DTO, Flyway/SQL, schema, banco, API, frontend, autorização, JWT, retificação, exclusão ou reset de dados. Também não remove setters nem reescreve genealogia ou ciclo de vida.

## 2. Decisões recebidas da ID2/ID2.1

1. O mesmo animal biológico conserva o mesmo `GoatId`.
2. TOD + TOE formam o RG conforme normalização canônica: trim externo, remoção de espaços internos, maiúsculas `Locale.ROOT`, preservação de zeros à esquerda, sem hífen/padding/inferência implícitos.
3. `GoatFarm.tod` é origem/escopo **somente de animais que se originam/nascem naquele criatório**; não fornece o TOD de todo animal cadastrado naquela fazenda. Animal adquirido/external-origin preserva seu próprio TOD/TOE/RG, mesmo no primeiro cadastro local; `Goat.tod` não é sincronizado automaticamente depois da criação.
4. Cadastro manual é válido antes de o animal existir na ABCC.
5. ABCC é fronteira externa: usa raça + dados registrais e não conhece `GoatId`.
6. RG atual é globalmente único no CapriGestor, independente de raça, fazenda ou status; raça é filtro do lookup ABCC. A constraint definitiva ainda deve ser confirmada antes da ID4.
7. RG anteriormente válido e RG digitado/incorreto são classes históricas distintas.
8. Correção, saída/inativação e exclusão são operações diferentes; exclusão física não corrige identidade.

## 3. Estado factual atual

### 3.1 JPA e aplicação

`Goat.registrationNumber` é `String`, `@Id`, coluna `cabras.num_registro`, única e não nula. `father` e `mother` são `@ManyToOne` para `Goat`, com `pai_num_registro`/`mae_num_registro`; referências externas ficam em `pai_rg_externo`/`mae_rg_externo`.

`GoatRepository` é `JpaRepository<Goat, String>`; ports, use cases e muitos serviços recebem `String goatId`, mas o valor é RG. Consultas de filhos e do grafo familiar comparam `registrationNumber`. Há uma JPQL com `g.id` embora `Goat` não tenha campo `id`; esta inconsistência deve ser corrigida/testada antes da onda correspondente.

`GoatBusinessMapper.updateEntity` ignora `registrationNumber`, portanto o PUT atual não retifica a chave. Criação e atualização ainda usam setters. `Goat` não sobrescreve `equals`/`hashCode`; o alvo deve igualar entidades persistidas apenas pelo `GoatId` imutável, nunca pelo RG.

### 3.2 Contagem honesta

O inventário ID1/ADR-004 chama o conjunto de “11 FKs diretas”. A leitura literal das migrations atuais encontra nove constraints SQL que apontam para `cabras` (incluindo as duas auto-referências) e duas dependências lógicas/legadas: `milk_production.goat_id` sem FK direta e a genealogia V8 removida por V12. A diferença de contagem é um item de verificação obrigatório na ID4 via `information_schema`; nenhuma migration deve assumir que a contagem documental está correta sem esse fechamento.

## 4. Mapa de dependências do banco

| Ordem | Tabela/coluna atual | FK/constraint existente | Papel | Nulo | Self/cross-farm | Alvo conceitual | Risco |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 0 | `cabras.pai_num_registro` | V7 → `cabras(num_registro)` `ON DELETE SET NULL` | pai local | sim | self; preservar fazenda | `pai_goat_id BIGINT` → `cabras.id` | crítico: ciclos/filhos |
| 0 | `cabras.mae_num_registro` | V7 → `cabras(num_registro)` `ON DELETE SET NULL` | mãe local | sim | self; preservar fazenda | `mae_goat_id BIGINT` → `cabras.id` | crítico: ciclos/filhos |
| 1 | `eventos.goat_registration_number` | V9, FK simples `ON DELETE CASCADE` | eventos operacionais | não | RG global | `goat_id BIGINT` | alto: cascade |
| 2 | `pregnancy.(farm_id,goat_id)` | V38 `fk_pregnancy_farm_goat`; antes só índice | gestação | não | composta cross-farm | `(farm_id,goat_id)` → `(capril_id,id)` | crítico |
| 3 | `reproductive_event.(farm_id,goat_id)` | V38 `fk_reproductive_event_farm_goat` | cobertura, diagnóstico, parto, correção | não | composta cross-farm | `(farm_id,goat_id)` → Goat | crítico |
| 4 | `health_events.(farm_id,goat_id)` | V20 simples + V38 composta | saúde/withdrawal | não | composta cross-farm | `(farm_id,goat_id)` → Goat | alto |
| 5 | `lactation.(farm_id,goat_id)` | V38 `fk_lactation_farm_goat`; V13/V14 VARCHAR | lactação | não | composta cross-farm | `(farm_id,goat_id)` → Goat | crítico |
| 6 | `milk_production.(farm_id,goat_id,lactation_id)` | V38 FK para `lactation`; `goat_id` próprio sem FK a `cabras` | medição diária | não | indireta via lactação | `goat_id BIGINT` + coerência com lactation | alto |
| 7 | `animal_sale.(farm_id,goat_registration_number)` | V28 simples + V38 composta; UNIQUE global RG | venda | não | composta cross-farm | `(farm_id,goat_id)` → Goat | crítico |
| 8 | `operational_audit_entry.(farm_id,goat_registration_number)` | V38 composta; índice V29 | auditoria | sim | composta cross-farm | `goat_id BIGINT NULL` + snapshot RG | alto |
| 9 | `genealogia.pai_registro`/`mae_registro` | V8; tabela removida em V12 | legado histórico | sim | não atual | não recriar; avaliar somente dump | médio |
| 10 | payloads/consultas de leite | sem FK adicional | lookup/apresentação | varia | sem FK | GoatId interno + RG display | alto |

As nove constraints ativas são as duas de `cabras`, eventos, pregnancy, reproductive_event, health, lactation, animal_sale e audit. O inventário de “11 referências” não pode ser usado para omitir a auditoria da coluna de leite nem para recriar `genealogia`.

### 4.2 Dependências estruturais fora de FKs declaradas

O inventário ID3 inclui também usos que não aparecem como constraint SQL, pois uma troca de PK pode quebrá-los mesmo sem o PostgreSQL acusar violação:

| Dependência | Evidência atual | Tratamento futuro |
| --- | --- | --- |
| SQL nativo de limpeza | `events/persistence/repository/EventRepository.java`, `deleteEventsFromOtherUsers`, consulta `DELETE FROM eventos ... g.num_registro` | atribuir à ID6-B; trocar para GoatId/farm scope e cobrir com teste de integração |
| Seeds/bootstrap | `src/main/resources/seed/seed_capril_vilar.sql` insere `cabras.num_registro`, `pai_num_registro` e `mae_num_registro`, com `ON CONFLICT (num_registro)` | atribuir à ID4/ID6; manter RG como dado de carga, resolver pais por id quando o schema exigir, sem transformar seed em contrato ABCC |
| JPQL e queries Spring Data | `GoatRepository` pesquisa `registrationNumber` e a consulta de grafo usa `g.id` apesar de não haver `id` atual | classificar cada query como structural, lookup ou legacy; corrigir `g.id` antes da troca de tipo |
| Serviços/adapters | `GoatGenderValidator`, reprodução, eventos, saúde, leite e comercial recebem `String goatId` e fazem lookup por RG | migrar para ports por GoatId; manter métodos de busca por RG somente como compatibilidade |
| Fixtures/helpers | testes de eventos, reprodução, saúde e genealogia constroem `Goat`/filhos com RG como `goatId` | atualizar na onda do módulo; adicionar fixtures dual e prova de continuidade |
| Relatórios/limpeza | consultas de auditoria, eventos e telas usam `goatRegistrationNumber` para filtro/apresentação | separar filtro por GoatId de snapshot RG; atribuir às ondas de API/frontend/audit |

Nenhuma dependência não-FK pode ficar sem uma onda responsável. A ID4 deve executar uma busca de código e de scripts/seeds, além de `information_schema`, e registrar cada ocorrência como `STRUCTURAL_ID`, `BUSINESS_IDENTIFIER`, `LOOKUP`, `PRESENTATION`, `EXTERNAL` ou `LEGACY`.

V38 deve continuar protegendo o escopo da fazenda. No alvo, validar `(farm_id, GoatId)` contra `(capril_id, id)` ou manter GoatId global e uma validação equivalente; não remover a proteção cross-farm durante a troca.

## 5. Transição de banco — somente design

Os passos seguintes são conceituais; não são SQL executável.

| Passo | Propósito | Pré-condições | Efeito | Lock/rollback | Validação/dependências |
| --- | --- | --- | --- | --- | --- |
| D0 | snapshot e inventário | backup restaurável, sem drift | nenhum | leitura; restauração | `information_schema`, contagens, constraints |
| D1 | adicionar `cabras.id` BIGINT | RG sem duplicatas; sequence aprovada | coluna técnica aditiva | DDL curto; remover antes de uso | tipo, sequence, uma linha por Goat |
| D2 | popular/validar ids | D1 | um id estável por Goat | repetir em batch; coluna ainda sombra | nulos/duplicatas = zero |
| D3 | adicionar colunas sombra nos filhos | D2; órfãos zero | `goat_id` técnico ao lado do RG | aditivo; descartar sombra antes de uso | joins por RG/farm, counts |
| D4 | backfill na ordem da seção 4 | D3 | copia id; preserva RG | transação por tabela/batch | zero órfãos, hashes/contagens |
| D5 | índices/unique técnicos | D4; conflitos resolvidos | acelera FKs e id | criação concorrente quando possível | planos e unicidade |
| D6 | migrar pai/mãe | D4; candidatos únicos | preenche ids locais | RG permanece; rollback por leitura antiga | filhos, direção, farm e ciclos |
| D7 | validar novas FKs | D4–D6 | constraints técnicas | validação separada/`NOT VALID` quando adequado | nenhuma violação |
| D8 | dual-read/dual-write temporário | aplicação preparada | novas escritas mantêm id + RG compatível | versão anterior ainda funciona | comparação shadow dos caminhos |
| D9 | JPA/ports/módulos | D7/D8, suíte verde | aplicação passa ao id | rollback de aplicação enquanto dual | integração e autorização |
| D10 | API/frontend/eventos | D9, contratos revisados | rotas novas por GoatId | aliases versionados por RG | OpenAPI, E2E, clientes |
| D11 | NOT NULL e retirada estrutural do RG | compatibilidade vencida | remove referências antigas | ponto de não retorno; restore | busca de código sem legado |
| D12 | aposentar aliases/colunas | consumidores migrados | limpeza final | somente restore/migration inversa ensaiada | métricas, CI e revisão humana |

Não usar `ON UPDATE CASCADE` da PK como atalho. Retificação só é segura quando relações estruturais já apontam para GoatId.

## 6. Preservar dados versus reset controlado

### Caminho A — preservar dados

Obrigatório para HML/produção ou histórico real. Exige backfill, resolução de órfãos/duplicatas, coexistência das colunas, dual-read/dual-write e ensaio de upgrade.

- Mais migrations e maior custo operacional.
- Rollback possível até a retirada estrutural, mantendo RG e id.
- Risco crítico em genealogia, reprodução, lactação/leite e comercial.
- Provas de cardinalidade e FKs antes/depois são obrigatórias.

### Caminho B — reset antes da HML

A massa atual do Capril Vilar foi declarada de teste e pode ser descartada antes da homologação, se houver aceite formal e backup. Recriar o banco pelas migrations elimina backfill e limpeza dessa massa.

- Não elimina mudança de JPA, ports, regras, contratos, frontend, ABCC, autorização ou testes.
- Não autoriza assumir que HML/produção poderá ser apagada.
- Ainda exige um fixture representativo para testar o Caminho A.

**Recomendação:** usar B apenas para a base descartável atual; desenhar e implementar A como caminho de segurança para qualquer dado real. A migração estrutural deve terminar antes da primeira HML que precise preservar histórico.

## 7. JPA/Hibernate e domínio mínimo

Sequência de ondas:

1. Introduzir contratos explícitos de `GoatId` e de busca por RG sem trocar o `@Id`; corrigir/testar a JPQL `g.id` inválida.
2. Adicionar o id técnico como coluna sombra, com `IDENTITY`/sequence PostgreSQL definida na ID4; o cliente nunca fornece id.
3. Migrar joins de pai/mãe e `Event.goat`; depois converter os campos String das entidades `Pregnancy`, `ReproductiveEvent`, `HealthEvent`, `Lactation` e `MilkProduction` para Long na onda de suas FKs.
4. Trocar `JpaRepository<Goat,String>` para `JpaRepository<Goat,Long>` apenas quando os chamadores estiverem migrados; manter `findByRegistrationNumber` para lookup.
5. Encaminhar criação para `Goat.register(...)` e futura correção para `rectifyRegistration(...)`, mantendo o construtor protegido do Hibernate.
6. Definir `equals`/`hashCode` por GoatId somente quando o id for estável em entidades persistidas; RG não pode participar.
7. Restringir setters de id/RG/TOD/TOE gradualmente; não remover setters em massa.

MapStruct deve continuar temporariamente no update de campos não identitários, ignorando id/RG. Uma factory/ObjectFactory deve assumir criação quando o schema dual estiver pronto; cadastro manual, confirmação ABCC, nascimento e fixtures devem convergir para ela sem tornar ABCC obrigatória.

## 8. API e frontend

Regra futura: `goatId` = GoatId técnico; `registrationNumber` = RG. Não inferir pelo tamanho ou por ser numérico; zeros à esquerda tornam isso inseguro.

| Categoria | Rotas atuais abrangidas | Semântica atual | Alvo/compatibilidade |
| --- | --- | --- | --- |
| A — recurso estrutural | GoatController (PUT/PATCH/DELETE/GET/offspring), GenealogyController, EventController, HealthEventController, LactationController, MilkProductionController e ReproductionController em `/goatfarms/{farmId}/goats/{goatId}/...` | `goatId` é RG/String | rotas versionadas/segmento explícito por GoatId; alias por RG durante janela |
| B — lookup registral | GoatController search/registro; GoatAbccImportController search/preview/registration-lookup/confirm/batch | RG/TOD/TOE + raça | `registrationNumber` e filtros explícitos; resolução retorna GoatId |
| C — leitura pública | GET de detalhe/genealogia liberado pela configuração atual | link público por RG | manter alias público ou versão nova; não quebrar links |
| D — legado | `/goatfarms/goats/registration/...`, `/cabras/{registration}/eventos`, `EventService` e helpers antigos | RG em URL/query/cache | resolver explicitamente por RG, marcar deprecated e medir uso |

Os aproximadamente 46 métodos identificados na ID1 pertencem a esses templates. Cada método deve escolher explicitamente rota nova, resolução legada ou `by-registration`; nunca dois significados no mesmo segmento. Responses devem carregar id técnico e RG separadamente.

No frontend, `GoatResponseDTO` deve transportar ambos; `lastGoatContext` deve persistir `{farmId, goatId, registrationNumber}` e migrar o formato antigo. Rotas internas e chaves de cache devem usar GoatId; RG continua como display, busca, tabela, impressão e fallback apenas quando explicitamente legado. `buildGoatEventsPath`, genealogia, reprodução, lactação, leite, saúde e venda precisam de aliases controlados. Nodes de genealogia usam id técnico para o grafo e exibem RG/fonte (`LOCAL`, `ABCC`, `DECLARADO`, `AUSENTE`).

## 9. ABCC e genealogia

```text
raça + RG/TOD/TOE → adapter ABCC → FOUND / NOT_FOUND / AMBIGUOUS
                                  ↓ confirmação explícita
                 RegistrationIdentity → resolver Goat por RG
                 encontrado: mesmo GoatId / retificação
                 ausente: Goat novo ou manual
                 ambíguo: exigir escolha, nunca first result
```

`GoatId` permanece invisível para a ABCC. `externalId` segue transitório. Pais retornados pela ABCC ficam como RG externo até correspondência local segura.

Pai/mãe locais são FKs técnicas nullable; pais externos são strings sem FK. No backfill, resolver dentro do mesmo `capril_id` (ou regra global confirmada), exigir zero/exatamente um candidato, preservar null/external, validar filhos e só então trocar JPQL. Não converter referência ABCC em Goat local automaticamente.

## 10. Retificação e segurança

Futuro caso de uso, fora da ID3:

```text
GoatId constante + old(TOD, TOE, RG) → new(TOD, TOE, RG)
```

Uma transação deve validar unicidade, coerência TOD/TOE, autorização, motivo e origem; registrar GoatId, antes/depois, autor e timestamp; alterar apenas identidade registral. RG legítimo anterior fica pesquisável como histórico inativo; RG incorreto fica como evidência `ERRONEOUS` e não reserva o valor.

A troca não muda autenticação/autorização. Preservar leituras públicas, farm scope, `canManageFarm`, `@FarmOwnerOnly`, `@CanManageFarm`, `ROLE_ADMIN`, `ROLE_FARM_OWNER`, `ROLE_OPERATOR` e todas as decisões da auditoria de autorização. Primeiro resolver Goat e fazenda; depois aplicar a mesma política. GoatId não é segredo nem mecanismo de segurança.

## 11. Ondas futuras e gates

| Onda | Escopo | Gate/stop condition |
| --- | --- | --- |
| ID4-A | `cabras.id` aditivo, sequence, população/validação | Flyway clean/upgrade e um id por Goat |
| ID4-B | colunas sombra, índices e FKs das 11 referências | zero órfãos/duplicatas; inventário fechado |
| ID5-A | pai/mãe e eventos | genealogia/eventos equivalentes |
| ID5-B | pregnancy/reproductive/health | reprodução, withdrawal e cross-farm verdes |
| ID5-C | lactation/milk/commercial/audit | históricos e auditoria equivalentes |
| ID6-A/B | JPA, repositories, ports, MapStruct, factories | JPA/Testcontainers/ArchUnit/unit |
| ID6-C | APIs, aliases e payloads | OpenAPI, autorização e eventos |
| ID7 | frontend, contexto, rotas, cache | UI/E2E de todos os fluxos |
| ID8 | retificação/histórico | autorização, colisão, antes/depois, rollback |
| ID9 | retirar RG estrutural/legado | prazo vencido, CI sem referências |

Cada PR deve ser pequeno, com branch nova e rollback definido. Qualquer órfão, divergência de contagem, alteração de autorização, falha ABCC ou teste de histórico interrompe a onda.

## 12. Provas de integridade e testes

Registrar antes/depois: número de Goat rows; um id por Goat; zero RG/id duplicado; zero órfão nas 11 referências; pai/mãe/filhos iguais; mesmas pregnancy, reproductive, birth, diagnosis, lactation, milk, health, commercial, event e audit links; mesmos farms e nenhuma violação cross-farm.

Gates obrigatórios: Flyway clean e upgrade V7–V38; Testcontainers PostgreSQL multi-farm com zeros, pais locais/externos, duplicata e órfão; JPA `findById`/`getReference`/lazy/self-join/equality; reprodução, leite, saúde, venda, inventário, eventos/RabbitMQ e auditoria; ABCC FOUND/NOT_FOUND/AMBIGUOUS, preview/confirm e manual; API pública/privada e autorização; frontend/cache/links/E2E; retry, idempotência e falha parcial.

## 13. Rollback e pontos de não retorno

- Até D7: tudo é aditivo; descartar sombra é reversível.
- Durante dual-read/write: versão anterior funciona enquanto RG e id coexistem; comparar ambos os caminhos.
- Após troca JPA/PK: manter RG e FKs antigas até observação e backup verificado.
- D11/D12 são ponto de não retorno lógico; rollback requer restore ou migration inversa previamente ensaiada.
- Nunca editar migration já aplicada; usar nova migration ou restore controlado.

## 14. Contagens e impacto

| Área | Contagem atual / confiança |
| --- | --- |
| Tabelas ativas com referência ao animal | 9 (inclui `cabras` self) + `genealogia` histórica removida; inventário ID1 fala em 11 referências; fechar no banco real |
| Entidades JPA | `Goat` + Event, Pregnancy, ReproductiveEvent, HealthEvent, Lactation, MilkProduction, AnimalSale, OperationalAuditEntry = 9 |
| Repositories | 9 potencialmente afetados, incluindo Goat e os módulos acima; confirmar queries nativas |
| Ports/use cases | todos dos módulos acima; número exato não determinado sem grafo gerado |
| Controllers/endpoints | aproximadamente 46 operações com `goatId`/RG (ID1) |
| DTOs/VOs | dezenas; número exato não determinado sem inventário automático |
| Rotas/páginas frontend | aproximadamente 17 usos/rotas (ID1) |
| Migrations | 10 diretamente relevantes: V7, V9, V13, V14, V15, V20, V28, V29, V36, V38; V8/V12 são legado |
| Eventos/payloads | EventPublication, EventMessage, publisher/consumer carregam RG; contagem externa não determinada |
| Testes | muitos fixtures e testes usam RG como `goatId`; número exato não determinado |

## 15. Bloqueadores antes da ID4

1. Fechar 11 versus 9 FKs com `information_schema` e banco representativo.
2. Confirmar unicidade global do RG e colisões históricas com a regra de negócio/ABCC.
3. Aprovar aliases/versão da API pública e prazo de compatibilidade.
4. Definir retenção, classificação e consulta física do histórico registral.
5. Formalizar descarte/backup da massa de desenvolvimento, se Caminho B for usado.
6. Aprovar sequence/identity PostgreSQL e estratégia de locks/índices.
7. Corrigir/testar a query JPQL `g.id`.
8. Inventariar consumidores externos, caches, relatórios e eventos fora dos repositórios.
9. Definir exclusão/inativação independentemente da retificação.

## 16. Entrada em HML e resultado

**Classificação: READY FOR IMPLEMENTATION PLANNING.**

Se HML possuir dados reais, a identidade estrutural deve estar completa antes da entrada. Se a base atual for realmente descartável, o reset controlado reduz somente `DATA MIGRATION COST`; não reduz `ARCHITECTURAL MIGRATION COST` e não justifica levar RG como PK para HML.

A próxima etapa é revisar os bloqueadores e abrir a primeira onda de implementação (ID4-A) a partir do `develop` reconciliado. Este documento não autoriza código, migration, schema, API, frontend ou alteração de dados.
