variable "aws_region" {
  default = "us-east-2"
}

variable "api_domain" {
  default = "api.alessandro-bezerra.me"
}

variable "app_domain" {
  default = "app.alessandro-bezerra.me"
}

variable "acm_certificate_arn" {
  default = "arn:aws:acm:us-east-1:356892335394:certificate/c5f8b2ca-08e8-4eca-bb17-7ae5e3b20276"
}

variable "cert_email" {
  default = "alessandrobezerra100@gmail.com"
}

variable "ssh_public_key" {
  description = "SSH public key for EC2 access. Generate with: ssh-keygen -t ed25519 -f ~/.ssh/banksimulator"
}

variable "db_user" {
  default = "postgres"
}

variable "jwt_secret_key" {
  sensitive = true
}

variable "jwt_expiration_time" {
  default = "86400000"
}

variable "admin_trigger_token" {
  sensitive = true
}

variable "kofi_verification_token" {
  sensitive = true
}

variable "google_client_id" {}

variable "aws_ses_from" {
  default = "no-reply@alessandro-bezerra.me"
}

variable "app_base_url" {
  default = "https://app.alessandro-bezerra.me"
}

variable "webhook_asset_url" {
  default = "https://api.alessandro-bezerra.me/webhook"
}

# ── Pipeline (Fase 2) ────────────────────────────────────────────────────────

variable "newsapi_key" {
  sensitive = true
}

variable "reddit_client_id" {
  sensitive = true
  default   = ""
}

variable "reddit_client_secret" {
  sensitive = true
  default   = ""
}

variable "bedrock_region" {
  default = "us-east-1"
}

variable "pipeline_schedule" {
  description = "EventBridge cron for the weekly pipeline (UTC)"
  default     = "cron(0 8 ? * MON *)"
}
