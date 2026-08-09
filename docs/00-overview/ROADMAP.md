# Roadmap do Projeto GoatFarm/CapriGestor Backend
Última atualização: 2026-08-08
Escopo: próximos ciclos após fechamento técnico do MVP backend.

Links relacionados: [Status do Projeto](./PROJECT_STATUS.md), [Prontidão do MVP](./MVP_READY.md), [Contratos API](../03-api/API_CONTRACTS.md), [Guia de Versionamento](../03-api/API_VERSIONING_MIGRATION_GUIDE.md)

## 1. Estado atual
- MVP backend concluído nos módulos: Security/Ownership, Goat/Farm, Reproduction, Lactation/MilkProduction, Health e Inventory.
- Convenção de rotas padronizada exclusivamente em `/api/v1`.
- Migração do frontend concluída e compatibilidade não versionada removida em 2026-08-07.

## 2. Próximos marcos (pós-MVP)
### Marco 1 - Hardening operacional
- Remover TODOs críticos e lacunas do fluxo assíncrono de eventos.
- Consolidar monitoramento de erros funcionais e técnicos.
- Reduzir warnings recorrentes de tooling em testes (Mockito/ByteBuddy).

### Marco 2 - Evolução de domínio (após estabilização)
- Compras e vendas com integração de estoque.
- Consolidação financeira mínima por fazenda.
- Painéis farm-level com agregação no backend, sem lógica pesada no frontend.

### Marco 3 - Vitrine pública de animais (planejado, não implementado)
- Criar na página inicial a seção `Animais disponíveis`, com anúncios em destaque e acesso ao catálogo completo.
- Permitir que gestores autorizados publiquem, pausem e encerrem anúncios dos animais da própria fazenda.
- Exibir fotografia principal, galeria, descrição comercial, preço opcional, localização, dados zootécnicos e acesso à genealogia.
- Usar os contatos públicos autorizados da fazenda para facilitar a negociação com o criador.
- Disponibilizar consulta pública com filtros por raça, sexo, localização e faixa de preço.
- Modelar o anúncio como recurso próprio (por exemplo, `AnimalListing`), separado do cadastro do animal e da venda concluída.
- Manter o animal `ATIVO` enquanto estiver apenas anunciado; o status `VENDIDO` continua reservado para a conclusão da venda.
- Ao registrar a venda no módulo Comercial, encerrar automaticamente o anúncio correspondente.
- Impedir anúncios ativos para animais vendidos, falecidos, transferidos ou fora da operação.
- Definir armazenamento de imagens fora do banco relacional, persistindo apenas metadados e URLs no domínio do anúncio.

#### Recorte inicial sugerido
1. Administração do anúncio: publicação, edição, pausa e encerramento.
2. Uma fotografia principal, descrição e preço opcional.
3. Destaques na página inicial e catálogo público de animais disponíveis.
4. Página pública do anúncio com genealogia e contato da fazenda.
5. Integração com a venda de animal já existente no módulo Comercial.

## 3. Critérios de prioridade
- Impacto direto na operação da fazenda.
- Redução de risco de regressão e custo de manutenção.
- Preservação da arquitetura hexagonal e limites entre módulos.
- Contratos de API sempre atualizados junto com o código.

## 4. Regras de execução
- Fluxo obrigatório de Git: `feature/* -> develop -> main` via PR.
- Sem push direto em branches protegidas.
- Gate obrigatório antes de merge: `./mvnw -U -T 1C clean test`.
- Não criar arquivos Markdown na raiz (exceto `README.md`).
