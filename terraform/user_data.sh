#!/bin/bash
set -euo pipefail
exec > /var/log/user-data.log 2>&1

# ── Packages ──────────────────────────────────────────────────────────────────
dnf update -y
dnf install -y java-17-amazon-corretto-headless nginx certbot python3-certbot-nginx

# ── App user and directory ────────────────────────────────────────────────────
useradd -r -s /sbin/nologin -d /opt/banksimulator banksimulator
mkdir -p /opt/banksimulator
chown banksimulator:banksimulator /opt/banksimulator

# ── SSM fetch script (runs at startup to refresh env vars) ───────────────────
cat > /opt/banksimulator/fetch-env.py << 'PYEOF'
#!/usr/bin/env python3
import json, subprocess, os, sys

region = sys.argv[1] if len(sys.argv) > 1 else "REGION_PLACEHOLDER"

r = subprocess.run(
    ["aws", "ssm", "get-parameters-by-path",
     "--path", "/banksimulator/",
     "--with-decryption",
     "--region", region,
     "--output", "json"],
    capture_output=True, text=True, check=True
)

params = json.loads(r.stdout)["Parameters"]
with open("/etc/app.env", "w") as f:
    for p in params:
        key = p["Name"].split("/")[-1]
        f.write("%s=%s\n" % (key, p["Value"]))

os.chmod("/etc/app.env", 0o600)
print("Fetched %d parameters from SSM" % len(params))
PYEOF

chmod +x /opt/banksimulator/fetch-env.py
python3 /opt/banksimulator/fetch-env.py ${aws_region}

# ── Systemd service ───────────────────────────────────────────────────────────
mkdir -p /opt/banksimulator/keys
chown banksimulator:banksimulator /opt/banksimulator/keys

cat > /etc/systemd/system/banksimulator.service << SVCEOF
[Unit]
Description=Bank Simulator API
After=network.target

[Service]
WorkingDirectory=/opt/banksimulator
User=banksimulator
EnvironmentFile=/etc/app.env
ExecStartPre=+/usr/bin/python3 /opt/banksimulator/fetch-env.py ${aws_region}
ExecStart=/usr/bin/java -jar /opt/banksimulator/app.jar
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal
SyslogIdentifier=banksimulator

[Install]
WantedBy=multi-user.target
SVCEOF

systemctl daemon-reload
systemctl enable banksimulator

# ── Nginx config ──────────────────────────────────────────────────────────────
rm -f /etc/nginx/conf.d/default.conf

cat > /etc/nginx/conf.d/banksimulator.conf << NGINXEOF
server {
    listen 80;
    server_name ${domain};

    location / {
        proxy_pass         http://127.0.0.1:5000;
        proxy_set_header   Host               \$host;
        proxy_set_header   X-Real-IP          \$remote_addr;
        proxy_set_header   X-Forwarded-For    \$proxy_add_x_forwarded_for;
        proxy_set_header   X-Forwarded-Proto  \$scheme;
        proxy_read_timeout 60s;
        client_max_body_size 10m;
    }
}
NGINXEOF

systemctl enable nginx
systemctl start nginx

# ── Certbot: retry until DNS propagates (runs in background) ─────────────────
nohup bash -c '
  until certbot --nginx \
    -d ${domain} \
    --non-interactive \
    --agree-tos \
    --email ${cert_email} \
    --redirect; do
    echo "[certbot] DNS not ready, retrying in 30s..."
    sleep 30
  done
  echo "[certbot] HTTPS configured successfully"
' >> /var/log/certbot-setup.log 2>&1 &

# ── Certbot auto-renewal ──────────────────────────────────────────────────────
echo "0 0,12 * * * root /usr/bin/certbot renew --quiet" > /etc/cron.d/certbot-renewal

echo "User data completed."
