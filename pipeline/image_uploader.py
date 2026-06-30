"""
S3 image upload helper.
Uploads card illustrations to the frontend S3 bucket, served via CloudFront.
"""

import re
import logging
import boto3

logger = logging.getLogger(__name__)

CLOUDFRONT_DISTRIBUTION_ID = "E2P13GEXYNJRCP"


def _slugify(name: str) -> str:
    slug = re.sub(r"[^a-z0-9]+", "-", name.lower()).strip("-")
    return slug[:80]


def upload_image(s3, image_bytes: bytes, artifact_name: str, bucket: str, cdn_base: str, seed: str = "") -> tuple[str, str]:
    """
    Upload a PNG illustration to S3 and return (illustration_url, background_url).
    Seed is included in the filename so each generation gets a unique, immutable URL.
    """
    slug = _slugify(artifact_name)
    # Seed in filename = unique URL per generation = no cache invalidation needed
    suffix = f"-{seed}" if seed else ""
    key = f"cards/{slug}{suffix}.png"

    s3.put_object(
        Bucket=bucket,
        Key=key,
        Body=image_bytes,
        ContentType="image/png",
        CacheControl="public, max-age=31536000, immutable",
    )
    logger.info(f"Uploaded to S3: {key}")

    cdn_base = cdn_base.rstrip("/")
    illustration_url = f"{cdn_base}/{key}"
    background_url   = f"{cdn_base}/{key}"

    return illustration_url, background_url
