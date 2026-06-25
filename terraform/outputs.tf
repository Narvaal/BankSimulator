output "ec2_public_ip" {
  value = aws_eip.app.public_ip
}

output "rds_endpoint" {
  value = aws_db_instance.postgres.endpoint
}

output "ssh_command" {
  value = "ssh -i ~/.ssh/banksimulator ec2-user@${aws_eip.app.public_ip}"
}

output "api_url" {
  value = "https://${var.api_domain}"
}
