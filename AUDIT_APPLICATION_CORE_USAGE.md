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

| Classe | Usada? | Ocorrências (Arquivos) | Módulos Afetados | Lista de Arquivos (Evidência) | Observação |
| :--- | :---: | :---: | :--- | :--- | :--- |
| `EntityFinder` | **SIM** | 4 | `address`, `farm`, `goat`, `phone` | `AddressBusiness.java`<br>`GoatFarmBusiness.java`<br>`GoatBusiness.java`<br>`PhoneBusiness.java` | Padroniza o `findOrThrow`. |
| `GoatGenderValidator` | **SIM** | 3 | `reproduction`, `milk` (lactation/production) | `ReproductionBusiness.java`<br>`MilkProductionBusiness.java`<br>`LactationBusiness.java` | Regra de domínio transversal (fêmeas apenas). |

### Verificação Arquitetural

| Classe | Imports Proibidos (HTTP/Servlet/Repository) | Imports Permitidos (Ports/Exceptions) | Status |
| :--- | :---: | :--- | :---: |
| `EntityFinder` | Nenhum (0) | `java.util.function`, `ResourceNotFoundException` | **OK** |
| `GoatGenderValidator` | Nenhum (0) | `GoatPersistencePort`, `BusinessRuleException` | **OK** |

---

## 3. Análise de Padrões e Inconsistências

Identificamos dois padrões para a operação "Buscar entidade ou lançar exceção":

1.  **Padrão Shared (EntityFinder):** Usado em 4 módulos (`farm`, `address`, `goat`, `phone`).
    *   *Prós:* Reutilização, zero boilerplate.
    *   *Contras:* Dependência externa ao módulo.
2.  **Padrão Local (Private Method):** Usado no módulo `Health`.
    *   *Prós:* Desacoplamento total, simplicidade (KISS).
    *   *Contras:* Leve duplicação de código (`orElseThrow` em cada classe).

O módulo `Health` divergiu intencionalmente para evitar overengineering, o que é válido. No entanto, a maioria dos módulos core utiliza `EntityFinder`.

---

## 4. Avaliação e Recomendação

### Decisão de Padronização: **Opção A (Recomendado mas Opcional)**

Não há necessidade de forçar um refactor em massa para remover ou impor o `EntityFinder`. Ambos os padrões respeitam a arquitetura hexagonal.

### `EntityFinder`
- **Status:** **KEEP**
- **Justificativa:** Usado em 80% dos casos de uso de busca. Removê-lo geraria trabalho desnecessário e duplicação em 4 módulos.
- **Recomendação:** Novos módulos podem usar `EntityFinder` se desejarem reduzir boilerplate. Módulos que preferem isolamento (como `Health`) podem continuar usando métodos privados.

### `GoatGenderValidator`
- **Status:** **KEEP**
- **Justificativa:** Resolve um problema de duplicação real de regra de negócio (regra "Somente Fêmeas" espalhada em 3 domínios).
- **Recomendação:** Deve ser mantido como Single Source of Truth para validação de gênero.

---

**Auditor:** TRAE (Antigravit Persona)
**Data:** 31/01/2026
