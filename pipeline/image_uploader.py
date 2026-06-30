"""
S3 image upload helper.
Uploads card illustrations to the frontend S3 bucket, served via CloudFront.
"""

import re
import logging

logger = logging.getLogger(__name__)


def _slugify(name: str) -> str:
    slug = re.sub(r"[^a-z0-9]+", "-", name.lower()).strip("-")
    return slug[:80]


def upload_image(s3, image_bytes: bytes, artifact_name: str, bucket: str, cdn_base: str) -> tuple[str, str]:
    """
    Upload a PNG illustration to S3 and return (illustration_url, background_url).
    background_url reuses the illustration as the background layer for now.
    """
    slug = _slugify(artifact_name)
    key = f"cards/{slug}.png"

    s3.put_object(
        Bucket=bucket,
        Key=key,
        Body=image_bytes,
        ContentType="image/png",
        CacheControl="public, max-age=31536000, immutable",
    )

    cdn_base = cdn_base.rstrip("/")
    illustration_url = f"{cdn_base}/{key}"
    background_url   = f"{cdn_base}/{key}"

    logger.info(f"Uploaded illustration: {illustration_url}")
    return illustration_url, background_url
