#!/usr/bin/env bash
set -e

BASE_URL="http://localhost:5000"
TOKEN="local-admin-token"

# ── args ────────────────────────────────────────────────
WEEK="weekly-$(date +%Y-W%V)"
CLIENT_ID="${1:-1}"
AMOUNT="${2:-1000.00}"

# ── deposit ─────────────────────────────────────────────
echo "💰 Depositing \$$AMOUNT for clientId=$CLIENT_ID..."
DEPOSIT=$(curl -s -X POST "$BASE_URL/admin/accounts/deposit" \
  -H "Content-Type: application/json" \
  -H "X-Admin-Token: $TOKEN" \
  -d "{\"clientId\": $CLIENT_ID, \"amount\": \"$AMOUNT\"}")
echo "   → $DEPOSIT"

# ── bundle ──────────────────────────────────────────────
echo ""
echo "📦 Creating bundle '$WEEK'..."
BUNDLE=$(curl -s -X POST "$BASE_URL/artifacts/bundles" \
  -H "Content-Type: application/json" \
  -H "X-Admin-Token: $TOKEN" \
  -d "{
    \"identifier\": \"$WEEK\",
    \"assets\": [
      { \"text\": \"Rare Lines #001\", \"totalSupply\": 5 },
      { \"text\": \"Epic Lines #001\", \"totalSupply\": 3 },
      { \"text\": \"Legendary Lines #001\", \"totalSupply\": 1 }
    ]
  }")
echo "   → $BUNDLE"

echo ""
echo "✅ Done. Open http://localhost:5173/reward to see the bundle."
