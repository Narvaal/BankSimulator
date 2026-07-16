# 🃏 RareLines

**RareLines** is a premium digital trading card platform where every card is born from a real-world event. Each week, a fully automated AI pipeline scans trending news, selects the most compelling stories, writes card metadata with a dry, sharp editorial voice, art-directs and generates a unique illustration — and publishes the cards for collectors to claim, trade and collect.

**Live:** [app.alessandro-bezerra.me](https://app.alessandro-bezerra.me) · **API:** [api.alessandro-bezerra.me](https://api.alessandro-bezerra.me)

## 🚀 How It Works

```
Google Trends + NewsAPI + Reddit  →  multi-source news ingestion
↓  Claude (AWS Bedrock) selects the 10 most card-worthy events of the week
↓  Claude writes each card's metadata (name, rarity, attributes, abilities, lore…)
↓  Claude produces a structured art direction (cinematic moment → camera → light → medium)
↓  Python assembles the final image prompt — style is the last layer, never the first
↓  Stability AI SD3 Ultra renders the illustration
↓  Images land on S3 with immutable seed-based URLs
↓  Bundle is registered in the backend — cards become claimable and tradeable
```

The pipeline runs unattended every Monday at 08:00 UTC (AWS Lambda + EventBridge). Estimated AI cost: **~$0.65/week** for ~10 cards.

## 🏗️ Architecture — C4 Container Diagram (C2)

```mermaid
flowchart TB
    collector(["👤 Collector\n(web browser)"])

    subgraph rarelines["RareLines Platform — AWS"]
        cdn["CloudFront CDN\napp.alessandro-bezerra.me"]
        spa["Single-Page App\nReact 19 · TypeScript 5.9 · Vite\nTailwind 4 · Framer Motion · React Query 5"]
        api["Backend API\nJava 17 · Spring Boot 3.3 · pure JDBC\nEC2 + Nginx · api.alessandro-bezerra.me"]
        db[("PostgreSQL\nAmazon RDS")]
        s3[("S3 Bucket\nfrontend build + cards/*.png")]
        pipeline["AI Pipeline\nPython 3.11 · AWS Lambda\nEventBridge cron — Mon 08:00 UTC"]
        ssm["SSM Parameter Store\nsecrets"]
    end

    subgraph ext["External Systems"]
        google["Google OAuth"]
        ses["AWS SES\ntransactional email"]
        bedrock["AWS Bedrock\nClaude"]
        stability["Stability AI\nSD3 Ultra"]
        news["Google Trends\nNewsAPI · Reddit"]
    end

    collector -->|"HTTPS"| cdn
    cdn -->|"serves static build\n+ card images"| s3
    collector -->|"runs"| spa
    spa -->|"HTTPS / JSON\nJWT HttpOnly cookie"| api
    api -->|"JDBC / HikariCP"| db
    api -->|"verify ID token"| google
    api -->|"verification / reset emails"| ses
    api -->|"secrets at startup"| ssm
    pipeline -->|"fetch trending events"| news
    pipeline -->|"event selection · metadata\n· art direction"| bedrock
    pipeline -->|"generate illustrations\nREST"| stability
    pipeline -->|"upload cards/{slug}-{seed}.png"| s3
    pipeline -->|"POST /artifacts/bundles\nX-Admin-Token"| api
    pipeline -->|"read secrets\nfailure alerts via SES"| ssm
```

## 🗄️ Data Model — ER Diagram

Faithful to [`src/main/resources/schema.sql`](src/main/resources/schema.sql). An `artifact` is the card *type* (the print run); an `artifact_unit` is one physical copy a user actually owns, with its own ownership chain and price history.

```mermaid
flowchart LR
    subgraph identity["Identity & Auth"]
        client["client\nemail · provider · picture"]
        credential["credential\npassword_hash"]
        email_verification["email_verification\ntoken · type · expires_at"]
    end

    subgraph banking["Banking"]
        account["account\naccount_number · balance\npublic_key (RSA)"]
        transactions["transactions\namount · signature · status"]
    end

    subgraph tcg["Artifacts — Trading Cards"]
        artifact["artifact\nmetadata JSONB · total_supply"]
        artifact_bundle["artifact_bundle\nidentifier (weekly)"]
        artifact_bundle_item["artifact_bundle_item"]
        artifact_unit["artifact_unit\nstatus: AVAILABLE · IN_MARKET\nRESERVED · TRANSFERRING"]
        artifact_listing["artifact_listing\nprice · status"]
        artifact_transfer["artifact_transfer"]
        artifact_price_history["artifact_price_history\nold_price · new_price · reason"]
    end

    credential -->|"client_id — 1:1"| client
    email_verification -->|"client_id — N:1"| client
    account -->|"client_id — N:1"| client
    transactions -.->|"from / to account\n(no FK, by number + id)"| account
    artifact_bundle_item -->|"bundle_id — N:1"| artifact_bundle
    artifact_bundle_item -->|"artifact_id — 1:1 UNIQUE"| artifact
    artifact_unit -->|"artifact_id — N:1"| artifact
    artifact_unit -->|"owner_account_id — N:1"| account
    artifact_listing -->|"artifact_unit_id — N:1"| artifact_unit
    artifact_listing -->|"seller_account_id — N:1"| account
    artifact_transfer -->|"artifact_unit_id — N:1"| artifact_unit
    artifact_transfer -->|"from / to_account_id"| account
    artifact_price_history -->|"artifact_listing_id — N:1"| artifact_listing
    artifact_price_history -->|"artifact_unit_id — N:1"| artifact_unit
    artifact_price_history -->|"changed_by_account_id"| account
```

## ✨ Features

- 🔐 **Authentication** — local (email + password with mandatory email verification) and Google OAuth; JWT delivered as an HttpOnly cookie
- 🔑 **RSA cryptography per account** — every transaction is signed with the account's 2048-bit private key and verified against the public key stored in the database
- 🃏 **Weekly AI-generated cards** — six rarity tiers (Common → Ultimate) with data-driven visual effects (foil, glow, shimmer, particles)
- 🎨 **Art Direction v2** — Claude reasons cinematically (moment → protagonist → camera → composition → light → medium) before a single prompt word is written; Python assembles the final prompt with style as the last layer
- 🖼️ **"Museum-label" card rendering** — full-bleed artwork with glassmorphism UI floating on top; overflow-proof by design (`line-clamp` everywhere)
- 🛒 **Marketplace** — public listings with search, price range, sorting and rarity filters; atomic status transitions eliminate race conditions
- 🎁 **Free claim with cooldown** — atomic supply decrement (`WHERE total_supply >= 1`), no overselling
- 📜 **Full provenance** — every unit carries its ownership chain and price history
- 👤 **Public profiles & user search** — read-only inventories, account lookup by name

## 🛠️ Tech Stack

| Layer | Technologies |
|---|---|
| **Backend** | Java 17 · Spring Boot 3.3 · pure JDBC (no ORM) · HikariCP · jjwt · BCrypt |
| **Frontend** | React 19 · TypeScript 5.9 · Vite · Tailwind CSS 4 · Framer Motion · React Query 5 · Recharts |
| **AI Pipeline** | Python 3.11 · AWS Bedrock (Claude) · Stability AI SD3 Ultra · pytrends · praw |
| **Database** | PostgreSQL (prod) · H2 in `MODE=PostgreSQL` (tests/local) |
| **Quality** | JUnit + JaCoCo (**90% line-coverage gate**) · Vitest + React Testing Library · ESLint · Spotless (google-java-format) · Husky hooks · commitlint (Conventional Commits) |
| **Infra** | EC2 · RDS · S3 · CloudFront · Route53 · Lambda · EventBridge · SES · SSM Parameter Store |
| **CI/CD** | GitHub Actions — push to `prod` deploys backend (SSH) and frontend (S3 sync + CloudFront invalidation) |

Estimated running cost: **~$27/month** infra + **~$2.60/month** AI generation.

## 💻 Running Locally

```bash
# Backend — H2 in-memory, emails logged to console, CORS open
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Frontend — http://localhost:5173
cd frontend/assetstore && npm install && npm run dev

# Seed the local database with test data (backend must be running)
./seed-local.sh

# Backend tests (integration tests on H2, schema identical to prod)
mvn test

# Frontend tests (Vitest + React Testing Library) and lint
cd frontend/assetstore && npm run test && npm run lint

# Java formatting (Spotless + google-java-format) — check / auto-fix
mvn spotless:check
mvn spotless:apply

# Backend coverage report + 90% line-coverage gate (JaCoCo)
mvn test jacoco:report jacoco:check   # report: target/site/jacoco/index.html

# Frontend coverage report (no gate)
cd frontend/assetstore && npm run test:coverage

# Git hooks (Husky) — installed by npm install at the repo root
npm install
```

A Husky pre-commit hook runs the checks for whichever area the commit touches: Spotless + backend tests + the JaCoCo 90% coverage gate for Java changes, ESLint + Vitest for frontend changes. Docs-only commits skip everything. A commit-msg hook (commitlint) rejects messages that don't follow [Conventional Commits](https://www.conventionalcommits.org/).

## 🗺️ Roadmap

| Phase | Scope | Status |
|---|---|---|
| 1 | Domain refactor — `metadata JSONB` | ✅ Complete |
| 2 | AI pipeline — Bedrock + Stability, Lambda + EventBridge | ✅ Complete |
| 3 | Card rendering engine (2D) | 🔶 Partial — flip animation pending |
| 4 | Three.js — shaders, tilt, particles | ⏳ Planned |
| 5 | Booster packs — probability engine, pity system | ⏳ Planned |
| 6 | Marketplace analytics — price charts, volume | 🔶 Partial |
| 7 | Collections, achievements, profiles | 🔶 Partial |
| 8 | Full automation | 🔶 Partial — Lambda deploy still manual |

## 📄 License

This project is licensed under the MIT License.

You are free to use, modify, and distribute this project, as long as proper credit is given.

## 👨‍💻 Author

Developed by **Alessandro Bezerra**
