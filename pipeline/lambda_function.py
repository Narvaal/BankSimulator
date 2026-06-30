"""
RareLines Weekly Card Pipeline — AWS Lambda entry point.

Triggered every Monday 08:00 UTC by EventBridge Scheduler.
Flow:
  1. Fetch trending news (Google Trends + NewsAPI + Reddit)
  2. Claude selects top 10 events
  3. For each event: Claude generates metadata + Stable Image Ultra generates illustration
  4. Illustrations uploaded to S3 (served via CloudFront)
  5. POST /artifacts/bundles → backend creates bundle
  6. On failure: SES alert email
"""

import json
import logging
import os
import traceback
from datetime import datetime

import boto3
import requests

from news_fetcher import fetch_all_news
from card_generator import select_events, generate_card, rarity_supply
from image_uploader import upload_image

logger = logging.getLogger()
logger.setLevel(logging.INFO)


def _ssm(ssm, name: str) -> str:
    resp = ssm.get_parameter(Name=f"/banksimulator/{name}", WithDecryption=True)
    return resp["Parameter"]["Value"]


def _send_failure_alert(ssm, error: Exception) -> None:
    try:
        ses_from = os.environ.get("SES_FROM", "no-reply@alessandro-bezerra.me")
        ses = boto3.client("ses", region_name=os.environ.get("AWS_DEFAULT_REGION", "us-east-2"))
        ses.send_email(
            Source=ses_from,
            Destination={"ToAddresses": [ses_from]},
            Message={
                "Subject": {"Data": "[RareLines] Pipeline failed"},
                "Body": {
                    "Text": {
                        "Data": f"Weekly card pipeline failed.\n\nError:\n{traceback.format_exc()}"
                    }
                },
            },
        )
    except Exception as alert_error:
        logger.error(f"Failed to send alert email: {alert_error}")


def handler(event, context):
    region       = os.environ.get("AWS_DEFAULT_REGION", "us-east-2")
    bedrock_region = os.environ.get("BEDROCK_REGION", "us-east-1")
    bucket       = os.environ["S3_BUCKET"]
    cdn_base     = os.environ["CDN_BASE_URL"]
    api_base     = os.environ["API_BASE_URL"].rstrip("/")

    ssm     = boto3.client("ssm",              region_name=region)
    bedrock = boto3.client("bedrock-runtime",  region_name=bedrock_region)
    s3      = boto3.client("s3",               region_name=region)

    try:
        admin_token       = _ssm(ssm, "ADMIN_TRIGGER_TOKEN")
        newsapi_key       = _ssm(ssm, "newsapi_key")
        stability_api_key = _ssm(ssm, "stability_api_key")

        try:
            reddit_client_id     = _ssm(ssm, "reddit_client_id")
            reddit_client_secret = _ssm(ssm, "reddit_client_secret")
        except Exception:
            logger.info("Reddit credentials not found in SSM — skipping Reddit source")
            reddit_client_id = reddit_client_secret = None
    except Exception as e:
        logger.error(f"Failed to load SSM parameters: {e}")
        raise

    week = datetime.utcnow().strftime("%Y-W%V")
    bundle_identifier = f"weekly-{week}"

    logger.info(f"Starting pipeline for bundle: {bundle_identifier}")

    n_cards = int(event.get("n", 10))

    try:
        # 1. Fetch news
        news = fetch_all_news(newsapi_key, reddit_client_id, reddit_client_secret)
        if not news:
            raise RuntimeError("No news fetched from any source")

        # 2. Select events via Claude
        events = select_events(bedrock, news, n=n_cards)
        if not events:
            raise RuntimeError("Claude returned no events")

        # 3. Generate cards
        artifacts = []
        errors    = []

        for i, ev in enumerate(events, 1):
            card_number = str(i).zfill(3)
            logger.info(f"[{card_number}/010] Generating: {ev.get('title', '?')}")

            try:
                metadata, image_bytes = generate_card(bedrock, ev, card_number, week, stability_api_key)

                illustration_url, background_url = upload_image(
                    s3, image_bytes, metadata["name"], bucket, cdn_base,
                    seed=metadata.get("seed", ""),
                )
                metadata["illustration"] = illustration_url
                metadata["background"]   = background_url

                artifacts.append({
                    "metadata":    metadata,
                    "totalSupply": rarity_supply(metadata["rarity"]),
                })
                logger.info(f"[{card_number}/010] Done — {metadata['name']} ({metadata['rarity']})")

            except Exception as card_err:
                logger.error(f"[{card_number}/010] Card generation failed: {card_err}")
                errors.append({"card": card_number, "event": ev.get("title"), "error": str(card_err)})

        if not artifacts:
            raise RuntimeError(f"All {len(events)} cards failed to generate")

        if errors:
            logger.warning(f"{len(errors)} card(s) failed: {errors}")

        # 4. Create bundle via backend
        resp = requests.post(
            f"{api_base}/artifacts/bundles",
            json={"identifier": bundle_identifier, "assets": artifacts},
            headers={
                "X-Admin-Token":  admin_token,
                "Content-Type":   "application/json",
            },
            timeout=30,
        )
        resp.raise_for_status()
        bundle = resp.json()
        logger.info(f"Bundle created: {bundle}")

        summary = {
            "bundleId":     bundle_identifier,
            "backendId":    bundle.get("id"),
            "cardsCreated": len(artifacts),
            "cardsFailed":  len(errors),
            "errors":       errors,
        }
        logger.info(f"Pipeline complete: {summary}")
        return {"statusCode": 200, "body": json.dumps(summary)}

    except Exception as e:
        logger.error(f"Pipeline failed: {traceback.format_exc()}")
        _send_failure_alert(ssm, e)
        raise
