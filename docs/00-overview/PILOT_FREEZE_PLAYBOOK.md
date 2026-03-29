# Freeze de escopo e piloto controlado

Ultima atualizacao: 2026-03-29  
Escopo: regra operacional para congelamento de escopo, execucao de piloto controlado e ciclo curto de correcoes antes da decisao final de entrega.

## Objetivo

Encerrar a fase de construcao aberta do CapriGestor/GoatFarm e entrar em operacao controlada, com foco em:

- validar uso real com fazendas piloto;
- capturar bugs e friccoes operacionais de verdade;
- corrigir apenas o que impacta a entrega;
- evitar loop de novas features.

## Freeze de escopo oficial

Data efetiva do freeze: `2026-03-29`

### O que fica congelado

- novos modulos de negocio;
- novas frentes amplas de frontend;
- refactors estruturais grandes;
- mudancas de arquitetura nao motivadas por bug real;
- integracoes externas novas;
- dashboards, BI ou analytics novos;
- qualquer item que nao tenha ligacao direta com operacao real do piloto.

### O que continua permitido

- correcao de bug real;
- ajuste curto de UX que remova atrito operacional claro;
- hotfix de runtime, erro de console ou regressao;
- documentacao operacional;
- smoke, homologacao e validacao de piloto;
- ajuste tecnico pequeno que reduza risco imediato da entrega.

## Escopo do piloto

O piloto deve validar os fluxos que sustentam o uso diario da fazenda.

### Fluxos obrigatorios do piloto

1. autenticacao e acesso a fazenda;
2. dashboard e navegacao principal;
3. rebanho e detalhe do animal;
4. reproducao, prenhez e eventos principais;
5. lactacao, secagem, retomada e producao de leite;
6. sanidade e carencia operacional;
7. estoque;
8. comercial;
9. financeiro operacional minimo.

### O que nao entra no piloto

- avaliacao de roadmap;
- wishlist de longo prazo;
- comparacao subjetiva com ERP;
- pedidos que exijam novo modulo antes da validacao do uso real.

## Preparacao minima antes de iniciar o piloto

1. backend com `health = UP`;
2. frontend publicado ou ambiente local/HML estavel;
3. usuario piloto com permissao adequada;
4. fazenda piloto definida;
5. checklist de smoke executado:
   - `.\scripts\restore-smoke-postgres.ps1`
   - `.\scripts\homologation-smoke.ps1`
   - `.\scripts\lactation-dryoff-smoke.ps1`
   - `.\scripts\health-withdrawal-smoke.ps1 -FarmId 17 -GoatId QAT03281450`
6. documentacao minima acessivel para suporte interno.

## Execucao do piloto

### Duracao recomendada

- piloto curto: `7 a 14 dias`

### Estrategia recomendada

- iniciar com `1` fazenda real;
- ampliar para `2 ou 3` somente se a primeira semana ficar estavel;
- registrar diariamente qualquer bloqueio operacional real.

### Registro minimo por ocorrencia

Cada problema do piloto deve conter:

- data e hora;
- fazenda;
- usuario;
- rota/tela;
- fluxo executado;
- resultado esperado;
- resultado observado;
- screenshot ou mensagem de erro, quando existir;
- severidade.

## Politica de correcoes curtas

Durante o piloto, so entram correcoes pequenas e justificadas por uso real.

### Severidade

- `P0`: sistema indisponivel, perda de dado ou fluxo critico inutilizavel
- `P1`: fluxo principal executa com erro ou bloqueio forte
- `P2`: atrito relevante, mas com contorno manual
- `P3`: melhoria desejavel sem impacto direto no piloto

### Regra de resposta

- `P0`: corrigir imediatamente
- `P1`: corrigir na mesma janela operacional ou no proximo ciclo curto
- `P2`: agrupar e corrigir somente se o custo for baixo
- `P3`: registrar para depois da discussao final de entrega

### O que nao fazer durante as correcoes curtas

- nao redesenhar fluxo inteiro;
- nao ampliar escopo para aproveitar o hotfix;
- nao criar abstração nova sem necessidade real;
- nao misturar bug real com wishlist.

## Criterio de saida do piloto

O sistema fica apto para discussao final de entrega quando:

1. nao houver `P0` aberto;
2. os `P1` estiverem corrigidos ou com contorno operacional claro e aceito;
3. os fluxos centrais forem executados sem regressao;
4. a equipe estiver conseguindo operar sem suporte tecnico continuo;
5. a lista remanescente for majoritariamente `P2/P3`.

## Resultado esperado desta fase

Ao final do piloto, a decisao nao deve ser "o que mais podemos construir?".

A decisao deve ser:

- esta pronto para entregar; ou
- existe um conjunto pequeno e objetivo de correcoes finais antes da entrega.
