# RareLines — Guia para Claude

## Visão Geral

**RareLines** é uma plataforma premium de cartas colecionáveis digitais, geradas semanalmente por IA com base em eventos reais do mundo. Toda semana, uma pipeline automatizada busca notícias relevantes, cria cartas temáticas com atributos únicos, gera ilustrações e as disponibiliza para os usuários em Booster Packs. Os usuários coletam, abrem packs, completam coleções e negociam cartas no marketplace.

**URL de produção:** `https://app.alessandro-bezerra.me`  
**API de produção:** `https://api.alessandro-bezerra.me`

O produto final não deve parecer um projeto de portfólio. Deve parecer uma plataforma de trading cards digitais pronta para produção.

### Design Goals

- **Premium** — cada interação deve parecer satisfatória. Nada aparece instantaneamente. Tudo anima naturalmente.
- **Collectible** — usuários devem sentir a satisfação de colecionar, completar coleções e possuir cartas raras.
- **Modern** — UI com Three.js, shaders e animações cinematográficas.
- **Interactive** — cartas reagem ao mouse, têm física e comportamento de objeto físico.

### Restrições Técnicas

- Backend agnóstico de framework (sem ORM, sem Spring Data).
- Renderização Three.js isolada da lógica de negócio.
- Animações data-driven (configuradas por raridade, não hardcoded).
- Metadata suporta futuros tipos sem migration.
- Nunca hardcodar layouts de carta no backend.

---

## Modelo de Dados — Artifact Metadata

O campo `metadata` da tabela `asset` armazena um documento JSONB. Todos os campos são **obrigatórios**.

```json
{
  "name":     "Apple Vision Pro",
  "subtitle": "Apple enters the spatial computing era",
  "category": "Technology",
  "rarity":   "Legendary",

  "effects": {
    "foil": true, "glow": "#ffd700", "shimmer": true,
    "particles": "heavy", "borderLight": true
  },

  "illustration": "https://cdn.rarelines.io/cards/apple-vision-pro.png",
  "background":   "https://cdn.rarelines.io/backgrounds/tech-blue.png",

  "attributes": { "influence": 91, "innovation": 95, "controversy": 48, "longevity": 72, "reach": 88 },

  "abilities": [{ "name": "Closed Ecosystem", "description": "Commands loyalty through exclusivity — impossible to replicate." }],

  "passive":    { "name": "Silicon Monopoly", "description": "Arguments backed by revenue above $1T gain extra weight." },
  "weakness":   "Premium pricing limits global adoption",
  "flavorText": "The future arrived. Apple priced it so you'd know your place in it.",
  "lore":       "When Apple unveiled its spatial computing headset in 2024, it marked the beginning of a new computing paradigm — expensive, polarizing, and impossible to ignore.",

  "traits":    [{ "name": "Era", "value": "AI Age" }, { "name": "Origin", "value": "United States" }],
  "timeline":  [{ "date": "2023-06-05", "event": "Announced at WWDC" }, { "date": "2024-02-02", "event": "Released in the United States" }],
  "references": ["https://www.apple.com/apple-vision-pro/"],

  "collection": "Tech Giants 2024", "cardNumber": "042", "releaseDate": "2024-06-03",
  "artist": "RareLines AI", "model": "dall-e-3", "prompt": "Futuristic spatial computing headset...", "seed": "4829301"
}
```

### Limits por campo

| Campo | Tipo | Limit |
|---|---|---|
| `name` | string | máx 30 chars |
| `subtitle` | string | máx 60 chars |
| `category` | enum | Technology · Finance · Science · Culture · Sports · Politics |
| `rarity` | enum | Common · Rare · Epic · Legendary · Mythic · Ultimate |
| `abilities` | array | **1 a 2 itens** (name ≤25, description ≤120 chars) |
| `passive` | object | name ≤25, description ≤120 chars |
| `weakness` | string | máx 80 chars |
| `flavorText` | string | máx 15 palavras |
| `lore` | string | máx 300 chars |
| `traits` | array | **2 a 4 itens** (name ≤15, value ≤20 chars) |
| `timeline` | array | **2 a 5 itens** (event ≤60 chars) |
| `references` | array | mín 1 · máx 5 |

- **passive** — sempre ativo, característica permanente. Nunca muda.
- **abilities** (1-2) — o que a carta "faz" no argumento de batalha.

### Sistema de Effects

| Campo | Tipo | Descrição |
|---|---|---|
| `foil` | boolean | Overlay holográfico rainbow |
| `glow` | string\|null | Cor hex do brilho de borda |
| `shimmer` | boolean | Reflexo sutil que segue o mouse |
| `particles` | string\|null | `"minimal"` · `"medium"` · `"heavy"` · `"cinematic"` |
| `borderLight` | boolean | Borda animada com luz pulsante |

Defaults por raridade:

| Raridade | Prob. | foil | glow | shimmer | particles | borderLight |
|---|---|---|---|---|---|---|
| Common | 55% | false | null | false | null | false |
| Rare | 25% | false | `"#c0c0c0"` | true | `"minimal"` | false |
| Epic | 12% | false | `"#9b30ff"` | true | `"medium"` | true |
| Legendary | 6% | true | `"#ffd700"` | true | `"heavy"` | true |
| Mythic | 1.8% | true | `"#00ffff"` | true | `"cinematic"` | true |
| Ultimate | 0.2% | true | exclusivo | true | `"cinematic"` | true |

---

## Tom da IA — Humor Ácido

Escrever como comentarista inteligente e levemente cínico: respeita os fatos, aponta o absurdo. O humor é no *como*, nunca no *o quê*.

**`flavorText`** — ironia, duplo sentido ou verdade desconfortável:
> *"The future arrived. Apple priced it so you'd know your place in it."*
> *"A financial revolution, mostly used to make the already-rich slightly richer."*

**`weakness`** — pode ser afiada:
> *"Depends on people continuing to care, which history suggests is optimistic."*

**Evitar:** humor sobre pessoas pelo nome, sarcasmo sobre tragédias, ironia que invalide a importância do evento.

## Filtro de Notícias

| Rejeitar | Aceitar |
|---|---|
| Mortes, desastres, guerras | Tecnologia: lançamentos, breakthroughs, IPOs |
| Epidemias com mortalidade | Ciência: descobertas, missões espaciais |
| Crimes violentos | Cultura, Esportes: recordes, conquistas |
| Catástrofes naturais com vítimas | Economia: fusões, tendências de mercado |
| Saúde mental / suicídio | Política: eleições, acordos (sem tragédia) |

**Critério prático:** *"Essa notícia pode virar uma carta que alguém ficaria feliz de colecionar?"*

---

## Sistema de Booster Packs

Cartas **nunca aparecem diretamente no inventário**. Toda aquisição ocorre pela abertura de um Booster Pack.

O `booster_pack` **não contém** `asset_unit`s antecipadamente. As cartas são geradas no momento da abertura, permitindo ajuste de probabilidades sem recriar packs.

**Garantias suportadas:** Guaranteed Rare por pack · Guaranteed Event Card · Pity System (após N aberturas sem Legendary)

**Tipos:** Daily · Weekly · Monthly · Event · Founder · Special Collection

**Fluxo:** Bundle released → Boosters generated → User receives Booster → Open animation → Reveal cards → Cards minted (asset_unit created) → Inventory updated

---

## Pipeline de IA Semanal

```
Google Trends + NewsAPI + Reddit API (multi-source)
↓ Claude API seleciona 10 eventos mais relevantes
↓ Claude API gera metadata JSON completo por evento
↓ DALL-E 3 gera ilustração por carta
↓ S3 armazena imagens permanentemente
↓ POST /artifacts/bundles → backend cria bundle + artifacts
↓ Boosters gerados para distribuição
```

**Execução:** AWS Lambda (Python 3.11) + EventBridge Scheduler (toda segunda, 08:00 UTC)

**Custo estimado/semana (~10 cartas):** ~$0.41 (Claude API ~$0.01 + DALL-E 3 ~$0.40; Lambda/S3 desprezível)

**Por que não X/Twitter API:** Trending topics exige tier Basic ($100/mês). Google Trends + Reddit cobrem gratuitamente.

---

## Renderização de Cartas — Frontend

**Composição em camadas** (cada uma é componente React independente, animado separadamente):
```
Glass → Reflection → Foil → Particles → Frame → Illustration → Background
```

**Frente:** Illustration · Name · Subtitle · Abilities · Attributes · Rarity · Collection · Card Number  
**Verso:** Lore · Timeline · Traits · Stats · References · AI Info (prompt, seed, model)

**Three.js:** mouse tilt 3D · parallax por camada · foil shader GLSL · glow pulsante · partículas por raridade · iluminação dinâmica · idle float. Cartas Mythic/Ultimate têm shaders exclusivos. Fallback 2D se WebGL indisponível.

---

## Roadmap de Implementação

### Fase 1 — Domain Refactor
- Adicionar `metadata JSONB` na tabela `asset`, remover campo `text`
- Introduzir entidades `Universe` e `Collection`
- Atualizar APIs de bundle/criação para aceitar metadata

### Fase 2 — AI Pipeline
- Ingestão multi-source: Google Trends + NewsAPI + Reddit API
- Claude API: seleção de eventos + geração de metadata JSON
- DALL-E 3 para ilustrações, upload para S3
- AWS Lambda (Python 3.11) + EventBridge Scheduler

### Fase 3 — Card Rendering Engine (2D)
- Componente React com frente/verso e animação de flip
- Composição em camadas (CSS + Framer Motion), variantes por raridade

### Fase 4 — Three.js
- Shaders GLSL: foil, reflection, glow
- Mouse tilt + parallax por camada, partículas, shaders exclusivos Mythic/Ultimate

### Fase 5 — Booster Packs
- Entidade `booster_pack` no banco, engine de probabilidade
- Pity system com histórico de aberturas
- Animação cinematográfica de abertura (Framer Motion + Three.js)

### Fase 6 — Marketplace
- Histórico de preços com gráficos (Recharts já disponível)
- Filtros por raridade, categoria, coleção; volume e analytics

### Fase 7 — Collections, Achievements, Profile
- Coleções com barra de progresso, achievements desbloqueáveis
- Perfil público com inventário, conquistas e estatísticas

### Fase 8 — Automação
- Pipeline Lambda semanal 100% sem intervenção manual
- Logs CloudWatch + alerta SES em caso de falha

---

## ADRs — Architecture Decision Records

### ADR-001: JSONB para Metadata de Cartas (Fase 1)
**Decisão:** `metadata JSONB` na tabela `asset`, removendo `text`. Backend deserializa para `Map<String, Object>` ou record `CardMetadata`.  
✅ Zero migrações futuras · Pipeline gera JSON diretamente · Frontend renderiza sem mudança de API  
⚠️ Queries por atributo precisam de índices JSONB · Validação de schema é responsabilidade da aplicação

### ADR-002: Claude API + DALL-E 3 (Fase 2)
**Decisão:** Claude API (Anthropic SDK Python) para metadata JSON estruturado. DALL-E 3 (OpenAI SDK) para ilustrações. Imagens no S3.  
✅ Claude tem alta precisão para JSON · Custo baixo ~$0.41/semana · Pipeline desacoplada do backend  
⚠️ Qualidade de imagem depende do prompt engineering · APIs externas precisam de retry

### ADR-003: Frontend Owns Artifact Rendering (Fase 3)
**Decisão:** Backend entrega apenas JSON de metadata. Cada camada é componente React em `position: absolute`. Raridade determina visual no frontend.  
✅ Backend nunca conhece detalhes visuais · Fácil adicionar raridades sem mudar backend  
⚠️ Lógica de raridade duplicada se houver outros clientes

### ADR-004: Three.js em Hook Dedicado (Fase 4)
**Decisão:** Toda lógica Three.js em `useArtifactRenderer(canvasRef, metadata, rarity)`. Shaders GLSL em arquivos `.glsl` separados importados via Vite. Hook faz dispose no unmount.  
✅ Componentes React declarativos · Sem memory leak · Shaders testáveis independentemente  
⚠️ Performance mobile precisa de fallback 2D

### ADR-005: Cartas Geradas na Abertura do Pack (Fase 5)
**Decisão:** `booster_pack` sem `asset_unit`s. Ao abrir, engine de probabilidade seleciona assets, cria units e atualiza inventário em transação única.  
✅ Probabilidades ajustáveis sem impacto em packs existentes · Pity system via histórico  
⚠️ Se `total_supply` esgotar antes da abertura, precisar de fallback

### ADR-006: Analytics do Marketplace Como Camada Separada (Fase 6)
**Decisão:** Endpoints de analytics separados (`/asset-listings/stats`, `/price-history`) com queries de agregação sob demanda. Índice em `asset_price_history(asset_unity_id, created_at)`.  
✅ Endpoints de listagem permanecem rápidos · Sem views materializadas agora  
⚠️ Queries de analytics podem ser lentas com volume alto

### ADR-007: Achievements no UseCase Layer (Fase 7)
**Decisão:** Cada UseCase chama `AchievementService.checkAndUnlock(accountId, TriggerType)` ao final, dentro da mesma transação. Sem event bus.  
✅ Garantidos em cenários de concorrência · Sem infraestrutura adicional · Fácil debugar  
⚠️ UseCases levemente acoplados ao sistema de achievements

### ADR-008: Pipeline como AWS Lambda + EventBridge (Fase 8)
**Decisão:** Lambda (Python 3.11) + EventBridge `cron(0 8 ? * MON *)`. Lê secrets do SSM, busca notícias multi-source, gera conteúdo com IA, chama `POST /artifacts/bundles` via `X-Admin-Token`. Logs no CloudWatch; falhas disparam SES.  
✅ Serverless · Zero manutenção · Falha na pipeline não afeta o backend  
⚠️ Lambda timeout 15 min — suficiente para 10 cartas; `pytrends` não é API oficial

---

## Stack Tecnológico

**Backend:** Java 17 + Spring Boot 3.3.2 · Maven · JDBC puro (sem ORM/JPA) · HikariCP · JWT via jjwt 0.12.5 · AWS SDK 2.25.28 (SES) · BCrypt · Spring Boot DevTools (hot reload)

**Frontend** (`frontend/assetstore/`): React 19 + TypeScript 5.9 · Vite · Tailwind CSS 4 + Framer Motion · React Router 7 · React Query 5 · Recharts · Three.js (Fase 4)

**Pipeline** (`pipeline/`): Python 3.11+ · `anthropic` SDK · `openai` SDK · `boto3` · `pytrends` · `praw`

**Banco:** PostgreSQL (prod) · H2 em `MODE=PostgreSQL` (testes/local) · Schema em `src/main/resources/schema.sql`

---

## Arquitetura do Backend

Pacote raiz: `br.com.ale`. Arquitetura em camadas inspirada em Clean Architecture.

```
application/   → Controllers REST, UseCases, Commands (account, auth, marketplace, claim)
domain/        → Entidades, enums, exceções (account, asset, auth, transaction)
dao/           → Data Access Objects com queries SQL diretas via JDBC
service/       → Serviços de negócio (auth, asset, crypto, email, marketplace)
dto/           → Request/Response DTOs (Java records)
infrastructure/ → ConnectionProvider, HikariCP, SchemaInitializer, AuthCookieService
```

**Padrão Command/UseCase:** cada operação de escrita tem um `Command` (dados imutáveis) e um `UseCase` (orquestra Services e DAOs). Ex: `CreateAccountCommand` → `CreateAccountUseCase`.

---

## Banco de Dados

Schema completo em `src/main/resources/schema.sql` (idêntico ao de testes em `src/test/resources/schema.sql`).

| Tabela | Descrição |
|---|---|
| `client` | Usuários (login local e Google OAuth) |
| `account` | Contas (1 por cliente, com chave pública RSA) |
| `credential` | Senhas hash separadas da tabela client |
| `transactions` | Transações com assinatura RSA |
| `email_verification` | Tokens de verificação e reset de senha |
| `artifact` | Tipo de artifact colecionável (`text`, `total_supply`, `created_at`) |
| `artifact_unit` | Instância individual de um artifact (owner, status: AVAILABLE/IN_MARKET/RESERVED/TRANSFERRING) |
| `artifact_listing` | Ofertas de venda no marketplace (ACTIVE/SOLD/CANCELED) |
| `artifact_price_history` | Histórico de preços por listing e por unit |
| `artifact_bundle` | Pacotes semanais de artifacts |
| `artifact_bundle_item` | Relacionamento bundle ↔ artifact (1 artifact por bundle, UNIQUE) |
| `artifact_transfer` | Histórico de transferências de artifact_unit (ownership chain) |
| `universe` | Universo temático das coleções (Fase 1) |
| `collection` | Coleção dentro de um universo (Fase 1) |
| `booster_pack` | Packs fechados aguardando abertura (Fase 5) |
| `pack_opening_history` | Histórico de aberturas para pity system (Fase 5) |
| `user_collection_progress` | Progresso do usuário por coleção (Fase 7) |
| `achievement` | Definições de conquistas (Fase 7) |
| `user_achievement` | Conquistas desbloqueadas por usuário (Fase 7) |

---

## Sistema de Artifacts

### Conceitos Fundamentais

**`artifact`** — o "tipo" do artifact: `text`, `total_supply`, `created_at`. Único globalmente. No futuro conterá `metadata JSONB` com raridade, ilustração, atributos etc.

**`artifact_unit`** — instância individual e transferível de um `artifact`. É o que o usuário realmente possui, com seu próprio dono (`owner_account_id`), status e, no futuro, variante visual (foil, holo etc.).

**Analogia:** `artifact` é a tiragem ("AI Titan, 10 cópias"). Cada `artifact_unit` é uma das 10 cópias físicas — cada uma com identidade própria, histórico de preços e cadeia de ownership.

### Ciclo de Vida

**1. Criação** — `POST /artifacts/bundles` (com `X-Admin-Token`) cria `artifact_bundle` + `artifact`s em transação única. Nenhum `artifact_unit` é criado aqui.

**2. Claim** — `POST /artifacts/claim` decrementa `total_supply` atomicamente (`UPDATE ... WHERE total_supply >= 1`) e insere `artifact_unit` com `status = AVAILABLE`. Cooldown por conta via `account.next_free_asset_at`.

**3. Listar no marketplace** — `POST /artifact-offers` atualiza `artifact_unit.status = IN_MARKET` atomicamente (`WHERE status = 'AVAILABLE' AND owner = ?`) e insere `artifact_listing`.

**4. Compra** — `POST /artifact-listings/{id}/purchase` transfere saldo, troca `owner_account_id` atomicamente (`WHERE status = 'IN_MARKET' AND owner = seller`), marca listing como `SOLD`, registra `artifact_transfer` e `artifact_price_history`.

**5. Cancelar** — `POST /artifact-offers/cancel` reverte listing para `CANCELED` e unit para `AVAILABLE`.

### Invariantes

1. `artifact.total_supply` nunca fica negativo — CHECK constraint + `WHERE total_supply >= 1` no UPDATE
2. `artifact_bundle_item.artifact_id` é UNIQUE — cada artifact pertence a exatamente um bundle
3. Transições de status são atômicas via UPDATE condicional — eliminam race conditions
4. Usuário não pode comprar a própria listing
5. Preço de listing deve ser > 0, máx 2 casas decimais
6. Marketplace público passa `accountId = -1` quando não autenticado (sem exclusão de próprias listings)

### Endpoints de Artifacts

| Endpoint | Acesso | Descrição |
|---|---|---|
| `GET /artifacts/bundles` | Público | Lista bundles paginados |
| `GET /artifacts/bundles/{id}/items` | Público | Artifacts de um bundle |
| `GET /artifacts/{id}` | Público | Artifact por ID (tipo, não instância) |
| `GET /artifact-listings` | Público | Listings ativos (exclui próprias se autenticado) |
| `GET /artifact-listings/me` | Privado | Próprias listings ativas |
| `GET /artifact-units/me` | Privado | Próprias units AVAILABLE |
| `GET /artifact-units/{id}` | Público | Instância específica: nome, owner, status, price history, ownership chain |
| `GET /artifact-transfers` | Público | Feed público de todas as transferências (paginado, newest first) |
| `GET /artifacts/{id}/price-history` | Público | Histórico de preços por unit |

---

## Configuração e Variáveis de Ambiente

Secrets em SSM Parameter Store `/banksimulator/*`. Script `fetch-env.py` os busca no startup e grava em `/etc/app.env`.

**Porta do servidor:** 5000 (não 8080)

| Variável | Uso |
|---|---|
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | PostgreSQL |
| `JWT_SECRET_KEY`, `JWT_EXPIRATION_TIME` | JWT HMAC-SHA |
| `ADMIN_TRIGGER_TOKEN` | Header `X-Admin-Token` para `/admin/*` |
| `GOOGLE_CLIENT_ID` | Google OAuth |
| `AWS_SES_FROM` | Email remetente |
| `APP_BASE_URL` | URL base (usado em emails) |
| `/banksimulator/anthropic_api_key` | Claude API (Fase 2+) |
| `/banksimulator/openai_api_key` | DALL-E 3 (Fase 2+) |
| `/banksimulator/newsapi_key` | NewsAPI (Fase 2+) |
| `/banksimulator/reddit_client_id/secret` | Reddit API (Fase 2+) |

**Cookie por profile:**

| Propriedade | Produção | Local |
|---|---|---|
| `auth.cookie.domain` | `.alessandro-bezerra.me` | `""` |
| `auth.cookie.secure` | `true` | `false` |
| `auth.cookie.same-site` | `None` | `Lax` |

---

## Autenticação

**Local** — email + senha, requer verificação de email antes do primeiro login.  
**Google OAuth** — token ID validado via `GoogleTokenVerifier` (HTTP para `googleapis.com/tokeninfo`).

JWT armazenado em **cookie HttpOnly** + retornado no body. Frontend usa **sessionStorage** (isolado por aba).

Endpoints protegidos aceitam token via: `Authorization: Bearer <token>` → query param `?token=` → cookie `AUTH_TOKEN`.

Para endpoints públicos com token opcional: `AuthCookieService.extractTokenOrNull()` (retorna `null` sem lançar 401).

`GET /auth/session` — retorna `{ "token": "<jwt>" }` lendo o cookie. Usado pelo `Router.tsx` ao montar para migrar sessão do cookie para sessionStorage.

Endpoints admin: header `X-Admin-Token` separado.

---

## Segurança Criptográfica

Cada conta tem par **RSA 2048 bits**. Chave pública em `account.public_key`. Chave privada via `PrivateKeyStorage`:
- `FilePrivateKeyStorage` — produção: `keys/account-{id}/private.key` relativo ao `WorkingDirectory` do systemd
- `InMemoryPrivateKeyStorage` — testes e profile `local`

Toda transação é assinada com a chave privada e verificada com a pública. Assinatura em `transactions.signature`.

---

## Email

`EmailService` é interface com duas implementações:
- `SesEmailService` — profile `!local`: envia via AWS SES
- `LogEmailService` — profile `local`: loga no console

---

## Frontend

Localizado em `frontend/assetstore/`. URL da API via `VITE_API_URL` (`.env` = produção, `.env.local` = localhost:5000).

| Rota | Acesso |
|---|---|
| `/` | Público — Landing page |
| `/login`, `/register`, `/forgot-password`, `/reset-password` | Público |
| `/market` | Público (ações requerem login) |
| `/reward` | Público (claim requer login) |
| `/logs` | Público — Feed de todas as transferências (paginado, newest first) |
| `/artifact/:id` | Público — Instância de artifact: nome, owner, status, price history, ownership chain |
| `/inventory` | Privado (AuthRequiredModal se não autenticado) |

`AuthRequiredModal` aparece quando não autenticado tenta ação protegida. Botões "Cancel" e "Create account" (→ `/register`). Sem redirect automático para `/login`.

---

## Como Rodar Localmente

```bash
# Backend
mvn spring-boot:run -Dspring-boot.run.profiles=local
# H2 in-memory, LogEmailService, InMemoryPrivateKeyStorage, CORS liberado, cookie sem Secure/Domain

# Frontend
cd frontend/assetstore && npm install && npm run dev   # http://localhost:5173
```

Google OAuth local: adicionar `http://localhost` e `http://localhost:5173` em **Authorized JavaScript origins** no Google Cloud Console.

---

## Testes

```bash
mvn test
```

Testes de integração com H2. Schema de teste idêntico ao de produção.

---

## Infraestrutura AWS

| Recurso | Detalhes |
|---|---|
| EC2 t3.micro | IP: `18.226.192.204`, SSH: `ssh -i ~/.ssh/banksimulator ec2-user@18.226.192.204` |
| RDS db.t4g.micro | `banksimulator-db.cbaeaa00azz5.us-east-2.rds.amazonaws.com` |
| Nginx | Reverse proxy: 443/80 → localhost:5000 |
| SSM Parameter Store | `/banksimulator/*` — todos os segredos |
| S3 | `banksimulator-frontend-356892335394` — frontend + imagens de cartas |
| CloudFront | `E2P13GEXYNJRCP` — CDN do frontend |
| Route53 | `api.` → EC2, `app.` → CloudFront |

**Custo estimado:** ~$27/mês (infra) + ~$1.65/mês (pipeline IA)

---

## CI/CD — GitHub Actions

Workflow `.github/workflows/deploy.yml`. Dispara em push para branch `prod`.

- **deploy-backend:** Maven build → SCP JAR → SSH restart systemd → health check
- **deploy-frontend:** `npm ci` + build → S3 sync → CloudFront invalidation

---

## Git — Branches

| Branch | Finalidade |
|---|---|
| `prod` | Deploy automático no AWS |
| `dev` | Testes locais, sem CI/CD |
| `master` | Mantido em sincronia com `prod` |

---

## Systemd — Produção

Arquivo: `/etc/systemd/system/banksimulator.service`

- `WorkingDirectory=/opt/banksimulator` — necessário para `FilePrivateKeyStorage`
- `ExecStartPre=+/usr/bin/python3 /opt/banksimulator/fetch-env.py us-east-2` — busca secrets do SSM
- `EnvironmentFile=/etc/app.env`

---

## Endpoints Admin

```
POST /artifacts/bundles      — cria bundle de artifacts (X-Admin-Token)
POST /admin/accounts/deposit — adiciona saldo a uma conta (X-Admin-Token)
```

---

## Bugs Corrigidos

| Bug | Arquivo | Descrição |
|---|---|---|
| JWT expirado aceito como válido | `JwtService.java:59` | `isTokenExpired()` sem negação |
| `AccessDeniedException: /keys` | `FilePrivateKeyStorage.java` | Path relativo; corrigido com `WorkingDirectory` no systemd |
| AWS SES trava startup local | `EmailService.java` | `SesClient.create()` no construtor sem credenciais; resolvido com `LogEmailService` |
| Google login 500 localmente | `application-local.properties` | `google.client-id` com valor dummy |
| 401 em `/accounts/me` local | `AuthCookieService.java` | Cookie `Secure=true` não funciona em HTTP |
| Claim 500 — syntax H2 | `AccountDAO.java` | H2 não suporta `INTERVAL '2 minutes'` |
| Claim 500 — RETURNING | `AccountDAO.java` | H2 não suporta `UPDATE ... RETURNING` |
| 401 no marketplace público | `AuthCookieService.java` | `extractToken()` lançava 401; resolvido com `extractTokenOrNull()` e `accountId = -1` |
| 500 em vez de 404 para artifact unit inexistente | `ArtifactUnitService.java` | `RuntimeException("not found")` era capturada pelo `catch (Exception e)` externo e re-embrulhada como "Service error"; corrigido com `ArtifactUnitNotFoundException extends BusinessRuleException` + re-throw explícito + handler em `ApiExceptionHandler` → HTTP 404 |

---

## Pontos de Atenção

- `SecurityConfig` permite **todas as requisições** — autenticação é feita manualmente nos use cases.
- Sem rate limiting implementado.
- Sem logs estruturados / observabilidade.
- Chaves RSA em `/opt/banksimulator/keys/`. Se o EC2 for recriado, chaves existentes são perdidas.
- 82 testes · 18 suites · `mvn test` retorna BUILD FAILURE por problema no fork do Surefire JVM (pré-existente, não relacionado a falhas de teste — verificar relatórios XML em `target/surefire-reports/`).
- Three.js ainda não está no projeto — Fase 4.
- Pipeline de IA ainda não existe — Fase 2.

---

## Ferramentas de Desenvolvimento

### seed-local.sh

Script para popular o banco H2 local com dados de teste. Cria accounts, artifacts, units, listings e transferências para facilitar o desenvolvimento sem precisar passar pelo fluxo manual completo.

```bash
./seed-local.sh
```

Requer o backend rodando em `localhost:5000` com profile `local`.
