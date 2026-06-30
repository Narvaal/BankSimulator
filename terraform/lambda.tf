# ── IAM Role ────────────────────────────────────────────────────────────────

resource "aws_iam_role" "pipeline_lambda" {
  name = "rarelines-pipeline-lambda"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect    = "Allow"
      Principal = { Service = "lambda.amazonaws.com" }
      Action    = "sts:AssumeRole"
    }]
  })
}

resource "aws_iam_role_policy" "pipeline_lambda" {
  name = "rarelines-pipeline-policy"
  role = aws_iam_role.pipeline_lambda.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      # CloudWatch Logs
      {
        Effect   = "Allow"
        Action   = ["logs:CreateLogGroup", "logs:CreateLogStream", "logs:PutLogEvents"]
        Resource = "arn:aws:logs:*:*:*"
      },
      # Bedrock — Claude only (images via Stability AI API)
      {
        Effect = "Allow"
        Action = ["bedrock:InvokeModel"]
        Resource = [
          "arn:aws:bedrock:*::foundation-model/anthropic.claude-sonnet-4-5-20250929-v1:0",
          "arn:aws:bedrock:*:${data.aws_caller_identity.current.account_id}:inference-profile/us.anthropic.claude-sonnet-4-5-20250929-v1:0",
        ]
      },
      # SSM — read pipeline secrets (reddit_* are optional: Lambda silently skips if absent)
      {
        Effect = "Allow"
        Action = ["ssm:GetParameter"]
        Resource = [
          "arn:aws:ssm:${var.aws_region}:${data.aws_caller_identity.current.account_id}:parameter/banksimulator/ADMIN_TRIGGER_TOKEN",
          "arn:aws:ssm:${var.aws_region}:${data.aws_caller_identity.current.account_id}:parameter/banksimulator/newsapi_key",
          "arn:aws:ssm:${var.aws_region}:${data.aws_caller_identity.current.account_id}:parameter/banksimulator/stability_api_key",
          "arn:aws:ssm:${var.aws_region}:${data.aws_caller_identity.current.account_id}:parameter/banksimulator/reddit_client_id",
          "arn:aws:ssm:${var.aws_region}:${data.aws_caller_identity.current.account_id}:parameter/banksimulator/reddit_client_secret",
        ]
      },
      # S3 — upload card illustrations to the frontend bucket
      {
        Effect   = "Allow"
        Action   = ["s3:PutObject"]
        Resource = "${aws_s3_bucket.frontend.arn}/cards/*"
      },
      # SES — send failure alert emails
      {
        Effect   = "Allow"
        Action   = ["ses:SendEmail"]
        Resource = "*"
      },
    ]
  })
}

# ── Lambda Function ──────────────────────────────────────────────────────────
#
# Deploy flow:
#   1. cd pipeline && bash build.sh          — builds pipeline.zip with deps
#   2. terraform apply                        — creates/updates Lambda
#
# After initial apply, update code with:
#   aws lambda update-function-code \
#     --function-name rarelines-pipeline \
#     --zip-file fileb://pipeline/pipeline.zip

resource "aws_lambda_function" "pipeline" {
  function_name = "rarelines-pipeline"
  role          = aws_iam_role.pipeline_lambda.arn
  handler       = "lambda_function.handler"
  runtime       = "python3.11"
  timeout       = 900 # 15 minutes — enough for 10 cards × ~60s each
  memory_size   = 512

  # Terraform uses this file when present; build with: cd pipeline && bash build.sh
  filename         = "${path.module}/../pipeline/pipeline.zip"
  source_code_hash = fileexists("${path.module}/../pipeline/pipeline.zip") ? filebase64sha256("${path.module}/../pipeline/pipeline.zip") : ""

  environment {
    variables = {
      S3_BUCKET      = aws_s3_bucket.frontend.id
      CDN_BASE_URL   = "https://${var.app_domain}"
      API_BASE_URL   = "https://${var.api_domain}"
      SES_FROM       = var.aws_ses_from
      BEDROCK_REGION = var.bedrock_region
    }
  }

  tags = { Name = "rarelines-pipeline" }
}

# ── CloudWatch Log Group ─────────────────────────────────────────────────────

resource "aws_cloudwatch_log_group" "pipeline" {
  name              = "/aws/lambda/rarelines-pipeline"
  retention_in_days = 30
  tags              = { Name = "rarelines-pipeline-logs" }
}

# ── EventBridge Scheduler ────────────────────────────────────────────────────

resource "aws_scheduler_schedule" "pipeline_weekly" {
  name       = "rarelines-pipeline-weekly"
  group_name = "default"

  flexible_time_window {
    mode = "OFF"
  }

  schedule_expression          = var.pipeline_schedule
  schedule_expression_timezone = "UTC"

  target {
    arn      = aws_lambda_function.pipeline.arn
    role_arn = aws_iam_role.scheduler_invoke_pipeline.arn
    input    = jsonencode({ source = "eventbridge-scheduler" })
  }
}

resource "aws_iam_role" "scheduler_invoke_pipeline" {
  name = "rarelines-scheduler-invoke-pipeline"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect    = "Allow"
      Principal = { Service = "scheduler.amazonaws.com" }
      Action    = "sts:AssumeRole"
    }]
  })
}

resource "aws_iam_role_policy" "scheduler_invoke_pipeline" {
  name = "invoke-pipeline-lambda"
  role = aws_iam_role.scheduler_invoke_pipeline.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect   = "Allow"
      Action   = "lambda:InvokeFunction"
      Resource = aws_lambda_function.pipeline.arn
    }]
  })
}

# ── Lambda Permission (allows EventBridge to invoke) ─────────────────────────

resource "aws_lambda_permission" "allow_scheduler" {
  statement_id  = "AllowEventBridgeScheduler"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.pipeline.function_name
  principal     = "scheduler.amazonaws.com"
  source_arn    = aws_scheduler_schedule.pipeline_weekly.arn
}
