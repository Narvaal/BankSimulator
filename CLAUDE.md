# BankSimulator — Guia para Claude

## Visão Geral

Simulador bancário full-stack de portfólio/aprendizado. Backend em Java/Spring, frontend em React/TypeScript, banco PostgreSQL em produção e H2 em testes.

**URL de produção:** `https://app.alessandro-bezerra.me`

---

## Stack Tecnológico

### Backend
- **Java 17** + **Spring Boot 3.3.2**
- **Maven** como build tool (`pom.xml` na raiz)
- **JDBC puro** — sem ORM (nada de JPA/Hibernate)
- JWT via `jjwt 0.12.5` (HMAC-SHA)
- AWS SDK 2.25.28 (Secrets Manager + SES)
- BCrypt para hash de senha

### Frontend (`frontend/assetstore/`)
- React 19 + TypeScript 5.9
- Vite como bundler
- Tailwind CSS 4 + Framer Motion
- React Router 7, React Query 5, Recharts

### Banco de Dados
- **PostgreSQL** em produção
- **H2** nos testes automatizados (`MODE=PostgreSQL`)
- Schema definido manualmente em `src/main/resources/schema.sql`

---

## Arquitetura do Backend

O projeto segue uma **arquitetura em camadas** inspirada em Clean Architecture. Pacote raiz: `br.com.ale`.

```
br.com.ale/
├── application/        # Camada de aplicação
│   ├── api/           # Controllers REST
│   ├── config/        # Configurações Spring (Security, CORS, etc)
│   ├── account/       # UseCases e Commands de conta
│   ├── auth/          # UseCases e Commands de autenticação
│   ├── marketplace/   # UseCases e Commands do marketplace
│   ├── claim/         # UseCases de claim de assets
│   ├── client/        # Queries de cliente
│   ├── transaction/   # Queries de transação
│   └── scheduling/    # Scheduler de geração de assets
├── domain/            # Camada de domínio (entidades, enums, exceções)
│   ├── account/
│   ├── asset/
│   ├── auth/
│   ├── client/
│   ├── emailVerification/
│   ├── exception/
│   └── transaction/
├── dao/               # Data Access Objects (queries SQL diretas via JDBC)
│   ├── AccountDAO.java
│   ├── ClientDAO.java
│   ├── TransactionDAO.java
│   ├── EmailVerificationDAO.java
│   └── asset/         # DAOs de asset
├── service/           # Serviços de negócio
│   ├── account/
│   ├── asset/
│   ├── auth/          # JwtService, GoogleTokenVerifier, AuthService
│   ├── crypto/        # RSA KeyPairService, SignatureVerifier, PrivateKeyStorage
│   ├── email/         # EmailService (via AWS SES)
│   ├── marketplace/
│   └── webhook/
├── dto/               # Request/Response DTOs (Java records)
├── infrastructure/
│   ├── db/            # ConnectionProvider, DynamicConnectionProvider, SchemaInitializer
│   └── auth/          # AuthCookieService, TokenGenerator
└── util/
```

### Padrão Command/UseCase

Cada operação de escrita segue o padrão:
1. **Command** — objeto imutável com os dados da operação
2. **UseCase** — executa a lógica de negócio orquestrando Services e DAOs

Exemplo: `CreateAccountCommand` → `CreateAccountUseCase`

---

## Banco de Dados

Schema completo em `src/main/resources/schema.sql`. Tabelas principais:

| Tabela | Descrição |
|---|---|
| `client` | Usuários (suporta login local e Google OAuth) |
| `account` | Contas bancárias (1 por cliente, com chave pública RSA) |
| `credential` | Senhas hash separadas da tabela client |
| `transactions` | Transações com assinatura RSA |
| `email_verification` | Tokens de verificação de email e reset de senha |
| `asset` | Assets únicos (texto único, supply controlado) |
| `asset_unit` | Unidades individuais de cada asset (status: AVAILABLE/IN_MARKET/RESERVED/TRANSFERRING) |
| `asset_listing` | Ofertas de venda no marketplace |
| `asset_price_history` | Histórico de preços por listing |
| `asset_bundle` | Pacotes de assets agrupados |
| `asset_transfer` | Histórico de transferências de asset_unit |

O schema de testes em `src/test/resources/schema.sql` é **idêntico** ao de produção.

---

## Configuração e Variáveis de Ambiente

O arquivo `src/main/resources/application.properties` usa variáveis de ambiente para todos os segredos. Nunca há valores hardcoded.

Variáveis necessárias para rodar:

| Variável | Uso |
|---|---|
| `DB_URL` | URL do PostgreSQL |
| `DB_USER` | Usuário do banco |
| `JWT_SECRET_KEY` | Secret HMAC-SHA (Base64) para assinar JWTs |
| `JWT_EXPIRATION_TIME` | Tempo de expiração em ms |
| `ADMIN_TRIGGER_TOKEN` | Token para endpoints `/admin/*` via header `X-Admin-Token` |
| `KOFI_VERIFICATION_TOKEN` | Token do webhook Ko-fi |
| `GOOGLE_CLIENT_ID` | ID do app Google OAuth |
| `AWS_SES_FROM` | Email remetente via AWS SES |
| `APP_BASE_URL` | URL base da aplicação (usado em emails) |
| `WEBHOOK_ASSET_URL` | URL do webhook de assets |

A senha do banco em produção é obtida via **AWS Secrets Manager** (`SecretsService`), não por variável de ambiente direta.

Para testes, o H2 é usado automaticamente quando `db.use.test=true`.

**Porta do servidor:** 5000 (não 8080)

---

## Autenticação

Dois fluxos de autenticação:

1. **Local** — email + senha. Requer verificação de email antes do primeiro login.
2. **Google OAuth** — token ID do Google validado via `GoogleTokenVerifier`.

O JWT é armazenado em **cookie HttpOnly** (via `AuthCookieService`), não no body.

Endpoints protegidos aceitam token via:
- Header `Authorization: Bearer <token>`
- Query param `?token=<token>`
- Cookie (implicitamente)

Endpoints admin usam header `X-Admin-Token` separado.

---

## Segurança Criptográfica

Cada conta possui um par de chaves **RSA 2048 bits**:
- **Chave pública** armazenada na tabela `account.public_key`
- **Chave privada** armazenada via `PrivateKeyStorage` (implementações: `InMemoryPrivateKeyStorage`, `FilePrivateKeyStorage`)

Toda transação é **assinada** com a chave privada do remetente via `SignatureService` e verificada com a chave pública. A assinatura fica em `transactions.signature`.

---

## Frontend

Localizado em `frontend/assetstore/`. Rotas principais:

| Rota | Componente |
|---|---|
| `/` | Landing page (`RareLines`) |
| `/login` | Login local |
| `/register` | Criar conta |
| `/forgot-password` | Solicitar reset de senha |
| `/reset-password` | Redefinir senha |
| `/inventory` | Dashboard do usuário (privado) |
| `/market` | Marketplace de assets (privado) |
| `/reward` | Claim de assets gratuitos (privado) |

Para rodar o frontend:
```bash
cd frontend/assetstore
npm install
npm run dev   # http://localhost:5173
```

---

## Testes

Testes de integração usando H2 in-memory. Localizados em `src/test/java/br/com/ale/`.

Para rodar:
```bash
mvn test
```

Cobertura atual: serviços de conta, asset, auth, marketplace e use cases do marketplace.

---

## Como Rodar o Backend (local)

```bash
mvn spring-boot:run
```

O servidor sobe na porta **5000**. Requer as variáveis de ambiente configuradas ou um `application-local.properties`.

---

## Infraestrutura AWS (gerenciada por Terraform)

Código Terraform em `terraform/`. Estado: aplicado.

| Recurso | Detalhes |
|---|---|
| EC2 t3.micro | IP: `18.226.192.204`, SSH: `ssh -i ~/.ssh/banksimulator ec2-user@18.226.192.204` |
| RDS db.t4g.micro | `banksimulator-db.cbaeaa00azz5.us-east-2.rds.amazonaws.com`, DB: `bank` |
| EIP | `18.226.192.204` |
| DNS | `api.alessandro-bezerra.me` → A record → EC2 EIP |
| HTTPS | Let's Encrypt (certbot), renovação automática |
| SSM Parameter Store | `/banksimulator/*` — todos os segredos da aplicação |
| Nginx | Reverse proxy: 443/80 → localhost:5000 |
| Chave SSH | `~/.ssh/banksimulator` (ed25519) |

### Custo estimado pós-migração

~$27/mês (vs ~$50/mês antes)

| Item | Custo |
|---|---|
| EC2 t3.micro | ~$8.35 |
| RDS db.t4g.micro (gp3) | ~$14 |
| EIP (1) | ~$3.65 |
| Route53 | ~$0.50 |
| SES, Amplify, S3 | ~$0.20 |

### Deploy da aplicação

```bash
./scripts/deploy.sh
```

O script compila o JAR, faz SCP para o EC2 e reinicia o serviço systemd.

### Gerenciar infraestrutura

```bash
cd terraform
terraform plan    # visualizar mudanças
terraform apply   # aplicar mudanças
```

---

## Pontos de Atenção (ainda por resolver)

- `SecurityConfig` está configurado para permitir **todas as requisições** — a autenticação é feita manualmente nos use cases, não pelo Spring Security filter chain.
- Não há paginação nos endpoints de listagem.
- Sem rate limiting implementado.
- Sem logs estruturados / observabilidade.
