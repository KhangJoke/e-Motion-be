-- ==============================================================================
-- V6__drop_vehicle_brand_column.sql: Drop legacy brand column from vehicles table
-- Brand name, code and logo are now normalized and retrieved from brands table.
-- ==============================================================================

ALTER TABLE vehicles DROP COLUMN IF EXISTS brand;
