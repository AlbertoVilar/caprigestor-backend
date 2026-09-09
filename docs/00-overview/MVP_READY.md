# Prontidao do MVP CapriGestor

Ultima atualizacao: 2026-09-09
Escopo: criterios verificaveis para considerar o backend apto ao MVP; nao substitui o status funcional do projeto.
Links relacionados: [Status](./PROJECT_STATUS.md), [Roadmap](./ROADMAP.md), [Portal](../INDEX.md), [Contratos API](../03-api/API_CONTRACTS.md), [Quality gates](../01-architecture/QUALITY_GATES.md)

## Papel deste documento

Este arquivo registra criterios de aceite do MVP. O [PROJECT_STATUS](./PROJECT_STATUS.md)
responde onde o produto esta; este documento responde quais condicoes precisam ser
atendidas. Assim evitamos manter tres documentos com a mesma narrativa.

## Escopo funcional atual

O MVP cobre a operacao diaria segura da fazenda nos modulos Authority, Farm,
Goat/Genealogy, Reproduction, Lactation/Milk, Health, Inventory, Commercial,
Article e Audit. O codigo e os documentos de modulo sao a fonte detalhada de
cada caso de uso.

## Criterios de aceite

- rotas de aplicacao somente em `/api/v1`;
- autenticacao JWT e refresh session funcionando;
- isolamento por fazenda e vinculo formal de operador;
- regras de negocio cobertas por testes unitarios e de integracao;
- Flyway validando o schema PostgreSQL, incluindo V38;
- cobertura JaCoCo minima de `0.7588` no bundle;
- gates de CI verdes: testes, deny_root_markdown, secret scan, CodeQL,
  dependency review, SBOM e container image scan;
- runbooks de homologacao e producao disponiveis e reproduziveis;
- contratos HTTP coerentes com controllers e configuracao;
- nenhuma credencial de ambiente real versionada.

## Validacao local recomendada

Windows:

```powershell
.\mvnw.cmd -B clean verify
```

Linux/macOS:

```bash
./mvnw -B clean verify
```

Testes PostgreSQL/Flyway dependem de Docker. Uma falha de infraestrutura do
runner deve ser registrada como bloqueio de ambiente, nunca mascarada.

## Fora do MVP e trabalho futuro

Novas funcionalidades, como vitrine publica de animais, ficam no ROADMAP. Nao
marque como futuro um modulo que ja esteja implementado no codigo atual.
