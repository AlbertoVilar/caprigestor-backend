# AUDIT_APPLICATION_CORE_USAGE.md

## 1. Inventário do Pacote `com.devmaster.goatfarm.application`

O pacote `application` (core/shared) contém componentes utilitários reutilizáveis focados em validação e busca de entidades, respeitando a arquitetura hexagonal (sem dependência de frameworks web).

| Classe | Responsabilidade |
| :--- | :--- |
| `com.devmaster.goatfarm.application.core.business.common.EntityFinder` | Utilitário genérico para buscar entidades via `Supplier` e lançar `ResourceNotFoundException` padronizada. |
| `com.devmaster.goatfarm.application.core.business.validation.GoatGenderValidator` | Serviço de domínio compartilhado para validar se uma cabra é fêmea (regra pré-requisito para produção/reprodução). |

---

## 2. Evidência de Uso Real

A busca foi realizada em todo o diretório `src/main/java`.

| Classe | Usada? | Ocorrências | Módulos Afetados | Lista de Arquivos (Exemplos) | Observação |
| :--- | :---: | :---: | :--- | :--- | :--- |
| `EntityFinder` | **SIM** | 4 Módulos | `address`, `farm`, `goat`, `phone` | `AddressBusiness.java`<br>`GoatFarmBusiness.java`<br>`GoatBusiness.java`<br>`PhoneBusiness.java` | Usada para padronizar o `findOrThrow` em operações de busca e update. Total de 8 arquivos (incluindo testes). |
| `GoatGenderValidator` | **SIM** | 3 Módulos | `reproduction`, `milk` (lactation & production) | `ReproductionBusiness.java`<br>`MilkProductionBusiness.java`<br>`LactationBusiness.java` | Centraliza a regra de negócio "Somente fêmeas" usada em contextos distintos. Total de 6 arquivos (incluindo testes). |

### Detalhe da Busca

**EntityFinder** é utilizado para evitar a repetição do bloco `optional.orElseThrow(...)` nos seguintes locais:
- `AddressBusiness`: Busca de Address.
- `GoatFarmBusiness`: Busca de Farm.
- `GoatBusiness`: Busca de Goat.
- `PhoneBusiness`: Busca de Phone.

**GoatGenderValidator** é injetado para garantir integridade de dados em processos que exigem fêmeas:
- `ReproductionBusiness`: Inseminações/gestações.
- `LactationBusiness`: Início de lactação.
- `MilkProductionBusiness`: Registro de produção de leite.

---

## 3. Avaliação e Recomendação

### `EntityFinder`
- **Status:** **KEEP**
- **Justificativa:** É amplamente utilizada (4 módulos core) para padronizar exceções de "Recurso não encontrado".
- **Análise Arquitetural:** ✅ Segura. Depende apenas de `java.util.function` e Exceções de Domínio. Não acessa Repositórios diretamente nem HTTP.
- **Recomendação:** Manter como utilitário padrão para módulos que desejam reduzir boilerplate.

### `GoatGenderValidator`
- **Status:** **KEEP**
- **Justificativa:** Implementa uma regra de domínio transversal ("Gender Constraint") usada em 3 subdomínios diferentes. Removê-la duplicaria a lógica de "buscar cabra + checar gênero + lançar erro" em 3 lugares.
- **Análise Arquitetural:** ✅ Segura. Depende de Port (`GoatPersistencePort`) e Exceções de Domínio. Não acopla com HTTP.

---

## 4. Nota sobre o Módulo Health e Padrões de Projeto

**Contexto da Mudança:**
O módulo `Health` removeu recentemente o `HealthEventVerifier` (uma classe específica daquele módulo) e optou por implementar métodos privados (`findOrThrow`, `verifyGoatExists`) diretamente no `HealthEventBusiness`.

**Por que não usar `EntityFinder` no Health?**
Embora o `Health` pudesse usar o `EntityFinder`, a decisão de usar métodos privados locais ("Inline Private Method") é válida e aceita como padrão **KISS (Keep It Simple, Stupid)**.
- **Vantagem:** Reduz o número de injeções de dependência no construtor.
- **Desvantagem:** Leve duplicação de código (o `orElseThrow`).

**Conclusão para o Projeto:**
- O uso de `EntityFinder` é **encorajado** para padronização, mas **não obrigatório**.
- Se um módulo preferir métodos privados para evitar acoplamento com o `application-core`, isso é aceitável, desde que mantenha a consistência das mensagens de erro e Exceções de Domínio.
- O que é proibido: Duplicar classes de validação complexas (como seria duplicar a lógica de `GoatGenderValidator`).

---

**Auditor:** Antigravit
**Data:** 31/01/2026
**Conclusão Final:**
- **EntityFinder**: KEEP (Usado em 4 módulos, clean architecture).
- **GoatGenderValidator**: KEEP (Usado em 3 módulos, evita duplicação de regra de negócio).
