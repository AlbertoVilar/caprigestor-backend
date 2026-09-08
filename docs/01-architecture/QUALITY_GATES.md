# Qualidade, supply chain e observabilidade

Última atualização: 2026-09-08  
Escopo: gates automatizados introduzidos na W5.

## Gates locais e CI

- `./mvnw -B clean verify` é o gate principal de compilação, testes, Flyway e
  verificação de cobertura.
- JaCoCo gera `target/site/jacoco` e mantém um piso de cobertura de linhas de
  70% no bundle. O valor foi escolhido como ratchet pragmático sobre o baseline
  observado (76,11% em 2026-09-08); novos módulos não podem reduzir o piso.
- Testes PostgreSQL com Testcontainers executam quando Docker está disponível;
  `disabledWithoutDocker=true` mantém o desenvolvimento local determinístico
  quando o daemon não está acessível, mas o CI Linux deve executá-los.
- `codeql.yml` analisa Java em pull requests, pushes protegidos e semanalmente.
- `dependency_review.yml` executa uma varredura de dependências do repositório
  (Maven e arquivos de lock reconhecidos) e bloqueia vulnerabilidades altas ou
  críticas corrigíveis. O recurso nativo Dependency Review do GitHub não está
  disponível neste repositório público, então o gate usa o mesmo scanner
  Trivy de forma explícita e auditável.
- `supply_chain.yml` publica um SBOM CycloneDX como artefato e verifica a imagem
  Docker com Trivy, falhando em vulnerabilidades altas/críticas corrigíveis.

## Observabilidade

O filtro `HttpRequestLoggingFilter` gera um `correlationId` por requisição ou
propaga um identificador seguro fornecido pelo cliente. O identificador é
devolvido no cabeçalho de resposta e incluído no padrão Logback. Logs de
negócio registram eventos, ids técnicos e duração, sem cabeçalhos de
autenticação, tokens ou corpos de requisição.

## Limites conhecidos

O scanner de imagem depende do acesso do runner ao registry público das imagens
base e o Testcontainers depende de um daemon Docker funcional. Falha de
infraestrutura do runner deve ser tratada como bloqueio de CI, não mascarada
com `continue-on-error`.
