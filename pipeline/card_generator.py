"""
AI card generation using Amazon Bedrock (Claude) + Stability AI API (images).
- Claude Sonnet 4.5: event selection + metadata JSON + art direction
- Stability AI SD3 Large: card illustration

Art Direction v2: every image must feel like an impossible photograph frozen at
the most important instant of a real story — never like an illustration. Claude
returns structured creative decisions (moment, protagonist, camera, composition,
momentum, light, mood, style, medium) and Python assembles the final prompt.
Style is the LAST decision, never the first.
"""

import json
import base64
import logging
import random
import re
import requests
from datetime import date

logger = logging.getLogger(__name__)

CLAUDE_MODEL      = "us.anthropic.claude-sonnet-4-5-20250929-v1:0"
IMAGE_MODEL       = "sd3-large"
STABILITY_API_URL = "https://api.stability.ai/v2beta/stable-image/generate/sd3"

EFFECTS_BY_RARITY = {
    "Common":    {"foil": False, "glow": None,      "shimmer": False, "particles": None,        "borderLight": False},
    "Rare":      {"foil": False, "glow": "#c0c0c0", "shimmer": True,  "particles": "minimal",   "borderLight": False},
    "Epic":      {"foil": False, "glow": "#9b30ff", "shimmer": True,  "particles": "medium",    "borderLight": True},
    "Legendary": {"foil": True,  "glow": "#ffd700", "shimmer": True,  "particles": "heavy",     "borderLight": True},
    "Mythic":    {"foil": True,  "glow": "#00ffff", "shimmer": True,  "particles": "cinematic", "borderLight": True},
    "Ultimate":  {"foil": True,  "glow": "#ff0080", "shimmer": True,  "particles": "cinematic", "borderLight": True},
}

RARITY_TO_SUPPLY = {
    "Common": 20, "Rare": 10, "Epic": 5,
    "Legendary": 3, "Mythic": 2, "Ultimate": 1,
}

SELECTION_PROMPT = """You are a curator for RareLines, a premium digital trading card platform that creates cards from real-world events.

SELECTION RULES:
✓ ACCEPT: Technology launches/breakthroughs/IPOs, scientific discoveries, space missions, cultural milestones, sports records, economic mergers/trends, political elections/agreements (without tragedy)
✗ REJECT: Deaths, natural disasters with casualties, wars, epidemics with mortality, violent crimes, mental health/suicide, catastrophic events

COLLECTIBILITY TEST: "Would this event make a card someone would be happy to own?"

From the following news and trends, select exactly {n} events that would make the most compelling, collectible trading cards. Prioritise global relevance, historical significance, and cultural impact.

NEWS/TRENDS:
{news_list}

Return a JSON array of {n} selected events:
[
  {{
    "title": "Brief punchy title (max 30 chars)",
    "description": "2-3 sentences: what happened and why it matters historically.",
    "category": "Technology|Finance|Science|Culture|Sports|Politics",
    "estimated_rarity": "Common|Rare|Epic|Legendary",
    "url": "source URL if available, else empty string"
  }}
]

Return ONLY the JSON array, no markdown, no explanation."""

# ── Art Direction v2 — independent decision modules ──────────────────────────
# The old monolithic ART_STYLES list is gone. Each category is chosen separately,
# and style is one of the LAST decisions, never the first.

STORYTELLING_STYLES = [
    "concept art",
    "editorial illustration",
    "movie key art",
    "splash art",
    "scientific illustration",
    "graphic poster",
    "book cover art",
    "museum reconstruction",
]

CAMERA_LANGUAGES = [
    "handheld documentary",
    "press photographer",
    "sports photography",
    "wildlife photography",
    "macro photography",
    "IMAX cinematography",
    "GoPro action camera",
    "bodycam footage",
    "telephoto",
    "drone",
    "security camera",
    "architecture photography",
    "fashion editorial",
]

LIGHTING_LANGUAGES = [
    "golden hour",
    "storm light",
    "industrial",
    "backlight",
    "firelight",
    "volumetric",
    "studio",
    "moonlight",
]

MOODS = [
    "urgent",
    "hopeful",
    "industrial",
    "sacred",
    "chaotic",
    "triumphant",
    "lonely",
    "dangerous",
]

MEDIUMS = [
    "oil painting",
    "watercolor",
    "woodblock print",
    "risograph print",
    "clay render",
    "voxel art",
    "etching",
    "screen print",
    "paper cut art",
]

METADATA_PROMPT = """You are generating metadata for a premium digital trading card on the RareLines platform.

EVENT:
Title: {title}
Description: {description}
Category: {category}
Estimated rarity: {estimated_rarity}
Card number: {card_number}
Collection: {collection}
Release date: {release_date}
Reference URL: {url}

Generate a complete metadata JSON following the schema below EXACTLY. Respect all character limits.
Do NOT include image-related fields (prompt, chosenStyle, seed, illustration) — the art direction is handled separately.

━━━ TONE — READ THIS BEFORE WRITING ANYTHING ━━━

The voice is acidic, skeptical, and precise. The humor lives in the OBSERVATION, never in the structure.

FORBIDDEN patterns — these are tired and lazy:
  ✗ Comparisons to relatable mundane things ("still less annoying than your CrossFit friend", "like a gym membership you never use")
  ✗ Setup → punchline joke structure ("X happened. And Y is the funny part.")
  ✗ Pop culture references used as punchlines (Netflix, Instagram, TikTok as the joke itself)
  ✗ Explaining the joke ("which means...", "ensuring that...", "turning X into Y")
  ✗ Hyperbole for comic effect ("the most important thing ever", "absolutely nobody asked for")
  ✗ Any sentence that could appear in a BuzzFeed listicle

WHAT WORKS — the discomfort comes from SPECIFICITY and HONESTY:
  ✓ State what actually happened with a word that makes the reader feel something
  ✓ The irony is in the facts themselves, not in the framing
  ✓ Dry. Clinical. Like a coroner's report with opinions.
  ✓ "Four billion dollars to confirm other planets are also mostly empty." ← the joke is the precision
  ✓ "The government officially validates what your doctor was already prescribing under different terminology." ← no punchline, just the truth stated clearly

━━━ SCHEMA AND LIMITS ━━━

- name: max 30 chars
- subtitle: max 60 chars. What actually happened, stated plainly but with one word that carries weight. Not philosophical. Not a punchline. The name is the label — the subtitle is the fact.
  ✗ "The universe built something our models say it couldn't." (too philosophical, echoes the name)
  ✗ "When politics discovers the drug everyone's on anyway" (joke structure)
  ✓ "Astronomers find a 1.3 billion light-year ring the math forbids"
  ✓ "Medicare extends coverage to GLP-1 drugs starting July 2026"
  ✓ "Perseverance rover crosses 42 kilometers on Martian surface"

- category: exactly one of Technology | Finance | Science | Culture | Sports | Politics
- rarity: exactly one of Common | Rare | Epic | Legendary | Mythic | Ultimate

- effects: set automatically based on rarity:
  Common:    foil=false, glow=null, shimmer=false, particles=null, borderLight=false
  Rare:      foil=false, glow="#c0c0c0", shimmer=true, particles="minimal", borderLight=false
  Epic:      foil=false, glow="#9b30ff", shimmer=true, particles="medium", borderLight=true
  Legendary: foil=true, glow="#ffd700", shimmer=true, particles="heavy", borderLight=true
  Mythic:    foil=true, glow="#00ffff", shimmer=true, particles="cinematic", borderLight=true
  Ultimate:  foil=true, glow="#ff0080", shimmer=true, particles="cinematic", borderLight=true

- attributes: influence/innovation/controversy/longevity/reach — all 1-100

- abilities: 1-2 items, name ≤25 chars, description ≤120 chars
  What this card DOES to the world around it — stated as a capability, as if it could activate.
  Imagine two players facing each other: one plays this card. What actually happens? What does this thing inflict, produce, or make impossible for the other side?
  The humor comes from the thing being accurately described, not from wordplay.
  Think: what is the real-world power of this subject, stated plainly?

  ✗ "Forces cosmologists to revise equations they've taught as gospel" — describes effect on abstractions
  ✗ "Theoretical Collapse — Makes the models wrong" — too vague, card-gamey
  ✓ Big Ring: name "Unmapped Territory" / desc "Every star atlas published before 2022 now has a footnote. Every cosmologist who said it was impossible still has tenure."
  ✓ ICE card: name "Jurisdiction Expansion" / desc "Can detain anyone within 100 miles of a border, which is where 2/3 of Americans live."
  ✓ Podcast: name "Zero Vetting" / desc "Reaches more people than the evening news and requires less fact-checking than a WhatsApp message."
  ✓ Medicare drug: name "Price Conversion" / desc "The same molecule costs $900 out-of-pocket or $50 with coverage. The molecule doesn't know the difference."

- passive: name ≤25 chars, description ≤120 chars
  The permanent condition this subject creates that nobody can turn off. Not a buff. The thing that's just true now.
  ✓ "The price is set by whoever needs to justify the price."
  ✓ "Podcasters don't need credentials. They need an RSS feed and something to be angry about."

- weakness: max 80 chars. The thing that limits it or makes it absurd. One sentence, no setup.
  ✓ "Requires the patient to already be inside the system that priced them out."
  ✓ "Only matters if anyone with funding agrees it should."

- flavorText: max 15 words. A sentence that lands like a verdict, not a punchline. Read it aloud — if it sounds like a tweet, rewrite it.
  ✗ "Ran a marathon on Mars. Still less annoying than your CrossFit friend."
  ✓ "The future arrived. Apple priced it so you'd know your place in it."

- lore: max 200 chars. Two sentences maximum. What happened, and what it reveals about the people who let it happen. No Wikipedia summary. No "starting in [year]". Start in the middle.
  ✗ "After years of pharmaceutical companies perfecting billion-dollar drugs and millions ordering them through telehealth loopholes, the federal government decided to make it official."
  ✓ "The drug already existed. The coverage arriving now means someone finally ran the numbers on who votes."

- traits: 2-4 items, name ≤15 chars, value ≤20 chars
- timeline: 2-5 items, date=YYYY-MM-DD, event ≤60 chars
- references: 1-5 URLs (use the provided URL plus any well-known public references)

- collection: "{collection}"
- cardNumber: "{card_number}"
- releaseDate: "{release_date}"
- artist: "RareLines AI"

Return ONLY the JSON object, no markdown fences, no explanation."""


ART_DIRECTION_PROMPT = """You are the art director for RareLines, a premium digital trading card platform.

Your job is NOT to design an illustration.
Your job is to produce an image that feels like a frozen frame of a real story — an impossible photograph captured at the single most important moment of the event.

The viewer must never feel they are looking at an illustration. They must feel someone managed to record an extraordinary instant. The image must immediately raise two questions: "What happened one second before?" and "What happens one second after?" If those questions exist, the image is alive.

EVENT:
Title: {title}
Description: {description}
Category: {category}

Follow this exact reasoning order. Each decision feeds the next. The prompt is a CONSEQUENCE of these decisions — never the starting point.

STEP 1 — FIND THE MOMENT
Which single second of this story deserves to become a card? Choose an instant, never a theme.
✗ "Discovery of a superconductor" → ✓ "the instant the metallic tape finally starts levitating above the magnet"
✗ "New space mission" → ✓ "the second an engineer holds the first returned fragment for the first time"
✗ "New political treaty" → ✓ "the pen touches the paper while an aide discreetly tries to swap the last page"

STEP 2 — CHOOSE THE PROTAGONIST
Not the event — a protagonist: hands, an object, a tool, a machine, an environment, a face, a shadow, a microscopic detail. It must be extremely specific and nameable.
✗ "computer" → ✓ "a disassembled mechanical keyboard showing a single removed keycap"

STEP 3 — CHOOSE THE CAMERA
The camera tells half the story. It must feel like a real camera operated by a real person, and you must say WHY it is positioned that way.
✗ "wide angle" → ✓ "The photographer instinctively dropped to the floor to capture the machine from below."
Pick ONE camera language: {camera_languages}

STEP 4 — CHOOSE THE COMPOSITION
The composition must feel accidental. Not perfect, not symmetrical, not centered. The camera may arrive late, be too close, have someone passing in front, have smoke covering part of the lens, cut off an arm, leave half the object out of frame, feel improvised. The goal is a real capture, not a render.

STEP 5 — ADD MOMENTUM (most important step)
The image must never look still. It must look frozen DURING an action. Always include elements like: dust, flying papers, sparks, hair in the wind, clothes in motion, smoke, water, breaking glass, particles, localized motion blur, objects entering or leaving the frame, foreground blur, depth, natural lens flare. Never a person just "standing".

STEP 6 — CHOOSE ATMOSPHERE AND LIGHT
Pick ONE lighting language: {lighting_languages}
Pick ONE mood: {moods}

STEP 7 — CHOOSE THE ARTISTIC LANGUAGE (last creative decision)
The style must not "match" the subject — it must tell THIS story better. A scientific discovery can work better as a 19th-century etching. A finance story as a constructivist poster. Style exists to reinforce the narrative, never to decorate.
Pick ONE storytelling style: {storytelling_styles}
Pick ONE medium: {mediums}

GOLDEN RULE — the image must look like a photographer had exactly one chance to take it. It can be slightly tilted, have blur, have something cutting across the lens, look improvised. It can never look posed.

BENCHMARK — if a viewer thinks "this looks like AI art", you failed. If they think "how did someone capture exactly that instant?", you succeeded.

FORBIDDEN WORDS anywhere in your output (they produce generic AI images): glowing, crystalline, cosmic, neural network, abstract, futuristic, surreal, ethereal, mystical, otherworldly.
FORBIDDEN SUBJECTS: starfields/planets/rockets for space events; humanoid robots/circuit boards for tech; pills/syringes/stethoscopes for medical; stock charts/coins for finance; flags/podiums/handshakes for politics.

Return ONLY this JSON object (each value in English, no markdown fences):
{{
  "cinematicMoment": "the exact instant being frozen — completes the sentence 'the exact instant when ...' (specific, one second of the story)",
  "protagonist": "the extremely specific, nameable subject of the frame",
  "cameraLanguage": "one full sentence: which camera language (from the list) and WHY the photographer shot it that way",
  "composition": "one full sentence describing the accidental-feeling framing",
  "movement": "one full sentence listing the frozen-mid-action elements in the frame",
  "lighting": "one full sentence describing the light, using the chosen lighting language",
  "mood": "the chosen mood word",
  "storytellingStyle": "the chosen storytelling style from the list",
  "medium": "the chosen medium from the list"
}}"""


def _invoke_claude(bedrock, prompt: str) -> str:
    body = {
        "anthropic_version": "bedrock-2023-05-31",
        "max_tokens": 4096,
        "messages": [{"role": "user", "content": prompt}],
    }
    resp = bedrock.invoke_model(
        modelId=CLAUDE_MODEL,
        body=json.dumps(body),
        contentType="application/json",
        accept="application/json",
    )
    result = json.loads(resp["body"].read())
    return result["content"][0]["text"].strip()


def _invoke_stability(prompt: str, seed: int, stability_api_key: str) -> bytes:
    resp = requests.post(
        STABILITY_API_URL,
        headers={
            "Authorization": f"Bearer {stability_api_key}",
            "Accept": "image/*",
        },
        files={"none": ""},
        data={
            "prompt": prompt,
            "negative_prompt": "text, watermark, signature, logo, frame, border, low quality, posed studio portrait, centered symmetrical composition, sterile render",
            "model": IMAGE_MODEL,
            "output_format": "png",
            "aspect_ratio": "2:3",
            "seed": seed % 4294967294,
        },
        timeout=120,
    )
    if resp.status_code != 200:
        raise RuntimeError(f"Stability AI error {resp.status_code}: {resp.text[:300]}")
    return resp.content


def _clean_json(text: str) -> str:
    """Strip markdown fences if Claude wraps the JSON."""
    text = re.sub(r"^```(?:json)?\s*", "", text.strip())
    text = re.sub(r"\s*```$", "", text.strip())
    return text.strip()


def _sentence(text: str) -> str:
    """Normalize a fragment into a sentence ending with a period."""
    text = str(text).strip()
    if text and text[-1] not in ".!?":
        text += "."
    return text


def _fallback_art_direction(event: dict) -> dict:
    """Random-but-valid art direction if Claude's response can't be parsed."""
    return {
        "cinematicMoment": f"the decisive moment of the story behind '{event['title']}' unfolds in front of an unprepared witness",
        "protagonist": "a single specific object at the center of the event, shown in concrete physical detail",
        "cameraLanguage": f"Shot in {random.choice(CAMERA_LANGUAGES)} language, the photographer reacting with no time to frame properly.",
        "composition": "Off-center, improvised framing with part of the subject cut off by the edge of the frame.",
        "movement": "Dust and small debris hang mid-air, with localized motion blur on the fastest element.",
        "lighting": f"Lighting: {random.choice(LIGHTING_LANGUAGES)}, coming from one committed direction.",
        "mood": random.choice(MOODS),
        "storytellingStyle": random.choice(STORYTELLING_STYLES),
        "medium": random.choice(MEDIUMS),
    }


def generate_art_direction(bedrock, event: dict) -> dict:
    """
    Art Direction v2 — Claude reasons through moment → protagonist → camera →
    composition → momentum → light/mood → artistic language, and returns the
    structured decisions. The final image prompt is assembled in Python.
    """
    prompt = ART_DIRECTION_PROMPT.format(
        title=event["title"],
        description=event.get("description", event["title"]),
        category=event.get("category", "Technology"),
        camera_languages=" | ".join(CAMERA_LANGUAGES),
        lighting_languages=" | ".join(LIGHTING_LANGUAGES),
        moods=" | ".join(MOODS),
        storytelling_styles=" | ".join(STORYTELLING_STYLES),
        mediums=" | ".join(MEDIUMS),
    )
    try:
        raw = _invoke_claude(bedrock, prompt)
        direction = json.loads(_clean_json(raw))
    except Exception as e:
        logger.warning(f"Art direction generation failed ({e}) — using fallback")
        return _fallback_art_direction(event)

    # Fill any missing piece from the fallback so assembly never breaks
    fallback = _fallback_art_direction(event)
    for key, value in fallback.items():
        if not direction.get(key):
            direction[key] = value
            logger.warning(f"Art direction missing '{key}' — filled with fallback")
    return direction


def assemble_image_prompt(direction: dict) -> str:
    """
    The backend is the director of photography: it turns Claude's structured
    creative decisions into the final image prompt. Order is mandatory —
    the moment comes first and the artistic style is only the last layer.
    """
    moment = str(direction["cinematicMoment"]).strip().rstrip(".")
    protagonist = str(direction["protagonist"]).strip().rstrip(".")
    parts = [
        f"The image captures the exact instant when {moment}.",
        f"The protagonist of the frame is {protagonist}.",
        _sentence(direction["cameraLanguage"]),
        _sentence(direction["composition"]),
        _sentence(direction["movement"]),
        _sentence(direction["lighting"]),
        f"The atmosphere feels {str(direction['mood']).strip().rstrip('.')}.",
        f"Storytelling style: {str(direction['storytellingStyle']).strip().rstrip('.')}.",
        f"Artistic medium: {str(direction['medium']).strip().rstrip('.')}.",
        "Captured in a single chance, slightly imperfect, never posed. No text, no watermarks.",
    ]
    return " ".join(parts)


def select_events(bedrock, news_items: list[dict], n: int = 10) -> list[dict]:
    """Ask Claude to select the best n events for card generation."""
    news_list = "\n".join(
        f"[{i+1}] {item['title']} — {item.get('description', '')[:200]}"
        for i, item in enumerate(news_items[:80])  # cap to avoid token limits
    )
    prompt = SELECTION_PROMPT.format(n=n, news_list=news_list)
    raw = _invoke_claude(bedrock, prompt)
    events = json.loads(_clean_json(raw))
    logger.info(f"Claude selected {len(events)} events")
    return events[:n]


def generate_card(
    bedrock,
    event: dict,
    card_number: str,
    week: str,
    stability_api_key: str = "",
) -> tuple[dict, bytes]:
    """
    Generate full metadata + illustration for one event.
    Returns (metadata_dict, image_bytes).
    """
    collection = f"Weekly Digest {week}"
    release_date = date.today().isoformat()

    # Seed generated here — Claude never sees or picks it (avoids repeated defaults)
    seed_int = random.randint(1000000, 9999999)

    # 1. Card text metadata (no image responsibility)
    prompt = METADATA_PROMPT.format(
        title=event["title"],
        description=event.get("description", event["title"]),
        category=event.get("category", "Technology"),
        estimated_rarity=event.get("estimated_rarity", "Rare"),
        card_number=card_number,
        collection=collection,
        release_date=release_date,
        url=event.get("url", ""),
    )
    raw = _invoke_claude(bedrock, prompt)
    metadata = json.loads(_clean_json(raw))

    # Enforce effects based on declared rarity (Claude might deviate)
    rarity = metadata.get("rarity", "Rare")
    if rarity not in EFFECTS_BY_RARITY:
        rarity = "Rare"
        metadata["rarity"] = rarity
    metadata["effects"] = EFFECTS_BY_RARITY[rarity]

    metadata.setdefault("artist", "RareLines AI")
    metadata["model"] = IMAGE_MODEL
    metadata["seed"] = str(seed_int)

    # 2. Art Direction v2 — structured creative decisions, prompt assembled here
    direction = generate_art_direction(bedrock, event)
    metadata["artDirection"] = direction
    metadata["prompt"] = assemble_image_prompt(direction)
    metadata["chosenStyle"] = f"{direction['medium']}, {direction['storytellingStyle']}"

    # 3. Generate illustration
    logger.info(f"Generating illustration for: {metadata.get('name', event['title'])}")
    image_bytes = _invoke_stability(metadata["prompt"], seed_int, stability_api_key)

    # Clear placeholder fields (will be filled by image_uploader)
    metadata["illustration"] = ""
    metadata["background"] = ""

    return metadata, image_bytes


def rarity_supply(rarity: str) -> int:
    return RARITY_TO_SUPPLY.get(rarity, 10)
