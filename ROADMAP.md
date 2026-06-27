# RareLines — Próximos Passos

## Onde estamos hoje

O backend está funcional e em produção. Já existe:

- Autenticação (local + Google OAuth)
- Sistema de assets com claim, venda e compra
- Marketplace com histórico de preços
- Inventário por usuário
- Pipeline de bundles via endpoint admin
- Frontend React com Marketplace e Reward públicos

O que **não existe** ainda: cartas com visual rico, booster packs, geração por IA, Three.js, coleções ou achievements.

---

## Para onde vamos

Transformar o projeto em uma plataforma de **cartas colecionáveis digitais** geradas semanalmente por IA.

Toda semana, uma pipeline automatizada:
1. Busca notícias relevantes do mundo
2. Usa Claude para criar cartas com nome, atributos, habilidades e texto temático
3. Usa DALL-E para gerar a ilustração de cada carta
4. Publica automaticamente no sistema

Os usuários abrem **Booster Packs** para receber cartas, colecionam, vendem no marketplace e completam coleções.

---

## As 8 Fases

```
Fase 1 → Domain Refactor      — mudar como os assets são modelados no banco
Fase 2 → AI Pipeline          — gerar cartas automaticamente com IA
Fase 3 → Card Rendering 2D    — exibir as cartas visualmente no frontend
Fase 4 → Three.js             — cartas 3D com física, shaders e animações
Fase 5 → Booster Packs        — sistema de abertura de packs cinematográfico
Fase 6 → Marketplace          — histórico, filtros e analytics de preços
Fase 7 → Collections          — coleções, achievements e perfil do usuário
Fase 8 → Automação            — pipeline 100% automática, sem intervenção manual
```

---

## Fase 1 em detalhe — O que muda agora

### O problema atual

O campo `text` da tabela `asset` guarda apenas uma string simples. Não há como representar raridade, ataque, defesa, ilustração, habilidade ou qualquer outro atributo de uma carta.

### A solução

Substituir o campo `text` por um campo `metadata` do tipo **JSONB**. Em vez de uma string, cada asset guarda um documento JSON com todos os atributos da carta:

```json
{
  "name": "AI Titan",
  "subtitle": "NVIDIA reaches 6 trillion dollars",
  "rarity": "Legendary",
  "category": "Technology",
  "attack": 98,
  "defense": 84,
  "ability": {
    "name": "CUDA Dominion",
    "description": "Gain influence whenever AI expands."
  },
  "illustration": "https://s3.../ai-titan.png",
  "flavorText": "The silicon giant that reshaped an era."
}
```

**Por que JSONB e não colunas separadas?**
- Qualquer atributo novo pode ser adicionado sem migration no banco
- A IA gera JSON diretamente — sem mapeamento extra
- Frontend renderiza qualquer campo sem mudar a API

### O que vai ser alterado na Fase 1

**Banco de dados**
- Adicionar `metadata JSONB` na tabela `asset`
- Remover coluna `text`
- Criar tabelas `universe` e `collection` (agrupamento de bundles por tema)
- Atualizar schema de testes (sempre idêntico ao de produção)

**Backend Java**
- `Asset.java` — trocar campo `String text` por `Map<String, Object> metadata`
- `AssetDAO.java` — query de insert passa a usar `CAST(? AS jsonb)`
- `CreateAssetRequest.java` — trocar `text` por `metadata`
- `AssetBundleService.java` — adaptar criação de bundle para o novo campo
- Endpoints de leitura — retornar `metadata` no lugar de `text`

**Frontend React**
- Marketplace, Reward e Inventory — exibir `metadata.name` em vez de `text`
- Preparar estrutura de componente de carta para a Fase 3

### O que **não muda** na Fase 1

- Fluxo de claim
- Fluxo de compra e venda
- Autenticação
- Booster packs (vêm na Fase 5)
- Visual das cartas (vem na Fase 3)

---

## O que você precisa decidir antes de começar a Fase 1

### 1. Migração dos dados existentes

Existem assets com `text` no banco de produção hoje?

- **Sim** → precisamos de uma migration que converta `text` existente para `{ "name": "<text>", "rarity": "Common" }` antes de dropar a coluna
- **Não / pode zerar** → simplesmente dropa e recria

### 2. Campos obrigatórios no metadata

Quais campos são obrigatórios em toda carta?

Sugestão mínima: `name`, `rarity`

Todo o resto pode ser opcional — a IA preenche conforme relevância.

### 3. Universo e Coleção

Cada bundle semanal pertence a uma coleção (ex: "Technology Q2 2026") que pertence a um universo (ex: "Real World Events").

Já quer configurar isso na Fase 1 ou deixar para depois?

---

## O que você precisa para a Fase 2 (AI Pipeline)

Quando chegar na Fase 2, precisará de:

| Item | Status |
|---|---|
| Conta na Anthropic (Claude API) | Verificar |
| Conta na OpenAI (DALL-E 3) | Verificar |
| Conta na NewsAPI | Gratuito para começar |
| Bucket S3 para imagens de cartas | Já existe (`banksimulator-frontend-*`) — pode usar uma pasta `/cards/` |
| Secrets no SSM | Adicionar 3 novos: `anthropic_api_key`, `openai_api_key`, `newsapi_key` |

---

## Visão de como vai ficar no final

### Fluxo do usuário

```
Usuário acessa o site
↓
Vê o Marketplace com cartas visuais (ilustração, raridade, atributos)
↓
Se registra para participar
↓
Recebe um Booster Pack gratuito
↓
Abre o pack com animação cinematográfica
↓
Cartas reveladas uma por uma
↓
Cartas vão para o inventário
↓
Pode vender no marketplace, completar coleções, ganhar achievements
```

### Fluxo semanal automático

```
Segunda-feira 08h UTC
↓
Script Python busca notícias da semana
↓
Claude cria 10 cartas com atributos temáticos
↓
DALL-E gera ilustração para cada carta
↓
Imagens enviadas para S3
↓
Bundle publicado via API
↓
Novos Boosters disponíveis para os usuários
↓
Nenhuma intervenção manual necessária
```

---

## Ordem de implementação recomendada

```
┌─────────────────────────────────────────────────────────┐
│  FASE 1 — Fazer agora                                   │
│  Migrar schema + atualizar backend + ajustar frontend   │
└─────────────────────────────────────────────────────────┘
         ↓
┌─────────────────────────────────────────────────────────┐
│  FASE 3 — Logo após a Fase 1                            │
│  Renderizar cartas visualmente (2D primeiro)            │
│  Dá para ver o resultado imediatamente                  │
└─────────────────────────────────────────────────────────┘
         ↓
┌─────────────────────────────────────────────────────────┐
│  FASE 2 — Quando tiver API keys                         │
│  Pipeline de IA gerando cartas reais                    │
└─────────────────────────────────────────────────────────┘
         ↓
┌─────────────────────────────────────────────────────────┐
│  FASE 5 → FASE 4 → FASE 6 → FASE 7 → FASE 8            │
│  (nessa ordem, cada uma adiciona uma camada)            │
└─────────────────────────────────────────────────────────┘
```

> Fase 3 foi antecipada antes da Fase 2 porque você pode criar cartas manualmente com o endpoint admin para testar o visual enquanto a pipeline de IA não está pronta.

---

## Decisões já tomadas (não precisam ser rediscutidas)

| Decisão | Escolha |
|---|---|
| Formato do metadata | JSONB (não colunas individuais) |
| Geração de conteúdo | Claude API |
| Geração de imagens | DALL-E 3 |
| Renderização | Frontend owns rendering (backend entrega só JSON) |
| Three.js | Isolado em hook dedicado, sem React Three Fiber |
| Cartas no pack | Geradas na abertura, não pré-determinadas |
| Achievements | Chamada síncrona no UseCase, sem event bus |
| Pipeline | Script Python externo, não Spring Scheduler |

---

## Referência rápida

- **Documentação técnica completa:** `CLAUDE.md`
- **ADRs e RFCs detalhadas:** `CLAUDE.md` (seções ADR-001 a ADR-008 e RFC-001 a RFC-008)
- **Schema atual do banco:** `src/main/resources/schema.sql`
- **Deploy:** push para branch `prod` → CI/CD automático
- **Produção:** `https://app.alessandro-bezerra.me`
