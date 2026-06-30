locals {
  ssm_prefix = "/banksimulator"
}

resource "aws_ssm_parameter" "db_url" {
  name  = "${local.ssm_prefix}/DB_URL"
  type  = "String"
  value = "jdbc:postgresql://${aws_db_instance.postgres.endpoint}/bank"
}

resource "aws_ssm_parameter" "db_user" {
  name  = "${local.ssm_prefix}/DB_USER"
  type  = "String"
  value = var.db_user
}

resource "aws_ssm_parameter" "db_password" {
  name  = "${local.ssm_prefix}/DB_PASSWORD"
  type  = "SecureString"
  value = random_password.db.result
}

resource "aws_ssm_parameter" "jwt_secret_key" {
  name  = "${local.ssm_prefix}/JWT_SECRET_KEY"
  type  = "SecureString"
  value = var.jwt_secret_key
}

resource "aws_ssm_parameter" "jwt_expiration_time" {
  name  = "${local.ssm_prefix}/JWT_EXPIRATION_TIME"
  type  = "String"
  value = var.jwt_expiration_time
}

resource "aws_ssm_parameter" "admin_trigger_token" {
  name  = "${local.ssm_prefix}/ADMIN_TRIGGER_TOKEN"
  type  = "SecureString"
  value = var.admin_trigger_token
}

resource "aws_ssm_parameter" "kofi_verification_token" {
  name  = "${local.ssm_prefix}/KOFI_VERIFICATION_TOKEN"
  type  = "SecureString"
  value = var.kofi_verification_token
}

resource "aws_ssm_parameter" "google_client_id" {
  name  = "${local.ssm_prefix}/GOOGLE_CLIENT_ID"
  type  = "String"
  value = var.google_client_id
}

resource "aws_ssm_parameter" "aws_ses_from" {
  name  = "${local.ssm_prefix}/AWS_SES_FROM"
  type  = "String"
  value = var.aws_ses_from
}

resource "aws_ssm_parameter" "app_base_url" {
  name  = "${local.ssm_prefix}/APP_BASE_URL"
  type  = "String"
  value = var.app_base_url
}

resource "aws_ssm_parameter" "webhook_asset_url" {
  name  = "${local.ssm_prefix}/WEBHOOK_ASSET_URL"
  type  = "String"
  value = var.webhook_asset_url
}

# ── Pipeline (Fase 2) ────────────────────────────────────────────────────────

resource "aws_ssm_parameter" "newsapi_key" {
  name  = "${local.ssm_prefix}/newsapi_key"
  type  = "SecureString"
  value = var.newsapi_key
}

resource "aws_ssm_parameter" "reddit_client_id" {
  name  = "${local.ssm_prefix}/reddit_client_id"
  type  = "SecureString"
  value = var.reddit_client_id
}

resource "aws_ssm_parameter" "reddit_client_secret" {
  name  = "${local.ssm_prefix}/reddit_client_secret"
  type  = "SecureString"
  value = var.reddit_client_secret
}
