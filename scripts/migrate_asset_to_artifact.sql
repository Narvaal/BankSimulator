-- Migration: Rename asset tables/columns/types to artifact
-- Run against production RDS BEFORE deploying the new backend JAR.
-- Connect via: psql <DB_URL>

BEGIN;

-- 1. Rename tables
ALTER TABLE asset RENAME TO artifact;
ALTER TABLE asset_bundle RENAME TO artifact_bundle;
ALTER TABLE asset_bundle_item RENAME TO artifact_bundle_item;
ALTER TABLE asset_unit RENAME TO artifact_unit;
ALTER TABLE asset_transfer RENAME TO artifact_transfer;
ALTER TABLE asset_listing RENAME TO artifact_listing;
ALTER TABLE asset_price_history RENAME TO artifact_price_history;

-- 2. Rename ENUM type
ALTER TYPE asset_unit_status RENAME TO artifact_unit_status;

-- 3. Rename columns
ALTER TABLE artifact_bundle_item RENAME COLUMN asset_id TO artifact_id;
ALTER TABLE artifact_unit RENAME COLUMN asset_id TO artifact_id;
ALTER TABLE account RENAME COLUMN next_free_asset_at TO next_free_artifact_at;

-- 4. Rename FK constraints (optional, but keeps names consistent)
ALTER TABLE artifact_bundle_item RENAME CONSTRAINT fk_bundle_item_asset TO fk_bundle_item_artifact;
ALTER TABLE artifact_unit RENAME CONSTRAINT fk_artifact_unit_asset TO fk_artifact_unit_artifact;

COMMIT;
