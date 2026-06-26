resource "aws_route53_record" "api" {
  zone_id         = data.aws_route53_zone.main.zone_id
  name            = var.api_domain
  type            = "A"
  ttl             = 300
  records         = [aws_eip.app.public_ip]
  allow_overwrite = true
}

resource "aws_route53_record" "app" {
  zone_id         = data.aws_route53_zone.main.zone_id
  name            = var.app_domain
  type            = "A"
  allow_overwrite = true

  alias {
    name                   = aws_cloudfront_distribution.frontend.domain_name
    zone_id                = aws_cloudfront_distribution.frontend.hosted_zone_id
    evaluate_target_health = false
  }
}
