#!/bin/bash
set -euo pipefail

EC2_IP=$(cd "$(dirname "$0")/../terraform" && terraform output -raw ec2_public_ip 2>/dev/null)
SSH_KEY="${SSH_KEY_PATH:-$HOME/.ssh/banksimulator}"
JAR_PATH="$(dirname "$0")/../target/bank-simulator-*.jar"
REMOTE_DIR="/opt/banksimulator"

if [ -z "$EC2_IP" ]; then
  echo "ERROR: Could not get EC2 IP from terraform output."
  echo "Run: cd terraform && terraform apply"
  exit 1
fi

echo "==> Building JAR..."
cd "$(dirname "$0")/.."
mvn clean package -DskipTests -q

JAR_FILE=$(ls target/bank-simulator-*.jar | head -1)
echo "==> Deploying $JAR_FILE to ec2-user@$EC2_IP..."

scp -i "$SSH_KEY" -o StrictHostKeyChecking=no "$JAR_FILE" "ec2-user@$EC2_IP:$REMOTE_DIR/app.jar"

ssh -i "$SSH_KEY" -o StrictHostKeyChecking=no "ec2-user@$EC2_IP" \
  "sudo chown banksimulator:banksimulator $REMOTE_DIR/app.jar && sudo systemctl restart banksimulator"

echo "==> Deploy complete. Checking status..."
ssh -i "$SSH_KEY" -o StrictHostKeyChecking=no "ec2-user@$EC2_IP" \
  "sudo systemctl status banksimulator --no-pager -l"
