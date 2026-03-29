# Manual do Usuário — CapriGestor / GoatFarm

Última atualização: 2026-03-29  
Escopo: guia funcional oficial do sistema para uso operacional no dia a dia da fazenda.  
Idioma: Português (Brasil).  
Links relacionados: [Portal](../INDEX.md), [Domínio](./BUSINESS_DOMAIN.md), [Runbook de Homologação](./HOMOLOGATION_OPERATION_RUNBOOK.md), [Playbook de Freeze e Piloto](./PILOT_FREEZE_PLAYBOOK.md)

## 1. Visão geral do sistema

O CapriGestor / GoatFarm é um sistema de gestão caprina focado em operação real de fazenda.

Ele foi organizado para centralizar, em um único ambiente:

- cadastro da fazenda e do rebanho;
- genealogia e histórico do animal;
- reprodução, prenhez, parto e desmame;
- lactação, produção e secagem;
- sanidade e carência sanitária;
- estoque;
- comercial;
- financeiro operacional mínimo.

O sistema foi desenhado para priorizar:

- clareza operacional;
- rastreabilidade;
- segurança por fazenda;
- uso diário por produtores e operadores.

## 2. Perfis de uso

Os perfis operacionais mais importantes são:

- `Administrador`: possui acesso total às operações permitidas no sistema.
- `Operador`: executa rotinas operacionais conforme as permissões da fazenda.
- `Proprietário da fazenda`: atua com controle sobre a própria fazenda.

Algumas ações podem ficar restritas conforme permissões e ownership.

## 3. Acesso ao sistema

### 3.1 Login

Use a tela de login para acessar a plataforma com email e senha.

Fluxo básico:

1. informe email;
2. informe senha;
3. entre no sistema;
4. selecione ou acesse a fazenda vinculada.

### 3.2 Recuperação de senha

O sistema possui fluxo de recuperação de senha por email.

Fluxo:

1. clique em `Esqueci minha senha`;
2. informe o email cadastrado;
3. abra o link recebido por email;
4. defina uma nova senha.

Observação:

- o sistema responde de forma neutra no pedido de recuperação para não revelar se o email existe ou não.

## 4. Navegação principal

As áreas principais do sistema são:

- `Início`;
- `Fazendas`;
- `Cabras`;
- `Blog`;
- páginas operacionais internas por fazenda.

Dentro da fazenda, as áreas mais importantes são:

- dashboard da fazenda;
- rebanho;
- detalhe do animal;
- reprodução;
- lactação;
- produção de leite;
- sanidade;
- estoque;
- comercial.

## 5. Fazenda e rebanho

### 5.1 Cadastro de fazenda

O sistema permite:

- cadastrar fazenda;
- atualizar dados da fazenda;
- consultar permissões de acesso;
- manter dados básicos operacionais.

### 5.2 Cadastro de cabras

O sistema permite:

- cadastrar cabra manualmente;
- consultar cabras da fazenda;
- pesquisar por nome;
- atualizar dados do animal;
- excluir quando permitido;
- registrar saída controlada do animal.

### 5.3 Importação ABCC

Quando aplicável, o sistema permite:

- consultar raças da ABCC;
- buscar animais públicos da ABCC;
- visualizar prévia da importação;
- confirmar importação unitária;
- confirmar importação em lote.

### 5.4 Saída controlada do animal

A saída do animal é rastreada e não apaga o histórico.

Tipos principais de saída:

- venda;
- morte;
- descarte;
- doação;
- transferência.

O sistema registra:

- tipo da saída;
- data da saída;
- observações.

## 6. Detalhe do animal

O detalhe do animal é uma das telas mais importantes da operação.

Nessa área, normalmente o usuário consegue ver:

- identificação do animal;
- status atual;
- dados zootécnicos;
- genealogia;
- histórico operacional;
- ações rápidas;
- acesso às rotas de reprodução, lactação, sanidade e produção.

Essa tela deve ser usada como ponto central de consulta do histórico do animal.

## 7. Reprodução

O módulo de reprodução cobre o ciclo reprodutivo da cabra.

### 7.1 Cobertura

É possível registrar cobertura do animal.

Regras importantes:

- uma nova cobertura não deve ser usada para mascarar correção de cobertura anterior;
- o sistema considera a cobertura efetiva mais recente como base do ciclo;
- coberturas inconsistentes podem ser bloqueadas.

### 7.2 Diagnóstico de prenhez

O sistema permite registrar diagnóstico positivo ou negativo.

Regra importante:

- o diagnóstico positivo só pode ser confirmado a partir de 60 dias após a cobertura válida usada como referência.

### 7.3 Gestação ativa e histórico

A tela de reprodução mostra:

- gestação ativa, quando existir;
- histórico de gestações;
- eventos reprodutivos recentes;
- recomendação de diagnóstico pendente;
- alertas de fazenda quando houver cabras elegíveis para diagnóstico.

### 7.4 Encerramento da gestação

A gestação pode ser encerrada com motivo apropriado, por exemplo:

- parto;
- aborto;
- perda;
- falso positivo;
- outro motivo operacional previsto.

### 7.5 Parto

O sistema permite registrar parto e cadastrar as crias vinculadas.

### 7.6 Desmame

O sistema permite registrar desmame quando o animal for elegível.

## 8. Lactação

O módulo de lactação controla o ciclo produtivo do leite.

### 8.1 Início da lactação

Use o início da lactação quando a cabra entrar em produção.

### 8.2 Lactação ativa

Quando a lactação está ativa, o sistema mostra:

- data de início;
- situação atual;
- ações rápidas;
- resumo do ciclo;
- recomendação de secagem, quando houver prenhez ativa confirmada.

### 8.3 Secagem

A secagem é uma etapa operacional importante.

Regras principais:

- secagem confirmada interrompe a produção daquele ciclo;
- secagem não significa necessariamente encerramento definitivo do ciclo;
- a secagem coloca a lactação em estado `DRY`.

### 8.4 Retomada da lactação

A retomada é permitida somente em casos coerentes.

Exemplo típico:

- falso positivo de prenhez;
- aborto;
- perda gestacional.

Regra importante:

- se a prenhez terminou em parto, o correto é abrir uma nova lactação, não retomar a lactação anterior.

### 8.5 Bloqueios de negócio ligados à prenhez

Com prenhez ativa após secagem confirmada:

- não é permitido abrir nova lactação;
- não é permitido retomar a lactação secada;
- não é permitido voltar a produzir naquele ciclo enquanto a regra estiver ativa.

## 9. Produção de leite

O módulo de produção registra ordenhas por cabra.

### 9.1 Registro da produção

É possível registrar produção por:

- data;
- turno;
- volume;
- observação.

O sistema também permite:

- consultar histórico paginado;
- atualizar campos permitidos;
- cancelar logicamente um registro, quando necessário.

### 9.2 Regra de unicidade

O sistema protege contra duplicidade operacional do mesmo turno e da mesma data.

### 9.3 Produção durante carência sanitária

Se houver carência de leite ativa:

- o sistema continua permitindo registrar a produção real do animal;
- o registro fica marcado como produzido durante carência;
- o objetivo é preservar o histórico zootécnico real.

Campos de rastreabilidade salvos no registro:

- indicador de produção em carência;
- evento sanitário de origem;
- data final da carência;
- origem resumida do tratamento.

## 10. Sanidade e veterinário

O módulo de saúde permite organizar o manejo sanitário do rebanho.

### 10.1 Tipos de evento sanitário

Exemplos principais:

- vacina;
- vermifugação;
- medicação;
- procedimento;
- doença ou ocorrência.

### 10.2 Estados do evento

Um evento pode ficar como:

- `Agendado`;
- `Realizado`;
- `Cancelado`.

### 10.3 Operações disponíveis

O sistema permite:

- criar evento sanitário;
- editar evento;
- marcar como realizado;
- cancelar evento;
- reabrir evento, quando permitido;
- consultar histórico por cabra;
- consultar calendário sanitário da fazenda;
- consultar alertas sanitários da fazenda.

## 11. Carência sanitária operacional

A carência sanitária é tratada como leitura operacional viva.

### 11.1 O que o sistema calcula

A partir de um evento sanitário realizado, o sistema pode determinar:

- se existe carência de leite ativa;
- se existe carência de carne ativa;
- até quando a carência vai;
- qual tratamento originou essa restrição.

### 11.2 Onde isso aparece

A leitura de carência aparece em:

- detalhe do evento de saúde;
- status da cabra;
- agenda/alertas da fazenda;
- telas ligadas à produção de leite.

### 11.3 Regra prática para o leite

Durante carência de leite:

- o sistema alerta fortemente;
- a produção pode ser registrada para manter o histórico real;
- o leite segue com restrição operacional/comercial até o fim da carência.

### 11.4 Regra prática para carne

Durante carência de carne:

- o sistema alerta fortemente no animal e na fazenda;
- essa etapa atual prioriza alerta claro, sem abrir uma frente comercial paralela maior.

## 12. Dashboard e alertas da fazenda

A dashboard da fazenda funciona como hub operacional.

Ela concentra, conforme o contexto da fazenda:

- indicadores principais;
- agenda operacional;
- alertas de reprodução;
- alertas de secagem;
- alertas sanitários;
- atalhos para módulos principais.

Os alertas ajudam o usuário a localizar rapidamente o que exige ação.

## 13. Estoque

O módulo de estoque controla entradas, saídas, ajustes, lotes e saldos.

### 13.1 Itens e lotes

O sistema permite:

- cadastrar item de estoque;
- cadastrar lote;
- ativar ou inativar lote;
- consultar lotes e saldos.

### 13.2 Movimentos

Tipos principais de movimento:

- entrada (`IN`);
- saída (`OUT`);
- ajuste (`ADJUST`).

Regras importantes:

- quantidade deve ser maior que zero;
- não é permitido saldo negativo;
- itens com lote exigem lote válido e ativo;
- o movimento permanece imutável após gravação.

### 13.3 Custo de compra na entrada

Na entrada por compra, o sistema pode registrar:

- custo unitário;
- custo total;
- data da compra;
- fornecedor;
- observação ou motivo.

Esse custo é usado no financeiro operacional, sem transformar o sistema em ERP contábil.

## 14. Comercial

O módulo comercial cobre a camada comercial mínima da fazenda.

### 14.1 Clientes

É possível:

- cadastrar cliente;
- listar clientes da fazenda.

### 14.2 Venda de animal

É possível:

- registrar venda de animal;
- listar vendas;
- registrar pagamento da venda.

### 14.3 Venda de leite

É possível:

- registrar venda de leite;
- listar vendas de leite;
- registrar pagamento.

### 14.4 Recebíveis mínimos

O sistema mantém leitura simples de recebíveis derivados das vendas, com estados mínimos como:

- `OPEN`;
- `PAID`.

## 15. Financeiro operacional mínimo

Essa camada responde perguntas operacionais simples do mês.

### 15.1 Despesas operacionais

É possível registrar despesas como:

- energia;
- água;
- frete;
- manutenção;
- serviço veterinário;
- combustível;
- mão de obra;
- taxas;
- outras despesas.

### 15.2 Resumo mensal

O resumo mensal informa:

- quanto entrou no mês;
- quanto saiu no mês;
- qual foi o saldo operacional;
- separação entre receita e despesa por origem principal.

Fontes consideradas nesta etapa:

- vendas recebidas;
- despesas operacionais;
- compras de estoque com custo registrado.

## 16. Boas práticas de uso

Recomendações práticas para o uso diário:

- manter os status dos animais corretos;
- registrar cobertura e diagnóstico na ordem real do ciclo;
- usar secagem somente no momento correto do manejo;
- registrar produção diária sem acumular vários dias no mesmo lançamento;
- marcar eventos sanitários como realizados logo após a execução;
- registrar custo de compra na entrada de estoque quando for aquisição real;
- registrar despesas operacionais no mesmo mês em que ocorrerem;
- consultar a dashboard e os alertas antes de iniciar a rotina diária.

## 17. Limites conscientes do sistema nesta fase

O sistema não foi projetado, nesta etapa, para ser:

- ERP completo;
- contabilidade formal;
- sistema fiscal;
- BI analítico avançado;
- plataforma regulatória ampla.

O foco atual é gestão operacional confiável da fazenda.

## 18. Resolução rápida de problemas

### Se não conseguir acessar uma fazenda

Verifique:

- se o usuário pertence à fazenda;
- se a permissão está correta;
- se a autenticação está válida.

### Se não conseguir registrar produção

Verifique:

- se existe lactação ativa;
- se o animal está com status `ATIVO`;
- se já existe produção lançada para a mesma data e turno.

### Se não conseguir iniciar ou retomar lactação

Verifique:

- se existe prenhez ativa;
- se a cabra está seca por secagem confirmada;
- se a última prenhez terminou com parto.

### Se não conseguir avançar em reprodução

Verifique:

- se a cobertura foi registrada corretamente;
- se já passou a janela mínima do diagnóstico;
- se a gestação ativa já foi encerrada ou ainda está aberta.

## 19. Conclusão

O CapriGestor / GoatFarm deve ser usado como sistema operacional da fazenda, com foco em clareza, rastreabilidade e disciplina de registro.

A melhor forma de obter valor do sistema é:

- registrar os eventos no momento em que acontecem;
- usar os alertas da fazenda como referência diária;
- manter reprodução, lactação, sanidade, estoque e comercial alinhados com a rotina real.
