# Checklist de Validação - Refatoração Address/GoatFarm

## 1. Verificação de Integridade (SQL)
Execute estas queries no banco de dados para garantir que não há endereços compartilhados.

### Detectar Endereços Compartilhados (Deve retornar 0 linhas)
```sql
SELECT address_id, COUNT(*) 
FROM capril 
WHERE address_id IS NOT NULL 
GROUP BY address_id 
HAVING COUNT(*) > 1;
```

### Verificar Constraint UNIQUE
Verifique se a constraint foi criada corretamente no banco de dados.
```sql
SELECT conname 
FROM pg_constraint 
WHERE conname = 'uk_capril_address_id';
```

## 2. Testes de Validação (Operacional)

### Cenário: Exclusão de Fazenda
1. Crie uma fazenda com endereço.
2. Delete a fazenda.
3. Verifique se o endereço foi removido (Cascade.ALL).
   - `SELECT * FROM endereco WHERE id = <id_do_endereco_da_fazenda_deletada>;` (Deve retornar vazio)

### Cenário: Tentativa de Compartilhamento (Via Banco)
1. Tente inserir manualmente duas fazendas com o mesmo `address_id`.
2. O banco deve rejeitar com erro de violação da constraint `uk_capril_address_id`.

## 3. Validação da Aplicação
- [ ] A aplicação iniciou com sucesso (Flyway aplicou a V11).
- [ ] `ddl-auto=validate` passou sem erros de SchemaValidation.
