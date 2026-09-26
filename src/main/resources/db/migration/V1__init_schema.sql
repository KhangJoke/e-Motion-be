-- ==============================================================================
-- V1__init_schema.sql: Initial Schema Migration for e-Motion EV Rental System
-- Target Database: PostgreSQL 17
-- ==============================================================================

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    user_id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(255),
    phone VARCHAR(50) UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255),
    role VARCHAR(50) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    blocked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    verification_code VARCHAR(255),
    verification_code_expires_at TIMESTAMP WITHOUT TIME ZONE,
    forgot_password_code VARCHAR(255),
    forgot_password_code_expires_at TIMESTAMP WITHOUT TIME ZONE,
    point INT NOT NULL DEFAULT 0
);

-- 2. Stations Table
CREATE TABLE IF NOT EXISTS stations (
    station_id BIGSERIAL PRIMARY KEY,
    station_name VARCHAR(255),
    station_address VARCHAR(255),
    station_latitude DOUBLE PRECISION,
    station_longitude DOUBLE PRECISION,
    station_status VARCHAR(50),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    station_city VARCHAR(50),
    is_delete BOOLEAN NOT NULL DEFAULT FALSE
);

-- 3. Staffs Table
CREATE TABLE IF NOT EXISTS staffs (
    staff_id BIGSERIAL PRIMARY KEY,
    station_id BIGINT REFERENCES stations(station_id) ON DELETE SET NULL,
    is_delete BOOLEAN NOT NULL DEFAULT FALSE,
    user_id BIGINT UNIQUE REFERENCES users(user_id) ON DELETE CASCADE
);

-- 4. Vehicles Table
CREATE TABLE IF NOT EXISTS vehicles (
    vehicle_id BIGSERIAL PRIMARY KEY,
    vehicle_name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    brand VARCHAR(50) NOT NULL,
    vehicle_status VARCHAR(50) NOT NULL DEFAULT 'AVAILABLE',
    vehicle_category VARCHAR(50),
    seats INT NOT NULL,
    price_4_hours DOUBLE PRECISION NOT NULL,
    price_8_hours DOUBLE PRECISION NOT NULL,
    price_12_hours DOUBLE PRECISION NOT NULL,
    price_day DOUBLE PRECISION NOT NULL,
    deposit_fee DOUBLE PRECISION NOT NULL,
    consumption_rate DOUBLE PRECISION NOT NULL,
    current_battery_level INT NOT NULL,
    battery_capacity DOUBLE PRECISION NOT NULL,
    plate_number VARCHAR(50) NOT NULL UNIQUE,
    last_maintenance TIMESTAMP WITHOUT TIME ZONE,
    is_delete BOOLEAN NOT NULL DEFAULT FALSE,
    point INT NOT NULL DEFAULT 0,
    station_id BIGINT REFERENCES stations(station_id) ON DELETE SET NULL
);

-- 5. Img Vehicles Table
CREATE TABLE IF NOT EXISTS img_vehicles (
    img_id BIGSERIAL PRIMARY KEY,
    img_url VARCHAR(500),
    main_img BOOLEAN NOT NULL DEFAULT FALSE,
    vehicle_id BIGINT REFERENCES vehicles(vehicle_id) ON DELETE CASCADE
);

-- 6. Reservations Table
CREATE TABLE IF NOT EXISTS reservations (
    reservation_id BIGSERIAL PRIMARY KEY,
    reservation_code VARCHAR(100) UNIQUE,
    reservation_status VARCHAR(50) NOT NULL,
    cancel_notified BOOLEAN DEFAULT FALSE,
    overdue_notified BOOLEAN DEFAULT FALSE,
    expiring_notified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reserved_start_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    reserved_end_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    user_id BIGINT REFERENCES users(user_id) ON DELETE SET NULL,
    vehicle_id BIGINT REFERENCES vehicles(vehicle_id) ON DELETE SET NULL,
    station_id BIGINT REFERENCES stations(station_id) ON DELETE SET NULL
);

-- 7. Rentals Table
CREATE TABLE IF NOT EXISTS rentals (
    rental_id BIGSERIAL PRIMARY KEY,
    rental_status VARCHAR(50) DEFAULT 'PENDING',
    start_time TIMESTAMP WITHOUT TIME ZONE,
    end_time TIMESTAMP WITHOUT TIME ZONE,
    cancel_notified BOOLEAN DEFAULT FALSE,
    overdue_notified BOOLEAN DEFAULT FALSE,
    expiring_notified BOOLEAN DEFAULT FALSE,
    rent_fee DOUBLE PRECISION,
    discount_point INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    pending_end_time TIMESTAMP WITHOUT TIME ZONE,
    pending_rent_fee DOUBLE PRECISION,
    pre_status VARCHAR(50),
    submission_id BIGINT NOT NULL DEFAULT 0,
    contract_url VARCHAR(500),
    submission_url VARCHAR(500),
    contract_status VARCHAR(50),
    vehicle_id BIGINT REFERENCES vehicles(vehicle_id) ON DELETE SET NULL,
    reservation_id BIGINT REFERENCES reservations(reservation_id) ON DELETE SET NULL,
    station_id BIGINT REFERENCES stations(station_id) ON DELETE SET NULL,
    user_id BIGINT REFERENCES users(user_id) ON DELETE SET NULL,
    staff_id BIGINT REFERENCES staffs(staff_id) ON DELETE SET NULL
);

-- 8. Deposits Table
CREATE TABLE IF NOT EXISTS deposits (
    deposit_id BIGSERIAL PRIMARY KEY,
    deposit_status VARCHAR(50) DEFAULT 'PENDING',
    deposit_amount DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    reservation_id BIGINT UNIQUE REFERENCES reservations(reservation_id) ON DELETE SET NULL,
    rental_id BIGINT UNIQUE REFERENCES rentals(rental_id) ON DELETE SET NULL
);

-- 9. Payments Table
CREATE TABLE IF NOT EXISTS payments (
    payment_id BIGSERIAL PRIMARY KEY,
    total_amount DOUBLE PRECISION NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    payment_type VARCHAR(50) NOT NULL,
    payment_status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_delete BOOLEAN NOT NULL DEFAULT FALSE,
    txn_ref VARCHAR(100) NOT NULL UNIQUE,
    payment_description VARCHAR(255),
    response_code VARCHAR(50),
    transaction_no VARCHAR(100),
    bank_code VARCHAR(50),
    pay_date TIMESTAMP WITHOUT TIME ZONE,
    user_id BIGINT REFERENCES users(user_id) ON DELETE SET NULL,
    rental_id BIGINT REFERENCES rentals(rental_id) ON DELETE SET NULL,
    deposit_id BIGINT REFERENCES deposits(deposit_id) ON DELETE SET NULL
);

-- 10. Ratings Table
CREATE TABLE IF NOT EXISTS ratings (
    rating_id BIGSERIAL PRIMARY KEY,
    comment TEXT,
    score INT NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    user_id BIGINT REFERENCES users(user_id) ON DELETE SET NULL,
    rental_id BIGINT UNIQUE REFERENCES rentals(rental_id) ON DELETE CASCADE
);

-- 11. Refresh Tokens Table
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(512) NOT NULL UNIQUE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    replaced_by BIGINT,
    user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE
);

-- 12. Rental Checklists Table
CREATE TABLE IF NOT EXISTS rental_checklists (
    checklist_id BIGSERIAL PRIMARY KEY,
    check_type VARCHAR(50),
    fee DOUBLE PRECISION DEFAULT 0.0,
    img VARCHAR(500),
    current_battery DOUBLE PRECISION,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    staff_id BIGINT REFERENCES staffs(staff_id) ON DELETE SET NULL,
    rental_id BIGINT REFERENCES rentals(rental_id) ON DELETE CASCADE
);

-- 13. Reports Table
CREATE TABLE IF NOT EXISTS reports (
    report_id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    type VARCHAR(50) NOT NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id BIGINT REFERENCES users(user_id) ON DELETE SET NULL,
    staff_id BIGINT REFERENCES staffs(staff_id) ON DELETE SET NULL
);

-- 14. Documents Table
CREATE TABLE IF NOT EXISTS documents (
    doc_id BIGSERIAL PRIMARY KEY,
    doc_img_url VARCHAR(500),
    doc_type VARCHAR(50) NOT NULL,
    doc_number VARCHAR(100) NOT NULL UNIQUE,
    user_id BIGINT REFERENCES users(user_id) ON DELETE CASCADE
);

-- 15. Vehicle Logs Table
CREATE TABLE IF NOT EXISTS vehicle_logs (
    log_id BIGSERIAL PRIMARY KEY,
    repair_items TEXT,
    total_cost DOUBLE PRECISION,
    images TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    vehicle_id BIGINT REFERENCES vehicles(vehicle_id) ON DELETE SET NULL,
    staff_id BIGINT REFERENCES staffs(staff_id) ON DELETE SET NULL,
    rental_id BIGINT UNIQUE REFERENCES rentals(rental_id) ON DELETE SET NULL
);

-- ==============================================================================
-- INDEXES FOR PERFORMANCE OPTIMIZATION
-- ==============================================================================
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_phone ON users(phone);
CREATE INDEX IF NOT EXISTS idx_vehicles_station ON vehicles(station_id);
CREATE INDEX IF NOT EXISTS idx_vehicles_status ON vehicles(vehicle_status);
CREATE INDEX IF NOT EXISTS idx_vehicles_brand ON vehicles(brand);
CREATE INDEX IF NOT EXISTS idx_reservations_user ON reservations(user_id);
CREATE INDEX IF NOT EXISTS idx_reservations_vehicle ON reservations(vehicle_id);
CREATE INDEX IF NOT EXISTS idx_reservations_status ON reservations(reservation_status);
CREATE INDEX IF NOT EXISTS idx_rentals_user ON rentals(user_id);
CREATE INDEX IF NOT EXISTS idx_rentals_vehicle ON rentals(vehicle_id);
CREATE INDEX IF NOT EXISTS idx_rentals_status ON rentals(rental_status);
CREATE INDEX IF NOT EXISTS idx_payments_txn_ref ON payments(txn_ref);
CREATE INDEX IF NOT EXISTS idx_payments_user ON payments(user_id);
CREATE INDEX IF NOT EXISTS idx_payments_rental ON payments(rental_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX IF NOT EXISTS idx_documents_user ON documents(user_id);
