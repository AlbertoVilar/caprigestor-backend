# Análise Sistêmica e Plano de Testes Unitários

## 1. Análise do Sistema

### Arquitetura e Organização
O sistema segue uma arquitetura em camadas com forte influência de **Arquitetura Hexagonal (Ports & Adapters)**.
- **Pontos Fortes**: Lógica de negócio centralizada em classes `*Business` (ex: `GoatFarmBusiness`, `GenealogyBusiness`), desacoplada da persistência direta.
- **Estrutura de Testes (Novo)**: A estrutura de testes foi reorganizada para espelhar fielmente a estrutura de pacotes da aplicação principal (`src/main/java`), facilitando a localização e manutenção dos testes.
- **Pontos de Atenção (Resolvido)**: O acoplamento indevido com a API Servlet (`RequestContextHolder`) nas classes de negócio foi removido. A responsabilidade de preencher o `path` nas exceções de validação foi movida para o `GlobalExceptionHandler`.

### Integridade de Dados
A recente refatoração na entidade `Address` impôs uma relação estrita de 1:1. O sistema deve garantir que endereços nunca sejam compartilhados entre fazendas.

### Domínio Crítico
O módulo `Genealogy` possui regras complexas de ancestralidade (evitar ciclos, validar hierarquia) que são vitais para a consistência do negócio.

---

## 2. Testes Unitários Prioritários (Must Have)

Abaixo estão listados os cenários de teste essenciais para garantir a robustez e integridade do sistema.

### A. Domínio Core: Genealogia (`GenealogyBusinessTest`)
*Risco: Alto (Corrupção de dados lógicos)*

1.  **Validação de Consistência de Requisição**:
    - **Cenário**: `goatId` na URL diferente do `goatId` no corpo da requisição (DTO).
    - **Resultado Esperado**: Lançar `IllegalArgumentException`.
    - **Justificativa**: Evita manipulação acidental ou maliciosa de registros cruzados.

2.  **Tratamento de Erros de Persistência**:
    - **Cenário**: Simular falha de banco de dados durante a busca de parentes.
    - **Resultado Esperado**: Lançar `DatabaseException` com mensagem clara, não engolir a exceção original.

3.  **Detecção de Ciclos Parentais (Recomendado)**:
    - **Cenário**: Tentar adicionar uma cabra como pai/mãe de si mesma ou de um descendente.
    - **Resultado Esperado**: Bloqueio da operação (Erro de Validação).

### B. Integridade e Refatoração: Endereços (`AddressBusinessTest`)
*Risco: Médio/Alto (Regressão de integridade)*

4.  **Isolamento de Entidades**:
    - **Cenário**: Método `findOrCreateAddressEntity` recebendo dados idênticos a um endereço existente.
    - **Resultado Esperado**: Deve retornar uma **nova instância** (ou novo ID), garantindo que a alteração em uma fazenda não afete outra.

5.  **Validação de Estado (UF)**:
    - **Cenário**: Enviar sigla de estado inválida (ex: "XX", "12").
    - **Resultado Esperado**: Lançar `ValidationException`.

### C. Lógica Transactional (`GoatFarmBusinessTest`)
*Risco: Alto (Dados inconsistentes/parciais)*

6.  **Rollback em Falha em Cascata**:
    - **Cenário**: Método `createFullGoatFarm`. Sucesso ao salvar `GoatFarm` e `Address`, mas falha ao salvar `Phone`.
    - **Resultado Esperado**: A transação deve ser revertida completamente. Nenhuma fazenda ou endereço deve restar no banco.

7.  **Atualização e Orfandade**:
    - **Cenário**: `updateGoatFarm` substituindo o endereço atual por um novo.
    - **Resultado Esperado**: O novo endereço é associado e o antigo (se não usado por mais ninguém/orphanRemoval) deve ser removido ou desassociado corretamente.

### D. Segurança (`SecurityTest`)
*Risco: Crítico (Acesso indevido)*

8.  **Verificação de Propriedade (Ownership)**:
    - **Cenário**: Chamar `updateGoatFarm` ou `deleteGoatFarm` para um ID que não pertence ao usuário logado.
    - **Resultado Esperado**: O serviço deve invocar `ownershipService.verifyFarmOwnership(id)` antes de qualquer operação. O teste deve falhar se essa chamada não ocorrer.

---

## 3. Recomendações de Melhoria (Refatoração)

Para viabilizar e simplificar os testes acima, recomenda-se:

- **Remover `RequestContextHolder` do Domínio (Concluído)**:
    - As classes `GoatFarmBusiness` e `AddressBusiness` não dependem mais do contexto HTTP.
    - O `path` em `ValidationException` agora é preenchido pelo `GlobalExceptionHandler`, garantindo que a camada de negócio permaneça agnóstica ao contexto web.

---

## 4. Observações sobre Testes de Integração

Alguns testes de integração legados podem falhar devido à configuração ausente de ambiente, especificamente o erro:
`Could not resolve placeholder 'jwt.public.key'`

Este problema **não está relacionado à refatoração da arquitetura hexagonal**, mas sim à configuração do ambiente de testes. Deve ser tratado posteriormente via:
- Atualização do `application-test.properties`
- Ou uso de `@TestPropertySource` nos testes afetados.

---

## 5. Checklist de Validação Manual

Para validar a integridade da refatoração e o funcionamento correto das exceções:

1.  **Subir a aplicação** com perfil `dev`.
2.  **Executar uma requisição inválida** (ex.: payload JSON com campos obrigatórios ausentes ou valores inválidos).
3.  **Confirmar resposta HTTP 422 (Unprocessable Entity)**.
4.  **Verificar o corpo da resposta JSON**:
    - Deve conter o campo `"path": "/api/..."` (correspondente à rota chamada).
    - O campo `path` deve estar **preenchido corretamente** e nunca ser `null`.

---

## 6. Estrutura de Diretórios de Teste (Refatorada)

A estrutura de testes foi padronizada para refletir a organização dos pacotes de produção (`src/main/java`), garantindo escalabilidade e facilidade de navegação.

```text
src/test/java/com/devmaster/goatfarm
├── address
│   ├── api         # Testes de Controller
│   └── business    # Testes de Regras de Negócio
├── authority
│   ├── api         # Testes de Controller e Integração de Auth
│   └── business    # Testes de UserBusiness
├── config
│   └── exceptions  # Testes de GlobalExceptionHandler
├── events
│   ├── api         # Testes de EventController
│   └── persistence # Testes de Repositório/DAO
├── farm
│   ├── api         # Testes de GoatFarmController
│   └── business    # Testes de GoatFarmBusiness
├── genealogy
│   ├── api         # Testes de GenealogyController
│   └── business    # Testes de GenealogyBusiness
├── goat
│   ├── api         # Testes de GoatController
│   └── business    # Testes de GoatBusiness
├── integration     # Testes de Integração End-to-End
│   ├── GoatFarmApplicationTests.java
│   └── ValidationPathIntegrationTest.java
├── phone
│   ├── api         # Testes de PhoneController
│   └── business    # Testes de PhoneBusiness
└── security        # Testes de Segurança e Ownership
```