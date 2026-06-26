# BankSimulator — Guia para Claude

## Visão Geral

Simulador bancário full-stack de portfólio/aprendizado. Backend em Java/Spring, frontend em React/TypeScript, banco PostgreSQL em produção e H2 em testes/local.

**URL de produção:** `https://app.alessandro-bezerra.me`
**API de produção:** `https://api.alessandro-bezerra.me`

---

## Stack Tecnológico

### Backend
- **Java 17** + **Spring Boot 3.3.2**
- **Maven** como build tool (`pom.xml` na raiz)
- **JDBC puro** — sem ORM (nada de JPA/Hibernate)
- **HikariCP** — pool de conexões (max 10, min 2 idle)
- JWT via `jjwt 0.12.5` (HMAC-SHA)
- AWS SDK 2.25.28 (SES para email)
- BCrypt para hash de senha
- **Spring Boot DevTools** — hot reload em desenvolvimento (recompila ao salvar no IntelliJ)

### Frontend (`frontend/assetstore/`)
- React 19 + TypeScript 5.9
- Vite como bundler
- Tailwind CSS 4 + Framer Motion
- React Router 7, React Query 5, Recharts

### Banco de Dados
- **PostgreSQL** em produção
- **H2** nos testes automatizados e no profile `local` (`MODE=PostgreSQL`)
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
│   ├── email/         # EmailService (interface), SesEmailService, LogEmailService
│   ├── marketplace/
│   └── webhook/
├── dto/               # Request/Response DTOs (Java records)
├── infrastructure/
│   ├── db/            # ConnectionProvider, HikariConnectionProvider, SchemaInitializer
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

O arquivo `src/main/resources/application.properties` usa variáveis de ambiente para todos os segredos.

Variáveis necessárias em produção (armazenadas no SSM Parameter Store em `/banksimulator/*`):

| Variável | Uso |
|---|---|
| `DB_URL` | URL do PostgreSQL |
| `DB_USER` | Usuário do banco |
| `DB_PASSWORD` | Senha do banco (via SSM, não Secrets Manager) |
| `JWT_SECRET_KEY` | Secret HMAC-SHA (Base64) para assinar JWTs |
| `JWT_EXPIRATION_TIME` | Tempo de expiração em ms |
| `ADMIN_TRIGGER_TOKEN` | Token para endpoints `/admin/*` via header `X-Admin-Token` |
| `KOFI_VERIFICATION_TOKEN` | Token do webhook Ko-fi |
| `GOOGLE_CLIENT_ID` | ID do app Google OAuth |
| `AWS_SES_FROM` | Email remetente via AWS SES |
| `APP_BASE_URL` | URL base da aplicação (usado em emails) |
| `WEBHOOK_ASSET_URL` | URL do webhook de assets |

Em produção, o script `fetch-env.py` busca todos esses parâmetros do SSM no startup e grava em `/etc/app.env`, que o systemd carrega via `EnvironmentFile`.

**Porta do servidor:** 5000 (não 8080)

### Propriedades de cookie (configuráveis por profile)

As flags do cookie de autenticação são configuráveis via `application.properties`:

| Propriedade | Produção | Local |
|---|---|---|
| `auth.cookie.domain` | `.alessandro-bezerra.me` | `` (vazio) |
| `auth.cookie.secure` | `true` | `false` |
| `auth.cookie.same-site` | `None` | `Lax` |

Isso é necessário porque cookies com `Secure=true` e `SameSite=None` não funcionam em HTTP `localhost`.

---

## Autenticação

Dois fluxos de autenticação:

1. **Local** — email + senha. Requer verificação de email antes do primeiro login.
2. **Google OAuth** — token ID do Google validado via `GoogleTokenVerifier` (chamada HTTP para `https://oauth2.googleapis.com/tokeninfo`).

O JWT é armazenado em **cookie HttpOnly** (via `AuthCookieService`) **e** retornado no body da resposta de login (campo `token` do `AuthResponse`).

O frontend armazena o JWT em **sessionStorage** (isolado por aba), permitindo múltiplas sessões simultâneas no mesmo navegador. Ver `frontend/assetstore/src/auth.ts`.

Endpoints protegidos aceitam token via:
- Header `Authorization: Bearer <token>` ← verificado primeiro em `AuthCookieService.extractToken()`
- Query param `?token=<token>`
- Cookie `AUTH_TOKEN` (fallback)

Endpoints admin usam header `X-Admin-Token` separado.

### Endpoint de sessão

`GET /auth/session` — lê o cookie HttpOnly no servidor e retorna `{ "token": "<jwt>" }` no body. Usado pelo frontend ao montar o `Router` para migrar a sessão do cookie para sessionStorage sem expor o JWT na URL. Cada aba do navegador chama esse endpoint ao iniciar se sessionStorage estiver vazio.

---

## Multi-Sessão (múltiplos usuários no mesmo navegador)

A arquitetura de sessão usa **sessionStorage por aba** para isolar usuários:

1. Após login (local ou Google), o backend seta cookie HttpOnly **e** retorna o JWT no campo `token` do body.
2. O frontend salva o JWT em `sessionStorage` via `setToken()` em `src/auth.ts`.
3. Todas as requisições autenticadas enviam `Authorization: Bearer <token>` via `authHeader()`.
4. O backend verifica o header antes do cookie em `AuthCookieService.extractToken()`.
5. Como `sessionStorage` é isolado por aba, duas abas com usuários diferentes funcionam independentemente.
6. Ao abrir uma nova aba (ex: verificação de email via link), `Router.tsx` chama `initSession()` que consulta `GET /auth/session` para recuperar o JWT do cookie existente.

---

## Segurança Criptográfica

Cada conta possui um par de chaves **RSA 2048 bits**:
- **Chave pública** armazenada na tabela `account.public_key`
- **Chave privada** armazenada via `PrivateKeyStorage`:
  - `FilePrivateKeyStorage` — em produção, salva em `keys/account-{id}/private.key` relativo ao `WorkingDirectory` do systemd (`/opt/banksimulator/keys/`)
  - `InMemoryPrivateKeyStorage` — em testes e no profile `local`

Toda transação é **assinada** com a chave privada do remetente via `SignatureService` e verificada com a chave pública. A assinatura fica em `transactions.signature`.

---

## Email

`EmailService` é uma **interface** com duas implementações:

| Classe | Profile | Comportamento |
|---|---|---|
| `SesEmailService` | `!local` (produção) | Envia via AWS SES. `SesClient.create()` usa credenciais do instance profile. |
| `LogEmailService` | `local` | Apenas loga `[LOCAL EMAIL] To: ... | Subject: ...` no console. Não faz chamada AWS. |

`EmailVerificationSender` depende da interface `EmailService` e funciona com ambas.

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

### URL da API no frontend

A URL da API é lida via `src/config.ts`:

```typescript
export const API_URL = import.meta.env.VITE_API_URL ?? "https://api.alessandro-bezerra.me";
```

| Arquivo | Valor |
|---|---|
| `frontend/assetstore/.env` | `VITE_API_URL=https://api.alessandro-bezerra.me` (produção, commitado) |
| `frontend/assetstore/.env.local` | `VITE_API_URL=http://localhost:5000` (local, gitignored) |

### Helpers de autenticação frontend

`frontend/assetstore/src/auth.ts` — funções de sessão:

```typescript
getToken()       // lê JWT do sessionStorage
setToken(token)  // salva JWT no sessionStorage
clearToken()     // remove JWT do sessionStorage
authHeader()     // retorna { Authorization: "Bearer <token>" } ou {}
initSession()    // se sessionStorage vazio, chama GET /auth/session e salva o JWT
```

---

## Como Rodar Localmente

### Backend

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

O profile `local` (`src/main/resources/application-local.properties`) configura:
- H2 in-memory no lugar do PostgreSQL (schema criado automaticamente)
- `LogEmailService` em vez de AWS SES
- `InMemoryPrivateKeyStorage` em vez de arquivos
- CORS libera `http://localhost:5173` e `http://localhost:3000`
- Tokens dummy — sem precisar de nenhuma env var
- Cookie sem `Secure` e sem `Domain` (necessário para HTTP localhost)

### Hot Reload com DevTools

Spring Boot DevTools está configurado (`pom.xml`). No IntelliJ:
1. Habilite **Build → Make Project Automatically**
2. Em **Advanced Settings**, habilite **Allow auto-make to start even if developed application is currently running**
3. A cada save, o IntelliJ recompila e o DevTools reinicia o contexto Spring em ~2s sem matar o JVM

### Frontend

```bash
cd frontend/assetstore
npm install
npm run dev   # http://localhost:5173
```

O Vite carrega `.env.local` automaticamente, apontando para `localhost:5000`.

### IntelliJ — rodar tudo junto

Crie uma **Compound run configuration**:
1. Config **Spring Boot** — Main class `br.com.ale.Main`, Active profiles: `local`
2. Config **npm** — package.json do frontend, Command: `run`, Script: `dev`
3. Compound `BankSimulator` com as duas configs acima

### Google OAuth local

Para que o login com Google funcione em `localhost:5173`, adicione no Google Cloud Console (APIs & Services → Credentials → OAuth 2.0 Client `1002611612778-...`):

**Authorized JavaScript origins:**
```
http://localhost
http://localhost:5173
```

O `google.client-id` em `application-local.properties` já está configurado com o ID real do OAuth app.

---

## Testes

Testes de integração usando H2 in-memory. Localizados em `src/test/java/br/com/ale/`.

```bash
mvn test
```

Cobertura atual: serviços de conta, asset, auth, marketplace e use cases do marketplace.

### Testes Quebrados — Prioridade para Próxima Sessão

As seguintes mudanças quebraram os testes existentes e precisam ser corrigidas:

1. **`AuthResponse`** agora tem 3 campos (`clientId`, `name`, `token`) — testes que constroem `AuthResponse` com 2 campos falham na compilação.
2. **`AccountOperationsController`** agora recebe `JwtService` no construtor — testes que fazem `new AccountOperationsController(depositUseCase, accountService)` precisam adicionar o 3º argumento.
3. **`AuthCookieService`** agora usa `@Value` para `auth.cookie.*` — testes que instanciam diretamente precisam de um `ApplicationContext` ou passar as propriedades.
4. **`AuthController`** tem o novo endpoint `GET /auth/session` — pode precisar de ajuste nos testes de controller.

---

## Infraestrutura AWS (gerenciada por Terraform)

Código Terraform em `terraform/`. Estado armazenado localmente em `terraform/terraform.tfstate` (gitignored).

| Recurso | Detalhes |
|---|---|
| EC2 t3.micro | IP: `18.226.192.204`, SSH: `ssh -i ~/.ssh/banksimulator ec2-user@18.226.192.204` |
| RDS db.t4g.micro | `banksimulator-db.cbaeaa00azz5.us-east-2.rds.amazonaws.com` |
| EIP | `18.226.192.204` |
| Nginx | Reverse proxy: 443/80 → localhost:5000 |
| Let's Encrypt | HTTPS via certbot, renovação automática via cron |
| SSM Parameter Store | `/banksimulator/*` — todos os segredos |
| S3 | `banksimulator-frontend-356892335394` — arquivos estáticos do frontend |
| CloudFront | `E2P13GEXYNJRCP` — CDN do frontend, alias `app.alessandro-bezerra.me` |
| GitHub OIDC | IAM role `banksimulator-github-frontend` — deploy sem access keys |
| Route53 | `api.` → EC2 EIP, `app.` → CloudFront alias |
| Chave SSH | `~/.ssh/banksimulator` (ed25519) |

### Custo estimado

~$27/mês

| Item | Custo |
|---|---|
| EC2 t3.micro | ~$8.35 |
| RDS db.t4g.micro (gp3) | ~$14 |
| EIP (1) | ~$3.65 |
| Route53 | ~$0.50 |
| S3 + CloudFront + SES | ~$0.20 |

### Gerenciar infraestrutura

```bash
cd terraform
terraform plan    # visualizar mudanças
terraform apply   # aplicar mudanças
```

---

## CI/CD — GitHub Actions

Workflow em `.github/workflows/deploy.yml`. Dispara em push para branch `prod`.

Dois jobs paralelos:

### deploy-backend
1. Build Maven (`mvn clean package -DskipTests`)
2. SCP do JAR para `/tmp/app.jar` no EC2
3. SSH: move para `/opt/banksimulator/app.jar` e reinicia o serviço systemd
4. Health check em `https://api.alessandro-bezerra.me/health`

### deploy-frontend
1. `npm ci` + `npm run build` em `frontend/assetstore/`
2. Assume IAM role via OIDC (sem access keys)
3. `aws s3 sync dist/ s3://banksimulator-frontend-356892335394 --delete`
4. `aws cloudfront create-invalidation --paths "/*"`

**GitHub Secrets necessários:**

| Secret | Valor |
|---|---|
| `EC2_SSH_PRIVATE_KEY` | Chave privada `~/.ssh/banksimulator` |
| `EC2_HOST` | `18.226.192.204` |
| `FRONTEND_BUCKET` | `banksimulator-frontend-356892335394` |
| `CLOUDFRONT_DIST_ID` | `E2P13GEXYNJRCP` |
| `FRONTEND_ROLE_ARN` | `arn:aws:iam::356892335394:role/banksimulator-github-frontend` |

---

## Git — Branches

| Branch | Finalidade |
|---|---|
| `prod` | Deploy automático no AWS ao fazer push |
| `dev` | Testes locais, sem CI/CD |
| `master` | Mantido em sincronia com `prod` |

Fluxo: desenvolve no `dev` → merge para `prod` para deployar.

---

## Systemd — Serviço em Produção

Arquivo: `/etc/systemd/system/banksimulator.service`

Pontos importantes:
- `WorkingDirectory=/opt/banksimulator` — necessário para que `FilePrivateKeyStorage` escreva em `keys/` relativo a esse diretório (sem isso tentaria escrever em `/keys` com acesso negado)
- `ExecStartPre=+/usr/bin/python3 /opt/banksimulator/fetch-env.py us-east-2` — o prefixo `+` roda como root para poder escrever `/etc/app.env`
- `EnvironmentFile=/etc/app.env` — variáveis buscadas do SSM no startup

---

## Bugs Corrigidos

| Bug | Arquivo | Descrição |
|---|---|---|
| JWT expirado aceito como válido | `JwtService.java:59` | `isTokenExpired(token)` estava sem negação; corrigido para `!isTokenExpired(token)` |
| `AccessDeniedException: /keys` | `FilePrivateKeyStorage.java` | Path relativo `keys/` resolvia para `/keys` (raiz); corrigido adicionando `WorkingDirectory=/opt/banksimulator` no systemd |
| AWS SES trava startup local | `EmailService.java` | `SesClient.create()` no construtor falha sem credenciais AWS; resolvido extraindo interface `EmailService` e criando `LogEmailService` para profile `local` |
| Google login 500 localmente | `application-local.properties` | `google.client-id` estava com valor dummy; corrigido para o ID real do OAuth app |
| 401 em `/accounts/me` após login local | `AuthCookieService.java` | Cookie setado com `Secure=true` e `Domain=.alessandro-bezerra.me` não é enviado em HTTP localhost; corrigido tornando essas flags configuráveis por profile via `auth.cookie.*` |
| Claim 500 — syntax H2 | `AccountDAO.java` | H2 não suporta `INTERVAL '2 minutes'`; corrigido para `INTERVAL '2' MINUTE` |
| Claim 500 — RETURNING não suportado | `AccountDAO.java` | H2 2.2.224 não suporta `UPDATE ... RETURNING`; reescrito como UPDATE separado + SELECT |

---

## Endpoints Admin

### POST /admin/accounts/deposit

Adiciona saldo a uma conta. Requer header `X-Admin-Token`.

```json
{ "clientId": 1, "amount": "1000.00" }
```

ou usando JWT do próprio usuário (extrai o `clientId` via JwtService):

```json
{ "token": "<jwt>", "amount": "1000.00" }
```

---

## Pontos de Atenção

- `SecurityConfig` está configurado para permitir **todas as requisições** — a autenticação é feita manualmente nos use cases, não pelo Spring Security filter chain.
- Não há paginação nos endpoints de listagem.
- Sem rate limiting implementado.
- Sem logs estruturados / observabilidade.
- Chaves privadas RSA em produção ficam em memória do processo + disco em `/opt/banksimulator/keys/`. Se o EC2 for recriado, as chaves existentes são perdidas e usuários não conseguem assinar transações.
- **Testes unitários estão quebrados** — ver seção "Testes Quebrados" acima.
