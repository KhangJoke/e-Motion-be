-- ==============================================================================
-- V5__link_vehicles_to_brands.sql: Link vehicles to brands table via brand_id
-- ==============================================================================

-- 1. Add brand_id column to vehicles if not exists
ALTER TABLE vehicles ADD COLUMN IF NOT EXISTS brand_id BIGINT;

-- 2. Populate brand_id based on legacy brand string (matching brand_code or brand_name)
UPDATE vehicles v
SET brand_id = b.brand_id
FROM brands b
WHERE UPPER(TRIM(v.brand)) = b.brand_code
   OR UPPER(TRIM(v.brand)) = UPPER(TRIM(b.brand_name));

-- 3. Fallback for any vehicles that couldn't be matched (assign first brand or VINFAST)
UPDATE vehicles
SET brand_id = (SELECT brand_id FROM brands WHERE brand_code = 'VINFAST' LIMIT 1)
WHERE brand_id IS NULL;

-- 4. In case brands table is completely empty (fresh test db), insert default brand
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM brands) THEN
        INSERT INTO brands (brand_name, brand_code, logo_url, display_order)
        VALUES ('VinFast', 'VINFAST', 'https://res.cloudinary.com/dy45rrkhf/image/upload/v1790846911/brands/vinfast.png', 1);

        UPDATE vehicles SET brand_id = (SELECT brand_id FROM brands WHERE brand_code = 'VINFAST' LIMIT 1) WHERE brand_id IS NULL;
    END IF;
END $$;

-- 5. Add Foreign Key and Not Null constraint
ALTER TABLE vehicles
    ALTER COLUMN brand_id SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_vehicles_brand'
    ) THEN
        ALTER TABLE vehicles
            ADD CONSTRAINT fk_vehicles_brand FOREIGN KEY (brand_id) REFERENCES brands(brand_id) ON DELETE RESTRICT;
    END IF;
END $$;

-- 6. Make old brand column nullable
ALTER TABLE vehicles ALTER COLUMN brand DROP NOT NULL;
