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
      { \"metadata\": { \"name\": \"Rare Lines #001\", \"rarity\": \"Rare\", \"category\": \"Technology\", \"subtitle\": \"The beginning of something rare\" }, \"totalSupply\": 5 },
      { \"metadata\": { \"name\": \"Epic Lines #001\", \"rarity\": \"Epic\", \"category\": \"Science\", \"subtitle\": \"A moment worth collecting\" }, \"totalSupply\": 3 },
      { \"metadata\": { \"name\": \"Legendary Lines #001\", \"rarity\": \"Legendary\", \"category\": \"Culture\", \"subtitle\": \"History in the making\" }, \"totalSupply\": 1 }
    ]
  }")
echo "   → $BUNDLE"

echo ""
echo "✅ Done. Open http://localhost:5173/reward to see the bundle."
