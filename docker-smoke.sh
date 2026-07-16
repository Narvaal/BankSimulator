#!/usr/bin/env bash
# Smoke test do stack Docker: builda a imagem, sobe backend + Postgres 17 real
# e exercita os endpoints que dependem de dialeto Postgres (JSONB/JSON_VALUE).
# ATENÇÃO: recria o stack do compose e zera o volume pgdata.
set -euo pipefail

BASE_URL="http://localhost:5000"
TOKEN="local-admin-token"
BODY=$(mktemp)

cleanup() {
  docker compose down -v > /dev/null 2>&1 || true
  rm -f "$BODY"
}
trap cleanup EXIT

check() {
  local desc="$1" expected="$2"
  shift 2
  local code
  code=$(curl -s -o "$BODY" -w '%{http_code}' "$@")
  if [ "$code" != "$expected" ]; then
    echo "❌ FAIL: $desc — esperado HTTP $expected, obtido $code"
    cat "$BODY"
    echo ""
    docker compose logs app | tail -40
    exit 1
  fi
  echo "✅ $desc [$code]"
}

check_body_contains() {
  local desc="$1" needle="$2"
  if ! grep -q "$needle" "$BODY"; then
    echo "❌ FAIL: $desc — resposta não contém '$needle'"
    cat "$BODY"
    exit 1
  fi
  echo "✅ $desc"
}

echo "── Subindo stack (build + Postgres 17) ──"
docker compose down -v > /dev/null 2>&1 || true
docker compose up -d --build

echo "── Aguardando health ──"
STATUS=""
for i in $(seq 1 30); do
  STATUS=$(curl -s -o /dev/null -w '%{http_code}' "$BASE_URL/health" || true)
  [ "$STATUS" = "200" ] && break
  sleep 3
done
if [ "$STATUS" != "200" ]; then
  echo "❌ FAIL: app não ficou saudável em 90s"
  docker compose logs app | tail -60
  exit 1
fi
echo "✅ health [200]"

echo "── Escrita: bundle com metadata JSONB ──"
check "POST /artifacts/bundles" 200 \
  -X POST "$BASE_URL/artifacts/bundles" \
  -H "Content-Type: application/json" \
  -H "X-Admin-Token: $TOKEN" \
  -d '{
    "identifier": "docker-smoke",
    "assets": [
      { "metadata": { "name": "Smoke Rare", "rarity": "Rare", "category": "Technology", "subtitle": "Smoke test artifact" }, "totalSupply": 5 },
      { "metadata": { "name": "Smoke Epic", "rarity": "Epic", "category": "Science", "subtitle": "Smoke test artifact" }, "totalSupply": 3 }
    ]
  }'
check_body_contains "bundle criado com identifier" "docker-smoke"

echo "── Leitura: JSONB e JSON_VALUE no Postgres real ──"
check "GET /artifacts/bundles" 200 "$BASE_URL/artifacts/bundles"
check_body_contains "lista de bundles" "docker-smoke"

check "GET /artifacts/bundles/1/items (metadata JSONB)" 200 \
  "$BASE_URL/artifacts/bundles/1/items?page=0&size=10"
check_body_contains "metadata deserializado" "Smoke Rare"

check "GET /artifact-listings com filtros (JSON_VALUE + SQL dinâmico)" 200 \
  "$BASE_URL/artifact-listings?page=0&pageSize=10&q=smoke&sort=price_asc&minPrice=0.01&maxPrice=999"

check "GET /artifact-transfers sem filtro (JSON_VALUE)" 200 \
  "$BASE_URL/artifact-transfers?page=0&pageSize=5"

check "GET /artifact-transfers filtrado por artifact (regressão ?ORDER)" 200 \
  "$BASE_URL/artifact-transfers?page=0&pageSize=5&artifactId=1"

echo "── Tratamento de erro ──"
check "GET /artifact-units/999999 inexistente" 404 "$BASE_URL/artifact-units/999999"

echo ""
echo "🎉 Smoke test do Docker passou."
