resource "aws_route53_record" "api" {
  zone_id         = data.aws_route53_zone.main.zone_id
  name            = var.api_domain
  type            = "A"
  ttl             = 300
  records         = [aws_eip.app.public_ip]
  allow_overwrite = true
}
