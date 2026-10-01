-- V4: Create brands table and seed initial brand data with real Cloudinary URLs
CREATE TABLE IF NOT EXISTS brands (
    brand_id BIGSERIAL PRIMARY KEY,
    brand_name VARCHAR(100) NOT NULL UNIQUE,
    brand_code VARCHAR(50) NOT NULL UNIQUE,
    logo_url TEXT NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO brands (brand_name, brand_code, logo_url, display_order) VALUES
('VinFast', 'VINFAST', 'https://res.cloudinary.com/dy45rrkhf/image/upload/v1790846911/brands/vinfast.png', 1),
('Tesla', 'TESLA', 'https://res.cloudinary.com/dy45rrkhf/image/upload/v1790846946/brands/tesla.png', 2),
('Hyundai', 'HYUNDAI', 'https://res.cloudinary.com/dy45rrkhf/image/upload/v1790846936/brands/hyundai.png', 3),
('BMW', 'BMW', 'https://res.cloudinary.com/dy45rrkhf/image/upload/v1790846930/brands/bmw.png', 4),
('Mercedes-Benz', 'MERCEDES_BENZ', 'https://res.cloudinary.com/dy45rrkhf/image/upload/v1790846941/brands/mercedes.png', 5),
('Toyota', 'TOYOTA', 'https://res.cloudinary.com/dy45rrkhf/image/upload/v1790846947/brands/toyota.png', 6),
('KIA', 'KIA', 'https://res.cloudinary.com/dy45rrkhf/image/upload/v1790846937/brands/kia.png', 7),
('Audi', 'AUDI', 'https://res.cloudinary.com/dy45rrkhf/image/upload/v1790846929/brands/audi.png', 8),
('BYD', 'BYD', 'https://res.cloudinary.com/dy45rrkhf/image/upload/v1790846932/brands/byd.png', 9),
('Mazda', 'MAZDA', 'https://res.cloudinary.com/dy45rrkhf/image/upload/v1790846940/brands/mazda.png', 10),
('Ford', 'FORD', 'https://res.cloudinary.com/dy45rrkhf/image/upload/v1790846933/brands/ford.png', 11),
('Lexus', 'LEXUS', 'https://res.cloudinary.com/dy45rrkhf/image/upload/v1790846938/brands/lexus.png', 12),
('Honda', 'HONDA', 'https://res.cloudinary.com/dy45rrkhf/image/upload/v1790846934/brands/honda.png', 13),
('Peugeot', 'PEUGEOT', 'https://res.cloudinary.com/dy45rrkhf/image/upload/v1790846945/brands/peugeot.png', 14),
('Nissan', 'NISSAN', 'https://res.cloudinary.com/dy45rrkhf/image/upload/v1790846944/brands/nissan.png', 15),
('Mitsubishi', 'MITSUBISHI', 'https://res.cloudinary.com/dy45rrkhf/image/upload/v1790846942/brands/mitsubishi.png', 16)
ON CONFLICT (brand_code) DO NOTHING;
