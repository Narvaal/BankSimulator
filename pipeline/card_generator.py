"""
AI card generation using Amazon Bedrock (Claude) + Stability AI API (images).
- Claude Sonnet 4.5: event selection + metadata JSON
- Stability AI SD3 Large: card illustration
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

ART_STYLES = [
    # Illustration & Painting
    "oil painting, dramatic chiaroscuro lighting, museum quality",
    "watercolor illustration, loose brushstrokes, paper texture visible",
    "gouache painting, flat graphic shapes, bold color blocks",
    "acrylic painting, impasto texture, palette knife strokes",
    "ink wash painting, sumi-e style, minimalist composition",
    "colored pencil illustration, soft hatching, dreamy tones",
    "pastel drawing, soft blending, impressionistic",
    "crayon illustration, childlike energy, waxy texture, vibrant colors",
    "charcoal drawing, dramatic shadows, rough texture",
    "woodblock print, ukiyo-e style, flat perspective, bold outlines",
    # Digital & 3D
    "3D render, octane render, cinematic lighting, photorealistic",
    "3D render, stylized low-poly, flat shading, geometric shapes",
    "3D render, clay material, matte surface, soft shadows, toy aesthetic",
    "3D render, glass and chrome materials, reflective surfaces",
    "unreal engine 5, hyperrealistic, volumetric lighting, film grain",
    "isometric 3D illustration, flat colors, game asset style",
    "voxel art, pixelated 3D, colorful, crisp edges",
    # Comic & Animation
    "comic book art, Ben-Day dots, bold outlines, Jack Kirby style",
    "manga illustration, screentone shading, dynamic action lines",
    "cel-shaded animation, Disney Renaissance style, expressive",
    "anime key visual, Studio Ghibli aesthetic, painterly backgrounds",
    "Saturday morning cartoon, retro 80s animation style",
    "graphic novel, noir atmosphere, high contrast, Frank Miller style",
    "pop art, Andy Warhol style, flat colors, halftone dots",
    "sticker illustration, clean outlines, bright palette, glossy",
    # Photography & Cinema
    "cinematic photography, anamorphic lens flare, shallow depth of field",
    "editorial photography, studio lighting, high contrast, magazine cover",
    "documentary photography, raw, gritty, available light only",
    "product photography, dramatic shadow, high-end commercial",
    "infrared photography, surreal tones, glowing highlights",
    "lomography, film grain, color leaks, vintage saturation",
    "long exposure photography, light trails, motion blur, night scene",
    # Historical & Movement
    "art nouveau, Alphonse Mucha style, ornate borders, floral motifs",
    "art deco, geometric patterns, gold and black, 1920s glamour",
    "bauhaus design, primary colors, geometric shapes, functional",
    "soviet propaganda poster, bold flat colors, heroic composition",
    "renaissance painting, sfumato technique, classical composition",
    "impressionist painting, Monet style, broken brushwork, light study",
    "surrealist painting, Salvador Dalí style, dreamlike impossible geometry",
    "constructivism, bold diagonals, red and black, avant-garde",
    # Texture & Material
    "linocut print, hand-carved texture, two-color, rough edges",
    "screen print, limited palette, misregistration effect, poster art",
    "risograph print, grainy texture, overlapping color layers",
    "embroidery illustration, thread texture, stitched appearance",
    "stained glass, leaded lines, jewel-toned colors, backlit glow",
    "mosaic, tesserae texture, Byzantine style, gold background",
    "paper cut art, layered silhouettes, shadow depth, craft aesthetic",
    "neon sign aesthetic, glowing tubes, dark background, city night",
    # Retro & Futuristic
    "retrofuturism, 1950s sci-fi illustration, chrome robots, atomic age",
    "synthwave, neon grid, 80s cyberpunk, retrowave sunset",
    "vaporwave aesthetic, pastel purple and pink, glitch, nostalgia",
    "steampunk illustration, brass gears, Victorian era, sepia tones",
    "cassette futurism, analog controls, warm CRT glow, knobs and dials",
    "Y2K aesthetic, chrome gradients, iridescent, early internet energy",
    "solarpunk, lush vegetation, community warmth, optimistic future light",
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

Available art styles (pick exactly ONE that best fits this event's theme and mood):
{art_styles}

Generate a complete metadata JSON following the schema below EXACTLY. Respect all character limits.

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
- model: "sd3-large"
- chosenStyle: the exact string of the art style you selected from the list above

- prompt: Stable Diffusion image prompt. Structure it as: [chosen art style], [dynamic composition], [specific scene], [camera/lens], [lighting], [mood]. No text, no watermarks.

  COMPOSITION RULES — the image must feel alive and specific:
  • One moment. One character or subject. Not a collage of symbols.
  • Never center a static object. Static = dead.
  • Choose one composition: subject caught mid-motion | extreme close-up filling the frame | worm's eye view looking up | bird's eye aerial | Dutch angle 30° | subject at rule-of-thirds edge | foreground element framing distant subject
  • Choose one lens: macro | wide-angle distortion | telephoto compression | fisheye | tilt-shift | anamorphic cinematic

  THE GOLDEN RULE — never illustrate the phenomenon. Illustrate the moment a person encountered it.
  The Big Ring is not a picture of a ring in space. It's an astronomer at 2am whose coffee is cold because she hasn't moved since the data printed.
  A superconductor discovery is not a glowing crystal. It's an engineer's hands holding a strip of gray tape over a magnet, watching it float.
  A drug policy is not a pill. It's a pharmacist's stamp mid-descent on a Medicare form.

  THE OBJECT MUST BE SPECIFIC AND NAMEABLE:
  ✓ "skeletal robotic frame" — you know exactly what this looks like
  ✓ "strip of superconducting tape hovering above a magnet on a lab bench"
  ✓ "thermal printout of spectral data being unrolled"
  ✗ "glowing crystalline lattice structure" — the AI draws any generic prism
  ✗ "impossible cosmic ring bending through space" — the AI draws generic space art
  ✗ "abstract geometric energy" — meaningless, produces noise

  FORBIDDEN OUTPUTS — if your prompt contains these words, rewrite it:
  • "glowing" (unless it literally glows, like neon or fire)
  • "crystalline", "lattice", "structure", "formation" (too abstract)
  • "cosmic", "celestial", "galactic", "stellar", "nebula" (space cliché)
  • "neural network", "circuit board", "binary", "data stream" (tech cliché)
  • "flowing fabric of spacetime", "bending reality", "impossible geometry" (Dalí cliché)
  • "abstract", "ethereal", "mystical", "otherworldly"

  FORBIDDEN SUBJECTS BY CATEGORY:
  • Space events: no starfields, no rocket launches, no floating planets, no cosmic structures
  • Tech/AI events: no humanoid robots, no circuit boards, no blue glowing networks
  • Medical events: no pills on tables, no syringes floating, no stethoscopes
  • Finance events: no stock charts, no falling coins, no suited men at desks
  • Political events: no flags, no podiums, no handshakes

  STYLE × SUBJECT — pair unexpected styles with subjects. The contrast creates tension:
  • Space discovery → soviet propaganda poster (not Dalí — Dalí + space = default AI output)
  • Drug policy → ukiyo-e woodblock
  • Tech event → watercolor illustration
  • Financial event → 1950s retrofuturist illustration
  • Political event → children's crayon or risograph

  THE BENCHMARK: the best card in this batch shows factory worker's hands assembling a skeletal robotic frame from below (worm's eye), soviet poster colors, harsh shadows, movement in the hands. That is the target — specific object, human hands, concrete action, strong composition.

- seed: a random 7-digit number as a string

Return ONLY the JSON object, no markdown fences, no explanation."""


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
            "negative_prompt": "text, watermark, signature, blurry, low quality, cartoon, anime, ugly, distorted, faces",
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

    art_styles_block = "\n".join(f"- {s}" for s in ART_STYLES)
    prompt = METADATA_PROMPT.format(
        title=event["title"],
        description=event.get("description", event["title"]),
        category=event.get("category", "Technology"),
        estimated_rarity=event.get("estimated_rarity", "Rare"),
        card_number=card_number,
        collection=collection,
        release_date=release_date,
        url=event.get("url", ""),
        art_styles=art_styles_block,
    )

    raw = _invoke_claude(bedrock, prompt)
    metadata = json.loads(_clean_json(raw))

    # Claude sometimes nests image fields under a "visual" sub-object — flatten it
    visual = metadata.pop("visual", None)
    if isinstance(visual, dict):
        for field in ("prompt", "seed", "chosenStyle", "model"):
            if field not in metadata or not metadata[field]:
                if field in visual:
                    metadata[field] = visual[field]
        logger.info("Flattened nested 'visual' sub-object from Claude output")

    # Guarantee an art style is present (fallback to random if Claude omitted it)
    if not metadata.get("chosenStyle"):
        metadata["chosenStyle"] = random.choice(ART_STYLES)
        logger.warning("Claude did not choose an art style — assigned randomly")

    # Enforce effects based on declared rarity (Claude might deviate)
    rarity = metadata.get("rarity", "Rare")
    if rarity not in EFFECTS_BY_RARITY:
        rarity = "Rare"
        metadata["rarity"] = rarity
    metadata["effects"] = EFFECTS_BY_RARITY[rarity]

    # Ensure artist and model fields
    metadata.setdefault("artist", "RareLines AI")
    metadata["model"] = IMAGE_MODEL

    # Generate illustration
    illustration_prompt = metadata.get("prompt", f"Digital trading card art for {event['title']}, cinematic, dramatic lighting, no text")
    seed_str = metadata.get("seed", str(random.randint(1000000, 9999999)))
    try:
        seed_int = int(seed_str)
    except (ValueError, TypeError):
        seed_int = random.randint(1000000, 9999999)
        metadata["seed"] = str(seed_int)

    logger.info(f"Generating illustration for: {metadata.get('name', event['title'])}")
    image_bytes = _invoke_stability(illustration_prompt, seed_int, stability_api_key)

    # Clear placeholder fields (will be filled by image_uploader)
    metadata["illustration"] = ""
    metadata["background"] = ""

    return metadata, image_bytes


def rarity_supply(rarity: str) -> int:
    return RARITY_TO_SUPPLY.get(rarity, 10)
