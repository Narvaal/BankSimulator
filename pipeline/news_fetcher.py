"""
Multi-source news fetcher: Google Trends + NewsAPI + Reddit.
Returns a deduplicated list of news items suitable for card generation.
"""

import logging
import time

logger = logging.getLogger(__name__)

REJECT_KEYWORDS = [
    "death", "killed", "dead", "murder", "shooting", "bomb", "attack",
    "suicide", "overdose", "crash", "disaster", "flood", "earthquake",
    "hurricane", "wildfire", "casualties", "fatalities", "victims",
    "genocide", "war crimes", "execution", "mass shooting", "terrorist",
    "epidemic", "pandemic", "outbreak", "funeral", "mourning",
]

NEWSAPI_CATEGORIES = ["technology", "science", "sports", "business", "entertainment"]

REDDIT_SUBREDDITS = [
    "technology", "science", "space", "worldnews",
    "sports", "Futurology", "business",
]


def _is_acceptable(title: str, description: str = "") -> bool:
    text = (title + " " + description).lower()
    return not any(kw in text for kw in REJECT_KEYWORDS)


def fetch_google_trends() -> list[dict]:
    """Top trending topics from Google Trends (pytrends, unofficial)."""
    try:
        from pytrends.request import TrendReq
        pt = TrendReq(hl="en-US", tz=0, timeout=(10, 25))
        trending = pt.trending_searches(pn="united_states")
        topics = trending[0].tolist()[:20]
        return [
            {"title": t, "description": t, "source": "google_trends"}
            for t in topics
            if _is_acceptable(t)
        ]
    except Exception as e:
        logger.warning(f"Google Trends unavailable: {e}")
        return []


def fetch_newsapi(api_key: str) -> list[dict]:
    """Top headlines from NewsAPI across relevant categories."""
    import requests

    items = []
    for category in NEWSAPI_CATEGORIES:
        try:
            resp = requests.get(
                "https://newsapi.org/v2/top-headlines",
                params={"category": category, "language": "en", "pageSize": 10},
                headers={"X-Api-Key": api_key},
                timeout=15,
            )
            resp.raise_for_status()
            articles = resp.json().get("articles", [])
            for a in articles:
                title = a.get("title") or ""
                desc = a.get("description") or ""
                if _is_acceptable(title, desc):
                    items.append({
                        "title": title,
                        "description": desc,
                        "source": "newsapi",
                        "url": a.get("url", ""),
                    })
        except Exception as e:
            logger.warning(f"NewsAPI category '{category}' failed: {e}")

        time.sleep(0.5)  # respect rate limits

    return items


def fetch_reddit(client_id: str, client_secret: str) -> list[dict]:
    """Hot posts from curated subreddits via PRAW."""
    try:
        import praw

        reddit = praw.Reddit(
            client_id=client_id,
            client_secret=client_secret,
            user_agent="RareLinesPipeline/1.0",
        )

        items = []
        for sub_name in REDDIT_SUBREDDITS:
            try:
                sub = reddit.subreddit(sub_name)
                for post in sub.hot(limit=5):
                    title = post.title
                    if _is_acceptable(title) and not post.stickied:
                        items.append({
                            "title": title,
                            "description": post.selftext[:300] if post.selftext else title,
                            "source": "reddit",
                            "url": f"https://reddit.com{post.permalink}",
                        })
            except Exception as e:
                logger.warning(f"Reddit r/{sub_name} failed: {e}")

        return items
    except Exception as e:
        logger.warning(f"Reddit unavailable: {e}")
        return []


def fetch_all_news(newsapi_key: str, reddit_client_id: str, reddit_client_secret: str) -> list[dict]:
    """Fetch and deduplicate news from all sources."""
    all_items = []

    logger.info("Fetching Google Trends...")
    all_items.extend(fetch_google_trends())

    logger.info("Fetching NewsAPI...")
    all_items.extend(fetch_newsapi(newsapi_key))

    logger.info("Fetching Reddit...")
    all_items.extend(fetch_reddit(reddit_client_id, reddit_client_secret))

    # Deduplicate by normalised title
    seen = set()
    unique = []
    for item in all_items:
        key = item["title"].lower()[:60]
        if key not in seen:
            seen.add(key)
            unique.append(item)

    logger.info(f"Total unique news items: {len(unique)}")
    return unique
