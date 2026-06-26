# Routes

Base URL: `http://localhost:5000`

---

## Health

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/health` | — | Retorna `"ok"` |

---

## Auth

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/auth/login` | — | Login local (email + senha). JWT retornado em cookie `AUTH_TOKEN` |
| POST | `/auth/google` | — | Login via Google OAuth. Body: `{ token }` (ID token do Google) |
| GET | `/auth/verify?token=<token>` | — | Verifica email e redireciona para `/inventory` |
| POST | `/auth/resend-verification` | — | Reenvia email de verificação. Body: `{ email }` |

---

## Accounts

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/accounts` | — | Cria conta + cliente. Envia email de verificação |
| GET | `/accounts/me` | Cookie/Bearer | Retorna dados da conta autenticada (`AccountDetailsResponse`) |
| GET | `/accounts/{id}/transfers` | Cookie/Bearer | Lista transações da conta |
| POST | `/accounts/password/reset-request` | — | Solicita reset de senha. Body: `{ email }` |
| POST | `/accounts/password/reset` | — | Redefine senha com token do email. Body: `{ password, token }` |

---

## Clients

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/clients/me` | Bearer/query `token` | Retorna perfil do cliente autenticado (`ClientProfileResponse`) |

---

## Assets

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/assets` | — | Lista todos os assets com resumo de preço |
| GET | `/assets/{id}` | — | Retorna asset por ID |
| GET | `/assets/{id}/price-history` | — | Histórico de preços de um asset |
| POST | `/assets/{id}/units` | Bearer/body `token` | Cria unit de asset para uma conta (admin) |
| GET | `/assets/bundles` | — | Lista bundles. Query: `page`, `size` |
| POST | `/assets/bundles` | `X-Admin-Token` | Cria bundle de assets |
| GET | `/assets/bundles/{id}/items` | — | Itens de um bundle. Query: `page`, `size` |
| GET | `/assets/claim` | Cookie/Bearer | Retorna `Instant` do próximo claim disponível |
| POST | `/assets/claim` | Cookie/Bearer | Resgata um asset gratuito. Body: `{ assetId }` |

---

## Asset Units

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/asset-units` | — | Lista units por dono. Query obrigatória: `ownerId`, `page`, `pageSize` |

---

## Asset Listings

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/asset-listings` | Cookie/Bearer | Lista listings ativos. Query: `page`, `pageSize` |
| GET | `/asset-listings/me` | Cookie/Bearer | Lista listings do usuário autenticado. Query: `page`, `pageSize` |
| GET | `/asset-listings/{id}` | — | Retorna listing por ID |
| GET | `/asset-listings/{id}/price-history` | — | Histórico de preços de um listing |
| POST | `/asset-listings/{id}/purchase` | Cookie/Bearer | Compra um listing |

---

## Asset Offers

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/asset-offers` | Cookie/Bearer | Cria oferta de venda. Body: `{ assetUnityId, price }` |
| POST | `/asset-offers/cancel` | Cookie/Bearer | Cancela oferta. Body: `{ assetListingId }` |

---

## Admin

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/admin/assets/generate` | `X-Admin-Token` | Gera assets semanais |

> **Admin Token local:** `local-admin-token` (configurado em `application-local.properties`)

---

## Notas de Autenticação

- JWT é retornado em **cookie HttpOnly** `AUTH_TOKEN` após login ou verificação de email
- Endpoints que aceitam `Cookie` também aceitam `Authorization: Bearer <token>` ou query param `?token=<token>`
- Endpoints `/admin/*` usam header separado `X-Admin-Token`
