resource "random_password" "db" {
  length  = 32
  special = false
}

resource "aws_security_group" "rds" {
  name        = "banksimulator-rds"
  description = "BankSimulator RDS - only from EC2"
  vpc_id      = data.aws_vpc.default.id

  ingress {
    description     = "PostgreSQL from EC2"
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.ec2.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = { Name = "banksimulator-rds" }
}

resource "aws_db_subnet_group" "main" {
  name       = "banksimulator"
  subnet_ids = data.aws_subnets.default.ids
  tags       = { Name = "banksimulator" }
}

resource "aws_db_instance" "postgres" {
  identifier = "banksimulator-db"

  engine            = "postgres"
  engine_version    = "17"
  instance_class    = "db.t4g.micro"
  allocated_storage = 20
  storage_type      = "gp3"

  db_name  = "bank"
  username = var.db_user
  password = random_password.db.result

  db_subnet_group_name   = aws_db_subnet_group.main.name
  vpc_security_group_ids = [aws_security_group.rds.id]
  publicly_accessible    = false

  backup_retention_period = 1
  deletion_protection     = false
  skip_final_snapshot     = true

  tags = { Name = "banksimulator" }
}
