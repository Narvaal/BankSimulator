# Routes

Base URL: `http://localhost:8080`

## Auth

POST `/auth/login`
- Body: `CreateAuthenticationRequest`
- Returns: `AuthToken`

## Health

GET `/health`
- Returns: `"ok"`

## Accounts

POST `/accounts`
- Body: `CreateAccountApiRequest`
- Returns: `Account`

GET `/accounts/{id}`
- Headers: `Authorization: Bearer <token>` (optional)
- Query: `token` (optional)
- Returns: `AccountDetailsResponse`

POST `/accounts/{id}/deposit`
- Headers: `Authorization: Bearer <token>` (optional)
- Body: `DepositAccountApiRequest` (supports `token`)
- Returns: `204 No Content`

GET `/accounts/{id}/transfers`
- Headers: `Authorization: Bearer <token>` (optional)
- Query: `token` (optional)
- Returns: `List<Transaction>`

## Clients

GET `/clients/me`
- Headers: `Authorization: Bearer <token>` (optional)
- Query: `token` (optional)
- Returns: `ClientProfileResponse`

## Assets

GET `/assets`
- Returns: `List<AssetSummaryResponse>`

GET `/assets/{id}`
- Returns: `Asset`

GET `/assets/{id}/price-history`
- Returns: `List<AssetPriceHistory>`

POST `/assets/{id}/units`
- Headers: `Authorization: Bearer <token>` (optional)
- Body: `CreateAssetUnityApiRequest` (supports `token`)
- Returns: `AssetUnity`

GET `/assets/bundles`
- Returns: `List<AssetBundleResponse>`

POST `/assets/bundles`
- Headers: `X-Admin-Token: <token>` (required)
- Body: `CreateAssetBundleApiRequest`
- Returns: `AssetBundleResponse`

GET `/assets/bundles/{id}/items`
- Returns: `List<AssetBundleItemResponse>`

## Asset Listings

GET `/asset-listings`
- Query: `ownerId` (optional)
- Returns: `List<AssetListing>`

GET `/asset-listings/{id}`
- Returns: `AssetListing`

POST `/asset-listings/{id}/purchase`
- Headers: `Authorization: Bearer <token>` (optional)
- Body: `PurchaseAssetApiRequest` (supports `token`)
- Returns: `AssetPurchase`

GET `/asset-listings/{id}/price-history`
- Returns: `List<AssetPriceHistory>`

## Asset Offers

POST `/asset-offers`
- Headers: `Authorization: Bearer <token>` (optional)
- Body: `CreateAssetOfferApiRequest` (supports `token`)
- Returns: `AssetListing`

POST `/asset-offers/{id}/cancel`
- Headers: `Authorization: Bearer <token>` (optional)
- Body: `CancelAssetOfferApiRequest` (supports `token`)
- Returns: `204 No Content`

## Admin

POST `/admin/assets/generate`
- Headers: `X-Admin-Token: <token>` (required)
- Returns: `List<Asset>`
