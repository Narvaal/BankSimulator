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


def upload_image(s3, image_bytes: bytes, artifact_name: str, bucket: str, cdn_base: str) -> tuple[str, str]:
    """
    Upload a PNG illustration to S3, invalidate CloudFront cache, and return
    (illustration_url, background_url).
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
    logger.info(f"Uploaded to S3: {key}")

    # Invalidate CloudFront so re-generated cards aren't served stale
    try:
        cf = boto3.client("cloudfront", region_name="us-east-1")
        cf.create_invalidation(
            DistributionId=CLOUDFRONT_DISTRIBUTION_ID,
            InvalidationBatch={
                "Paths": {"Quantity": 1, "Items": [f"/{key}"]},
                "CallerReference": f"{slug}-{id(image_bytes)}",
            },
        )
        logger.info(f"CloudFront invalidation created for /{key}")
    except Exception as e:
        logger.warning(f"CloudFront invalidation failed (non-fatal): {e}")

    cdn_base = cdn_base.rstrip("/")
    illustration_url = f"{cdn_base}/{key}"
    background_url   = f"{cdn_base}/{key}"

    return illustration_url, background_url
