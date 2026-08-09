# ADR-003 — Composição do custo de compra no estoque

## Status

Aceita em 2026-08-09.

## Contexto

O movimento de entrada permitia informar custo unitário e custo total separadamente. Isso criava dois problemas: o usuário podia digitar valores incompatíveis e o modelo não distinguia mercadorias, frete e desconto. Para valores monetários, o backend já utilizava `BigDecimal`, mas o navegador ainda podia produzir um total divergente ao operar com `number`.

## Decisão

- O backend é a autoridade do cálculo financeiro.
- `unitCost` representa somente as mercadorias e usa escala 4.
- `subtotalCost` é derivado por `round(quantity × unitCost, 2)` e não é armazenado.
- `freightCost` e `discountAmount` usam escala 2 e são armazenados no movimento.
- `totalCost` é armazenado como `subtotalCost + freightCost - discountAmount`.
- O frontend calcula uma prévia em inteiros (`BigInt`, em centavos), mantém o total somente leitura e omite `totalCost` em comandos novos.
- O contrato continua aceitando payloads legados com `totalCost`.
- Frete vinculado à compra integra o custo de estoque e não deve ser duplicado como despesa operacional.

## Consequências

Positivas:
- cálculo determinístico e consistente entre API, banco e resumo mensal;
- composição financeira auditável no histórico;
- ausência de divergências causadas por ponto flutuante no navegador;
- migração compatível com compras já persistidas.

Trade-offs:
- o resumo mensal permanece operacional, não contábil;
- custo médio, FIFO, impostos e rateio de frete entre itens continuam fora do escopo;
- clientes legados podem enviar o total, mas ele será rejeitado quando não respeitar a fórmula.
