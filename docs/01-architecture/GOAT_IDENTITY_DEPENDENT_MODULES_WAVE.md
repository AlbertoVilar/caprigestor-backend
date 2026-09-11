# GoatId — onda de consumidores dependentes

**Status:** IMPLEMENTADA EM CAMADAS / TRANSIÇÃO CONTROLADA / C1 EM REVIEW
**Escopo:** eventos, genealogia, reprodução, saúde, lactação/leite, comercial,
auditoria e contratos do frontend.

Este documento registra o estado comprovado da onda posterior ao núcleo
hexagonal do Goat. O código, as migrations e os testes continuam sendo a fonte
primária de verdade.

## Resultado arquitetural

`cabras.id` (GoatId) agora é a identidade estrutural JPA/PK e é propagado como referência técnica estável nos
registros que dependem do animal. O RG (`num_registro` ou seu snapshot
equivalente) permanece disponível para exibição, busca registral, integração
ABCC e histórico. A separação é aditiva: nenhuma rota RG existente foi
reinterpretada silenciosamente.

```text
Controller/API (RG compatível + GoatId aditivo)
        ↓
Application/business
        ↓  portas de referência GoatId/RG
Adapters de persistência
        ↓
FK técnica farm-scoped → cabras.id
```

O grafo local de pai/mãe usa `pai_goat_id` e `mae_goat_id`. Pais que não são
animais locais continuam sendo snapshots de RG externo. A integridade de
fazenda é preservada pelas FKs compostas `(farm_id, goat_technical_id)`.

## Módulos cobertos

- Eventos: domínio e portas independentes de JPA; publicação e resposta
  carregam `goatTechnicalId`; RG é snapshot.
- Genealogia: consultas atravessam `GoatGenealogyQueryPort` e snapshots
  técnicos, sem expor entidades JPA ao business.
- Reprodução: pregnancy e reproductive_event fazem dual-write técnico e as
  consultas de alertas usam chave técnica coalescida durante a transição.
- Saúde: health events, carência e alertas transportam a referência técnica.
- Lactação/leite: lactation e milk production preservam a coerência técnica
  com a lactação e alertas retornam `goatTechnicalId`.
- Comercial: animal sale grava o GoatId e mantém RG da venda como snapshot.
- Auditoria: a referência técnica é opcional quando o evento não é de animal;
  quando existe snapshot de RG, a consistência técnica é obrigatória.
- Frontend: modelos aceitam `technicalId`/`goatTechnicalId` de forma aditiva;
  componentes antigos continuam consumindo o alias `id` e as rotas RG até a
  onda de contrato estrutural explícito.

## Migrations V41 e V42

`V41__enforce_goat_technical_identity_consumers.sql` completa a primeira onda
estrutural sem editar V39 ou V40. Em uma instalação limpa, ela:

1. falha de forma explícita se houver registro dependente sem GoatId técnico;
2. torna não nulas as referências técnicas de eventos, reprodução, saúde,
   lactação, leite e venda;
3. cria índices técnicos farm-scoped e a unicidade de prenhez ativa por
   `(farm_id, goat_technical_id)`;
4. adiciona a FK de leite para a lactação técnica;
5. mantém RG e suas constraints enquanto existirem consumidores legados;
6. preserva a regra de que auditoria sem animal pode ter `goatTechnicalId`
   nulo, mas auditoria com RG precisa apontar para um Goat.

`V42__promote_goat_technical_identity.sql` conclui a promoção estrutural:

1. troca a PK de `cabras.num_registro` para `cabras.id`;
2. mantém `num_registro` com unicidade própria para lookup, ABCC e contratos
   de compatibilidade;
3. recria as FKs simples legadas contra a chave única registral, sem reverter
   a integridade técnica de V40/V41.

As migrations publicadas V1–V40 não foram alteradas ou condensadas. A política
de reset da base descartável de desenvolvimento continua separada: nenhuma
base local foi resetada nesta onda.

## Compatibilidade e limites intencionais

Os adapters preferem a referência técnica. Há fallback de leitura por RG para
fixtures legadas criadas diretamente pelos testes antes da aplicação do fluxo
de dual-write; esse fallback não enfraquece a instalação PostgreSQL final,
onde V41 exige a coluna técnica. O fallback deve ser removido somente depois
que todas as rotas e fixtures estiverem na API estrutural.

Na intervenção ID4-C1, a costura de persistência legada foi retirada do código
de produção: `LegacyGoatPersistencePort` e o adapter de parentagem legado não
existem mais. Reprodução, saúde, importação ABCC, ownership e parentagem
genealógica usam portas de domínio/referência; `GoatReferenceResolver`
centraliza a regra de rota (`technical-<id>` explícito e demais tokens como RG,
sem inferência numérica). Um teste ArchUnit impede que camadas de aplicação,
negócio, API, domínio ou configuração voltem a importar `GoatEntity`,
`GoatRepository` ou projeções de persistência.

O JPA de `GoatEntity` usa `id` como `@Id` técnico e o repository é tipado com
`Long`. As rotas v1 ainda recebem RG por compatibilidade explícita; quando um
cliente interno possui o id estrutural, usa o token inequívoco
`technical-<id>`. Assim, a API não precisa adivinhar se um valor numérico é RG
ou GoatId. Catálogo público e ABCC continuam usando RG. A retirada dos aliases
de RG, as rotas versionadas por GoatId e a retificação registral pertencem às
próximas ondas.

## Evidência de validação

- instalação Testcontainers PostgreSQL V1→V42 validada;
- integridade cross-farm validada com inserções válidas e rejeitadas;
- testes de reprodução, lactação, saúde, comercial e segurança executados;
- frontend: typecheck, suíte unitária, build de produção e lint executados.
- suíte backend completa: 589 testes, 0 falhas, 0 erros e 1 ignorado; gate
  JaCoCo aprovado.

## Próximas ondas

1. migrar os ports legados e casos de uso restantes para `GoatId` sem JPA;
2. introduzir rotas versionadas por GoatId e aliases de RG mensuráveis;
3. expandir o uso de `technical-<id>` para cache, contexto e navegação interna
   do frontend;
4. criar histórico registral/retificação mantendo o mesmo GoatId;
5. somente depois tornar RG estruturalmente removível e retirar fallbacks.
