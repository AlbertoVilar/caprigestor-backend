# Guia de Migração: Owner → User

Este guia descreve o processo completo de migração da entidade `Owner` para `User` no sistema GoatFarm.

## 📋 Pré-requisitos

- ✅ Backup completo da base de dados
- ✅ Ambiente de desenvolvimento/teste configurado
- ✅ Acesso ao banco de dados com privilégios de administrador
- ✅ Aplicação parada durante a migração

## 🔄 Processo de Migração

### Fase 1: Preparação

1. **Backup da Base de Dados**
   ```bash
   # PostgreSQL
   pg_dump -h localhost -U username -d goatfarm > backup_pre_migration.sql
   
   # MySQL
   mysqldump -u username -p goatfarm > backup_pre_migration.sql
   ```

2. **Parar a Aplicação**
   ```bash
   # Parar o servidor Spring Boot
   # Certificar-se de que nenhuma conexão está ativa
   ```

### Fase 2: Execução da Migração

1. **Executar Script de Migração**
   ```sql
   -- Executar o arquivo:
   -- src/main/resources/db/migration/V003__migrate_owner_to_user.sql
   ```

2. **Verificar Integridade dos Dados**
   ```sql
   -- Executar o arquivo:
   -- src/main/resources/db/migration/V004__verify_migration_integrity.sql
   ```

3. **Analisar Resultados da Verificação**
   - Todos os status devem mostrar "OK"
   - Quantidade de erros deve ser 0
   - Verificar se o relatório de migração está correto

### Fase 3: Validação

1. **Iniciar a Aplicação**
   ```bash
   mvn spring-boot:run
   ```

2. **Executar Testes**
   ```bash
   mvn test
   ```

3. **Testes Funcionais**
   - Login com usuários migrados (senha padrão: `password123`)
   - Criar nova fazenda
   - Registrar nova cabra
   - Consultar dados existentes

## 📊 Mapeamento de Dados

| Owner | User | Observações |
|-------|------|-------------|
| `id` | - | Não migrado (novo ID gerado) |
| `nome` | `name` | Mapeamento direto |
| `cpf` | `cpf` | Mapeamento direto |
| `email` | `email` | Mapeamento direto (chave de associação) |
| - | `password` | Senha padrão: `password123` |
| - | `roles` | Role padrão: `ROLE_OPERATOR` |

## 🔧 Atualizações de Referências

### Tabela `capril` (GoatFarm)
- `owner_id` → `user_id`
- Mapeamento baseado no email

### Tabela `goats`
- `owner_id` → `user_id`
- Mapeamento baseado no email

## ⚠️ Pontos de Atenção

1. **Senhas Padrão**
   - Todos os usuários migrados terão a senha `password123`
   - Implementar mecanismo de alteração obrigatória no primeiro login

2. **Roles**
   - Todos os usuários migrados recebem `ROLE_OPERATOR`
   - Ajustar roles manualmente se necessário

3. **Duplicatas**
   - Script verifica duplicatas por email e CPF
   - Resolver conflitos antes da migração

## 🚨 Rollback (Emergência)

Em caso de problemas graves:

1. **Parar a Aplicação Imediatamente**

2. **Executar Script de Rollback**
   ```sql
   -- Executar o arquivo:
   -- src/main/resources/db/migration/rollback_owner_migration.sql
   ```

3. **Restaurar Código**
   - Reverter mudanças no código Java
   - Restaurar referências para Owner
   - Recompilar aplicação

4. **Restaurar Backup (se necessário)**
   ```bash
   # PostgreSQL
   psql -h localhost -U username -d goatfarm < backup_pre_migration.sql
   
   # MySQL
   mysql -u username -p goatfarm < backup_pre_migration.sql
   ```

## ✅ Checklist de Validação

### Pré-Migração
- [ ] Backup realizado
- [ ] Aplicação parada
- [ ] Scripts de migração revisados
- [ ] Ambiente de teste validado

### Pós-Migração
- [ ] Script de verificação executado
- [ ] Todos os status "OK"
- [ ] Aplicação iniciada sem erros
- [ ] Testes automatizados passando
- [ ] Login funcionando
- [ ] CRUD de fazendas funcionando
- [ ] CRUD de cabras funcionando
- [ ] Relatórios funcionando

### Limpeza (Após Validação)
- [ ] Remover entidade Owner (próxima fase)
- [ ] Remover controllers Owner
- [ ] Remover repositories Owner
- [ ] Atualizar documentação

## 📞 Suporte

Em caso de problemas:
1. Consultar logs da aplicação
2. Verificar logs do banco de dados
3. Executar scripts de verificação
4. Se necessário, executar rollback

## 📝 Logs Importantes

```bash
# Logs da aplicação
tail -f logs/goatfarm.log

# Logs do PostgreSQL
tail -f /var/log/postgresql/postgresql-*.log

# Logs do MySQL
tail -f /var/log/mysql/error.log
```

---

**⚠️ IMPORTANTE**: Este processo é irreversível após a remoção da entidade Owner. Certifique-se de que todos os testes foram executados e validados antes de prosseguir para a próxima fase.