> ⚠️ DOCUMENTO HISTÓRICO
> Este arquivo reflete decisões ou análises de fases anteriores do projeto.
> **Não representa o estado atual da arquitetura ou do código.**

# Análise Arquitetural: Módulo de Genealogia (GoatFarm)

## 1. Visão Geral
Este documento apresenta a arquitetura do módulo `Genealogy` do sistema GoatFarm (CapriGestor).
**Status:** Refatorado para Projeção Sob Demanda (On-Demand Projection).

O módulo foi migrado de um modelo de "Projeção Materializada" (tabela dedicada `genealogia`) para um modelo de "Projeção em Tempo de Execução" (Read Model sem persistência).

---

## 2. Arquitetura Atual (Pós-Refatoração)

### A) Geração da Genealogia
A genealogia é gerada dinamicamente no momento da consulta (`GET`), projetando os relacionamentos existentes na entidade `Goat`.

*   **Trigger:** Requisição ao endpoint `GET /api/goatfarms/{id}/goats/{id}/genealogies`.
*   **Processo:**
    1.  O `GenealogyController` chama `GenealogyUseCase.findGenealogy`.
    2.  O `GenealogyBusiness` carrega a entidade `Goat` (Aggregate Root) do repositório.
    3.  O `GenealogyMapper` navega pelo grafo de objetos (`goat.getFather()`, `goat.getMother()`, etc.) mapeando-os para o `GenealogyResponseVO` (Value Object).
*   **Fonte da Verdade:** A própria entidade `Goat` e seus auto-relacionamentos. Não há mais duplicação de dados.

### B) Persistência
Não existe mais persistência dedicada para genealogia.

*   **Tabela:** A tabela `genealogia` foi removida (Migration `V12__Drop_Genealogy_Table`).
*   **Entidade:** A entidade JPA `Genealogy` foi removida.
*   **Benefício:** Eliminação completa de problemas de inconsistência (dados "stale") onde a árvore genealógica divergia dos dados reais dos animais.

### C) Consulta / Performance
*   **Mecanismo:** Navegação JPA no grafo de objetos.
*   **Performance:** A performance agora depende da eficiência do carregamento da entidade `Goat` e seus ancestrais. Como a profundidade é fixa (até bisavós) e o volume de dados por animal é pequeno, o overhead é desprezível em comparação ao ganho de consistência.

### D) Compatibilidade (Legacy)
Para manter compatibilidade com clientes antigos (Frontend), os endpoints de escrita foram mantidos mas depreciados.

*   **Endpoints:** `POST /genealogies` e `POST /genealogies/with-data`.
*   **Comportamento:** Retornam a projeção atual ou simulam uma resposta de sucesso, mas **não persistem** nada na (extinta) tabela de genealogia.
*   **Interface:** Isolados na interface `LegacyGenealogyUseCase`.

---

## 3. Histórico: Arquitetura Anterior (Depreciada)
*Mantido apenas para registro histórico.*

### Problemas Resolvidos pela Refatoração
1.  **Inconsistência de Dados:** Updates em nomes de ancestrais (`Goat`) não refletiam na tabela `genealogia`.
2.  **Complexidade de Escrita:** O processo de criação de um animal (`createGoat`) era penalizado pela criação síncrona da genealogia.
3.  **Lixo de Dados:** Exclusão de animais deixava registros órfãos na tabela `genealogia`.

---

## 4. Avaliação da Arquitetura Atual

**Veredito:** ✅ **Robusto, Consistente e Simplificado.**

| Critério | Avaliação | Justificativa |
| :--- | :---: | :--- |
| **Integridade do Domínio** | ✅ | Fonte única de verdade (`Goat`). Impossível haver divergência. |
| **Manutenibilidade** | ✅ | Menos código (sem Entity/Repo/Adapter de Genealogia). |
| **Performance de Leitura** | ✅ | Adequada para o volume de dados e profundidade da árvore. |
| **Complexidade** | ✅ | Reduzida drasticamente pela remoção da sincronização de estado. |

---

## 5. Estratégia de Testes

### Testes de Integração
*   Verificar se o endpoint `GET` retorna a árvore correta baseada nos relacionamentos de `Goat`.
*   Garantir que os endpoints `POST` (Legacy) não quebram a aplicação e retornam 200/201.

A lógica mais complexa está no Mapper.
*   **O que testar:** Criar um `Goat` com toda a árvore preenchida (pais, avós) e verificar se o `GenealogyMapper` preenche corretamente todos os campos achatados (flat fields).
*   **Cenários:** Animal sem pai, animal só com mãe, animal com árvore completa.

### Testes de Integração (`GenealogyIntegrationTest`)
*   **O que testar:** O fluxo `createGoat` -> verifica se `Genealogy` foi persistida no banco.
*   **Segurança:** Tentar buscar genealogia de uma cabra de outra fazenda (deve falhar).

### O que NÃO testar
*   Getters/Setters simples da entidade `Genealogy`.

---

## 6. Git Ignore

Para garantir que este arquivo de análise não seja versionado no repositório, adicione a seguinte linha ao seu arquivo `.gitignore`:

```gitignore
# Documentation / Analysis
GENEALOGY_MODULE_ANALYSIS.md
```

Para adicionar via terminal:
```bash
echo "GENEALOGY_MODULE_ANALYSIS.md" >> .gitignore
```
