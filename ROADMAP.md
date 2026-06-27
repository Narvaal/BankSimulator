# RareLines — Próximos Passos

## Estado atual (2026-06-27)

O backend está funcional e em produção com a nomenclatura correta (`Artifact`). Já existe:

- Autenticação (local + Google OAuth)
- Sistema de artifacts com claim, venda e compra
- Marketplace público (sem login), com exclusão correta das próprias listings
- Inventário privado com AuthRequiredModal
- Balance sempre atualizado (refetch on focus + polling 30s + invalidate após ações)
- Pipeline de bundles via endpoint admin (`POST /artifacts/bundles`)
- Frontend React com Marketplace e Reward públicos
- Schema do banco com tabelas `artifact_*` em produção
- CLAUDE.md com visão completa do produto, ADRs, RFCs e decisões tomadas

O que **não existe** ainda: metadata JSONB nas cartas, visual rico, booster packs, geração por IA, Three.js, coleções ou achievements.

---

## Para onde vamos

Transformar o projeto em uma plataforma de **cartas colecionáveis digitais** geradas semanalmente por IA, com um sistema de batalha narrativo julgado por IA.

---

## As 8 Fases

```
Fase 1 → Domain Refactor      — migrar schema para metadata JSONB ✦ PRÓXIMA
Fase 2 → AI Pipeline          — gerar cartas automaticamente com IA
Fase 3 → Card Rendering 2D    — exibir as cartas visualmente no frontend
Fase 4 → Three.js             — cartas 3D com física, shaders e animações
Fase 5 → Booster Packs        — sistema de abertura de packs cinematográfico
Fase 6 → Marketplace          — histórico, filtros e analytics de preços
Fase 7 → Collections          — coleções, achievements e perfil do usuário
Fase 8 → Automação            — pipeline 100% automática, sem intervenção manual
```

---

## ✦ Fase 1 — O que fazer amanhã

### O problema atual

O campo `text` da tabela `artifact` guarda apenas uma string. Não tem como representar raridade, atributos narrativos, ilustração, habilidades, effects ou qualquer outro dado da carta.

### A solução

Substituir `text` por `metadata JSONB`. Cada artifact passa a guardar o documento completo da carta conforme definido no `CLAUDE.md`.

---

### Checklist da Fase 1

#### Backend

- [ ] Migration: `ALTER TABLE artifact ADD COLUMN metadata JSONB NOT NULL DEFAULT '{}'`
- [ ] Migration: `ALTER TABLE artifact DROP COLUMN text`
- [ ] Atualizar `Artifact.java`: trocar `String text` por `Map<String, Object> metadata`
- [ ] Atualizar `ArtifactDAO.java`: insert/select com `CAST(? AS jsonb)`
- [ ] Atualizar `CreateArtifactRequest.java`: trocar `text` por `metadata`
- [ ] Atualizar `ArtifactBundleService.java`: adaptar criação para novo campo
- [ ] Atualizar endpoints de leitura: retornar `metadata` em vez de `text`
- [ ] Adicionar índices JSONB: `metadata->>'rarity'` e `metadata->>'category'`
- [ ] Atualizar `schema.sql` e `src/test/resources/schema.sql`
- [ ] Validação mínima no backend: `name` e `rarity` obrigatórios no JSON

#### Frontend

- [ ] `Marketplace.tsx`: exibir `metadata.name` em vez de `artifactText`
- [ ] `Reward.tsx`: exibir `metadata.name` em vez de `text`
- [ ] `Home.tsx` (inventário): exibir `metadata.name` e `metadata.rarity`
- [ ] Criar badge de raridade nos cards existentes (cor por raridade, sem visual completo ainda)

#### Teste manual

- [ ] `POST /artifacts/bundles` com o novo formato de metadata
- [ ] Marketplace lista cartas com nome correto
- [ ] Reward exibe cartas com nome correto
- [ ] Inventário exibe cartas com nome e raridade

---

### Novo formato do endpoint admin após Fase 1

```json
POST /artifacts/bundles
X-Admin-Token: ...

{
  "identifier": "weekly-2026-W27",
  "assets": [
    {
      "totalSupply": 3,
      "metadata": {
        "name": "Apple Vision Pro",
        "subtitle": "Apple enters the spatial computing era",
        "category": "Technology",
        "rarity": "Legendary",
        "effects": {
          "foil": true,
          "glow": "#ffd700",
          "shimmer": true,
          "particles": "heavy",
          "borderLight": true
        },
        "illustration": "",
        "background": "",
        "attributes": {
          "influence": 91, "innovation": 95,
          "controversy": 48, "longevity": 72, "reach": 88
        },
        "abilities": [
          { "name": "Closed Ecosystem", "description": "Commands loyalty through exclusivity." }
        ],
        "passive": {
          "name": "Silicon Monopoly",
          "description": "Arguments backed by revenue above $1T gain extra weight."
        },
        "weakness": "Premium pricing limits global adoption",
        "flavorText": "The future arrived. Apple priced it so you'd know your place in it.",
        "lore": "When Apple unveiled its spatial computing headset in 2024...",
        "traits": [
          { "name": "Era", "value": "AI Age" },
          { "name": "Origin", "value": "United States" }
        ],
        "timeline": [
          { "date": "2023-06-05", "event": "Announced at WWDC" },
          { "date": "2024-02-02", "event": "Released in the United States" }
        ],
        "references": ["https://www.apple.com/apple-vision-pro/"],
        "collection": "Tech Giants 2024",
        "cardNumber": "042",
        "releaseDate": "2024-06-03",
        "artist": "RareLines AI",
        "model": "dall-e-3",
        "prompt": "",
        "seed": ""
      }
    }
  ]
}
```

---

## Fase 3 — Logo após a Fase 1

Renderizar cartas visualmente com o metadata novo. Vale fazer antes da Fase 2 porque dá para criar cartas manualmente para testar o visual enquanto a pipeline de IA não está pronta.

**O que vai mudar:**
- Criar componente `<ArtifactCard>` com front e back
- Frente: illustration · name · subtitle · rarity badge · attributes bar · ability · passive · weakness · flavorText
- Verso: lore · timeline · traits · references · AI metadata (prompt, seed, model)
- Efeitos visuais CSS/Framer Motion baseados no campo `effects`
- Badges de raridade com cor correta em todo o app

---

## Fase 2 — Quando tiver as API keys

| Item | Status |
|---|---|
| Conta na Anthropic (Claude API) | Verificar |
| Conta na OpenAI (DALL-E 3) | Verificar |
| Conta na NewsAPI | Gratuito para começar |
| Bucket S3 para imagens | Já existe — usar pasta `/cards/` |
| Secrets no SSM | Adicionar: `anthropic_api_key`, `openai_api_key`, `newsapi_key` |

---

## Decisões já tomadas (não precisam ser rediscutidas)

| Decisão | Escolha |
|---|---|
| Formato do metadata | JSONB com todos os campos obrigatórios |
| Geração de conteúdo | Claude API |
| Geração de imagens | DALL-E 3 |
| Nomenclatura do domínio | `Artifact` (não Asset) |
| Effects visuais | Camadas independentes: foil, glow, shimmer, particles, borderLight |
| Atributos da carta | influence · innovation · controversy · longevity · reach (0-100, contexto para IA) |
| Habilidades | 1-2 abilities + 1 passive + 1 weakness (campos obrigatórios com limits) |
| Tom da IA | Humor ácido e cínico no flavorText, weakness e descriptions |
| Filtro de notícias | Rejeitar tragédias, mortes, guerras, desastres |
| Renderização | Frontend owns rendering, backend entrega só JSON |
| Three.js | Isolado em hook dedicado, sem React Three Fiber |
| Cartas no pack | Geradas na abertura, não pré-determinadas |
| Achievements | Chamada síncrona no UseCase, sem event bus |
| Pipeline | Script Python externo, não Spring Scheduler |

---

## Referência rápida

- **Documentação técnica completa:** `CLAUDE.md`
- **Schema atual do banco:** `src/main/resources/schema.sql`
- **Endpoints:** `requests.http`
- **Deploy:** push para branch `prod` → CI/CD automático
- **Produção:** `https://app.alessandro-bezerra.me`
- **API produção:** `https://api.alessandro-bezerra.me`
