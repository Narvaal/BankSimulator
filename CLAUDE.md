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
  "artist": "RareLines AI", "model": "stability-ai-sd3-ultra", "chosenStyle": "soviet propaganda poster", "prompt": "Futuristic spatial computing headset...", "seed": "4829301"
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

**`flavorText`** — ironia seca, verdade desconfortável, especificidade que dói:
> *"The future arrived. Apple priced it so you'd know your place in it."*
> *"Four billion dollars to confirm other planets are also mostly empty."*

**Proibido em `flavorText`:** setup→punchline (estilo de piada), comparações com cultura pop, adjetivos entusiasmados ("stunning", "incredible"), conclusões filosóficas amplas. Tom clínico/seco é melhor que tom humorístico forçado.

**`subtitle`** — factual. O que aconteceu. Uma palavra carregando peso.
> ✅ *"Astronomers find a 1.3 billion light-year ring the math forbids"*  
> ❌ *"The universe built something our models say it couldn't"* (editorial — reframing, não fato)

**`abilities`** — efeito real no mundo, não mecânica de jogo. Imaginável como ativável numa batalha de argumentos.
> ✅ *"Can detain anyone within 100 miles of a border, which is where 2/3 of Americans live."*  
> ❌ *"Forces cosmologists to revise equations."* (abstrato, não playable)

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
↓ Claude via AWS Bedrock seleciona 10 eventos mais relevantes
↓ Claude via Bedrock gera metadata JSON (texto) por evento
  (seed gerado em Python — Claude não escolhe seed)
↓ Claude via Bedrock gera art direction estruturada por evento (ADR-013)
↓ Python monta o prompt final (assemble_image_prompt — estilo é a última camada)
↓ Stability AI SD3 Ultra gera ilustração por carta (REST API)
↓ S3 armazena imagens com nome {slug}-{seed}.png (URL única por geração)
↓ POST /artifacts/bundles → backend cria bundle + artifacts
↓ Boosters gerados para distribuição
```

**Execução:** AWS Lambda (Python 3.11) + EventBridge Scheduler (toda segunda, 08:00 UTC)

**Custo estimado/semana (~10 cartas):** ~$0.65 (Bedrock Claude ~$0.01 + Stability AI SD3 Ultra ~$0.065/imagem × 10; Lambda/S3 desprezível)

**Deploy da Lambda:** CI/CD **não** faz deploy da Lambda. Após mudanças em `pipeline/`:
```bash
cd pipeline && bash build.sh
aws lambda update-function-code --function-name rarelines-pipeline --zip-file fileb://pipeline.zip
```

**Por que não X/Twitter API:** Trending topics exige tier Basic ($100/mês). Google Trends + Reddit cobrem gratuitamente.

**Atenção — bundle identifier único por semana:** Re-rodar a pipeline na mesma semana causa `unique constraint violation` em `artifact_bundle_identifier_key`. Solução: deletar o bundle anterior no RDS antes de re-rodar.

### Geração de Imagens — Art Direction v2

**Filosofia:** a imagem nunca deve parecer uma ilustração. Deve parecer uma fotografia impossível congelada no instante mais importante de uma história real. Benchmark: se o usuário pensa "isso é arte de IA", falhou; se pensa "como alguém registrou exatamente esse instante?", funcionou.

**Ordem obrigatória de raciocínio** (o prompt é consequência, nunca ponto de partida):
1. Momento cinematográfico (um instante, nunca um tema) → 2. Protagonista (específico e nomeável) → 3. Câmera (linguagem real + porquê) → 4. Composição (acidental, nunca posada) → 5. Momentum (congelada durante ação: poeira, faíscas, motion blur localizado) → 6. Luz + atmosfera → 7. Linguagem artística (última decisão) → 8. Prompt final.

**Arquitetura:** Claude devolve um objeto `artDirection` estruturado (`cinematicMoment`, `protagonist`, `cameraLanguage`, `composition`, `movement`, `lighting`, `mood`, `storytellingStyle`, `medium`) em uma chamada separada da de metadata. O Python (`assemble_image_prompt`) é o "diretor de fotografia" que monta o prompt final a partir dessas peças — o estilo entra apenas como última camada. O objeto inteiro é persistido em `metadata.artDirection`; `chosenStyle` = `"{medium}, {storytellingStyle}"`.

**Módulos independentes** (substituem a antiga lista `ART_STYLES` de 55 estilos): `STORYTELLING_STYLES` (8) · `CAMERA_LANGUAGES` (13) · `LIGHTING_LANGUAGES` (8) · `MOODS` (8) · `MEDIUMS` (9). Cada categoria é escolhida separadamente. Estilo não deve "combinar" com o assunto — deve contar melhor aquela história.

**Proibido por categoria:**
- Tech → robôs humanoides, circuit boards
- Medical → pílulas, seringas, stethoscopes
- Space → starfields, planetas, foguetes
- Finance → gráficos de bolsa, moedas
- Politics → bandeiras, púlpitos, apertos de mão

**Palavras proibidas no prompt:** glowing, crystalline, cosmic, neural network, abstract, futuristic, surreal, ethereal, mystical, otherworldly

---

## Renderização de Cartas — Frontend

**Composição em camadas** (cada uma é componente React independente, animado separadamente):
```
Glass → Reflection → Foil → Particles → Frame → Illustration → Background
```

### Frente da carta — "RareLines TCG" museum-label spec (ADR-014)

`ArtifactCardFront` (`ArtifactCard.tsx`) segue o princípio "a arte é sempre a heroína, a UI é uma etiqueta de museu flutuando por cima". Seis regiões, sem sobreposição, ritmo vertical de 8px:

1. **Header** — chip glass esquerdo (`#cardNumber` + ícone de categoria via `CATEGORY_ICONS`) e chip de raridade à direita (ícone `SparklesIcon` + cor via `RARITY_ACCENT`).
2. **Title row** — nome (até 3 linhas, `clamp()` fluido) + `flavorText` em itálico direto sobre a arte (sem caixa); radar de atributos à direita, dentro de um quadrado glass, com altura acompanhando o bloco nome+texto (`self-stretch`).
3. **Abilities** — três cards glass iguais (Passive + até 2 Abilities), estilo Apple feature card.
4. **Weakness** — cápsula horizontal única (ícone + label + valor truncado em 1 linha).
5. **Metadata bar** — `traits` (2-4) como colunas editoriais com separador, sem caixas.
6. **Quote** — reservado (ver histórico; atualmente o flavorText vive na title row, não há mais quote separada no rodapé).

**Canvas:** cantos de 24px, borda de 1px cuja cor muda por raridade (`RARITY_ACCENT`: cinza/azul/roxo/dourado/ciano/rosa) com glow quase invisível via `boxShadow`. Roxo (`ACCENT_PURPLE`) é a única cor de destaque fixa, usada apenas no polígono do radar e nos micro-labels das abilities — nunca varia por raridade.

**Glassmorphism** (`GLASS_STYLE`, usado em todos os painéis flutuantes): `background: rgba(20,20,25,0.35)`, `border: rgba(255,255,255,0.12)`, `backdropFilter: blur(20px)`.

**Anti-overflow por design:** todo texto variável (título, descrições de ability, weakness, traits, quote) usa `line-clamp`/`truncate` — o card nunca cresce além da altura fixa (`min(75vh, 640px)`), a filosofia é "nunca explicar tudo na frente, o resto fica no verso".

**Verso (`ArtifactCardBack`):** Lore · Traits · Timeline · Sources (oculta se `references` vier vazio/só espaços) · AI Info — sempre expandido, sem botão de toggle, sem caixa própria (seções flat como Lore/Traits). Auto-shrink: `useLayoutEffect` mede `scrollHeight` vs `clientHeight` do container e ativa modo compacto (fonte menor) na seção AI Info se o conteúdo ultrapassar a altura fixa do card, evitando ativar o scroll interno.

**Radar de atributos (`AttributeRadar`):** SVG holográfico transparente, sem fundo opaco quando fora de um painel glass — traços brancos, polígono preenchido em roxo translúcido, `drop-shadow` para legibilidade sobre qualquer imagem de fundo.

**Three.js:** mouse tilt 3D · parallax por camada · foil shader GLSL · glow pulsante · partículas por raridade · iluminação dinâmica · idle float. Cartas Mythic/Ultimate têm shaders exclusivos. Fallback 2D se WebGL indisponível. **Ainda não implementado** (Fase 4).

---

## Roadmap de Implementação

### ✅ Fase 1 — Domain Refactor (completa)
- ✅ `metadata JSONB` na tabela `artifact`, campo `text` removido
- ✅ Entidades `Universe` e `Collection` no schema
- ✅ APIs de bundle/criação aceitam `metadata` completo
- ✅ `GET /artifact-listings`, `GET /artifact-units/me`, `GET /artifact-units/{id}` retornam `metadata`

### ✅ Fase 2 — AI Pipeline (completa)
- ✅ Ingestão multi-source: Google Trends + NewsAPI + Reddit API
- ✅ Bedrock Claude: seleção de eventos + geração de metadata JSON com tom ácido
- ✅ Stability AI SD3 Ultra: ilustrações via REST API
- ✅ AWS Lambda (Python 3.11) + EventBridge Scheduler (toda segunda, 08:00 UTC)
- ✅ S3 com filenames baseados em seed (URLs imutáveis por geração)
- ✅ Art Direction v2 (ADR-013): objeto `artDirection` estruturado, prompt montado em Python, estilo como última camada

### Fase 3 — Card Rendering Engine (2D) — parcial ✅
- ✅ `ArtifactCard.tsx`: `ArtifactCardThumb` (grid) + `ArtifactCardDetail` (modal/detalhe) + `ArtifactCardFront`/`ArtifactCardBack` (fullscreen) com todos os campos
- ✅ Variantes visuais por raridade (badge, border, glow)
- ✅ `ArtifactCardFront`: redesign completo "museum-label" (ADR-014) — header, title row (nome+quote+radar), abilities glass cards, weakness capsule, metadata bar, tudo com line-clamp anti-overflow
- ✅ `ArtifactCardBack`: AI Info sempre expandido com auto-shrink por overflow, Sources oculta se vazia
- ✅ Seções: ilustração hero, atributos com barras (detail) / radar holográfico (front), abilities, passive, weakness, lore, traits, timeline, sources, AI Info
- ✅ Integrado em: Inventory, Marketplace, Reward, ArtifactDetail, ProfilePage
- Animação de flip frente/verso — pendente
- Composição em camadas com Framer Motion — pendente
- `ArtifactCardThumb` (grid pequeno) ainda não segue o novo visual "museum-label" — fora de escopo por enquanto (não cabe abilities/weakness/metadata bar num tile pequeno)

### Fase 4 — Three.js
- Shaders GLSL: foil, reflection, glow
- Mouse tilt + parallax por camada, partículas, shaders exclusivos Mythic/Ultimate

### Fase 5 — Booster Packs
- Entidade `booster_pack` no banco, engine de probabilidade
- Pity system com histórico de aberturas
- Animação cinematográfica de abertura (Framer Motion + Three.js)

### Fase 6 — Marketplace
- ✅ Filtros no marketplace: artifactId, busca por nome, ordenação, faixa de preço
- ✅ Transfer log filtrado por artifact (via URL params do ArtifactDetail)
- ✅ Filtro por raridade desbloqueado (Fase 1 completa — `metadata JSONB` disponível)
- Histórico de preços com gráficos (Recharts já disponível)
- Volume e analytics por artifact

### Fase 7 — Collections, Achievements, Profile
- ✅ Perfil público: `GET /accounts/{id}/profile` + rota `/profile/:accountId`
- ✅ Busca de usuários: `GET /accounts/search` + rota `/search`
- Coleções com barra de progresso, achievements desbloqueáveis

### Fase 8 — Automação
- ✅ Pipeline Lambda semanal rodando (EventBridge cron segunda 08:00 UTC)
- ✅ Logs CloudWatch + alerta SES em caso de falha
- Deploy da Lambda ainda manual (ver seção Pipeline)

---

## ADRs — Architecture Decision Records

### ADR-001: JSONB para Metadata de Cartas (Fase 1)
**Decisão:** `metadata JSONB` na tabela `asset`, removendo `text`. Backend deserializa para `Map<String, Object>` ou record `CardMetadata`.  
✅ Zero migrações futuras · Pipeline gera JSON diretamente · Frontend renderiza sem mudança de API  
⚠️ Queries por atributo precisam de índices JSONB · Validação de schema é responsabilidade da aplicação

### ADR-002: AWS Bedrock (Claude) + Stability AI SD3 Ultra (Fase 2)
**Decisão:** Claude via AWS Bedrock (`anthropic.claude-sonnet-4-5`) para metadata JSON. Stability AI SD3 Ultra REST API para ilustrações (sem SDK oficial). Imagens no S3.  
✅ Bedrock elimina chave de API Anthropic direta · SD3 Ultra ~$0.065/imagem (6x mais barato que DALL-E 3) · Pipeline desacoplada do backend  
⚠️ Stability AI não tem SDK Python oficial — chamada HTTP direta · Bedrock disponível apenas em us-east-1 (separado da região principal us-east-2)

### ADR-011: Seed-based S3 Filenames para Cache Permanente (Fase 2)
**Decisão:** Imagens salvas como `cards/{slug}-{seed}.png` em vez de `cards/{slug}.png`. Seed gerado em Python antes de chamar Claude, injetado no prompt como `{seed}`, e sempre sobrescrito após o parse (Claude não controla o seed).  
✅ `CacheControl: immutable` + URL única = browser nunca precisa re-fetch · Sem invalidação de CloudFront necessária · Cada re-geração tem URL diferente  
⚠️ Imagens de gerações antigas ficam no S3 indefinidamente (custo desprezível)

### ADR-012: Art Style Selection via Lista Injetada no Prompt (Fase 2) — SUPERSEDED por ADR-013
**Decisão:** 55 estilos artísticos (7 famílias) são injetados no prompt de metadata. Claude escolhe 1 estilo por carta e retorna em `chosenStyle`. Python atribui estilo aleatório como fallback se Claude omitir. Flattening defensivo do sub-objeto `visual` caso Claude o crie.  
✅ Variedade visual sem intervenção manual · `chosenStyle` persistido no metadata = reproduzível · Fallback robusto  
⚠️ Claude às vezes aninha `prompt/seed/chosenStyle` em sub-objeto `visual` — código deve fazer flatten

### ADR-013: Art Direction v2 — Objeto Estruturado + Prompt Montado em Python (Fase 2)
**Decisão:** O `METADATA_PROMPT` perde toda responsabilidade de imagem. Uma segunda chamada ao Claude (`ART_DIRECTION_PROMPT`) segue a ordem obrigatória momento → protagonista → câmera → composição → momentum → luz/mood → linguagem artística e devolve um objeto estruturado. `assemble_image_prompt()` em Python monta o prompt final (estilo é a última camada). A lista `ART_STYLES` foi substituída por 5 módulos independentes (`STORYTELLING_STYLES`, `CAMERA_LANGUAGES`, `LIGHTING_LANGUAGES`, `MOODS`, `MEDIUMS`). Objeto persistido em `metadata.artDirection`.  
✅ Direção de arte evolui sem reescrever prompt monolítico · Decisões criativas estruturadas e auditáveis · Fallback por campo (nunca quebra a montagem)  
⚠️ 2 chamadas ao Claude por carta (custo ainda desprezível) · Flatten do sub-objeto `visual` não é mais necessário (campos de imagem saíram do metadata prompt)  
⚠️ Bug corrigido: `cinematicMoment` às vezes vinha com "the exact instant when..." já embutido, duplicando a frase no prompt montado (`assemble_image_prompt` agora limpa esse prefixo redundante via regex defensivo, além da instrução ter sido reforçada)

### ADR-015: Art Direction v2 — Correção de Qualidade (mãos deformadas e fotos "de qualquer jeito") (Fase 2)
**Contexto:** primeiro lote real gerado com a ADR-013 saiu com mãos deformadas (fraqueza conhecida de modelos de difusão em close-ups) e clima de "foto tirada de qualquer jeito" em vez de "fotografia premiada" — o request do usuário para parecer "instante real e imperfeito" foi interpretado longe demais na direção de câmeras mundanas.  
**Decisão:** Removidas da lista `CAMERA_LANGUAGES` as opções mundanas (`handheld documentary`, `GoPro action camera`, `bodycam footage`, `security camera`); mantidas/priorizadas as cinematográficas (imprensa, editorial, telephoto, drone, war correspondent). Mãos deixam de ser escolha padrão de protagonista — só entram quando nada mais conta a história, e devem ficar parcialmente obscurecidas (desfoque, silhueta, ângulo, luvas). "Imperfeito" agora significa candidamente dramático (nível Pulitzer/World Press Photo), nunca mal composto. Negative prompt do Stability reforçado contra `deformed hands, extra fingers, fused fingers, mutated hands, bad anatomy`. Moods trocaram `industrial`/`chaotic` por `epic`/`awe-inspiring`.  
✅ Testado localmente antes e depois do fix (Bedrock + Stability reais) — resultado sem mãos deformadas, composição dramática  
⚠️ Ainda existe risco residual de mãos em cenas onde o protagonista é claramente uma mão (ex: produto sendo desembalado) — mitigado, não eliminado

### ADR-016: `--exclude "cards/*"` no Deploy do Frontend (Fase 2/Fase 8) — bug crítico corrigido
**Contexto:** o workflow de deploy fazia `aws s3 sync frontend/assetstore/dist/ s3://bucket --delete` no MESMO bucket onde a pipeline salva ilustrações em `cards/`. Sem exclusão, todo deploy do frontend apagava a pasta `cards/` inteira — destruindo as imagens de **todas** as cartas já geradas (incluindo bundles reais da pipeline semanal, não só testes). Descoberto porque imagens pararam de aparecer em produção depois de múltiplos deploys de frontend na mesma sessão.  
**Decisão:** `.github/workflows/deploy.yml` → `aws s3 sync ... --delete --exclude "cards/*"`. Imagens já perdidas foram restauradas regenerando com o mesmo `prompt`+`seed` já salvos no metadata (mesma URL exata, sem precisar alterar o banco).  
✅ Deploy de frontend nunca mais apaga imagens de cartas · Recuperação sem downtime nem migração de dados  
⚠️ Bug pode ter apagado imagens silenciosamente antes desta sessão também, sempre que houve deploy de frontend após geração de cartas — vale monitorar cartas "sem imagem" por precaução

### ADR-014: Front Card Redesign — "RareLines TCG" Museum-Label Spec (Fase 3)
**Decisão:** `ArtifactCardFront` reescrito do zero seguindo spec fornecida pelo usuário: arte sempre em full-bleed, UI como "etiqueta de museu" flutuando por cima, 6 regiões sem sobreposição (header, title row, abilities, weakness, metadata bar, quote). Borda fina (1px) colorida por raridade com glow quase invisível (`RARITY_ACCENT`), painéis glassmorphism idênticos (`GLASS_STYLE`) para header/abilities/weakness/radar. Roxo é cor de destaque fixa (radar + micro-labels), independente da raridade. Todo texto variável usa `line-clamp`/`truncate` para nunca ultrapassar a altura fixa do card.  
✅ Validado com Playwright em casos normais, extremos (textos no limite de cada campo) e mínimos, incluindo mobile — `scrollHeight === clientHeight` sempre, garantindo zero overflow por design  
⚠️ `ArtifactCardThumb` (grid pequeno) não foi atualizado — não cabe abilities/weakness/metadata bar num tile pequeno, fica para decisão futura

### ADR-017: Quality Gates — Husky + Conventional Commits + google-java-format + JaCoCo 90%
**Contexto:** o repo não tinha nenhuma automação de qualidade: sem hooks, ESLint falhando com 8 erros, formatação Java inconsistente, cobertura de 43.6% e CI fazendo deploy com `-DskipTests`.  
**Decisão:** (1) **Husky** na raiz (`package.json` raiz mínimo) com pre-commit **condicional por área staged** — `*.java`/`pom.xml` → `mvn spotless:check test jacoco:check`; `frontend/assetstore/**` → `npm run lint && npm test`; commit só de docs passa direto. (2) **commitlint** no hook commit-msg **bloqueia** mensagens fora do Conventional Commits; commits devem ser atômicos. (3) **Spotless = google-java-format 1.25.2** (estilo GOOGLE) — codebase inteiro reformatado (225 arquivos). (4) **JaCoCo com gate de 90% de linhas** (BUNDLE) no pre-commit e no CI (`mvn verify` no deploy, antes `-DskipTests`); exclusões justificadas: `Application`, `application/config/**`, `SesEmailService`. Cobertura levada de 43.6% → **90.3%** com 157 testes novos (259 no total). Frontend: Vitest + React Testing Library (21 testes) + `npm run test:coverage` sem gate.  
✅ Escrever os testes expôs 2 bugs reais de produção (ver Bugs Corrigidos) · Deploy nunca mais sobe código sem testes · Histórico de commits padronizado  
⚠️ Pre-commit de mudança Java leva ~30-40s (testes + gate) — bypass consciente via `--no-verify` · Padrões de teste: `DbTestSupport` (H2 + seeds), `TestJwt`, `RecordingEmailService` em `src/test/java/br/com/ale/support/`

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

### ADR-009: SQL Dinâmico com List<Object> + instanceof Pattern Matching
**Decisão:** Filtros opcionais em queries SQL são construídos com `StringBuilder` + `List<Object> params`. O bind no `PreparedStatement` usa Java 17 instanceof pattern matching: `if (p instanceof Long l) stmt.setLong(...)`. Sort usa whitelist interna mapeada para SQL — nunca interpolação direta.  
✅ Type-safe sem ORM · Zero SQL injection · Compatível com H2 e PostgreSQL  
⚠️ Queries complexas ficam verbosas — aceitar como custo do JDBC puro

### ADR-010: URL Params como Handshake Cross-Route
**Decisão:** Páginas que precisam passar contexto entre si (ex: ArtifactDetail → Marketplace) usam query params na URL (`?artifactId=X&artifactText=Y`). A página destino lê os params no mount via `useSearchParams()` e aplica o filtro sem estado global.  
✅ Bookmarkable · Back/forward funciona · Sem Context API ou Zustand  
⚠️ Label do artifact viaja na URL — não é problema aqui pois é dado público de exibição

### ADR-008: Pipeline como AWS Lambda + EventBridge (Fase 8)
**Decisão:** Lambda (Python 3.11) + EventBridge `cron(0 8 ? * MON *)`. Lê secrets do SSM, busca notícias multi-source, gera conteúdo com IA, chama `POST /artifacts/bundles` via `X-Admin-Token`. Logs no CloudWatch; falhas disparam SES.  
✅ Serverless · Zero manutenção · Falha na pipeline não afeta o backend  
⚠️ Lambda timeout 15 min — suficiente para 10 cartas; `pytrends` não é API oficial

---

## Stack Tecnológico

**Backend:** Java 17 + Spring Boot 3.3.2 · Maven · JDBC puro (sem ORM/JPA) · HikariCP · JWT via jjwt 0.12.5 · AWS SDK 2.25.28 (SES) · BCrypt · Spring Boot DevTools (hot reload)

**Frontend** (`frontend/assetstore/`): React 19 + TypeScript 5.9 · Vite · Tailwind CSS 4 + Framer Motion · React Router 7 · React Query 5 · Recharts · Three.js (Fase 4)

**Pipeline** (`pipeline/`): Python 3.11+ · `boto3` (Bedrock + S3 + SSM + SES) · Stability AI REST API (HTTP direto) · `pytrends` · `praw` · `requests`

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
| `artifact` | Tipo de artifact colecionável (`metadata JSONB`, `total_supply`, `created_at`) |
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

**`artifact`** — o "tipo" do artifact: `metadata JSONB`, `total_supply`, `created_at`. Único globalmente. O campo `metadata` contém nome, raridade, ilustração, atributos, habilidades, lore e todos os campos de exibição da carta.

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
| `GET /artifact-listings` | Público | Listings ativos com filtros opcionais (ver abaixo) |
| `GET /artifact-listings/me` | Privado | Próprias listings ativas |
| `GET /artifact-units/me` | Privado | Próprias units AVAILABLE |
| `GET /artifact-units/{id}` | Público | Instância específica: nome, owner, status, price history, ownership chain |
| `GET /artifact-transfers` | Público | Feed público de transferências — aceita `?artifactId=` para filtrar por tipo |
| `GET /artifacts/{id}/price-history` | Público | Histórico de preços por unit |
| `GET /accounts/{accountId}/profile` | Público | Perfil público: nome, foto, account number |
| `GET /accounts/search` | Público | Busca contas por nome (`?q=&page=&pageSize=`, mín 2 chars) |

### Filtros de `GET /artifact-listings`

Query params opcionais — todos combináveis:

| Param | Tipo | Comportamento |
|---|---|---|
| `artifactId` | `Long` | Filtra pelo tipo de artifact (mesmo artifact, units diferentes) |
| `q` | `String` | Busca parcial no nome (ILIKE, mín 2 chars) |
| `sort` | `String` | `newest` (padrão) · `price_asc` · `price_desc` |
| `minPrice` | `BigDecimal` | Preço mínimo (inclusivo) |
| `maxPrice` | `BigDecimal` | Preço máximo (inclusivo) |

Implementação: SQL dinâmico via `ArtifactListingFilter` record + `List<Object> params` com instanceof pattern matching para bind type-safe. `sort` usa whitelist interna — valores inválidos caem no default `newest`.

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
| `/banksimulator/stability_api_key` | Stability AI SD3 Ultra (pipeline) |
| `/banksimulator/newsapi_key` | NewsAPI (pipeline) |
| `/banksimulator/reddit_client_id` | Reddit API client ID (pipeline, opcional) |
| `/banksimulator/reddit_client_secret` | Reddit API secret (pipeline, opcional) |

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
| `/search` | Público — Busca usuários por nome (debounce 400ms, mín 2 chars) |
| `/profile/:accountId` | Público — Perfil público: avatar, nome, account number, inventário (somente leitura) |

`AuthRequiredModal` aparece quando não autenticado tenta ação protegida. Botões "Cancel" e "Create account" (→ `/register`). Sem redirect automático para `/login`.

### Navegação entre páginas com contexto

`ArtifactDetail` → "View in Market" leva para `/market?artifactId=X&artifactText=Y` — o marketplace lê os params no mount, aplica o filtro automaticamente e exibe um chip removível.

`ArtifactDetail` → "Transfer Log" leva para `/logs?artifactId=X&artifactText=Y` — a página de logs filtra o feed e exibe o nome do artifact no header com botão para limpar.

Esse padrão (URL params como handshake entre páginas) é a convenção do projeto para passagem de contexto cross-route.

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
mvn test                                  # backend — integração com H2, schema idêntico ao de produção
cd frontend/assetstore && npm run test    # frontend — Vitest + React Testing Library (jsdom)
```

Frontend: config de teste no bloco `test` do `vite.config.ts`, setup em `src/test/setup.ts` (jest-dom + cleanup + clear do sessionStorage). Suites em `*.test.ts(x)` ao lado do código.

**Atenção — Maven vs JAVA_HOME:** o `mvn` da distro pode escolher um JRE sem `javac` (falha com "release version 17 not supported"). Solução: derivar `JAVA_HOME` do `java` do PATH (o hook de pre-commit já faz isso automaticamente):

```bash
export JAVA_HOME=$(dirname $(dirname $(readlink -f $(command -v java))))
```

## Qualidade de Código — Hooks, Formatação e Cobertura

- **Husky** (`.husky/`, instalado via `package.json` raiz + `npm install`): verificações **apenas das áreas tocadas** pelo commit — `*.java`/`pom.xml` staged → `mvn spotless:check test jacoco:check`; arquivos em `frontend/assetstore/` staged → `npm run lint && npm test`. Commit só de docs passa direto. Bypass consciente: `git commit --no-verify`.
- **Conventional Commits obrigatórios** (`.husky/commit-msg` + commitlint): mensagens fora do padrão (`feat:`, `fix:`, `test:`, `build:`, `style:`, `docs:`, `ci:`, `chore:`...) são **bloqueadas**. Commits devem ser atômicos (uma mudança lógica por commit).
- **Spotless = google-java-format** (`spotless-maven-plugin` no `pom.xml`, GJF 1.25.2 estilo GOOGLE): `mvn spotless:check` verifica, `mvn spotless:apply` reformata. Todo o codebase já foi reformatado.
- **JaCoCo — gate de 90% de linhas no backend** (`jacoco-maven-plugin` no `pom.xml`): `mvn test jacoco:report jacoco:check` falha se a cobertura de linhas do bundle ficar abaixo de 90%. Exclusões justificadas: `Application` (main), `application/config/**` (wiring Spring), `SesEmailService` (exige AWS real). Relatório em `target/site/jacoco/index.html`. O gate roda no pre-commit e no CI (`mvn verify` no deploy).
- **ESLint** (`frontend/assetstore`): `npm run lint` — deve ficar em 0 erros (warnings de `exhaustive-deps` tolerados).
- **Vitest coverage** (frontend, sem gate): `npm run test:coverage`.

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

**Custo estimado:** ~$27/mês (infra) + ~$2.60/mês (pipeline IA — ~$0.65/semana × 4)

---

## CI/CD — GitHub Actions

Workflow `.github/workflows/deploy.yml`. Dispara em push para branch `prod`.

- **deploy-backend:** Maven build → SCP JAR → SSH restart systemd → health check
- **deploy-frontend:** `npm ci` + build → S3 sync (`--delete --exclude "cards/*"` — o exclude é obrigatório, ver ADR-016) → CloudFront invalidation

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
| CI build falha — verbatimModuleSyntax | `*.tsx` (5 arquivos) | `tsconfig.app.json` tem `"verbatimModuleSyntax": true`; imports de tipo devem usar `import { type Foo }`. tsc local não capturava pois usava `tsconfig.json`, não `tsconfig.app.json` |
| Browser serve imagens antigas | `image_uploader.py` | `CacheControl: immutable` + mesmo nome de arquivo = browser nunca re-fetcha. Fix: seed no nome do arquivo (`{slug}-{seed}.png`) — cada geração tem URL única |
| Claude sempre usa seed `7294816` | `card_generator.py` | O valor de exemplo no schema virou o default de Claude. Fix: seed gerado em Python antes da chamada, injetado via `{seed}` no prompt, sempre sobrescrito após parse |
| Claude aninha campos em sub-objeto `visual` | `card_generator.py` | Claude às vezes retorna `{ "visual": { "prompt": ..., "seed": ..., "chosenStyle": ... } }`. Fix: flatten defensivo do sub-objeto após parse JSON |
| Lambda executando código antigo | `pipeline/` | CI/CD não faz deploy da Lambda. Mudanças só chegam com `bash build.sh && aws lambda update-function-code` manual |
| Unique constraint ao re-rodar pipeline na mesma semana | `lambda_function.py` | Identifier é `weekly-{YYYY-W##}` — reutilizado na mesma semana. Fix: deletar bundle anterior no RDS antes de re-rodar |
| AI Info cortava texto (letras como "g" decepadas) | `ArtifactCard.tsx` | `line-height` apertado (1.05) + `line-clamp` cortava descenders de glifos. Fix: `leading-[1.15]` + mais espaçamento antes do flavorText |
| Prompt de imagem duplicava "the exact instant when..." | `card_generator.py` | Claude às vezes já incluía essa frase dentro de `cinematicMoment`, e `assemble_image_prompt` a repetia. Fix: instrução reforçada + limpeza defensiva via regex do prefixo redundante |
| **Deploy do frontend apagava todas as imagens de cartas** | `.github/workflows/deploy.yml` | `aws s3 sync ... --delete` no mesmo bucket onde a pipeline salva `cards/*.png` — sem exclusão, todo deploy do frontend apagava a pasta inteira. Fix: `--exclude "cards/*"`. Imagens perdidas foram restauradas regenerando com o mesmo prompt+seed salvos no metadata (ver ADR-016) |
| Insert de transação era descartado silenciosamente | `TransactionService.java` | `createTransaction` abria transação (`autoCommit=false`), inseria e retornava **sem commit** — o insert era revertido no fechamento da conexão. Encontrado pelos testes da ADR-017. Fix: `conn.commit()` após o insert |
| Webhook Ko-fi respondia 500 para token inválido | `KofiWebhookController.java` | Token errado lançava `IllegalAccessError`, que estende `Error` e escapava do `catch (Exception)` → 500 não tratado. Fix: `IllegalArgumentException` → 400 com "Invalid Token" |
| `mvn test` falhava com "release version 17 not supported" | ambiente (Fedora) | O `mvn` da distro escolhia o `java-25-openjdk`, que é um **JRE sem javac**. Fix: derivar `JAVA_HOME` do `java` do PATH (Corretto 21) — o pre-commit já faz isso; a nota antiga sobre "Surefire fork" estava desatualizada |

---

## Pontos de Atenção

- `SecurityConfig` permite **todas as requisições** — autenticação é feita manualmente nos use cases.
- Sem rate limiting implementado.
- Sem logs estruturados / observabilidade.
- Chaves RSA em `/opt/banksimulator/keys/`. Se o EC2 for recriado, chaves existentes são perdidas.
- 259 testes backend (90.3% de linhas, gate JaCoCo ≥90%) · 21 testes frontend. O antigo BUILD FAILURE do fork do Surefire não se reproduz mais — se `mvn test` falhar com "release version 17 not supported", é o `JAVA_HOME` apontando para JRE sem javac (ver seção Testes).
- Three.js ainda não está no projeto — Fase 4.
- Pipeline de IA rodando em produção (Fase 2 completa). Deploy da Lambda é manual — CI não atualiza o código.
- Existem bundles antigos no banco (`identifier` tipo "staff 😘", "colleague 🇷", "luck 🐰") sem `prompt`/`seed`/`illustration` — dados de teste sem imagem de IA real, não são da pipeline. Não confundir com bundles legítimos (`weekly-{YYYY-W##}`) ao investigar problemas de imagem.
- Monitorar cartas "sem imagem" em produção — o bug do ADR-016 (deploy de frontend apagando `cards/` no S3) pode ter causado perdas silenciosas antes de ser corrigido nesta sessão.

---

## Ferramentas de Desenvolvimento

### seed-local.sh

Script para popular o banco H2 local com dados de teste. Cria accounts, artifacts, units, listings e transferências para facilitar o desenvolvimento sem precisar passar pelo fluxo manual completo.

```bash
./seed-local.sh
```

Requer o backend rodando em `localhost:5000` com profile `local`.
