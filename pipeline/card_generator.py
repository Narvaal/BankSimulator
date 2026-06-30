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

SCHEMA AND LIMITS:
- name: max 30 chars
- subtitle: max 60 chars
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
- passive: name ≤25 chars, description ≤120 chars
- weakness: max 80 chars
- flavorText: max 15 words, ironic/cynical/sharp tone ("The future arrived. Apple priced it so you'd know your place in it.")
- lore: max 300 chars
- traits: 2-4 items, name ≤15 chars, value ≤20 chars
- timeline: 2-5 items, date=YYYY-MM-DD, event ≤60 chars
- references: 1-5 URLs (use the provided URL plus any well-known public references)

- collection: "{collection}"
- cardNumber: "{card_number}"
- releaseDate: "{release_date}"
- artist: "RareLines AI"
- model: "amazon.titan-image-generator-v2:0"
- prompt: A detailed Stable Diffusion prompt for a cinematic digital trading card illustration representing this event. Focus on visual imagery, dramatic lighting, no text, no watermarks. Include art style directives.
- seed: a random 7-digit number as a string

TONE GUIDE:
- flavorText: reveal uncomfortable truths or ironic angles ("A financial revolution, mostly used to make the already-rich slightly richer.")
- weakness: can be sharp ("Depends on people continuing to care, which history suggests is optimistic.")
- Avoid: humor about individuals by name, sarcasm about tragedies

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
