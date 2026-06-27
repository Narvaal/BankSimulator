# RareLines — Guia para Claude

## Visão Geral

**RareLines** é uma plataforma premium de cartas colecionáveis digitais, geradas semanalmente por IA com base em eventos reais do mundo. Toda semana, uma pipeline automatizada busca notícias relevantes, cria cartas temáticas com atributos únicos, gera ilustrações e as disponibiliza para os usuários em Booster Packs. Os usuários coletam, abrem packs, completam coleções e negociam cartas no marketplace.

O projeto é construído sobre um backend Java/Spring já funcional que implementa autenticação, marketplace, inventário e sistema de assets. A evolução transforma esse backend em uma plataforma completa de trading cards digitais.

**URL de produção:** `https://app.alessandro-bezerra.me`
**API de produção:** `https://api.alessandro-bezerra.me`

### O que o projeto demonstra (portfólio)

- Clean Architecture + DDD-inspired modeling
- REST APIs robustas com JDBC puro (sem ORM)
- Autenticação local + Google OAuth com JWT
- Marketplace com histórico de preços e analytics
- Inventário com sistema de coleções e progressão
- Integração com IA (geração de conteúdo + imagens)
- Three.js com shaders avançados (foil, reflection, particles)
- Animações cinematográficas (abertura de booster pack)
- PostgreSQL modeling com JSONB
- Pipeline semanal 100% automatizada sem intervenção manual

### Objetivo final

O produto final não deve parecer um projeto de portfólio. Deve parecer uma plataforma de trading cards digitais pronta para produção que acontece de demonstrar boas práticas de engenharia.

---

## Visão do Produto

### Design Goals

- **Premium** — cada interação deve parecer satisfatória. Nada aparece instantaneamente. Tudo anima naturalmente.
- **Collectible** — usuários devem sentir a satisfação de colecionar, completar coleções e possuir cartas raras.
- **Modern** — UI de alto nível com Three.js, shaders e animações cinematográficas.
- **Interactive** — cartas reagem ao mouse, têm física e comportamento de objeto físico.
- **High-end** — cartas raras têm shaders exclusivos, sons e revelações especiais.

### Restrições Técnicas

- Backend deve permanecer agnóstico de framework (sem ORM, sem Spring Data).
- Componentes frontend devem ser reutilizáveis.
- Renderização Three.js deve ser isolada da lógica de negócio.
- Animações devem ser data-driven (configuradas por raridade/tipo, não hardcoded).
- Metadata deve suportar futuros tipos de colecionável sem migration.
- Nunca hardcodar layouts de carta no backend.

---

## Modelo de Dados — Card Metadata

### Princípio

Em vez de colunas individuais para cada atributo, o campo `metadata` da tabela `artifact` armazena um documento JSONB. Isso elimina migrações futuras, torna o modelo extensível e facilita a geração por IA.

Todos os campos são **obrigatórios**. A pipeline de IA deve sempre preencher o schema completo para garantir padronização no layout e consistência entre cartas.

### Estrutura completa

```json
{
  "name":     "Apple Vision Pro",
  "subtitle": "Apple enters the spatial computing era",
  "category": "Technology",
  "rarity":   "Legendary",

  "effects": {
    "foil":        true,
    "glow":        "#ffd700",
    "shimmer":     true,
    "particles":   "heavy",
    "borderLight": true
  },

  "illustration": "https://cdn.rarelines.io/cards/apple-vision-pro.png",
  "background":   "https://cdn.rarelines.io/backgrounds/tech-blue.png",

  "attributes": {
    "influence":   91,
    "innovation":  95,
    "controversy": 48,
    "longevity":   72,
    "reach":       88
  },

  "abilities": [
    {
      "name":        "Closed Ecosystem",
      "description": "Commands loyalty through exclusivity — impossible to replicate."
    }
  ],

  "passive": {
    "name":        "Silicon Monopoly",
    "description": "Arguments backed by revenue above $1T gain extra weight."
  },

  "weakness": "Premium pricing limits global adoption",

  "flavorText": "The future arrived. Apple priced it so you'd know your place in it.",

  "lore": "When Apple unveiled its spatial computing headset in 2024, it marked the beginning of a new computing paradigm — expensive, polarizing, and impossible to ignore.",

  "traits": [
    { "name": "Era",    "value": "AI Age" },
    { "name": "Origin", "value": "United States" }
  ],

  "timeline": [
    { "date": "2023-06-05", "event": "Announced at WWDC" },
    { "date": "2024-02-02", "event": "Released in the United States" }
  ],

  "references": [
    "https://www.apple.com/apple-vision-pro/"
  ],

  "collection":  "Tech Giants 2024",
  "cardNumber":  "042",
  "releaseDate": "2024-06-03",

  "artist": "RareLines AI",
  "model":  "dall-e-3",
  "prompt": "Futuristic spatial computing headset, cinematic lighting, dark background...",
  "seed":   "4829301"
}
```

### Limits por campo

| Campo | Tipo | Limit |
|---|---|---|
| `name` | string | máx 30 chars |
| `subtitle` | string | máx 60 chars |
| `category` | enum | Technology · Finance · Science · Culture · Sports · Politics |
| `rarity` | enum | Common · Rare · Epic · Legendary · Mythic · Ultimate |
| `abilities` | array | **1 a 2 itens** |
| `abilities[].name` | string | máx 25 chars |
| `abilities[].description` | string | máx 120 chars |
| `passive.name` | string | máx 25 chars |
| `passive.description` | string | máx 120 chars |
| `weakness` | string | máx 80 chars |
| `flavorText` | string | máx 15 palavras |
| `lore` | string | máx 300 chars |
| `traits` | array | **2 a 4 itens** |
| `traits[].name` | string | máx 15 chars |
| `traits[].value` | string | máx 20 chars |
| `timeline` | array | **2 a 5 itens** |
| `timeline[].event` | string | máx 60 chars |
| `references` | array | mín 1 · máx 5 |

### `abilities` vs `passive`

- **passive** — sempre ativo, representa uma característica permanente da carta. Nunca muda durante a batalha.
- **abilities** (1-2) — representam o que a carta "faz" no argumento de batalha. São a base da estratégia do jogador.

### Sistema de Effects (camadas independentes)

Cada efeito visual é controlado individualmente e podem ser combinados livremente:

| Campo | Tipo | Descrição |
|---|---|---|
| `foil` | boolean | Overlay holográfico rainbow |
| `glow` | string \| null | Cor hex do brilho de borda, ex: `"#ffd700"` |
| `shimmer` | boolean | Reflexo sutil que segue o mouse |
| `particles` | string \| null | `"minimal"` · `"medium"` · `"heavy"` · `"cinematic"` |
| `borderLight` | boolean | Borda animada com luz pulsante |

Defaults por raridade (podem ser sobrescritos por carta):

| Raridade | Prob. | foil | glow | shimmer | particles | borderLight |
|---|---|---|---|---|---|---|
| Common | 55% | false | null | false | null | false |
| Rare | 25% | false | `"#c0c0c0"` | true | `"minimal"` | false |
| Epic | 12% | false | `"#9b30ff"` | true | `"medium"` | true |
| Legendary | 6% | true | `"#ffd700"` | true | `"heavy"` | true |
| Mythic | 1.8% | true | `"#00ffff"` | true | `"cinematic"` | true |
| Ultimate | 0.2% | true | exclusivo | true | `"cinematic"` | true |

A raridade controla: frame, effects, animação, audio, pack reveal, cor no marketplace e borda no inventário.

---

## Tom da IA — Humor Ácido

A IA deve escrever como um comentarista inteligente e levemente cínico: respeita os fatos, mas não resiste a apontar o absurdo das situações. O humor é no *como*, nunca no *o quê* — a importância real do evento é sempre preservada.

### Onde o humor aparece

**`flavorText`** — é onde mais brilha. Deve ter ironia, duplo sentido ou uma verdade desconfortável dita com elegância:
> *"The future arrived. Apple priced it so you'd know your place in it."*
> *"A financial revolution, mostly used to make the already-rich slightly richer."*
> *"Democracy found its voice. It was louder than anyone expected and made less sense than anyone hoped."*

**`weakness`** — pode ser particularmente afiada:
> *"Depends on people continuing to care, which history suggests is optimistic."*

**`passive.description`** e **`abilities[].description`** — fio de ironia sem quebrar a coerência:
> *"Thrives in environments where disruption is celebrated more than it is understood."*

**`lore`** — mais contido, mas pode terminar com uma observação cortante:
> *"...and so the company that once sold computers to rebels became the establishment it promised to destroy."*

### O que evitar
- Humor que ridicularize pessoas específicas pelo nome
- Sarcasmo sobre tragédias (ver filtro abaixo)
- Ironia que invalide a importância real do evento

---

## Filtro de Notícias — O que não vira carta

A plataforma celebra o mundo, não o lamenta. A pipeline rejeita automaticamente notícias das seguintes categorias:

| Rejeitar | Exemplos |
|---|---|
| Mortes e desastres | Tragédias, acidentes com vítimas, assassinatos |
| Guerras e conflitos armados | Batalhas, bombardeios, ataques terroristas |
| Epidemias com mortalidade | Surtos com número significativo de mortes |
| Crimes violentos | Chacinas, sequestros, atrocidades |
| Catástrofes naturais com vítimas | Terremotos, furacões com mortos |
| Saúde mental e suicídio | Qualquer cobertura com esse viés |

| Aceitar | Exemplos |
|---|---|
| Tecnologia | Lançamentos, breakthroughs, aquisições, IPOs |
| Ciência | Descobertas, missões espaciais, pesquisas |
| Cultura | Filmes, música, arte, fenômenos culturais |
| Esportes | Recordes, conquistas, momentos históricos |
| Economia | Fusões, tendências de mercado, novos players |
| Política | Eleições, acordos, mudanças de poder sem tragédia associada |
| Meio ambiente | Iniciativas positivas, recordes — com cuidado no framing |

**Critério prático:** *"Essa notícia pode virar uma carta que alguém ficaria feliz de colecionar?"* Se não, rejeita.

---

## Sistema de Booster Packs

### Princípio

Cartas **nunca aparecem diretamente no inventário**. Toda aquisição ocorre pela abertura de um Booster Pack. A experiência de abertura é um dos recursos centrais da plataforma.

### Entidade `booster_pack`

```sql
booster_pack (
    id            BIGSERIAL PRIMARY KEY,
    collection_id BIGINT    NOT NULL,   -- FK → collection
    bundle_id     BIGINT    NOT NULL,   -- FK → asset_bundle
    owner_id      BIGINT    NOT NULL,   -- FK → account
    status        VARCHAR   NOT NULL,   -- UNOPENED | OPENED
    created_at    TIMESTAMP NOT NULL DEFAULT now(),
    opened_at     TIMESTAMP NULL
)
```

O pack **não contém** `asset_unit`s antecipadamente. As cartas são geradas no momento da abertura, permitindo ajuste de probabilidades sem recriar packs.

### Fluxo de Abertura (Cinematográfico)

```
Pack aparece na tela
↓ Hover com parallax
↓ Shake animation
↓ Light leaks pelos cantos
↓ Pack rasga
↓ Partículas explodem
↓ N cartas aparecem face down
↓ Usuário vira uma por uma
↓ Cartas voam para o inventário
```

### Algoritmo de Recompensa

Probabilidades configuráveis por tipo de pack. Garantias suportadas:
- **Guaranteed Rare** — ao menos uma carta Rare por pack
- **Guaranteed Event Card** — cartas de eventos especiais em packs temáticos
- **Pity System** — após N aberturas sem Legendary, a próxima garante uma

### Tipos de Pack

Daily Pack · Weekly Pack · Monthly Pack · Event Pack · Founder Pack · Special Collection Pack

### Fluxo completo bundle → inventário

```
Bundle released
↓ Boosters generated
↓ User receives Booster
↓ Open animation
↓ Reveal cards
↓ Cards minted (asset_unit created per card)
↓ Inventory updated
```

---

## Pipeline de IA Semanal

```
Fontes de notícias (multi-source):
  Google Trends (pytrends) — o que o mundo está buscando
  NewsAPI           — artigos completos com contexto jornalístico
  Reddit API        — o que está sendo discutido (r/technology, r/science, etc.)
↓
Claude API seleciona os 10 eventos mais relevantes e únicos
↓
Claude API gera metadata JSON completo por evento
↓
DALL-E 3 gera ilustração por carta
↓
S3 armazena imagens permanentemente
↓
POST /artifacts/bundles → backend cria bundle + artifacts
↓
Boosters gerados para distribuição
```

**Execução:** AWS Lambda (Python 3.11) + EventBridge Scheduler (toda segunda, 08:00 UTC)

**Custo estimado por semana (~10 cartas):** ~$0.41
- Claude API (seleção + geração de metadata): ~$0.01
- DALL-E 3 (10 imagens 1024×1024): ~$0.40
- Lambda + EventBridge: desprezível (free tier cobre amplamente)
- S3 storage: desprezível

**Por que não X (Twitter) API:** O endpoint de trending topics exige tier Basic ($100/mês) ou Pro ($5.000/mês). Google Trends + Reddit cobrem o mesmo caso de uso gratuitamente.

---

## Renderização de Cartas — Frontend

### Composição em Camadas

Cada camada é um componente React independente, animado separadamente:

```
Glass (vidro / transparência)
↓ Reflection (reflexo dinâmico baseado no mouse)
↓ Foil (efeito holográfico por raridade)
↓ Particles (partículas configuradas pela raridade)
↓ Frame (moldura — visual determinado pela raridade)
↓ Illustration (artwork gerado por IA, do campo metadata.illustration)
↓ Background (fundo temático, do campo metadata.background)
```

### Faces da Carta

**Frente:** Illustration · Name · Subtitle · Attack · Defense · Ability · Category · Rarity · Collection · Card Number · Print Number

**Verso:** Summary · Timeline · Historical Context · References · QR Code · Traits · Stats · AI Information (prompt, seed, model)

### Three.js — Comportamento Físico

Cada carta se comporta como um objeto físico:

| Feature | Descrição |
|---|---|
| Mouse tilt | Inclinação 3D seguindo o cursor |
| Parallax | Camadas se movem em velocidades diferentes |
| Reflection | Reflexo dinâmico baseado na posição do mouse |
| Foil shader | Efeito holográfico animado |
| Animated glow | Brilho pulsante por raridade |
| Dynamic lighting | Luz que reage ao movimento |
| Particles | Sistema de partículas por raridade |
| Glass effect | Transparência com refração |
| Border lighting | Borda iluminada animada |
| Hover animation | Elevação ao passar o mouse |
| Idle float | Flutuação suave quando em repouso |

Cartas Mythic e Ultimate têm shaders exclusivos não compartilhados com raridades menores.

---

## Roadmap de Implementação

### Fase 1 — Domain Refactor
Refatorar o modelo de domínio para suportar o novo conceito de cartas colecionáveis.
- Adicionar `metadata JSONB` na tabela `asset`
- Remover campo `text` (substituído por `metadata.name`)
- Introduzir entidades `Universe` e `Collection`
- Atualizar APIs de bundle/criação para aceitar metadata
- Manter compatibilidade com o fluxo de claim e marketplace existentes

### Fase 2 — AI Pipeline
Pipeline automatizada de geração de conteúdo semanal.
- Ingestão de notícias multi-source: Google Trends + NewsAPI + Reddit API
- Claude API para seleção dos eventos e geração de metadata JSON completo
- DALL-E 3 para geração de ilustrações
- Upload de imagens para S3
- AWS Lambda (Python 3.11) + EventBridge Scheduler como trigger semanal

### Fase 3 — Card Rendering Engine (2D)
Renderizador 2D responsivo das cartas no frontend.
- Componente React de carta com frente e verso
- Composição em camadas (CSS + Framer Motion)
- Layout responsivo por tamanho de tela
- Animação de flip (frente ↔ verso)
- Variantes visuais por raridade

### Fase 4 — Three.js
Experiência 3D premium para cartas.
- Shaders GLSL para foil, reflection e glow
- Mouse tilt com parallax por camada
- Sistema de partículas por raridade
- Iluminação dinâmica
- Shaders exclusivos para Mythic e Ultimate
- Idle float animation

### Fase 5 — Booster Packs
Sistema completo de packs com abertura cinematográfica.
- Entidade `booster_pack` no banco
- Engine de probabilidade configurável por tipo de pack
- Pity system com histórico de aberturas
- Animação cinematográfica de abertura (Framer Motion + Three.js)
- Tipos de pack (Daily, Weekly, Monthly, Event, Founder)
- Geração de `asset_unit` no momento da abertura

### Fase 6 — Marketplace
Expansão do marketplace atual com analytics.
- Histórico de preços por carta com gráficos
- Volume de vendas, mínimo, máximo, média
- Filtros por raridade, categoria e coleção
- Histórico de donos de cada carta
- Charts interativos (Recharts já disponível)

### Fase 7 — Collections, Achievements, Profile
Camada de progressão e gamificação.
- Sistema de coleções com barra de progresso
- Achievements desbloqueáveis por ações do usuário
- Perfil público com inventário, conquistas e estatísticas
- Cartas faltantes por coleção
- Ranking de completude

### Fase 8 — Automação
Pipeline 100% sem intervenção manual.
- AWS Lambda + EventBridge Scheduler (trigger toda segunda 08:00 UTC)
- Ingestão multi-source: Google Trends + NewsAPI + Reddit API
- Geração de conteúdo, validação de schema, publicação automática
- Notificações para usuários (email / in-app)
- Logs no CloudWatch + alerta via SES em caso de falha

---

## Visão de Longo Prazo — RareLines TCG Narrativo

Esta seção documenta a visão de produto além das 8 fases do roadmap atual. Não deve ser implementada agora, mas deve informar decisões de arquitetura para garantir que o caminho esteja aberto.

### Conceito Principal

O RareLines não será um TCG tradicional baseado em ataque, defesa e dano numérico. As cartas são **artefatos narrativos** gerados por IA a partir de acontecimentos reais — notícias da semana, tendências, tecnologia, cultura, esportes, eventos históricos.

A batalha não será:
> "Minha carta tem 100 de ataque e a sua tem 80"

Será:
> "Minha carta representa uma ideia mais forte que a sua — e eu preciso convencer a IA juiz."

O jogador vence por criatividade, interpretação e argumento. O "meta" muda constantemente porque as cartas nascem do mundo real. Uma carta criada hoje pode ter peso completamente diferente de uma criada daqui a 6 meses.

---

### Sistema de Cartas (TCG)

Cada carta é um Artifact com atributos narrativos, não numéricos:

```
Artifact: Apple Vision Pro
Categoria: Tecnologia

Atributos:
- Inovação
- Influência
- Popularidade
- Impacto cultural
- Evolução

Habilidade: Ecossistema Fechado
Fraqueza: Dependência de tendências

Lore: Gerado pela IA a partir de notícias reais
```

Os atributos não servem como "dano" — servem como **contexto para a IA entender a carta** e julgar argumentos.

---

### Sistema de Batalha

**Fluxo de uma partida:**

```
1. Cada jogador recebe 3 cartas do seu deck
2. Cada jogador escolhe 1 carta para jogar
3. Ambos escrevem um argumento: por que sua carta venceria?
4. A IA juiz analisa os argumentos e decide o vencedor
5. Melhor de 3 rounds vence a partida
```

**Exemplo — Apple vs Android:**

Jogador A (Apple Vision Pro):
> "A Apple vence porque criou um ecossistema extremamente desejado e mudou a forma como pessoas usam tecnologia."

Jogador B (Android):
> "O Android vence porque está presente em bilhões de dispositivos e representa liberdade e adaptação."

**Critérios da IA juiz:**
- Criatividade do argumento
- Uso coerente dos atributos da carta
- Alinhamento com a história e o lore da carta
- Poder de persuasão

---

### Sistema de Abertura de Packs (Transparente)

A experiência de abertura mantém a emoção do Pokémon TCG, mas **sem loot box opaco com dinheiro real**. O conteúdo possível é sempre visível antes de abrir.

**Fontes de packs:**
- Recompensas diárias
- Missões e conquistas
- Progressão de coleções
- Eventos temáticos
- Loot box transparente (conteúdo público antes de abrir)

**Exemplo de loot box transparente:**
```
Bundle Semanal — Tech Week:
  100 cartas comuns
  20 cartas raras
  5 cartas épicas
  1 carta lendária
```

Nada fica escondido. O usuário sabe exatamente o que pode receber.

---

### Geração da Abertura (Backend Owned)

O servidor é responsável por gerar a sequência de cartas. O frontend apenas consome e anima.

```json
{
  "packId": "weekly_pack_001",
  "cards": [
    { "rarity": "common",    "artifactId": "123" },
    { "rarity": "common",    "artifactId": "456" },
    { "rarity": "rare",      "artifactId": "789" },
    { "rarity": "legendary", "artifactId": "999" }
  ]
}
```

**Lógica de ordenação:** as cartas seguem ordem crescente de raridade para criar tensão:
```
Comuns → Raras → Épicas → Lendárias
```

Isso cria o momento: *"Será que a última carta é a lendária?"*

---

### Transparência de Supply

Toda carta tem quantidade pública e o servidor controla o total disponível:

```
Apple Vision Pro
Quantidade existente: 10.000 unidades
```

Cada vez que uma carta é obtida: `totalDisponivel -= 1`

O usuário sempre sabe quantas cópias existem no mundo.

---

### Mistura de Gêneros

O produto final é uma combinação única de:

| Gênero | Como aparece |
|---|---|
| TCG | Decks, packs, raridades, coleções |
| RPG Narrativo | Lore gerado por IA, história das cartas |
| Debate | Argumentos vs IA juiz |
| Colecionismo | Supply limitado, cartas únicas por evento |
| IA Generativa | Conteúdo novo toda semana baseado no mundo real |

---

### Implicações Arquiteturais

Esta visão valida decisões já tomadas e adiciona restrições futuras:

- **JSONB metadata** — os campos `ability`, `traits`, `stats`, `flavorText` e `lore` já existem no schema e são suficientes para o sistema de batalha
- **Backend owns game logic** — o engine de probabilidade de packs (Fase 5) será a base para o engine de batalha
- **IA como serviço** — a integração com Claude API (Fase 2) será reutilizada para o juiz de batalha
- **Frontend declarativo** — a separação render/logic permite adicionar a interface de batalha sem tocar no backend de cartas
- **Nunca hardcodar regras de batalha no frontend** — toda lógica de julgamento vive no backend

---

## ADRs — Architecture Decision Records

### ADR-001: JSONB para Metadata de Cartas

**Status:** Accepted
**Fase:** 1

**Contexto:** O modelo atual de `asset` tem apenas o campo `text`. O novo modelo de carta tem dezenas de atributos (attack, defense, ability, stats, traits, illustration, etc.) que variam por tipo de carta e evoluem ao longo do tempo.

**Decisão:** Adicionar um campo `metadata JSONB` na tabela `asset` e remover o campo `text`. Todo atributo da carta vive dentro desse documento. O backend Java deserializa para `Map<String, Object>` ou um record `CardMetadata` quando necessário para validação.

**Consequências:**
- ✅ Zero migrações futuras para adicionar atributos
- ✅ Pipeline de IA pode gerar JSON diretamente sem conhecer o schema
- ✅ Frontend pode renderizar qualquer campo sem mudança de API
- ⚠️ Queries de filtragem por atributo precisam de índices JSONB (`jsonb_path_ops`)
- ⚠️ Validação de schema passa a ser responsabilidade da aplicação, não do banco

---

### ADR-002: Claude API para Geração de Conteúdo, DALL-E 3 para Imagens

**Status:** Accepted
**Fase:** 2

**Contexto:** A pipeline precisa gerar metadata estruturado (JSON) e ilustrações para cada carta semanalmente, baseado em notícias reais.

**Decisão:** Usar Claude API (via Anthropic SDK Python) para geração do JSON de metadata com prompt estruturado + few-shot examples. Usar DALL-E 3 (via OpenAI API Python) para ilustrações com prompt derivado do metadata gerado. Imagens armazenadas no S3 existente.

**Consequências:**
- ✅ Claude tem alta precisão para saída JSON estruturada
- ✅ DALL-E 3 produz imagens de alta qualidade com prompt textual
- ✅ Custo baixo (~$0.41/semana para 10 cartas)
- ✅ Pipeline desacoplada do backend (script Python independente)
- ⚠️ Qualidade de imagem depende do prompt engineering
- ⚠️ Falhas na API externa precisam de retry e fallback

---

### ADR-003: Frontend Owns Card Rendering com Camadas Independentes

**Status:** Accepted
**Fase:** 3

**Contexto:** A carta tem múltiplas camadas visuais (frame, illustration, foil, particles, glass) que variam por raridade e precisam ser animadas independentemente.

**Decisão:** O frontend é 100% responsável pela renderização visual. O backend entrega apenas o JSON de metadata. Cada camada é um componente React independente posicionado em `position: absolute` dentro de um container `position: relative`. Propriedades visuais (shader, particles, glow) são determinadas pela raridade no frontend.

**Consequências:**
- ✅ Backend nunca precisa conhecer detalhes visuais
- ✅ Camadas podem ser animadas independentemente com Framer Motion
- ✅ Fácil adicionar novas raridades sem mudar backend
- ⚠️ Lógica de raridade duplicada se houver outros clientes (mobile, etc.)

---

### ADR-004: Three.js Isolado em Hook Dedicado

**Status:** Accepted
**Fase:** 4

**Contexto:** Three.js tem estado global, lifecycle próprio e lógica de shader complexa. Misturar com componentes React pode causar memory leaks e rerenders desnecessários.

**Decisão:** Toda lógica Three.js vive em um hook `useCardRenderer(canvasRef, metadata, rarity)`. O componente React monta um `<canvas>` e passa a ref para o hook. O hook gerencia o scene, camera, renderer e dispose no unmount. Shaders GLSL ficam em arquivos `.glsl` separados, importados via Vite.

**Consequências:**
- ✅ Componentes React permanecem declarativos e simples
- ✅ Sem memory leak — hook faz cleanup no unmount
- ✅ Shaders podem ser desenvolvidos e testados independentemente
- ⚠️ Debugging de shaders requer ferramentas específicas (Spector.js)
- ⚠️ Performance em dispositivos mobile precisa de fallback 2D

---

### ADR-005: Cartas Geradas no Momento da Abertura do Pack, Não na Criação

**Status:** Accepted
**Fase:** 5

**Contexto:** Dois modelos possíveis: (A) pack contém `asset_unit`s pré-determinados — distribuição fixa. (B) pack é um container vazio — cartas são geradas no momento da abertura.

**Decisão:** Modelo B. O `booster_pack` não contém `asset_unit`s. Ao abrir, o engine de probabilidade determina quantas cartas de cada raridade o usuário recebe, seleciona assets disponíveis do bundle, cria os `asset_unit`s e atualiza o inventário. Isso permite ajustar probabilidades e implementar pity system sem recriar packs já emitidos.

**Consequências:**
- ✅ Probabilidades ajustáveis sem impacto em packs existentes
- ✅ Pity system pode ser implementado com histórico de aberturas
- ✅ Suporte a garantias (Guaranteed Rare) trivial de implementar
- ⚠️ Abertura precisa ser transacional (pack → OPENED + unit criadas atomicamente)
- ⚠️ Se `total_supply` de um asset acabar antes do pack ser aberto, precisa de fallback

---

### ADR-006: Analytics do Marketplace Como Camada de Leitura Separada

**Status:** Accepted
**Fase:** 6

**Contexto:** O marketplace atual consulta `asset_listing` diretamente. Analytics (volume, preço médio, histórico) requerem agregações pesadas que não devem ser feitas em queries de listagem.

**Decisão:** Criar endpoints de analytics separados (`/asset-listings/stats`, `/asset-listings/{id}/history`) que fazem queries de agregação sob demanda. Não materializar views por enquanto — o volume de dados não justifica. Adicionar índice em `asset_price_history(asset_unity_id, created_at)` para suportar as queries de histórico.

**Consequências:**
- ✅ Endpoints de listagem permanecem rápidos
- ✅ Sem complexidade de views materializadas ou cache agora
- ✅ Fácil migrar para views materializadas no futuro se necessário
- ⚠️ Queries de analytics podem ser lentas com volume alto sem cache

---

### ADR-007: Achievements Triggered por Domain Events no UseCase Layer

**Status:** Accepted
**Fase:** 7

**Contexto:** Achievements precisam ser desbloqueados quando o usuário executa ações (abrir pack, comprar carta, completar coleção). Onde checar?

**Decisão:** Cada UseCase relevante chama `AchievementService.checkAndUnlock(accountId, TriggerType)` ao final da operação, dentro da mesma transação. O `AchievementService` consulta os critérios e registra conquistas novas. Não usar event bus por enquanto — o volume não justifica a complexidade.

**Consequências:**
- ✅ Achievements são garantidos mesmo em cenários de concorrência
- ✅ Sem infraestrutura adicional (sem Kafka, sem event bus)
- ✅ Fácil debugar — o fluxo é síncrono e rastreável
- ⚠️ UseCases ficam levemente acoplados ao sistema de achievements
- ⚠️ Se achievements ficarem complexos, a chamada síncrona pode atrasar a resposta

---

### ADR-008: Pipeline de Automação como AWS Lambda + EventBridge

**Status:** Accepted
**Fase:** 8

**Contexto:** A pipeline semanal (notícias → IA → imagens → bundle) precisa ser agendada e executada. Opções avaliadas: cron no EC2 (simples, mas acoplado ao servidor), n8n (visual, mas sem node nativo para Claude e cara para self-host), AWS Lambda + EventBridge (serverless, zero manutenção).

**Decisão:** AWS Lambda (Python 3.11) disparado por EventBridge Scheduler toda segunda-feira às 08:00 UTC. O Lambda lê secrets do SSM, busca notícias de múltiplas fontes (Google Trends + NewsAPI + Reddit), gera metadata com Claude API, ilustrações com DALL-E 3, faz upload para S3 e chama `POST /artifacts/bundles` via `X-Admin-Token`. Logs vão para CloudWatch; falhas disparam alerta via SES.

**Por que não n8n:** Não tem node nativo para Claude API, adiciona uma peça de infraestrutura para manter e o plano cloud tem custo mensal fixo. Para lógica de validação de schema e pity system, código Python é mais flexível.

**Por que não X (Twitter) API para tendências:** Endpoint de trending topics exige tier Basic ($100/mês). Substituído por Google Trends (via `pytrends`, gratuito) + Reddit API (gratuito, 100 req/min) que cobrem o mesmo caso de uso sem custo.

**Consequências:**
- ✅ Serverless — zero processo rodando, zero manutenção de servidor
- ✅ Custo quase zero (Lambda free tier cobre amplamente)
- ✅ Logs automáticos no CloudWatch
- ✅ Falha na pipeline não afeta o backend
- ✅ Multi-source de notícias: Google Trends + NewsAPI + Reddit = cobertura mais rica
- ⚠️ Lambda tem timeout de 15 min — suficiente para 10 cartas, monitorar se escalar
- ⚠️ Secrets (Anthropic, OpenAI, NewsAPI, Reddit) precisam estar no SSM

---

## RFCs — Request for Comments

### RFC-001: Domain Refactor — Asset Metadata e Novas Entidades

**Fase:** 1

**Problema:** O modelo atual de `asset` é centrado em texto simples (`text TEXT NOT NULL UNIQUE`). O novo modelo de carta requer dezenas de atributos estruturados, imagens, raridades e relacionamentos com coleções.

**Proposta:**

**1. Migração da tabela `asset`:**
```sql
ALTER TABLE asset ADD COLUMN metadata JSONB NOT NULL DEFAULT '{}';
ALTER TABLE asset DROP COLUMN text;
-- Adicionar índice para queries por raridade e categoria
CREATE INDEX idx_asset_metadata_rarity ON asset ((metadata->>'rarity'));
CREATE INDEX idx_asset_metadata_category ON asset ((metadata->>'category'));
```

**2. Novas tabelas:**
```sql
-- Universo temático (ex: "Real World Events 2026")
CREATE TABLE universe (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);

-- Coleção dentro de um universo (ex: "Technology Q1 2026")
CREATE TABLE collection (
    id           BIGSERIAL PRIMARY KEY,
    universe_id  BIGINT      NOT NULL REFERENCES universe(id),
    name         VARCHAR(100) NOT NULL,
    total_cards  INT         NOT NULL DEFAULT 0,
    created_at   TIMESTAMP   NOT NULL DEFAULT now()
);

-- Associar asset_bundle a uma collection
ALTER TABLE asset_bundle ADD COLUMN collection_id BIGINT REFERENCES collection(id);
```

**3. API de criação de bundle (request atualizado):**
```json
POST /admin/assets/bundles
{
  "identifier": "tech-week-2026-W26",
  "collectionId": 1,
  "assets": [
    {
      "totalSupply": 3,
      "metadata": {
        "name": "AI Titan",
        "rarity": "Legendary",
        "attack": 98,
        "defense": 84,
        ...
      }
    }
  ]
}
```

**4. Domain Java:**
- `Asset.java`: substituir campo `text: String` por `metadata: Map<String, Object>`
- `CreateAssetRequest.java`: substituir `text` por `metadata`
- Adicionar `CardMetadata.java` como record Java para validação estruturada
- `AssetDAO.insert()`: usar `CAST(? AS jsonb)` para o campo metadata

**Trade-offs:**
- ✅ Extensível sem migrations futuras
- ✅ AI-friendly (LLM gera JSON diretamente)
- ⚠️ Validação de schema é responsabilidade da aplicação
- ⚠️ Queries por atributos internos do JSON requerem índices GIN

**Schema test:** Atualizar `src/test/resources/schema.sql` identicamente ao de produção (regra existente do projeto).

**Questões em aberto:**
- Validar o JSON no backend ou aceitar qualquer estrutura?
- Quais campos são obrigatórios no metadata? Sugestão: `name`, `rarity`, `totalSupply`.

---

### RFC-002: AI Generation Pipeline

**Fase:** 2

**Problema:** Criar semanalmente 10-20 cartas baseadas em notícias reais, com metadata estruturado e ilustrações, sem intervenção manual.

**Proposta:**

**Script:** `pipeline/generate_weekly.py` (empacotado como Lambda)

**Fluxo:**
```python
# 1. Buscar notícias — múltiplas fontes
google_trends = fetch_google_trends()          # pytrends, gratuito
newsapi_articles = fetch_newsapi_headlines()   # artigos completos
reddit_posts = fetch_reddit_top(subs=[         # praw, gratuito
    "technology", "science", "worldnews", "sports"
])
news = google_trends + newsapi_articles + reddit_posts

# 2. Selecionar eventos mais relevantes (Claude escolhe, elimina duplicatas)
events = claude.select_events(news, count=10)

# 3. Para cada evento, gerar metadata
for event in events:
    metadata = claude.generate_card_metadata(event, existing_cards)
    # Retorna o JSON completo com name, rarity, attack, defense, etc.

    # 4. Gerar ilustração
    image_url = dalle.generate_illustration(
        prompt=metadata["prompt"],
        style="digital trading card art, detailed, vibrant"
    )
    metadata["illustration"] = image_url

# 5. Upload imagens para S3
# (DALL-E retorna URL temporária — fazer download e upload para S3)

# 6. POST para API
requests.post(
    f"{API_URL}/admin/assets/bundles",
    headers={"X-Admin-Token": ADMIN_TOKEN},
    json={"identifier": f"weekly-{week}", "assets": cards}
)
```

**Prompt para Claude (metadata):**
```
You are generating a collectible trading card for the RareLines platform.
Based on this news event: {event}
Generate a complete card metadata JSON following this schema: {schema}
Rules:
- rarity must be one of: Common, Rare, Epic, Legendary, Mythic, Ultimate
- attack and defense must be between 0 and 100
- flavorText must be poetic and under 15 words
- ability.description must relate to the real-world event
Return only valid JSON, no explanation.
```

**Agendamento:** AWS EventBridge Scheduler
```
schedule: cron(0 8 ? * MON *)   # toda segunda, 08:00 UTC
target: Lambda function rarelines-pipeline
```

**Secrets no SSM:**
- `/banksimulator/anthropic_api_key`
- `/banksimulator/openai_api_key`
- `/banksimulator/newsapi_key`
- `/banksimulator/reddit_client_id`
- `/banksimulator/reddit_client_secret`

**Monitoramento:** CloudWatch Logs automático + SNS/SES alert em caso de erro na invocação do Lambda.

**Trade-offs:**
- ✅ Serverless — sem processo rodando, sem manutenção de cron
- ✅ Totalmente desacoplado do backend
- ✅ Multi-source de notícias dá mais variedade e cobertura
- ⚠️ Retry logic necessário (APIs externas podem falhar)
- ⚠️ Validar JSON gerado antes de enviar para a API
- ⚠️ Lambda timeout 15 min — suficiente para 10 cartas, monitorar se escalar

**Questões em aberto:**
- Implementar validação de qualidade das imagens geradas (ex: rejeitar se NSFW)?
- Quantas fontes por fonte? Sugestão: top 20 Google Trends + 10 NewsAPI + 10 Reddit = 40 candidatos → Claude seleciona 10.

---

### RFC-003: Card Rendering Engine — 2D

**Fase:** 3

**Problema:** O frontend atual exibe cartas como cards de texto simples. O novo modelo requer uma renderização visual rica com ilustração, frame por raridade, atributos e animações.

**Proposta:**

**Componente principal:** `src/component/card/CardView.tsx`

```tsx
interface CardViewProps {
  metadata: CardMetadata;
  size?: "sm" | "md" | "lg" | "full";
  interactive?: boolean;
  showBack?: boolean;
}

export default function CardView({ metadata, size = "md", interactive = false, showBack = false }: CardViewProps) {
  const [flipped, setFlipped] = useState(showBack);
  return (
    <div className="card-container" style={cardSizeStyle(size)}>
      <AnimatePresence>
        {flipped ? <CardBack metadata={metadata} /> : <CardFront metadata={metadata} />}
      </AnimatePresence>
      {interactive && <button onClick={() => setFlipped(v => !v)}>Flip</button>}
    </div>
  );
}
```

**Estrutura de arquivos:**
```
src/component/card/
├── CardView.tsx          — componente principal
├── CardFront.tsx         — face frontal
├── CardBack.tsx          — face traseira
├── layers/
│   ├── FrameLayer.tsx    — moldura por raridade
│   ├── IllustrationLayer.tsx
│   ├── FoilLayer.tsx     — overlay holográfico CSS
│   ├── ParticleLayer.tsx — partículas CSS/canvas
│   └── GlassLayer.tsx    — efeito de vidro
├── rarity/
│   ├── rarityConfig.ts   — mapeamento raridade → visual
│   └── frames/           — SVGs de frame por raridade
└── CardSkeleton.tsx      — loading state
```

**`rarityConfig.ts`:**
```ts
export const RARITY_CONFIG = {
  Common:    { frameColor: "#8c8c8c", glow: false, foil: false, particles: 0 },
  Rare:      { frameColor: "#c0c0c0", glow: true,  foil: false, particles: 5 },
  Epic:      { frameColor: "#9b30ff", glow: true,  foil: false, particles: 15 },
  Legendary: { frameColor: "#ffd700", glow: true,  foil: true,  particles: 30 },
  Mythic:    { frameColor: "#00ffff", glow: true,  foil: true,  particles: 60 },
  Ultimate:  { frameColor: "unique",  glow: true,  foil: true,  particles: 100 },
};
```

**Trade-offs:**
- ✅ Funciona sem Three.js — fallback garantido para mobile
- ✅ Framer Motion já é dependência do projeto
- ⚠️ Efeito foil em CSS é limitado comparado a Three.js

**Questões em aberto:**
- Canvas ou CSS puro para as partículas na fase 2D?
- Dimensão padrão da carta: proporção 63×88mm (padrão físico)?

---

### RFC-004: Three.js Card Renderer

**Fase:** 4

**Problema:** A versão 2D não entrega a sensação premium de objeto físico. Cartas Legendary+ precisam de shaders, parallax real e comportamento de objeto 3D.

**Proposta:**

**Hook principal:** `src/hooks/useCardRenderer.ts`

```ts
export function useCardRenderer(
  canvasRef: RefObject<HTMLCanvasElement>,
  metadata: CardMetadata
) {
  useEffect(() => {
    const renderer = new THREE.WebGLRenderer({ canvas: canvasRef.current!, alpha: true });
    const scene = new THREE.Scene();
    const camera = new THREE.PerspectiveCamera(45, 63/88, 0.1, 100);
    // ... setup cena, geometria do card, shaders
    const animate = () => { requestAnimationFrame(animate); renderer.render(scene, camera); };
    animate();
    return () => { renderer.dispose(); };
  }, [metadata]);
}
```

**Shaders por raridade:**

| Raridade | Shader |
|---|---|
| Common | Phong básico |
| Rare | Phong + rim light |
| Epic | Phong + purple glow uniform |
| Legendary | Holographic foil shader (rainbow iridescence) |
| Mythic | Rainbow foil + animated noise |
| Ultimate | Custom per-card shader |

**Foil shader (GLSL fragment):**
```glsl
uniform sampler2D uTexture;
uniform float uTime;
uniform vec2 uMouse;
varying vec2 vUv;

void main() {
  vec2 uv = vUv;
  float rainbow = sin(uv.x * 10.0 + uTime + uMouse.x * 3.14) * 0.5 + 0.5;
  vec3 foilColor = vec3(rainbow, 1.0 - rainbow, sin(rainbow * 3.14));
  vec4 tex = texture2D(uTexture, uv);
  gl_FragColor = mix(tex, vec4(foilColor, 1.0), 0.3);
}
```

**Mouse tilt:**
```ts
canvas.addEventListener("mousemove", (e) => {
  const rect = canvas.getBoundingClientRect();
  const x = (e.clientX - rect.left) / rect.width - 0.5;  // -0.5 a 0.5
  const y = (e.clientY - rect.top) / rect.height - 0.5;
  card.rotation.y = x * 0.4;
  card.rotation.x = -y * 0.4;
});
```

**Fallback:** Se `WebGL` não disponível (mobile antigo), renderiza `<CardFront>` 2D sem Three.js.

**Trade-offs:**
- ✅ Experiência premium para desktop
- ✅ Shaders isolados em `.glsl` — fácil de editar
- ⚠️ Three.js adiciona ~550KB ao bundle (usar lazy import)
- ⚠️ Mobile com GPU fraca pode ter performance ruim

**Questões em aberto:**
- Usar `drei` (React Three Fiber) ou Three.js vanilla? Sugestão: vanilla para manter isolamento.
- Lazy load Three.js apenas quando a carta for visível (`IntersectionObserver`)?

---

### RFC-005: Booster Pack System

**Fase:** 5

**Problema:** O fluxo atual cria `asset_unit`s diretamente no claim. O novo modelo requer que cartas sejam adquiridas exclusivamente via abertura de Booster Pack, com experiência cinematográfica.

**Proposta:**

**Schema:**
```sql
CREATE TABLE booster_pack (
    id            BIGSERIAL PRIMARY KEY,
    collection_id BIGINT    NOT NULL REFERENCES collection(id),
    bundle_id     BIGINT    NOT NULL REFERENCES asset_bundle(id),
    owner_id      BIGINT    NOT NULL REFERENCES account(id),
    status        VARCHAR   NOT NULL DEFAULT 'UNOPENED' CHECK (status IN ('UNOPENED', 'OPENED')),
    created_at    TIMESTAMP NOT NULL DEFAULT now(),
    opened_at     TIMESTAMP NULL
);

-- Histórico de aberturas para pity system
CREATE TABLE pack_opening_history (
    id              BIGSERIAL PRIMARY KEY,
    account_id      BIGINT    NOT NULL REFERENCES account(id),
    booster_pack_id BIGINT    NOT NULL REFERENCES booster_pack(id),
    cards_received  JSONB     NOT NULL,  -- [{assetId, rarity}]
    opened_at       TIMESTAMP NOT NULL DEFAULT now()
);
```

**Endpoint de abertura:**
```
POST /booster-packs/{id}/open
Authorization: Bearer <token>

Response:
{
  "cardsRevealed": [
    { "assetUnitId": 42, "metadata": {...} },
    { "assetUnitId": 43, "metadata": {...} },
    ...
  ]
}
```

**Engine de probabilidade (Java):**
```java
public class PackProbabilityEngine {
    private static final Map<Rarity, Double> DEFAULT_WEIGHTS = Map.of(
        COMMON, 0.55, RARE, 0.25, EPIC, 0.12,
        LEGENDARY, 0.06, MYTHIC, 0.018, ULTIMATE, 0.002
    );

    public List<Rarity> roll(int cardCount, PityState pity, PackConfig config) {
        // 1. Rolar raridade para cada carta usando weights
        // 2. Aplicar garantias (guaranteed rare, guaranteed event)
        // 3. Aplicar pity: se pity.failedLegendaryCount >= PITY_THRESHOLD, forçar Legendary
        // 4. Retornar lista de raridades
    }
}
```

**Transação de abertura:**
```
BEGIN
  SELECT booster_pack FOR UPDATE WHERE id = ? AND status = 'UNOPENED'
  → falha se já aberto
  roll raridades
  para cada raridade: SELECT asset disponível do bundle com aquela raridade
  INSERT asset_unit (assetId, ownerId)
  UPDATE booster_pack SET status = 'OPENED', opened_at = now()
  INSERT pack_opening_history
COMMIT
```

**Frontend — abertura:**
```
Fase 1 (0-2s):   Pack aparece, hover parallax
Fase 2 (2-3s):   Shake animation (Framer Motion)
Fase 3 (3-4s):   Light leaks nos cantos
Fase 4 (4-5s):   Pack rasga (clip-path animation)
Fase 5 (5-6s):   N cartas face-down voam para a tela
Fase 6 (6s+):    Usuário clica para virar cada carta
Fase 7 (final):  Cartas voam para o inventário
```

**Trade-offs:**
- ✅ Probabilidades ajustáveis sem impacto em packs não abertos
- ✅ Pity system implementável com histórico
- ⚠️ Se `total_supply` esgotar antes de abertura: selecionar carta de raridade diferente ou notificar

**Questões em aberto:**
- Quantas cartas por pack? Sugestão: 5 padrão, 10 para packs especiais.
- Pity threshold: após quantas aberturas sem Legendary garantir uma? Sugestão: 30.

---

### RFC-006: Marketplace Expansion

**Fase:** 6

**Problema:** O marketplace atual lista apenas os preços atuais. Para uma plataforma de trading cards, é essencial mostrar histórico de preços, volume, e estatísticas de mercado.

**Proposta:**

**Novos endpoints:**
```
GET /asset-listings/stats?assetId=&period=7d
→ { minPrice, maxPrice, avgPrice, volume, totalSales, priceChange24h }

GET /asset-units/{id}/price-history?limit=50
→ [{ price, soldAt, seller, buyer }]

GET /asset-listings/recent-sales?collectionId=&limit=20
→ Últimas vendas com metadata da carta
```

**Índices adicionais:**
```sql
-- Histórico de preços por asset e data (queries de analytics)
CREATE INDEX idx_price_history_asset_date
    ON asset_price_history (asset_unity_id, created_at DESC);

-- Listings ativos por raridade (filtro no marketplace)
CREATE INDEX idx_listing_rarity
    ON asset_listing ((
        (SELECT metadata->>'rarity' FROM asset a
         JOIN asset_unit u ON u.asset_id = a.id
         WHERE u.id = asset_listing.asset_unit_id)
    )) WHERE status = 'ACTIVE';
```

**Frontend — novos filtros:**
```ts
interface MarketplaceFilters {
  rarity?: Rarity[];
  category?: string[];
  collectionId?: number;
  minPrice?: number;
  maxPrice?: number;
  sortBy?: "price_asc" | "price_desc" | "recent" | "volume";
}
```

**Charts (Recharts já disponível):**
- `<PriceHistoryChart>` — linha temporal de preços (já existe, expandir)
- `<VolumeChart>` — barras de volume por semana
- `<RarityDistributionChart>` — pizza de raridades no marketplace

**Trade-offs:**
- ✅ Recharts já é dependência — sem dependência nova
- ✅ Índices existentes cobrem a maioria das queries
- ⚠️ Índice de raridade em expression index pode ser lento em tabelas grandes

**Questões em aberto:**
- Cache para stats do marketplace? Redis seria over-engineering neste momento.
- Período padrão do histórico de preços: 7 dias, 30 dias, all-time?

---

### RFC-007: Collections, Achievements e Profile

**Fase:** 7

**Problema:** Usuários não têm motivação além de colecionar cartas aleatórias. Falta progressão, metas e identidade na plataforma.

**Proposta:**

**Schema:**
```sql
-- Progresso do usuário numa coleção
CREATE TABLE user_collection_progress (
    id            BIGSERIAL PRIMARY KEY,
    account_id    BIGINT NOT NULL REFERENCES account(id),
    collection_id BIGINT NOT NULL REFERENCES collection(id),
    cards_owned   INT    NOT NULL DEFAULT 0,
    completed_at  TIMESTAMP NULL,
    UNIQUE (account_id, collection_id)
);

-- Definição de achievement
CREATE TABLE achievement (
    id          BIGSERIAL PRIMARY KEY,
    key         VARCHAR(100) NOT NULL UNIQUE,  -- ex: "OPEN_100_PACKS"
    name        VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    icon        VARCHAR(50)  NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);

-- Achievements desbloqueados por usuário
CREATE TABLE user_achievement (
    id             BIGSERIAL PRIMARY KEY,
    account_id     BIGINT NOT NULL REFERENCES account(id),
    achievement_id BIGINT NOT NULL REFERENCES achievement(id),
    unlocked_at    TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (account_id, achievement_id)
);
```

**Achievements iniciais:**

| Key | Descrição | Trigger |
|---|---|---|
| `FIRST_PACK` | Abriu o primeiro pack | Abertura de qualquer pack |
| `FIRST_LEGENDARY` | Primeira carta Legendary | Reveal de carta Legendary |
| `OPEN_10_PACKS` | 10 packs abertos | Contagem de aberturas |
| `OPEN_100_PACKS` | 100 packs abertos | Contagem de aberturas |
| `FIRST_SALE` | Vendeu pela primeira vez | Conclusão de venda |
| `FIRST_PURCHASE` | Comprou no marketplace | Conclusão de compra |
| `COMPLETE_COLLECTION` | Completou uma coleção | Trigger em `user_collection_progress` |
| `OWN_ALL_MYTHICS` | Possui todas as Mythics | Trigger após aquisição |

**Endpoint de perfil:**
```
GET /profile/{accountId}
→ {
    account: { name, picture, joinedAt },
    stats: { totalCards, totalPacks, totalSales, totalPurchases },
    favoriteCard: CardMetadata,
    mostValuableCard: CardMetadata + currentPrice,
    collections: [{ collection, progress, completedAt }],
    achievements: [{ achievement, unlockedAt }],
    recentActivity: [...]
  }
```

**Trade-offs:**
- ✅ Gamificação aumenta retention
- ✅ Schema simples, fácil de expandir achievements
- ⚠️ `AchievementService.checkAndUnlock()` chamado em múltiplos UseCases — precisa ser rápido

**Questões em aberto:**
- Achievements são globais ou por coleção? Sugestão: ambos.
- Notificação in-app quando achievement é desbloqueado?

---

### RFC-008: Automação — Weekly Pipeline Sem Intervenção Manual

**Fase:** 8

**Problema:** Atualmente bundles são criados manualmente pelo admin. A visão final é zero intervenção humana: toda semana, novas cartas aparecem automaticamente.

**Proposta:**

**Infraestrutura:** AWS Lambda (Python 3.11) + EventBridge Scheduler

**Dependências:**
```
anthropic>=0.25
openai>=1.30
boto3>=1.34
requests>=2.31
pytrends>=4.9       # Google Trends (sem API key, não oficial)
praw>=7.7           # Reddit API
```

**Estrutura do Lambda:**
```python
def lambda_handler(event, context):
    pipeline = WeeklyPipeline()
    pipeline.run()

class WeeklyPipeline:
    def run(self):
        week_id = get_current_week_id()            # "2026-W27"
        if bundle_already_exists(week_id): return  # idempotência

        # 1. Ingestão multi-source
        candidates = []
        candidates += fetch_google_trends()        # trending searches do Google
        candidates += fetch_newsapi_headlines()    # artigos completos
        candidates += fetch_reddit_top_posts()     # discussões mais votadas da semana

        # 2. Claude seleciona os 10 melhores (sem duplicatas, sem tragédias)
        events = claude.select_events(candidates, count=10)

        # 3. Geração de cards
        cards = self.generate_cards(events)        # Claude gera metadata JSON
        cards = self.generate_illustrations(cards) # DALL-E 3 gera imagens
        cards = self.upload_images(cards)          # S3 URLs permanentes
        self.validate_cards(cards)                 # validação de schema

        # 4. Publicar
        self.post_bundle(week_id, cards)           # POST /artifacts/bundles
        self.log_success(week_id, len(cards))
```

**Ingestão multi-source:**
```python
def fetch_google_trends():
    pytrends = TrendReq(hl="en-US", tz=0)
    return pytrends.trending_searches(pn="united_states").values.tolist()

def fetch_newsapi_headlines():
    r = requests.get("https://newsapi.org/v2/top-headlines",
        params={"language": "en", "pageSize": 20, "apiKey": NEWSAPI_KEY})
    return [a["title"] + " — " + a["description"] for a in r.json()["articles"]]

def fetch_reddit_top_posts():
    reddit = praw.Reddit(client_id=REDDIT_CLIENT_ID, client_secret=REDDIT_SECRET,
                         user_agent="rarelines-pipeline/1.0")
    posts = []
    for sub in ["technology", "science", "worldnews", "sports", "business"]:
        posts += [p.title for p in reddit.subreddit(sub).top("week", limit=5)]
    return posts
```

**Retry e resiliência:**
```python
@retry(max_attempts=3, backoff=exponential)
def generate_card_metadata(event): ...

@retry(max_attempts=2, backoff=linear)
def generate_illustration(prompt): ...
```

**Idempotência:** Verificar se já existe um bundle com `identifier = "weekly-{week_id}"` antes de executar. Permite re-invoke seguro sem duplicatas.

**Monitoramento:** CloudWatch Logs automático. EventBridge DLQ + SNS alert em caso de falha de invocação.

**Novos secrets no SSM:**
- `/banksimulator/anthropic_api_key`
- `/banksimulator/openai_api_key`
- `/banksimulator/newsapi_key`
- `/banksimulator/reddit_client_id`
- `/banksimulator/reddit_client_secret`

**Trade-offs:**
- ✅ Serverless — zero manutenção, zero processo rodando
- ✅ Idempotente — pode ser re-invocado com segurança
- ✅ Multi-source aumenta variedade e cobertura de eventos
- ✅ Logs automáticos no CloudWatch
- ✅ Google Trends e Reddit API são gratuitos
- ⚠️ Lambda timeout 15 min — suficiente para 10 cartas, monitorar se escalar
- ⚠️ `pytrends` não é API oficial — pode quebrar se o Google mudar o HTML

**Questões em aberto:**
- Validação humana das cartas antes de publicar? Sugestão: modo dry-run para review opcional.
- Quantas cartas por semana? Sugestão: 10 (balanceia custo e frequência).
- Candidatos para Claude avaliar: top 20 Google Trends + 20 NewsAPI + 25 Reddit = ~65 candidatos → Claude seleciona 10.

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
- Three.js (a ser adicionado na Fase 4)

### Pipeline de IA (`pipeline/`)
- Python 3.11+
- `anthropic` SDK (Claude API)
- `openai` SDK (DALL-E 3)
- `boto3` (S3 upload)

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
| `asset` | Cartas colecionáveis (metadata JSONB, supply controlado) |
| `asset_unit` | Cópias individuais de cada carta (status: AVAILABLE/IN_MARKET/RESERVED/TRANSFERRING) |
| `asset_listing` | Ofertas de venda no marketplace |
| `asset_price_history` | Histórico de preços por listing |
| `asset_bundle` | Pacotes semanais de cartas |
| `asset_transfer` | Histórico de transferências de asset_unit |
| `universe` | Universo temático das coleções (Fase 1) |
| `collection` | Coleção dentro de um universo (Fase 1) |
| `booster_pack` | Packs fechados aguardando abertura (Fase 5) |
| `pack_opening_history` | Histórico de aberturas para pity system (Fase 5) |
| `user_collection_progress` | Progresso do usuário por coleção (Fase 7) |
| `achievement` | Definições de conquistas (Fase 7) |
| `user_achievement` | Conquistas desbloqueadas por usuário (Fase 7) |

O schema de testes em `src/test/resources/schema.sql` é **idêntico** ao de produção.

---

## Sistema de Assets — Documentação Completa

### Conceitos Fundamentais

O sistema de assets é o núcleo do projeto. Existem dois tipos de entidade com papéis distintos:

**`asset`** — o "tipo" do colecionável. Define o que é o item: seu conteúdo (campo `metadata` JSONB com name, rarity, attack, defense, etc.), quantas cópias existem no total (`total_supply`) e quando foi criado. É único — não podem existir dois assets com o mesmo nome.

**`asset_unit`** — uma cópia individual e transferível de um `asset`. É o que o usuário realmente possui. Vários `asset_unit` podem apontar para o mesmo `asset`, cada um com seu próprio dono (`owner_account_id`) e ciclo de vida independente.

**Analogia:** `asset` é a tiragem de uma carta (ex: "AI Titan, 10 cópias"). Cada `asset_unit` é uma das 10 cópias físicas, que pode ser possuída, vendida e transferida individualmente.

---

### Schema das Tabelas

```sql
-- O tipo/modelo da carta colecionável
asset (
    id           BIGSERIAL PRIMARY KEY,
    metadata     JSONB     NOT NULL,              -- conteúdo completo da carta
    total_supply INT       NOT NULL CHECK (total_supply >= 0),
    created_at   TIMESTAMP NOT NULL DEFAULT now()
)

-- Uma cópia individual possuída por uma conta
asset_unit (
    id               BIGSERIAL PRIMARY KEY,
    asset_id         BIGINT            NOT NULL,   -- FK → asset
    owner_account_id BIGINT            NOT NULL,   -- FK → account (dono atual)
    status           asset_unit_status NOT NULL DEFAULT 'AVAILABLE',
    locked_at        TIMESTAMP         NULL,
    created_at       TIMESTAMP         NOT NULL DEFAULT now()
)

-- Status possíveis de asset_unit
AVAILABLE    -- posse normal, pode ser vendido ou listado
IN_MARKET    -- listado para venda no marketplace (não pode ser re-listado)
RESERVED     -- reservado (uso futuro)
TRANSFERRING -- em processo de transferência (uso futuro)

-- Agrupamento semanal de assets (release semanal)
asset_bundle (
    id            BIGSERIAL PRIMARY KEY,
    identifier    VARCHAR(100) NOT NULL UNIQUE,  -- ex: "weekly-2026-W26"
    collection_id BIGINT       NULL REFERENCES collection(id),
    created_at    TIMESTAMP    NOT NULL DEFAULT now()
)

-- Relacionamento bundle ↔ asset (1 asset pertence a exatamente 1 bundle)
asset_bundle_item (
    bundle_id BIGINT NOT NULL,  -- FK → asset_bundle
    asset_id  BIGINT NOT NULL UNIQUE  -- FK → asset (UNIQUE: 1 asset por bundle)
)

-- Oferta de venda de um asset_unit
asset_listing (
    id                BIGSERIAL PRIMARY KEY,
    asset_unit_id     BIGINT         NOT NULL,   -- FK → asset_unit
    seller_account_id BIGINT         NOT NULL,   -- FK → account
    price             NUMERIC(19,2)  NOT NULL CHECK (price > 0),
    status            VARCHAR(50)    NOT NULL,   -- ACTIVE | SOLD | CANCELED
    created_at        TIMESTAMP      NOT NULL DEFAULT now(),
    updated_at        TIMESTAMP      NOT NULL DEFAULT now()
)

-- Histórico de transferências de posse
asset_transfer (
    asset_unit_id   BIGINT NOT NULL,   -- FK → asset_unit
    from_account_id BIGINT NOT NULL,
    to_account_id   BIGINT NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT now()
)

-- Histórico de preços por listing
asset_price_history (
    asset_listing_id      BIGINT         NOT NULL,
    asset_unity_id        BIGINT         NOT NULL,
    old_price             DECIMAL(19,2),
    new_price             DECIMAL(19,2)  NOT NULL,
    changed_by_account_id BIGINT         NOT NULL,
    reason                VARCHAR(50)    NOT NULL,   -- SOLD | LISTED | PRICE_CHANGE
    created_at            TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
)
```

---

### Ciclo de Vida Completo

#### 1. Criação — Bundle → Assets (sem units ainda)

O admin POST para `POST /admin/assets/bundles`:

```json
{
  "identifier": "weekly-2026-W26",
  "assets": [
    { "totalSupply": 3, "metadata": { "name": "AI Titan", "rarity": "Legendary", ... } }
  ]
}
```

Fluxo: `AdminAssetGenerationController` → `CreateAssetBundleUseCase` → `AssetBundleService.createBundle()`

Dentro de **uma única transação**:
1. Insere `asset_bundle` com o `identifier`
2. Para cada item: insere `asset` com `metadata` JSONB e `total_supply`
3. Insere `asset_bundle_item` linkando cada `asset` ao bundle

Nenhum `asset_unit` é criado aqui. Units são criados quando usuários abrem Booster Packs (Fase 5) ou fazem claim diretamente.

---

#### 2. Claim — Usuário recebe uma unit gratuita

POST `/assets/claim` com `{ "assetId": 1 }` + JWT

Fluxo: `ClaimAssetUnityController` → `ClaimAssetUnityUseCase`

1. Valida JWT, obtém conta do usuário
2. Obtém o `asset` pelo `assetId`
3. `accountService.tryClaimAssetUnity(accountNumber)` — verifica se `next_free_asset_at <= now()` e o atualiza (cooldown de claim). Retorna a próxima data de claim.
4. `AssetUnityService.createAssetUnity()`:
   - `AssetDAO.updateTotalSupply(conn, assetId, 1)` — decrementa `total_supply` em 1 (`UPDATE asset SET total_supply = total_supply - 1 WHERE id = ? AND total_supply >= 1`)
   - Retorna 0 linhas afetadas se supply esgotado — falha silenciosamente
   - `AssetUnityDAO.insert()` — cria o `asset_unit` com `owner_account_id = account.id` e `status = AVAILABLE`

**Invariante:** `total_supply` nunca fica negativo (CHECK constraint no banco + WHERE na query).

---

#### 3. Listagem no Marketplace — unit vai para venda

POST `/asset-offers` com `{ "assetUnityId": 5, "price": "10.00" }` + JWT

Fluxo: `AssetOfferController` → `CreateAssetOfferUseCase` → `AssetListingService.createAssetOffer()`

1. `AssetUnityDAO.tryUpdateToMarket(conn, assetUnityId, accountId)`:
   ```sql
   UPDATE asset_unit
   SET status = 'IN_MARKET'
   WHERE id = ? AND owner_account_id = ? AND status = 'AVAILABLE'
   ```
   Retorna `false` se a unit não pertence ao usuário ou não está `AVAILABLE` — proteção atômica.
2. Insere `asset_listing` com `status = ACTIVE`, `price`, `seller_account_id`

---

#### 4. Compra — troca de dono da unit

POST `/asset-listings/{id}/purchase` + JWT

Fluxo: `AssetPurchaseController` → `PurchaseAssetUseCase`

1. Valida JWT, obtém conta do comprador
2. Obtém `asset_listing` pelo id
3. Bloqueia se `listing.sellerAccountId == comprador.id` (não pode comprar de si mesmo)
4. `accountService.transfer(buyerId, sellerId, price)` — deduz saldo do comprador, credita ao vendedor (dentro de transação)
5. `AssetPurchaseService.purchase()`:
   - `AssetUnityDAO.tryTransferOwnership(conn, unitId, sellerId, buyerId)`:
     ```sql
     UPDATE asset_unit
        SET owner_account_id = ?, status = 'AVAILABLE'
      WHERE id = ? AND owner_account_id = ? AND status = 'IN_MARKET'
     ```
   - `asset_listing.status` → `SOLD`
   - Insere `asset_transfer` (histórico)
6. Registra `asset_price_history` com `reason = SOLD`

**Invariante:** a troca de dono é atômica e exige que o status seja `IN_MARKET` e o dono seja o vendedor esperado — impede condições de corrida.

---

#### 5. Cancelar Listagem

POST `/asset-offers/cancel` com `{ "assetListingId": 3 }` + JWT

Fluxo: `AssetListingService.cancelListing()`:
1. Obtém `asset_listing` e verifica `status == ACTIVE`
2. Obtém `asset_unit`, verifica que o dono é o caller
3. `asset_listing.status` → `CANCELED`
4. `asset_unit.status` → `AVAILABLE` (unit volta ao inventário do dono)

---

### Endpoints de Consulta

| Endpoint | Acesso | Descrição |
|---|---|---|
| `GET /assets/bundles?page=&size=` | Público | Lista bundles paginados |
| `GET /assets/bundles/{id}/items?page=&size=` | Público | Assets de um bundle com `totalSupply` atual |
| `GET /asset-listings?page=&pageSize=` | Público | Listings ativos. Se autenticado, exclui próprias listings |
| `GET /asset-listings/me?page=&pageSize=` | Privado | Próprias listings ativas |
| `GET /asset-units/me?page=&pageSize=` | Privado | Próprias units com `status = AVAILABLE` |
| `GET /assets/{assetUnityId}/price-history` | Público | Histórico de preços de uma unit |

---

### Arquivos por Camada

| Camada | Arquivos |
|---|---|
| Domain | `Asset.java`, `AssetUnity.java`, `AssetBundle.java`, `AssetListing.java`, `AssetUnityStatus.java`, `ReasonType.java` |
| DAO | `AssetDAO.java`, `AssetUnityDAO.java`, `AssetBundleDAO.java`, `AssetBundleItemDAO.java`, `AssetListingDAO.java`, `AssetPriceHistoryDAO.java`, `AssetTransferDAO.java` |
| Service | `AssetService.java`, `AssetUnityService.java`, `AssetBundleService.java`, `AssetListingService.java`, `AssetPurchaseService.java`, `AssetPriceHistoryService.java`, `AssetTransferService.java` |
| UseCase | `CreateAssetBundleUseCase`, `ClaimAssetUnityUseCase`, `CreateAssetOfferUseCase`, `CancelAssetOfferUseCase`, `PurchaseAssetUseCase`, `ListAssetBundlesUseCase`, `ListAssetBundleItemsUseCase`, `ListActiveAssetListingsUseCase`, `ListAssetListingsByOwnerUseCase`, `ListAssetUnitsByOwnerUseCase` |
| Controller | `AdminAssetGenerationController`, `AssetQueryController`, `AssetUnityQueryController`, `AssetListingQueryController`, `AssetOfferController`, `AssetPurchaseController`, `ClaimAssetUnityController`, `AssetPriceHistoryController` |

---

### Invariantes e Regras de Negócio

1. `asset.metadata.name` deve ser único globalmente (validação na aplicação)
2. `asset.total_supply` nunca fica negativo — garantido por CHECK constraint e pela cláusula `AND total_supply >= ?` no UPDATE de claim
3. `asset_bundle_item.asset_id` é UNIQUE — cada asset pertence a exatamente um bundle
4. Transições de status de `asset_unit` são atômicas via UPDATE condicional — eliminam race conditions sem lock explícito
5. Um usuário não pode comprar a própria listing
6. Claim tem cooldown por conta (`account.next_free_asset_at`) — o intervalo é configurável
7. Preço de listing deve ser > 0 com no máximo 2 casas decimais
8. Listings no marketplace público excluem as próprias listings do usuário autenticado; se não autenticado, passa `accountId = -1` (sem exclusão)

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

**Secrets adicionais a partir da Fase 2 (pipeline de IA):**

| Variável SSM | Uso |
|---|---|
| `/banksimulator/anthropic_api_key` | Claude API para seleção de eventos e geração de metadata |
| `/banksimulator/openai_api_key` | DALL-E 3 para geração de ilustrações |
| `/banksimulator/newsapi_key` | NewsAPI para artigos completos |
| `/banksimulator/reddit_client_id` | Reddit API para trending posts da semana |
| `/banksimulator/reddit_client_secret` | Reddit API (par com client_id) |

Em produção, o script `fetch-env.py` busca todos esses parâmetros do SSM no startup e grava em `/etc/app.env`, que o systemd carrega via `EnvironmentFile`.

**Porta do servidor:** 5000 (não 8080)

### Propriedades de cookie (configuráveis por profile)

| Propriedade | Produção | Local |
|---|---|---|
| `auth.cookie.domain` | `.alessandro-bezerra.me` | `` (vazio) |
| `auth.cookie.secure` | `true` | `false` |
| `auth.cookie.same-site` | `None` | `Lax` |

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

Para endpoints públicos que aceitam token opcional: usar `AuthCookieService.extractTokenOrNull()` (retorna `null` sem lançar 401).

Endpoints admin usam header `X-Admin-Token` separado.

### Endpoint de sessão

`GET /auth/session` — lê o cookie HttpOnly no servidor e retorna `{ "token": "<jwt>" }` no body. Usado pelo frontend ao montar o `Router` para migrar a sessão do cookie para sessionStorage.

---

## Multi-Sessão

`sessionStorage` é isolado por aba — duas abas com usuários diferentes funcionam independentemente. Ao abrir nova aba, `Router.tsx` chama `initSession()` que consulta `GET /auth/session` para recuperar o JWT do cookie existente.

---

## Segurança Criptográfica

Cada conta possui um par de chaves **RSA 2048 bits**:
- **Chave pública** armazenada na tabela `account.public_key`
- **Chave privada** via `PrivateKeyStorage`:
  - `FilePrivateKeyStorage` — produção: `keys/account-{id}/private.key` relativo ao `WorkingDirectory` do systemd
  - `InMemoryPrivateKeyStorage` — testes e profile `local`

Toda transação é **assinada** com a chave privada do remetente e verificada com a chave pública. A assinatura fica em `transactions.signature`.

---

## Email

`EmailService` é uma **interface** com duas implementações:

| Classe | Profile | Comportamento |
|---|---|---|
| `SesEmailService` | `!local` (produção) | Envia via AWS SES. Usa credenciais do instance profile. |
| `LogEmailService` | `local` | Loga `[LOCAL EMAIL] To: ... | Subject: ...` no console. |

---

## Frontend

Localizado em `frontend/assetstore/`. Rotas principais:

| Rota | Componente | Acesso |
|---|---|---|
| `/` | Landing page (`RareLines`) | Público |
| `/login` | Login local | Público |
| `/register` | Criar conta | Público |
| `/forgot-password` | Solicitar reset de senha | Público |
| `/reset-password` | Redefinir senha | Público |
| `/market` | Marketplace de cartas | Público (ações requerem login) |
| `/reward` | Claim de cartas gratuitas | Público (claim requer login) |
| `/inventory` | Dashboard do usuário | Privado (modal AuthRequired se não autenticado) |

### AuthRequiredModal

Quando usuário não autenticado tenta executar ação que requer login (comprar, claim, ver inventory), aparece `AuthRequiredModal` com botões "Cancel" e "Create account" (→ `/register`). Não há redirect automático para `/login`.

### URL da API

```typescript
// src/config.ts
export const API_URL = import.meta.env.VITE_API_URL ?? "https://api.alessandro-bezerra.me";
```

| Arquivo | Valor |
|---|---|
| `frontend/assetstore/.env` | `VITE_API_URL=https://api.alessandro-bezerra.me` (produção) |
| `frontend/assetstore/.env.local` | `VITE_API_URL=http://localhost:5000` (local, gitignored) |

---

## Como Rodar Localmente

### Backend

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

O profile `local` configura H2 in-memory, `LogEmailService`, `InMemoryPrivateKeyStorage`, CORS liberado para localhost, tokens dummy e cookie sem `Secure`/`Domain`.

### Frontend

```bash
cd frontend/assetstore
npm install
npm run dev   # http://localhost:5173
```

### Google OAuth local

Adicionar no Google Cloud Console (OAuth 2.0 Client `1002611612778-...`) em **Authorized JavaScript origins:**
```
http://localhost
http://localhost:5173
```

---

## Testes

```bash
mvn test
```

Testes de integração com H2 in-memory. Schema de teste idêntico ao de produção.

### Testes Quebrados — Prioridade

1. **`AuthResponse`** agora tem 3 campos (`clientId`, `name`, `token`) — testes com 2 campos falham na compilação.
2. **`AccountOperationsController`** agora recebe `JwtService` no construtor — testes precisam do 3º argumento.
3. **`AuthCookieService`** usa `@Value` para `auth.cookie.*` — testes que instanciam diretamente precisam de `ApplicationContext`.
4. **`AuthController`** tem o novo endpoint `GET /auth/session` — pode precisar de ajuste.

---

## Infraestrutura AWS

| Recurso | Detalhes |
|---|---|
| EC2 t3.micro | IP: `18.226.192.204`, SSH: `ssh -i ~/.ssh/banksimulator ec2-user@18.226.192.204` |
| RDS db.t4g.micro | `banksimulator-db.cbaeaa00azz5.us-east-2.rds.amazonaws.com` |
| Nginx | Reverse proxy: 443/80 → localhost:5000 |
| Let's Encrypt | HTTPS via certbot, renovação automática via cron |
| SSM Parameter Store | `/banksimulator/*` — todos os segredos |
| S3 | `banksimulator-frontend-356892335394` — frontend + imagens de cartas (Fase 2+) |
| CloudFront | `E2P13GEXYNJRCP` — CDN do frontend |
| GitHub OIDC | IAM role `banksimulator-github-frontend` — deploy sem access keys |
| Route53 | `api.` → EC2, `app.` → CloudFront |

**Custo estimado:** ~$27/mês (infra) + ~$1.65/mês (pipeline IA)

---

## CI/CD — GitHub Actions

Workflow em `.github/workflows/deploy.yml`. Dispara em push para branch `prod`.

**deploy-backend:** Maven build → SCP JAR → SSH restart systemd → health check

**deploy-frontend:** `npm ci` + `npm run build` → S3 sync → CloudFront invalidation

---

## Git — Branches

| Branch | Finalidade |
|---|---|
| `prod` | Deploy automático no AWS ao fazer push |
| `dev` | Testes locais, sem CI/CD |
| `master` | Mantido em sincronia com `prod` |

---

## Systemd — Serviço em Produção

Arquivo: `/etc/systemd/system/banksimulator.service`

- `WorkingDirectory=/opt/banksimulator` — necessário para `FilePrivateKeyStorage`
- `ExecStartPre=+/usr/bin/python3 /opt/banksimulator/fetch-env.py us-east-2` — busca secrets do SSM
- `EnvironmentFile=/etc/app.env` — variáveis carregadas no startup

---

## Bugs Corrigidos

| Bug | Arquivo | Descrição |
|---|---|---|
| JWT expirado aceito como válido | `JwtService.java:59` | `isTokenExpired(token)` estava sem negação |
| `AccessDeniedException: /keys` | `FilePrivateKeyStorage.java` | Path relativo resolvia para `/keys`; corrigido com `WorkingDirectory` no systemd |
| AWS SES trava startup local | `EmailService.java` | `SesClient.create()` no construtor falha sem credenciais AWS; resolvido com interface + `LogEmailService` |
| Google login 500 localmente | `application-local.properties` | `google.client-id` estava com valor dummy |
| 401 em `/accounts/me` após login local | `AuthCookieService.java` | Cookie com `Secure=true` não funciona em HTTP localhost |
| Claim 500 — syntax H2 | `AccountDAO.java` | H2 não suporta `INTERVAL '2 minutes'` |
| Claim 500 — RETURNING não suportado | `AccountDAO.java` | H2 não suporta `UPDATE ... RETURNING` |
| 401 no marketplace público | `AuthCookieService.java` / `ListActiveAssetListingsUseCase.java` | `extractToken()` lançava 401 sem autenticação; resolvido com `extractTokenOrNull()` e `accountId = -1` |

---

## Endpoints Admin

### POST /admin/assets/bundles

Cria um bundle de cartas. Requer header `X-Admin-Token`.

```json
{
  "identifier": "weekly-2026-W26",
  "assets": [
    { "totalSupply": 3, "metadata": { "name": "AI Titan", "rarity": "Legendary", ... } }
  ]
}
```

### POST /admin/accounts/deposit

Adiciona saldo a uma conta. Requer header `X-Admin-Token`.

```json
{ "clientId": 1, "amount": "1000.00" }
```

---

## Pontos de Atenção

- `SecurityConfig` está configurado para permitir **todas as requisições** — autenticação é feita manualmente nos use cases.
- Sem paginação em alguns endpoints de listagem.
- Sem rate limiting implementado.
- Sem logs estruturados / observabilidade.
- Chaves RSA em produção ficam em disco em `/opt/banksimulator/keys/`. Se o EC2 for recriado, chaves existentes são perdidas.
- **Testes unitários estão quebrados** — ver seção "Testes Quebrados" acima.
- Three.js ainda não está no projeto — será adicionado na Fase 4.
- Pipeline de IA ainda não existe — será criada na Fase 2.
